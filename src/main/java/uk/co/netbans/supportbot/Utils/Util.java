package uk.co.netbans.supportbot.Utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import uk.co.netbans.supportbot.Music.MusicManager;
import uk.co.netbans.supportbot.NetBansBot;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final MusicManager musicManager = new MusicManager();

    public static MusicManager getMusicManager() {
        return musicManager;
    }

    private static void sendMessage(MessageChannel channel, Message message) {
        channel.sendMessage(message).queue(null, null);
    }

    public static void sendMessage(MessageChannel channel, MessageEmbed embed) {
        sendMessage(channel, new MessageBuilder().setEmbed(embed).build());
    }

    public static void sendMessage(MessageChannel channel, String message) {
        sendMessage(channel, new MessageBuilder().append(message).build());
    }

    public static String filter(String msgContent) {
        return msgContent.length() > 2000
                ? "*The output message is over 2000 characters!*"
                : msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");
    }

    public static String userDiscrimSet(User u) {
        return stripFormatting(u.getName()) + "#" + u.getDiscriminator();
    }

    public static String stripFormatting(String s) {
        return s.replace("*", "\\*")
                .replace("`", "\\`")
                .replace("_", "\\_")
                .replace("~~", "\\~\\~")
                .replace(">", "\u180E>");
    }

    public static boolean containsLink(String message) {
        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(message);
        return m.find();
    }

    public static Member randomMember(NetBansBot bot) {
        List<Member> members = new ArrayList<>();
        for (Role role : bot.getJDA().getGuildById(bot.getGuildID()).getRoles()) {
            if (role.getName().toLowerCase().contains("leader") || role.getName().toLowerCase().contains("cont") || role.getName().toLowerCase().contains("dev") || role.getName().toLowerCase().contains("admin")) {
                for (Member member : bot.getJDA().getGuildById(bot.getGuildID()).getMembers()) {
                    if (member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.IDLE)) {
                        if (member.getRoles().contains(role)) {
                            if (!member.getUser().isBot())
                                members.add(member);
                        }
                    }
                }
            }
        }
        return members.get(ThreadLocalRandom.current().nextInt(members.size()));
    }

    public static ArrayList<TextChannel> getSupportChannels(NetBansBot bot) {
        ArrayList<TextChannel> channels = new ArrayList<>();
        for (TextChannel channel : bot.getJDA().getGuildById(bot.getGuildID()).getCategoryById(bot.getSupportCategoryID()).getTextChannels()) {
            channels.add(channel);
            if (channel.getIdLong() == 526622624655343626L || channel.getIdLong() == 521478045924589568L) {
                channels.remove(channel);
            }
        }
        return channels;
    }

    public static void doExpiryCheck(NetBansBot bot) {
        int expiryCount = 0;
        for (TextChannel channel : getSupportChannels(bot)) {
            for (Message message : channel.getHistory().retrievePast(5).complete()) {
                if (!channel.getMessageById(channel.getLatestMessageId()).complete().getAuthor().isBot()) {
                    if (message.getAuthor().equals(channel.getPinnedMessages().complete().get(0).getMentionedUsers().get(0))) {
                        if (message.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(1))) {
                            expiryCount++;
                        }
                    }
                }
            }
            if (expiryCount > 0) {
                expireChannel(bot, channel);
            }
        }
    }

    private static void expireChannel(NetBansBot bot, TextChannel channel) {
        for (Message message : channel.getPinnedMessages().complete()) {
            if (message.getAuthor().isBot()) {
                for (Member member : message.getMentionedMembers()) {
                    String reason = "This issue has not been replied to in over a week by the Ticket Creator!";
                    member.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder()
                            .setTitle("Issue Expired!")
                            .setDescription("This issue has not been replied to in over a week by the Ticket Creator!" + " \nBecause of this we have sent you a log file containing the history, so that you may look at it incase you encounter the issue again!")
                            .addField("Next Step: ", "If the issue still persists, please create a github issue using the link provided below!\n" +
                                    "https://github.com/NetBans/NetBans/issues/new", false)
                            .build()).complete();
                    member.getUser().openPrivateChannel().complete()
                            .sendFile(bot.getLogDirectory().resolve(channel.getName() + ".log").toFile())
                            .complete();
                    bot.getJDA().getGuildById(bot.getGuildID()).getTextChannelById(Long.valueOf((String) bot.getConf().get("logChannelID")))
                            .sendFile(bot.getLogDirectory().resolve(channel.getName() + ".log").toFile(), new MessageBuilder()
                                    .append(reason)
                                    .build())
                            .complete();
                    channel.delete().reason("Issue Expired!").complete();
                }
            }
        }
    }

    public static String[] quotesOrSpaceSplits(String str) {
        str += " ";
        ArrayList<String> strings = new ArrayList<String>();
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"' || c == ' ' && !inQuote) {
                if (c == '"')
                    inQuote = !inQuote;
                if (!inQuote && sb.length() > 0) {
                    strings.add(sb.toString());
                    sb.delete(0, sb.length());
                }
            } else
                sb.append(c);
        }
        return strings.toArray(new String[strings.size()]);
    }

    public static String formatProgressBar(long progress, long full) {
        double percentage = (double) progress / full;
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if ((int) (percentage * 20) == i)
                progressBar.append("\uD83D\uDD18");
            else
                progressBar.append("▬");
        }
        return progressBar.toString();
    }
}
