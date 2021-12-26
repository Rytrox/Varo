package de.rytrox.varo.database.repository;

import de.rytrox.varo.chat_log.ChatLogType;
import de.rytrox.varo.database.entity.ChatLog;
import io.ebean.Database;
import io.ebean.Expr;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatLogRepository {

    private static final String FIELD_SENDER = "sender";
    private static final String FIELD_TARGET = "target";
    private static final String FIELD_TIMESTAMP = "timestamp";
    
    private final Database database;

    public ChatLogRepository(@NotNull Database database) {
        this.database = database;
    }

    public void addChatLog(@NotNull ChatLog chatLog) {
        database.save(chatLog);
    }

    /**
     * Selects all ChatLogs from the conversation between the given players
     *
     * @param players players that participated at the conversation
     * @param from The earliest date you want to read the chatlogs from
     * @param to The latest date you want to read the chatlogs from
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    public List<ChatLog> getConversation(@NotNull ChatLogType chatLogType, LocalDateTime from, LocalDateTime to, @NotNull String... players) {
        switch(chatLogType) {
            case ALL: return getConversationAll(from, to, players);
            case MSG: return getConversationMSG(from, to, players);
            case GLOBAL: return getConversationGlobal(from, to, players);
            default: return new ArrayList<>();
        }
    }

    /**
     * Selects all ChatLogs from the conversation between the given players
     *
     * @param players players that participated at the conversation
     * @param from The earliest date you want to read the chatlogs from
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    public List<ChatLog> getConversation(@NotNull ChatLogType chatLogType, LocalDateTime from, @NotNull String... players) {
        return getConversation(chatLogType, from, LocalDateTime.MAX, players);
    }

    /**
     * Selects all ChatLogs from the conversation between the given players
     *
     * @param players players that participated at the conversation
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    public List<ChatLog> getConversation(@NotNull ChatLogType chatLogType, @NotNull String... players) {
        return getConversation(chatLogType, LocalDateTime.MIN, LocalDateTime.MAX, players);
    }


    /**
     * Selects all ChatLogs from the given players
     *
     * @param players players that participated at the conversation
     * @param from The earliest date you want to read the chatlogs from
     * @param to The latest date you want to read the chatlogs from
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    private List<ChatLog> getConversationAll(LocalDateTime from, LocalDateTime to, @NotNull String... players) {
        return database.find(ChatLog.class)
                .where()
                .or(
                    Expr.in(FIELD_SENDER, players),
                    Expr.in(FIELD_TARGET, players)
                )
                .orderBy(FIELD_TIMESTAMP)
                .findList()
                .stream()
                .filter(chatLog -> chatLog.getPrimaryKey().getTimestamp().isAfter(from)
                                && chatLog.getPrimaryKey().getTimestamp().isBefore(to))
                .collect(Collectors.toList());
    }

    /**
     * Selects all MSG ChatLogs from the given players
     *
     * @param players players that participated at the conversation
     * @param from The earliest date you want to read the chatlogs from
     * @param to The latest date you want to read the chatlogs from
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    private List<ChatLog> getConversationMSG(LocalDateTime from, LocalDateTime to, @NotNull String... players) {
        return database.find(ChatLog.class)
                .where()
                .and(
                    Expr.not(Expr.eq(FIELD_TARGET, ChatLogType.GLOBAL.getName())),
                    Expr.or(
                            Expr.in(FIELD_SENDER, players),
                            Expr.in(FIELD_TARGET, players)
                    )
                )
                .orderBy(FIELD_TIMESTAMP)
                .findList()
                .stream()
                .filter(chatLog -> chatLog.getPrimaryKey().getTimestamp().isAfter(from)
                        && chatLog.getPrimaryKey().getTimestamp().isBefore(to))
                .collect(Collectors.toList());
    }

    /**
     * Selects all MSG ChatLogs from the given players
     *
     * @param players players that participated at the conversation
     * @param from The earliest date you want to read the chatlogs from
     * @param to The latest date you want to read the chatlogs from
     * @return the list of ChatLogs, sorted by the timestamp
     */
    @NotNull
    private List<ChatLog> getConversationGlobal(LocalDateTime from, LocalDateTime to, @NotNull String... players) {
        return database.find(ChatLog.class)
                .where()
                .and(
                    Expr.in(FIELD_SENDER, players),
                    Expr.eq(FIELD_TARGET, ChatLogType.GLOBAL.getName())
                )
                .orderBy(FIELD_TIMESTAMP)
                .findList()
                .stream()
                .filter(chatLog -> chatLog.getPrimaryKey().getTimestamp().isAfter(from)
                        && chatLog.getPrimaryKey().getTimestamp().isBefore(to))
                .collect(Collectors.toList());
    }
}
