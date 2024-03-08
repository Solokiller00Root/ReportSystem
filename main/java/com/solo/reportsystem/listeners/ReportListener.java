    package com.solo.reportsystem.listeners;

    import com.solo.reportsystem.discord.DiscordBot;
    import com.solo.reportsystem.ReportSystem;
    import net.dv8tion.jda.api.EmbedBuilder;
    import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
    import org.bukkit.ChatColor;
    import org.bukkit.Material;
    import org.bukkit.World;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.Listener;
    import org.bukkit.event.inventory.InventoryClickEvent;
    import org.bukkit.event.inventory.InventoryCloseEvent;
    import org.bukkit.event.inventory.InventoryType;
    import org.bukkit.event.player.AsyncPlayerChatEvent;
    import org.bukkit.metadata.FixedMetadataValue;
    import org.bukkit.plugin.Plugin;

    import java.awt.*;

    public class ReportListener implements Listener {
        private Plugin plugin;
        private DiscordBot discordBot;

        public ReportListener(Plugin plugin, DiscordBot discordBot){
            this.plugin = plugin;
            this.discordBot = discordBot;
        }

        @EventHandler
        public void onInteract(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();

            if (player.hasMetadata("OpenedReportMenu")) {
                if (e.getView().getTitle().startsWith("Report: ")) {
                    if (e.getInventory().getType() == InventoryType.PLAYER) {
                        e.setCancelled(true);
                        return;
                    }
                    if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                        return;
                    }
                    e.setCancelled(true);
                    switch (e.getRawSlot()) {
                        case 10:
                            submitReport(player, "Chat Offense");
                            break;
                        case 13:
                            submitReport(player, "Gameplay Offense");
                            break;
                        case 16:
                            submitReport(player, "Hacking / Cheating");
                            break;
                        case 28:
                            submitReport(player, "Report for Skin/Name");
                            break;
                        case 31:
                            submitReport(player, "Report for No Knockback");
                            break;
                        case 34:
                            player.closeInventory();
                            player.sendMessage(ChatColor.YELLOW + "Type the reason for your report in chat:");
                            player.setMetadata("WaitingForReportReason", new FixedMetadataValue(ReportSystem.getInstance(), "Waiting for report reason"));
                            break;
                        case 49:
                            player.closeInventory();
                            player.sendMessage(ChatColor.YELLOW + "You closed the report menu.");
                            break;
                    }
                }
            }
        }

        private void submitReport(Player player, String reason) {
            player.sendMessage(ChatColor.YELLOW + "Your report has been submitted. Thank you for your help!");
            player.closeInventory();


            sendReportToDiscord(player, reason);
        }
        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            if (player.hasMetadata("WaitingForReportReason")) {
                String reason = event.getMessage();
                player.removeMetadata("WaitingForReportReason", ReportSystem.getInstance());
                submitReport(player, reason);
                event.setCancelled(true);
            }
        }

        private void sendReportToDiscord(Player player, String reason) {
            String playerName = player.getName();
            World world = player.getWorld();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(":rotating_light: New Report :rotating_light:");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField("Reporter", playerName, false);
            embedBuilder.addField("Reason", reason, false);
            embedBuilder.addField("World", world.getName(), false);

            TextChannel reportChannel = discordBot.getShardManager().getTextChannelById(plugin.getConfig().getString("discord.report_channel"));

            if (reportChannel != null) {
                reportChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            } else {
                plugin.getLogger().warning("Report channel not found!");
            }
        }
        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getPlayer().hasMetadata("OpenedReportMenu"))
                e.getPlayer().removeMetadata("OpenedReportMenu", ReportSystem.getInstance());
        }
    }
