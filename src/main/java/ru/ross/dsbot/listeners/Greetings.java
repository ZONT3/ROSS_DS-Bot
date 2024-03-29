package ru.ross.dsbot.listeners;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static ru.ross.dsbot.Strings.*;

public class Greetings extends ListenerAdapter {
    public static final String ID_CHANNEL_CP     = "268463314982404097";
    public static final String ID_CHANNEL_CP_MSG = "682935497492004874";
    public static final String ID_MSG_CP         = "789180103858192436";
    public static final String ID_GUILD          = "268463314982404097";
    public static final String ID_ROLE_CHECKED   = "501357059665821696";
    public static final String ID_ROLE_MEDIA     = "819137208375836683";
    public static final String ID_MSG_MEDIA      = "834153825354383360";
    public static final String EMOJI             = "U+1F6EC";
    public static final String EMOJI_ANTENNA     = "U+1F4E1";
    private Role roleChecked;
    private Role roleMedia;


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) { // TODO Instantiate all the shit
        Guild guild = event.getGuild();
        final Message message = guild.getTextChannelById(ID_CHANNEL_CP_MSG).retrieveMessageById(ID_MSG_CP).complete();
        final Message messageStreams = guild.getTextChannelById(ID_CHANNEL_CP_MSG).retrieveMessageById(ID_MSG_MEDIA).complete();

        message.addReaction(EMOJI).complete();
        messageStreams.addReaction(EMOJI_ANTENNA).complete();

        roleChecked = event.getJDA().getRoleById(ID_ROLE_CHECKED);
        roleMedia = event.getJDA().getRoleById(ID_ROLE_MEDIA);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        switch (event.getMessageId()) {
            case ID_MSG_CP -> addRole(event.getGuild(), event.getMember(), roleChecked);
            case ID_MSG_MEDIA -> addRole(event.getGuild(), event.getMember(), roleMedia);
        }

    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() != null && event.getUser().isBot()) return;
        switch (event.getMessageId()) {
            case ID_MSG_CP -> rmRole(event.getGuild(), event.getMember(), roleChecked);
            case ID_MSG_MEDIA -> rmRole(event.getGuild(), event.getMember(), roleMedia);
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(ID_GUILD)) return;
        final TextChannel channel = event.getGuild().getTextChannelById(ID_CHANNEL_CP);
        final TextChannel bureau  = event.getGuild().getTextChannelById(ID_CHANNEL_CP_MSG);

        if (channel == null || bureau == null) return;

        if (channel.getGuild().getIdLong() != event.getGuild().getIdLong()) return;

        final String memberMention = event.getUser().getAsMention();
        final String bureauMention = bureau.getAsMention();
        channel.sendMessage(String.format(STR.getString("checkpoint.greetings"), memberMention)).queue();
        event.getUser().openPrivateChannel().complete()
                .sendMessage(String.format(STR.getString("checkpoint.greetings.pm"),
                        memberMention, bureauMention)).queue();
    }

    private synchronized void addRole(Guild guild, Member member, Role role) {
        guild.addRoleToMember(member, role).complete();
    }

    private synchronized void rmRole(Guild guild, Member member, Role role) {
        guild.removeRoleFromMember(member, role).complete();
    }
}
