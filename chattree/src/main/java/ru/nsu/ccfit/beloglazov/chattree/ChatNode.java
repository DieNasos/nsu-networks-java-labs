package ru.nsu.ccfit.beloglazov.chattree;

import ru.nsu.ccfit.beloglazov.chattree.dto.DTO;
import ru.nsu.ccfit.beloglazov.chattree.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatNode {
    private String name;
    private int packetLoss;
    private DatagramSocket receiveSocket;
    private UUID id = UUID.randomUUID();
    private Neighbour delegate = null;
    private Queue<PendingMessageRecord> pendingResponses = new ConcurrentLinkedQueue<>();
    private final Map<MessageUniqueRecord, PendingMessageRecord> pendingRequests = new ConcurrentHashMap<>();
    private final Map<MessageUniqueRecord, PendingMessageRecord> receivedMessages = new ConcurrentHashMap<>();
    private final Map<UUID, Neighbour> neighbours = new ConcurrentHashMap<>();
    private static int timeoutToDisconnect = 30000; // 30 seconds
    private static final UUID defaultTargetToConnectUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /*
        PUBLIC METHODS
    */

    public ChatNode(String name, int packetLoss, int port) {
        // default initialization
        try {
            this.name = name;
            this.packetLoss = packetLoss;
            receiveSocket = new DatagramSocket(port);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public ChatNode(String name, int packetLoss, int port, String neighbourIP, int neighbourPort) {
        // 1. default initialization
        this(name, packetLoss, port);
        try {
            // 2. initializing target-to-connect with default UUID & putting it in neighbours-map
            Neighbour targetToConnect = new Neighbour(InetAddress.getByName(neighbourIP), neighbourPort);
            neighbours.put(defaultTargetToConnectUUID, targetToConnect);
        } catch (UnknownHostException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void startCommunication() {
        // starting chat-node's threads
        new Thread(this::receivePackets).start();
        new Thread(this::manageStuff).start();

        Scanner scanner = new Scanner(System.in);
        String message;

        while (!Thread.currentThread().isInterrupted()) {
            // reading messages from console & sending them to everybody
            message = scanner.nextLine();
            sendMessageToAll(name + ": " + message);
        }
    }

    /*
        PRIVATE METHODS
    */

    private void receivePackets() {
        int maxDatagramSize = 32768;
        byte[] receiveBuffer = new byte[maxDatagramSize];

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // receiving packet
                DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                receiveSocket.receive(packet);

                if (!generatePacketLoss()) {
                    // if packet is not "lost" -> handling it
                    handleReceivedPacket(packet);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private boolean generatePacketLoss() {
        return ThreadLocalRandom.current().nextInt(0,100) < packetLoss;
    }

    private synchronized void handleReceivedPacket(DatagramPacket packet) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()))) {
            DTO dto = (DTO) ois.readObject();

            // checking if sender is our neighbour

            Neighbour sender = neighbours.get(dto.getSenderID());

            if (sender == null) {
                // met this guy for the 1st time

                // if default target-to-connect record still exists in neighbours-map -> deleting it
                Neighbour t2c = neighbours.get(defaultTargetToConnectUUID);
                if (t2c != null) {
                    neighbours.remove(defaultTargetToConnectUUID);

                    // finding pending-request with null receiver-id in key & deleting it
                    for (MessageUniqueRecord mur : pendingRequests.keySet()) {
                        if (mur.getReceiverID() == null) {
                            pendingRequests.remove(mur);
                        }
                    }
                }

                // initializing sender & putting it in neighbours-map
                sender = new Neighbour(packet.getAddress(), packet.getPort());
                sender.setID(dto.getSenderID());
                sender.setName(dto.getSenderName());
                neighbours.put(sender.id, sender);
            }

            // checking type of DTO
            if (dto.getType().equals(MessageType.MESSAGE)
                    && !receivedMessages.containsKey(new MessageUniqueRecord(dto.getID(),dto.getSenderID()))) {
                // printing received message in console
                System.out.println(dto.getData());
                // putting it in map
                receivedMessages.put(new MessageUniqueRecord(dto.getID(), dto.getSenderID()), new PendingMessageRecord(System.currentTimeMillis(), packet));
                // sending confirmation
                sendConfirmation(dto.getID().toString(), dto.getID(), sender);
                // forwarding received message
                forwardMessage(dto, sender);
            } else if (dto.getType().equals(MessageType.CONFIRMATION)) {
                // adding successful dispatch for sender
                sender.addSuccessfulDispatch(new MessageUniqueRecord(dto.getID(), sender.getID()), new PendingMessageRecord(System.currentTimeMillis()));
                // deleting dispatched message from pending-queue
                popMessageFromPendingQueue(dto, sender);
                // this guy was last time pinged just now
                sender.setLastPingTime(System.currentTimeMillis());
            }

            // updating info about sender's delegate
            sender.setDelegate(dto.getDelegate());

            if (delegate == null || delegate.getID() == this.id) {
                chooseDelegate();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void sendConfirmation(String data, UUID id, Neighbour neighbour) {
        DTO dto = new DTO(MessageType.CONFIRMATION, id, this.id, name, neighbour.getName(), data, delegate);
        putMessageToPendingResponses(dto, neighbour);
    }

    private void sendMessageToNode(DatagramPacket packet) {
        try {
            receiveSocket.send(packet);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void sendMessageToAll(String data) {
        UUID dtoID = UUID.randomUUID();
        for (Neighbour receiver : neighbours.values()) {
            DTO dto = new DTO(MessageType.MESSAGE, dtoID, id, name, receiver.getName(), data, delegate);
            putMessageToPendingRequests(dto, receiver);
        }
    }

    private void forwardMessage(DTO dto, Neighbour neighbour) {
        // sending message to everybody except of its original sender
        for (Neighbour receiver : neighbours.values()) {
            if (!receiver.getID().equals(neighbour.getID())) {
                DTO dtoToForward = new DTO(dto.getType(), dto.getID(), id, name, receiver.getName(), dto.getData(), dto.getDelegate());
                putMessageToPendingRequests(dtoToForward, receiver);
            }
        }
    }

    private void putMessageToPendingRequests(DTO dto, Neighbour receiver) {
        synchronized (pendingRequests) {
            DatagramPacket packet = dtoToPacket(dto, receiver);
            MessageUniqueRecord mur = new MessageUniqueRecord(dto.getID(), receiver.getID());

            if (pendingRequests.containsKey(new MessageUniqueRecord(dto.getID(), receiver.getID()))
                    || receiver.checkSuccessfulDispatch(mur)) {
                // message already exists in pending-requests map OR message is successfully dispatched
                return;
            }

            // putting message in map
            pendingRequests.put(mur, new PendingMessageRecord(System.currentTimeMillis(), packet));
        }
    }

    private void putMessageToPendingResponses(DTO dto, Neighbour receiver) {
        // putting message to pending-responses queue
        DatagramPacket packet = dtoToPacket(dto, receiver);
        pendingResponses.offer(new PendingMessageRecord(packet));
    }

    private void popMessageFromPendingQueue(DTO dto, Neighbour receiver) {
        synchronized (pendingRequests) {
            // removing message from pending-requests queue
            System.out.println(pendingRequests.remove(new MessageUniqueRecord(dto.getID(), receiver.getID())));
        }
    }

    private DatagramPacket dtoToPacket(DTO dto, Neighbour receiver) {
        // converting DTO -> DatagramPacket

        DatagramPacket packet = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(dto);
            byte[] sendBuffer = baos.toByteArray();
            packet = new DatagramPacket(sendBuffer, sendBuffer.length, receiver.getIP(), receiver.getPort());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return packet;
    }

    private void chooseDelegate() {
        // choosing next neighbour as new delegate
        if (neighbours.values().iterator().hasNext()) {
            delegate = neighbours.values().iterator().next();
        }
    }

    private synchronized void disconnectNeighbour(Neighbour neighbour) {
        if (neighbour == null || !neighbours.containsKey(neighbour.getID())) {
            return;
        }

        // removing neighbour from neighbours-map
        neighbours.remove(neighbour.getID());

        // setting ex-neighbour's delegate instead of him
        Neighbour newNeighbour = neighbour.getDelegate();

        if (neighbour == delegate) {
            // disconnected neighbour is our delegate -> have to choose new one
            delegate = null;
            chooseDelegate();
        }

        if (newNeighbour != null && !ChatNode.this.id.equals(newNeighbour.getID())) {
            // adding new neighbour in neighbours-map
            neighbours.put(newNeighbour.getID(), newNeighbour);
        }

        System.out.println("DID NOT RECEIVE RESPONSES " + 1.0 * timeoutToDisconnect / 1000 + " SECONDS. " + neighbour.getName() + " DISCONNECTED");
    }

    private void manageStuff() {
        long timeout = 80L;

        while (true) {
            synchronized (this) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Long currentTime = System.currentTimeMillis();

            // 1. managing pending requests
            synchronized (pendingRequests) {
                for (Map.Entry<MessageUniqueRecord, PendingMessageRecord> mes : pendingRequests.entrySet()) {
                    DatagramPacket packet = mes.getValue().getPacket();
                    Long lastTimeStamp = mes.getValue().getLastTimeStamp();
                    Long firstTimeStamp = mes.getValue().getFirstTimeStamp();

                    if (currentTime - firstTimeStamp > timeoutToDisconnect) {
                        synchronized (neighbours) {
                            if (mes.getKey().getReceiverID() != null
                                    && neighbours.containsKey(mes.getKey().getReceiverID())) {
                                disconnectNeighbour(neighbours.get(mes.getKey().getReceiverID()));
                            }
                        }
                    } else if (currentTime - lastTimeStamp > timeout || lastTimeStamp.equals(firstTimeStamp)) {
                        sendMessageToNode(packet);
                        mes.getValue().setLastTimeStamp(currentTime);
                    }
                }

                pendingRequests.entrySet().removeIf(e ->
                        System.currentTimeMillis() - e.getValue().getFirstTimeStamp() > timeoutToDisconnect);
            }

            // 2. managing pending responses
            PendingMessageRecord record;
            while ((record = pendingResponses.poll()) != null) {
                // sending messages from pending-responses queue
                sendMessageToNode(record.getPacket());
            }

            // 3. cleaning received-messages map
            synchronized (receivedMessages) {
                receivedMessages.entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue().getFirstTimeStamp() > timeoutToDisconnect);
            }

            // 4. checking connections
            neighbours.forEach((key, neighbour) -> {
                if (neighbour.getLastPingTime() != -1 && currentTime - neighbour.getLastPingTime() > timeoutToDisconnect) {
                    // going through all neighbours & disconnecting gone (?) ones
                    disconnectNeighbour(neighbour);
                }
            });
        }
    }

    /*
        NESTED CLASS
    */

    public static class Neighbour implements Serializable {
        private InetAddress ip;
        private int port;
        private String name;
        private UUID id;
        private Neighbour delegate;
        private transient long lastPingTime;
        private transient Map<MessageUniqueRecord, PendingMessageRecord> successfulSentMessages = new HashMap<>();

        public Neighbour(InetAddress ip, int port) {
            this.ip = ip;
            this.port = port;
            name = "UNKNOWN";
            id = null;
            lastPingTime = -1;
            startCleaner();
        }

        public InetAddress getIP() { return ip; }
        public int getPort() { return port; }
        public String getName() { return name; }
        public UUID getID() { return id; }
        public Neighbour getDelegate() { return delegate; }
        public long getLastPingTime() { return lastPingTime; }

        public void setName(String name) { this.name = name; }
        public void setID(UUID id) { this.id = id; }
        public void setDelegate(Neighbour delegate) { this.delegate = delegate; }
        public void setLastPingTime(long time) { lastPingTime = time; }

        public synchronized void addSuccessfulDispatch(MessageUniqueRecord messageUniqueRecord, PendingMessageRecord pendingMessageRecord) {
            successfulSentMessages.put(messageUniqueRecord, pendingMessageRecord);
        }

        public synchronized boolean checkSuccessfulDispatch(MessageUniqueRecord messageUniqueRecord) {
            return successfulSentMessages.containsKey(messageUniqueRecord);
        }

        private void startCleaner() {
            new Thread(() -> {
                while (true) {
                    synchronized (this) {
                        try {
                            this.wait(timeoutToDisconnect);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        successfulSentMessages.entrySet().removeIf(e-> System.currentTimeMillis() - e.getValue().getFirstTimeStamp() > timeoutToDisconnect);
                    }
                }
            }).start();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            successfulSentMessages = new ConcurrentHashMap<>();
            lastPingTime = -1;
            startCleaner();
        }
    }
}