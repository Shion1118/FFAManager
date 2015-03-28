package com.shion1118.ffamanager;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Stats {

	public static boolean playerExists(String uuid){
		try {
			ResultSet rs = Main.mysql.query("SELECT * FROM FFA WHERE UUID = '" + uuid + "'");

			if(rs.next()){
				return rs.getString("UUID") != null;
			}
			return false;
		} catch (SQLException e) {
			Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + " MySQL ERROR " + e.getMessage());
		}
		return false;
	}

	public static void createPlayer(String uuid, String name){
		if(!(playerExists(uuid))) {
			Main.mysql.update("INSERT INTO FFA(UUID, Name, Kills, Deaths, KillStreak) VALUES ('" + uuid + "', '"+ name +"', '0', '0', '0');");
		}
	}

	public static Integer getKills(String uuid, String name) {
		int i = 0;

		if(playerExists(uuid)) {
			try{
				ResultSet rs = Main.mysql.query("SELECT * FROM FFA WHERE UUID= '" + uuid + "'");
				//if((!rs.next()) || (Integer.valueOf(rs.getInt("Kills")) == null));
				rs.next();
				i = rs.getInt("Kills");
			} catch (SQLException e) {
				Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + "MySQL ERROR " + e.getMessage());
			}
		} else {
			createPlayer(uuid, name);
			getKills(uuid, name);
		}

		return i;
	}

	public static Integer getKills2(String name) {
		int i = 0;

		try{
			ResultSet rs = Main.mysql.query("SELECT * FROM FFA WHERE Name= '" + name + "'");
			//if((!rs.next()) || (Integer.valueOf(rs.getInt("Kills")) == null));
			rs.next();
			i = rs.getInt("Kills");
		} catch (SQLException e) {
			i = -1;
		}


		return i;
	}

	public static Integer getDeaths(String uuid, String name) {
		int i = 0;

		if(playerExists(uuid)) {
			try{
				ResultSet rs = Main.mysql.query("SELECT * FROM FFA WHERE UUID= '" + uuid + "'");
				//if((!rs.next()) || (Integer.valueOf(rs.getInt("Deaths")) == null));
				rs.next();
				i = rs.getInt("Deaths");
			} catch (SQLException e) {
				Bukkit.getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + "MySQL ERROR " + e.getMessage());
			}
		} else {
			createPlayer(uuid, name);
			getDeaths(uuid, name);
		}

		return i;
	}

	public static Integer getDeaths2(String name) {
		int i = 0;

		try{
			ResultSet rs = Main.mysql.query("SELECT * FROM FFA WHERE Name= '" + name + "'");
			//if((!rs.next()) || (Integer.valueOf(rs.getInt("Deaths")) == null));
			rs.next();
			i = rs.getInt("Deaths");
		} catch (SQLException e) {
			i = -1;
		}

		return i;
	}

	public static Integer getRank(String uuid) {
		int i = 0;
		try {
			ResultSet rs = Main.mysql.query("SELECT COUNT(*) FROM FFA WHERE Kills >= (SELECT Kills FROM FFA WHERE UUID = '" + uuid + "');");
			rs.next();
			i = rs.getInt("COUNT(*)");
			return i;
		} catch (SQLException e) {
			return i;
		}
	}

	public static Integer getRank2(String name) {
		int i = 0;
		try {
			ResultSet rs = Main.mysql.query("SELECT COUNT(*) FROM FFA WHERE Kills >= (SELECT Kills FROM FFA WHERE Name = '" + name + "');");
			rs.next();
			i = rs.getInt("COUNT(*)");
			return i;
		} catch (SQLException e) {
			return i;
		}
	}

	public static void setKills(String uuid, String name, Integer kills) {
		if(playerExists(uuid)){
			Main.mysql.update("UPDATE FFA SET Kills= '" + kills + "' WHERE UUID= '" + uuid + "';");
		} else {
			createPlayer(uuid, name);
			setKills(uuid, name, kills);
		}
	}

	public static void setDeaths(String uuid, String name, Integer deaths) {
		if(playerExists(uuid)){
			Main.mysql.update("UPDATE FFA SET Deaths= '" + deaths + "' WHERE UUID= '" + uuid + "';");
		} else {
			createPlayer(uuid, name);
			setKills(uuid, name, deaths);
		}
	}

	public static void addKills(String uuid, String name, Integer kills){
		if(playerExists(uuid)){
			setKills(uuid, name, Integer.valueOf(getKills(uuid, name).intValue() + kills.intValue()));
		} else {
			createPlayer(uuid, name);
			setKills(uuid, name, kills);
		}
	}

	public static void addDeaths(String uuid, String name, Integer deaths){
		if(playerExists(uuid)){
			setDeaths(uuid, name, Integer.valueOf(getDeaths(uuid, name).intValue() + deaths.intValue()));
		} else {
			createPlayer(uuid, name);
			setKills(uuid, name, deaths);
		}
	}


}
