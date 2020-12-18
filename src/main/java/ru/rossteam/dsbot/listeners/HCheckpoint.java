package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.dsbot.core.tools.Tools;

import static ru.rossteam.dsbot.tools.Commons.getCheckedRoleID;
import static ru.rossteam.dsbot.tools.Commons.getCheckpointMessageID;

public class HCheckpoint extends LStatusHandler {

    private String checkpointID;
    private Role role;

    public HCheckpoint(ZDSBot bot) {
        super(bot);
    }

    @Override
    public void prepare(ReadyEvent event) throws Exception {
        checkpointID = getCheckpointMessageID();
        final Message message = Tools.tryFindMessage(checkpointID, getJda());
        Messages.addOK(message);

        role = Tools.tryFindRole(getCheckedRoleID(), getJda());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        if (!event.getMessageId().equals(checkpointID)) return;
        if (!event.getReactionEmote().getEmoji().equals(Messages.EMOJI_OK)) return;
        addRole(event.getGuild(), event.getMember());
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() != null && event.getUser().isBot()) return;
        if (!event.getMessageId().equals(checkpointID)) return;
        if (!event.getReactionEmote().getEmoji().equals(Messages.EMOJI_OK)) return;
        rmRole(event.getGuild(), event.getMember());
    }

    private synchronized void addRole(Guild guild, Member member) {
        guild.addRoleToMember(member, role).complete();
    }

    private synchronized void rmRole(Guild guild, Member member) {
        guild.removeRoleFromMember(member, role).complete();
    }

    @Override
    public void update() throws Exception { }

    @Override
    public long getPeriod() {
        return 0;
    }
}
