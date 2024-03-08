package com.solo.reportsystem.util;


import com.solo.reportsystem.ReportSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Database {
    private static String url;
    private static String user;
    private static String psw;
    private static Connection connection;
    private static final long IDLE_TIMEOUT = 1800000;
    private static long lastConnectionTime = System.currentTimeMillis();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            establishConnection();
        }
        lastConnectionTime = System.currentTimeMillis();
        return connection;
    }

    public static CompletableFuture<Boolean> createDiscordUserAsync(String discordUserName, String discordUserId) {
        return CompletableFuture.supplyAsync(() -> createDiscordUser(discordUserName,discordUserId));
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

    public static boolean createProfileAsync(String uuid, String name, String rank) {
        Bukkit.getScheduler().runTaskAsynchronously(ReportSystem.getInstance(), () -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO Profiles (UUID, USERNAME, RANK) VALUES (?, ?, ? )")) {

                statement.setString(1, uuid);
                statement.setString(2, name);
                statement.setString(3, rank);

                statement.executeUpdate();
                Bukkit.getLogger().info("Player " + name + " registered successfully.");
            } catch (SQLException e) {
                handleSQLException(e, "Error inserting player data into the database. UUID: " + uuid + ", Name: " + name);
            }
        });
        return false;
    }

    public static boolean findPlayerUUID(String uuid) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Profiles WHERE UUID = ?")) {
            statement.setString(1, uuid);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, ChatColor.RED + "Error finding player in the database", e);
        }
        return false;
    }




    public static void onEnable() {
        String ip = ReportSystem.getInstance().getConfig().getString("database.ip");
        String name = ReportSystem.getInstance().getConfig().getString("database.name");
        url = "jdbc:mysql://" + ip + "/" + name;
        user = ReportSystem.getInstance().getConfig().getString("database.user");
        psw = ReportSystem.getInstance().getConfig().getString("database.password");
        try {
            createTables();
            createTablesDiscord();
        } catch (SQLException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, ChatColor.RED + "Error creating tables in the database", e);
        }
        executorService.scheduleWithFixedDelay(() -> {
            long currentTime = System.currentTimeMillis();
            if (connection != null && (currentTime - lastConnectionTime) > IDLE_TIMEOUT) {
                try {
                    destroyConnection();
                    establishConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, IDLE_TIMEOUT, IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static void onDisable() {
        try {
            destroyConnection();
        } catch (SQLException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, ChatColor.RED + "Error closing database connection", e);
        }
        executorService.shutdown();
    }

    private static void establishConnection() throws SQLException {
        connection = DriverManager.getConnection(url, user, psw);
    }


    private static void createTables() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String sqlPlayerProfilesTable = "CREATE TABLE IF NOT EXISTS Profiles (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT," +
                    "UUID VARCHAR(36) UNIQUE NOT NULL," +
                    "USERNAME VARCHAR(16) NOT NULL," +
                    "RANK VARCHAR(16) NOT NULL" +
                    ")";
            statement.execute(sqlPlayerProfilesTable);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[DB] Player profiles tables loaded");
        }
    }

    private static void createTablesDiscord() throws SQLException {
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
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[DB] Discord profiles tables loaded");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[DB] Discord profiles tables not loaded");
        }
    }
    private static void handleSQLException(SQLException e, String errorMessage) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[DB] " + errorMessage + " " + e.getMessage());
    }

    private static void destroyConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

}
