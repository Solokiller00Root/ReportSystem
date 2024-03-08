package com.solo.reportsystem.discord;

import com.solo.reportsystem.User.UserRegistery;
import com.solo.reportsystem.util.Database;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class DiscordBot extends ListenerAdapter {
    private static ShardManager shardManager;
    private String guildId;
    public DiscordBot(Plugin plugin) throws SQLException {
        String token = plugin.getConfig().getString("discord.token");
        this.guildId = plugin.getConfig().getString("discord.guildId");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setStatus(OnlineStatus.ONLINE);
        shardManager = builder.build();
        shardManager.addEventListener(this);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getId().equals(guildId)) {
            getLogger().info("Discord Bot is ready for guild: " + event.getGuild().getName());
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (event.getGuild().getId().equals(guildId)) {
            String discordUserId = event.getUser().getId();
            String discordUserName = event.getUser().getName();
            Database.createDiscordUserAsync(discordUserName, discordUserId);
        }
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static void shutdown() {
        shardManager.shutdown();
    }
}