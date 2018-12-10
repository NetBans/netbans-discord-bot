package uk.co.netbans.supportbot.Support.Command;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import uk.co.netbans.supportbot.NetBansBot;
import uk.co.netbans.supportbot.PermType;

public interface Command {
    CommandResult onExecute(NetBansBot bot, Member sender, TextChannel channel, String label, String[] args);

    String name();
    String desc();
    String usage();
    String[] aliases();

    default PermType getPermission() {
        return PermType.DEFAULT;
    }

    default boolean hasPermission(NetBansBot bot, long user) {
        return bot.getPermForPlayer(user).getPriority() >= this.getPermission().getPriority();
    }
}