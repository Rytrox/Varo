package de.rytrox.varo.teams;

import io.avaje.lang.Nullable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConversationService {

    private static final Map<CommandSender, CommandSender> lastConversationPartner = new HashMap<>();

    private ConversationService() {}

    public static void setLastConversationPartner(@NotNull CommandSender messageTarget, @NotNull CommandSender messageSender) {
        ConversationService.lastConversationPartner.put(messageTarget, messageSender);
    }

    @Nullable
    public static CommandSender getLastConversationPartner(CommandSender messageTarget) {
        return lastConversationPartner.get(messageTarget);
    }

}
