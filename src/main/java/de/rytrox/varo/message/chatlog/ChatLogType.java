package de.rytrox.varo.message.chatlog;

import java.util.Arrays;
import java.util.Optional;

public enum ChatLogType {

    ALL("all"),
    GLOBAL("global"),
    MSG("msg");

    private final String name;

    ChatLogType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<ChatLogType> fromName(String name) {
        return Arrays.stream(values()).filter(type -> type.getName().equalsIgnoreCase(name)).findFirst();
    }
}
