package com.solo.reportsystem.commands;

import com.solo.reportsystem.ReportSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ReportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /report <name>");
            return true;
        }

        String playerName = strings[0];

        Player reportedPlayer = Bukkit.getPlayer(playerName);
        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player " + playerName + " not found or not online.");
            return true;
        }
        player.sendMessage(ChatColor.GRAY + "Submitting your report...");

        Inventory reportUI = Bukkit.createInventory(null, 54, "Report: " + playerName);

        ItemStack chatItem = createItem(Material.BOOK, ChatColor.GREEN + "Chat Offense", ChatColor.GRAY + "Report for chat-related offenses");
        ItemStack gameplayItem = createItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "Gameplay Offense", ChatColor.GRAY + "Report KillAura/Hacking");
        ItemStack hackingItem = createItem(Material.DIAMOND_PICKAXE, ChatColor.GREEN + "Hacking / Cheating", ChatColor.GRAY + "Report for hacking or cheating");
        ItemStack knockBack = createItem(Material.LEATHER_CHESTPLATE, ChatColor.GREEN + "Report for No Knockback", ChatColor.GRAY + "Report for suspicious no knockback behavior");
        ItemStack other = createItem(Material.FEATHER, ChatColor.GREEN + "Other");
        ItemStack skin = createItem(Material.SKULL_ITEM, ChatColor.GREEN + "Report for Skin/Name", ChatColor.GRAY + "Report for inappropriate skin or name");
        ItemStack close = createItem(Material.ARROW, ChatColor.RED + "Close", ChatColor.GRAY + "Close the report menu");

        reportUI.setItem(10, chatItem);
        reportUI.setItem(13, gameplayItem);
        reportUI.setItem(16, hackingItem);
        reportUI.setItem(28, skin);
        reportUI.setItem(31, knockBack);
        reportUI.setItem(34, other);
        reportUI.setItem(49, close);

        ItemStack mirrorItem = createItem(Material.STAINED_GLASS_PANE, "Mirror", "View yourself");
        for (int i = 0; i < reportUI.getSize(); i++) {
            ItemStack currentItem = reportUI.getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                reportUI.setItem(i, mirrorItem);
            }
        }
        player.setMetadata("ReportedPlayer", new FixedMetadataValue(ReportSystem.getInstance(), playerName));
        player.openInventory(reportUI);
        player.setMetadata("OpenedReportMenu", new FixedMetadataValue(ReportSystem.getInstance(), "Report menu"));
        sendReportMessage(playerName, player.getName(), getServerName(player));
        return true;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private String getServerName(Player player) {
        String server = player.getServer().getServerName();
        return server;
    }

    private void sendReportMessage(String playerName, String reporterName,  String serverName) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Report");
            out.writeUTF(playerName);
            out.writeUTF(reporterName);
            out.writeUTF(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Player player = Bukkit.getPlayer(reporterName);
        player.sendPluginMessage(ReportSystem.getInstance(), "ReportSystemBungee", b.toByteArray());

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.hasMetadata("OpenedReportMenu")) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String itemName = clickedItem.getItemMeta().getDisplayName();
        if (itemName.equals(ChatColor.RED + "Close")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Type the reason for your report in chat:");
            player.setMetadata("WaitingForReportReason", new FixedMetadataValue(ReportSystem.getInstance(), "Waiting for report reason"));
        }
    }

}
