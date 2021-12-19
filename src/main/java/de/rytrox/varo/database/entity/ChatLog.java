package de.rytrox.varo.database.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity that represents a Chat-Log
 */
@Entity
@Table(name = "ChatLogs")
public class ChatLog {

    @EmbeddedId
    private ChatLogPrimaryKey primaryKey;

    @Column(name = "target", nullable = false)
    private String target;

    @Column(name = "message", nullable = false)
    private String message;


    /**
     * Constructor for JPA
     */
    public ChatLog() {
    }

    public ChatLog(String sender, String target, String message) {
        this.primaryKey = new ChatLogPrimaryKey(sender, LocalDateTime.now());
        this.target = target;
        this.message = message;
    }

    public ChatLogPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ChatLogPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[%s] player '%s' sent message '%s' to '%s'",
                primaryKey.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                primaryKey.getSender(),
                message,
                target);
    }

}
