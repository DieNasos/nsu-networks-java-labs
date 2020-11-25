package ru.nsu.ccfit.beloglazov.chattree.dto;

import ru.nsu.ccfit.beloglazov.chattree.*;
import java.io.Serializable;
import java.util.UUID;

public class DTO implements Serializable {
    private MessageType type;
    private UUID dtoID;
    private UUID senderID;
    private String senderName;
    private String receiverName;
    private String data;
    private ChatNode.Neighbour delegate;

    public DTO(MessageType type, UUID dtoID, UUID senderID, String senderName, String receiverName, String data, ChatNode.Neighbour delegate) {
        this.type = type;
        this.dtoID = dtoID;
        this.senderID = senderID;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.data = data;
        this.delegate = delegate;
    }

    public MessageType getType() { return type; }
    public UUID getID() { return dtoID; }
    public UUID getSenderID() { return senderID; }
    public String getSenderName() { return senderName; }
    public String getData() { return data; }
    public ChatNode.Neighbour getDelegate() { return delegate; }

    @Override
    public String toString() {
        return dtoID.toString() + " :: FROM: " + senderName + ", TO: " + receiverName + ", DATA: " + data;
    }
}