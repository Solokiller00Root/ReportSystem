package com.solo.reportsystem.User;

import com.solo.reportsystem.util.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UserRegistery {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistery.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool();


    public static boolean findById(String discordUserId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM DiscordSync WHERE discordUserId = ?")) {
            statement.setString(1, discordUserId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding player in the database", e);
            throw e;
        }
    }

    public static CompletableFuture<Boolean> createDiscordUserAsync(String discordUserName, String discordUserId) {
        return CompletableFuture.supplyAsync(() -> createDiscordUser(discordUserName,discordUserId), executor);
    }

    private static boolean createDiscordUser(String discordUserName,String discordUserId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO DiscordSync (discordUserName,discordUserId, privateKey) VALUES (?,?, ?)")) {

            statement.setString(1, discordUserName);
            statement.setString(2, discordUserId);

            String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            StringBuilder syncCode = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                syncCode.append(symbols.charAt(random.nextInt(symbols.length())));
            }
            statement.setString(3, syncCode.toString());

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            handleSQLException(e, "Error inserting user data into the database. DiscordUserID: " + discordUserId);
            return false;
        }
    }

    public static void createTables() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            String sqlPlayerProfilesTable = "CREATE TABLE IF NOT EXISTS DiscordSync ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "discordUserName VARCHAR(255) NOT NULL,"
                    + "discordUserId VARCHAR(255) NOT NULL,"
                    + "privateKey VARCHAR(10) NOT NULL,"
                    + "synced TINYINT(1) DEFAULT 0"
                    + ")";
            statement.execute(sqlPlayerProfilesTable);
            System.out.println("[DB] Discord profiles loaded");
        } catch (SQLException e) {
            LOGGER.error("[DB] Discord profiles NOT tables loaded", e);
        }
    }

    private static void handleSQLException(SQLException e, String errorMessage) {
        LOGGER.error("[DB] " + errorMessage + " " + e.getMessage());
    }
}
