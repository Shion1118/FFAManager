package com.shion1118.ffamanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MySQL {

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    private Connection con;

    public MySQL() {
    	File file = new File("plugins/FFAManager/","config.yml");
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		String db = "MySQL.";
		this.host = cfg.getString(db + "Host");
		this.port = cfg.getInt(db + "Port");
        this.user = cfg.getString(db + "Username");
        this.password = cfg.getString(db + "Password");
        this.database = cfg.getString(db + "Database");

        connect();
    }

    public void connect() {
            try {
            	con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
            	Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.DARK_AQUA + " MySQL Connected!!");
            } catch (SQLException e) {
            	Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + " MySQL Can't Connected!!" + e.getMessage());
            }
    }

    public void close() {
            try {
                    if(con != null) {
                            con.close();
                            Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.DARK_AQUA + "MySQL Disconnected!!");
                    }
            } catch (SQLException e) {
            	Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + "MySQL Can't Disconnected!!" + e.getMessage());
            }
    }

    public void update(String qry) {
            try {
            	Statement st = con.createStatement();
            	st.executeUpdate(qry);
            	st.close();
            } catch (SQLException e) {
            	connect();
            	System.err.println(e);
            }
    }

    public ResultSet query(String qry) {
            ResultSet rs = null;

            try {
                    Statement st = con.createStatement();
                    rs = st.executeQuery(qry);
            } catch (SQLException e) {
                    connect();
                    System.err.println(e);
            }
            return rs;
    }
}
