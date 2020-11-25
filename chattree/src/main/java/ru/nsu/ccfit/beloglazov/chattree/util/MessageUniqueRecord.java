package ru.nsu.ccfit.beloglazov.chattree.util;

import java.util.Objects;
import java.util.UUID;

public class MessageUniqueRecord {
    private UUID dtoID;
    private UUID receiverID;

    public MessageUniqueRecord(UUID dtoID, UUID receiverID) {
        this.dtoID = dtoID;
        this.receiverID = receiverID;
    }

    public UUID getReceiverID() { return receiverID; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageUniqueRecord that = (MessageUniqueRecord) o;

        return dtoID.equals(that.dtoID) && receiverID.equals(that.receiverID);
    }

    @Override
    public int hashCode() { return Objects.hash(dtoID, receiverID); }
}