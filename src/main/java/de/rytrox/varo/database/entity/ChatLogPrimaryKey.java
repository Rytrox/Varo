package de.rytrox.varo.database.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ChatLogPrimaryKey implements Serializable {

    private String sender;
    private LocalDateTime timestamp;

    public ChatLogPrimaryKey() {

    }

    public ChatLogPrimaryKey(String sender, LocalDateTime timestamp) {
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatLogPrimaryKey that = (ChatLogPrimaryKey) o;
        return Objects.equals(sender, that.sender) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, timestamp);
    }
}
