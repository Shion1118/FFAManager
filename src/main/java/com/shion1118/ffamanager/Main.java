package com.shion1118.ffamanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Main extends JavaPlugin {

	public static Economy eco = null;
	public static String Servername;
	public static Double KillMoney;
	public static Double DeathMoney;
	public static String KillMessage;
	public static String KillMessage2;
	public static String DeathMessage;
	public static String DeathMessage2;

	public static File kitsFile;
	public static FileConfiguration kits;

	public static HashMap<Player, Boolean> onekitperlife = new HashMap<Player, Boolean>();

	public static MySQL mysql = null;

    @Override
    public void onEnable() {

    	saveDefaultConfig();

    	MySQLConnect();

        registerEvents();

        try {
			setupConfig();
		} catch (IOException | InvalidConfigurationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

        if(!setupEconomy()){
        	getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.RED + " Vault Not Found!");
        	Bukkit.getPluginManager().disablePlugin(this);
        } else  {
        	getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.DARK_AQUA + " Vault Found!!");
        }
        getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.DARK_AQUA + " Plugin Enable");
    }

	@Override
    public void onDisable() {

    	getServer().getConsoleSender().sendMessage("[FFAManager]" + ChatColor.DARK_AQUA + " Disable");
    }

	private void MySQLConnect() {
		mysql = new MySQL();
		mysql.update("CREATE TABLE IF NOT EXISTS FFA(UUID varchar(64), Name varchar(64), Kills int, Deaths int, KillStreak int);");
	}

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }

    private void setupConfig() throws IOException, InvalidConfigurationException{
    	//Basic Config
    	Servername = getConfig().getString("Server Name");
    	KillMoney = getConfig().getDouble("Money per kill");
        DeathMoney = getConfig().getDouble("Money per death");
        KillMessage = getConfig().getString("Kill Message");
        DeathMessage = getConfig().getString("Death Message");
        if(KillMessage.contains("<money>")){
        	KillMessage = KillMessage.replace("<money>", KillMoney.toString());
        }
        if(DeathMessage.contains("<money>")) {
        	DeathMessage = DeathMessage.replace("<money>", DeathMoney.toString());
        }
    	if(KillMessage.contains("&")){
    		KillMessage = KillMessage.replaceAll("&", "§");
    	}
    	if(DeathMessage.contains("&")){
    		DeathMessage = DeathMessage.replaceAll("&", "§");
    	}

    	KillMessage2 = KillMessage;
    	DeathMessage2 = DeathMessage;

    	//Kits Config
    	kitsFile = new File(getDataFolder(), "kits.yml");
    	if(!kitsFile.exists()){
    		kitsFile.getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            copy(getResource("kits.yml"), kitsFile);
    	}
    	kits = new YamlConfiguration();
    	kits.load(kitsFile);
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
    	PluginManager pm = Bukkit.getPluginManager();
    	pm.registerEvents(new Event(), this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (sender instanceof Player) {
    		Player p = (Player) sender;
    		int kills = 0;
    		int deaths = 0;
    		double kd = 0;
    		int rank = 0;
    		int killstreak = 0;
    		if(cmd.getName().equalsIgnoreCase("stats") && args.length <= 1){
    			p.sendMessage("§7<-----§5"+ Servername +"§7----->");
    			if(args.length == 0){
    				kills = Stats.getKills(p.getUniqueId().toString(), p.getName());
    				deaths = Stats.getDeaths(p.getUniqueId().toString(), p.getName());
    				kd = (double)kills/(double)deaths;
    				rank = Stats.getRank(p.getUniqueId().toString());
    				//killstreak = Stats.getKillStreak(p.getUniqueId().toString());
    				p.sendMessage("§7ID: " + "§A" + p.getName());
    			} else if(args.length == 1) {
    				kills = Stats.getKills2(args[0]);
    				if(kills==-1) {
    					p.sendMessage("§cPlayer doesn't exists");
    					return true;
    				}
    				deaths = Stats.getDeaths2(args[0]);
    				kd = (double)kills/(double)deaths;
    				rank = Stats.getRank2(args[0]);
    				//killstreak = Stats.getKillStreak(Bukkit.getPlayer(args[0]).getUniqueId().toString());
    				p.sendMessage("§7ID: " + "§A" + args[0]);
    			}
				p.sendMessage("\n");
				p.sendMessage("§7§lFFA\n");
				p.sendMessage("§7Kills: " + "§6" + kills + " §7Deaths: " + "§6" + deaths + "\n");
				p.sendMessage("§7K/D: " + "§6" + String.format("%.2f", kd) + " §7Rank: " + "§B" + rank);
				p.sendMessage("§7HighestKillStreak: " + "§6" + killstreak + "\n");
    		}
    		if(cmd.getName().equalsIgnoreCase("kit") && args.length <= 1){
    			if(args.length == 0){
    				KitGUI(p);
    			} else if(p.hasPermission("kit." + args[0])){
    				if(!onekitperlife.containsKey(p)){
    					Kit(p,args[0]);
    				} else if(onekitperlife.get(p)){
    					p.sendMessage("§6You have already chosen the kit!");
    				} else {
    					Kit(p,args[0]);
    				}
    			} else if((eco.getBalance(p.getName()) > kits.getInt("Kits." + args[0] + ".Unlock"))){
    				eco.withdrawPlayer(p.getName(), kits.getInt("Kits." + args[0] + ".Unlock") );
    				Bukkit.dispatchCommand(getServer().getConsoleSender(),"pex user "+p.getName()+" add kit." + args[0]);
    			}
    		}
    		if(cmd.getName().equalsIgnoreCase("rkit") && args.length <= 0){
    			if(eco.getBalance(p.getName()) >= 100){
    				eco.withdrawPlayer(p.getName(), 100);
    				RandomKit(p);
    			} else {
    				p.sendMessage("§6 No Money!");
    			}
    		}
    		return true;
    	} else {
    		sender.sendMessage("This Command for Player!");
    		return false;
    	}
    }

    public void KitGUI(Player p){
    	int i = 0;
    	List<String> lore = new ArrayList<String>();
    	Inventory inv = Bukkit.createInventory(null, 54,"§aKit Selector");
		for(String s : kits.getConfigurationSection("Kits").getKeys(false)){
			ItemStack is = new ItemStack(kits.getInt("Kits." + s + ".Item"));
			ItemMeta im = is.getItemMeta();

			im.setDisplayName(kits.getString("Kits." + s + ".Name").replaceAll("&", "§"));

			if(!p.hasPermission("kit." + s)){
				lore.add("You can't use this kit!");
				lore.add("Unlock Cost:" + kits.getInt("Kits." + s + ".Unlock"));
				im.setLore(lore);
			}

			is.setItemMeta(im);
			inv.setItem(i, is);
			i++;
		}
		p.openInventory(inv);
    }

    public void RandomKit(Player p){
    	Inventory inv = Bukkit.createInventory(null, 45,"§bGet Random Kit");
    	HashMap<String, ItemStack> randomkit = new HashMap<String, ItemStack>();
    	for(String s : kits.getConfigurationSection("Kits").getKeys(false)){
			ItemStack is = new ItemStack(kits.getInt("Kits." + s + ".Item"));
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(kits.getString("Kits." + s + ".Name").replaceAll("&", "§"));
			is.setItemMeta(im);
			randomkit.put(s, is);
		}

		Random rand = new Random();
        int ran = rand.nextInt(100);
		int j=0;
		for(String s : kits.getConfigurationSection("Kits").getKeys(false)){
			if(j == ran%randomkit.size()){
				ItemStack is = new ItemStack(randomkit.get(s));
				inv.setItem(22, is);
				p.openInventory(inv);
				break;
			}
			j++;
		}
    }

    @SuppressWarnings("deprecation")
    public static void Kit(Player p, String kit){
    	for(String s : kits.getConfigurationSection("Kits").getKeys(false)){
    		if(s.equalsIgnoreCase(kit)){
    			p.sendMessage(kits.getString("Kits." + s + ".Name").replaceAll("&", "§") + " Selected!");
    			p.getInventory().clear();
    			for(String ar : kits.getConfigurationSection("Kits." + s + ".Armor").getKeys(false)){
    				ItemStack is = new ItemStack(kits.getInt("Kits." + s + ".Armor." + ar + ".ID"));
    				if(ar.equalsIgnoreCase("Helmet")){
    					p.getInventory().setHelmet(is);
    				} else if(ar.equalsIgnoreCase("Chestplate")){
    					p.getInventory().setChestplate(is);
    				} else if(ar.equalsIgnoreCase("Leggings")){
    					p.getInventory().setLeggings(is);
    				} else if(ar.equalsIgnoreCase("Boots")){
    					p.getInventory().setBoots(is);
    				}
    			}

    			for(String im : kits.getConfigurationSection("Kits." + s + ".Items").getKeys(false)){
    				ItemStack is = new ItemStack(kits.getInt("Kits." + s + ".Items." + im + ".ID"),kits.getInt("Kits."+ s + ".Items." + im + ".Amount"));
    				if(kits.getConfigurationSection("Kits." + s + ".Items." + im ).getKeys(false).contains("Enchant")){
    					for(String en : kits.getConfigurationSection("Kits." + s + ".Items." + im + ".Enchant").getKeys(false)){
    						is.addEnchantment(Enchantment.getById(Integer.valueOf(en)), kits.getInt("Kits." + s + ".Items." + im + ".Enchant." + en));
    					}
    				}
    				String[] slot = im.split(" ");
    				p.getInventory().setItem(Integer.valueOf(slot[1])-1, is);
    			}

    			onekitperlife.put(p, true);
    			p.updateInventory();
    		}
    	}
    }

}
