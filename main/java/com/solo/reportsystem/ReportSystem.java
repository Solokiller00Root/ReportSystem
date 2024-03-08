package com.solo.reportsystem;

import com.solo.reportsystem.commands.ReportCommand;
import com.solo.reportsystem.discord.DiscordBot;
import com.solo.reportsystem.listeners.ReportListener;
import com.solo.reportsystem.util.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;


public final class ReportSystem extends JavaPlugin {

    private static ReportSystem instance;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        DiscordBot discordBot = null;
        try {
            discordBot = new DiscordBot(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        getCommand("report").setExecutor(new ReportCommand());

        //Bukkit.getMessenger().registerOutgoingPluginChannel(this, "ReportSystemBungee");
        Bukkit.getPluginManager().registerEvents(new ReportListener(instance, discordBot), this);

        System.out.println("\u001B[34m" + "****************************************************" + "\u001B[0m");
        System.out.println("\u001B[34m" + "*                                                  *" + "\u001B[0m");
        System.out.println("\u001B[32m" + "*              ReportSystem Plugin Enabled         *" + "\u001B[0m");
        System.out.println("\u001B[33m" + "*                Version: 1.0.0                    *" + "\u001B[0m");
        System.out.println("\u001B[31m" + "*                Created by: Solo .solwx           *" + "\u001B[0m");
        System.out.println("\u001B[34m" + "*                                                  *" + "\u001B[0m");
        System.out.println("\u001B[34m" + "****************************************************" + "\u001B[0m");
    }

    @Override
    public void onDisable() {
        try {
            DiscordBot.shutdown();
        } catch (Exception ignore) {
        }
        Database.onDisable();
    }

    public static ReportSystem getInstance(){
        return instance;
    }
}
