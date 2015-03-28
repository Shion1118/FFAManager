package com.shion1118.ffamanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Event implements Listener {

	Scoreboard scoreboard;
	Objective obj;

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Stats.createPlayer(p.getUniqueId().toString(), p.getName());

		defaultitem(p);

		setupscoreboard(p);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		Action action = e.getAction();
		ItemStack is = e.getItem();

		if(action == Action.PHYSICAL || is == null || is.getType() == Material.AIR){
			return;
		}

		if(is.getType() == Material.COMPASS){
			p.chat("/kit");
		}
		if(is.getType() == Material.DIAMOND){
			p.chat("/rkit");
		}

	}

	@EventHandler
	public void onItemDrop (PlayerDropItemEvent e) {
		Item drop = e.getItemDrop();
		if(drop.getItemStack().getType() == Material.COMPASS || drop.getItemStack().getType() == Material.DIAMOND){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onClickItem(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();

		if(item != null && item.hasItemMeta() && e.getInventory().getName().contains("Kit Selector")){
			for(String s : Main.kits.getConfigurationSection("Kits").getKeys(false)){
				if(Main.kits.getString("Kits." + s + ".Name").equalsIgnoreCase(item.getItemMeta().getDisplayName().replace("§", "&"))){
					p.chat("/kit " + s);
					setupscoreboard(p);
				}
			}
			e.setCancelled(true);
		}

		if(item != null && item.hasItemMeta() && e.getInventory().getName().contains("Get Random Kit")){
			for(String s : Main.kits.getConfigurationSection("Kits").getKeys(false)){
				if(Main.kits.getString("Kits." + s + ".Name").equalsIgnoreCase(item.getItemMeta().getDisplayName().replace("§", "&"))){
					Main.Kit(p, s);
					setupscoreboard(p);
				}
			}
			e.setCancelled(true);
		}

		if(p.getGameMode().equals(GameMode.SURVIVAL)){
			if(item != null && item.getType() == Material.DIAMOND){
				e.setCancelled(true);
			}
			if (item != null && item.getType() == Material.COMPASS){
				e.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent e){
		Player p = e.getPlayer();
		if(p.getGameMode().equals(GameMode.SURVIVAL)){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjHit(ProjectileHitEvent e){

		if(e.getEntity() instanceof Arrow){
			Arrow arrow = (Arrow) e.getEntity();
			if(arrow.getShooter() instanceof Player){
				arrow.getShooter();
				arrow.remove();
			}
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void Kill(PlayerDeathEvent e){
		Player p = (Player) e.getEntity();
		e.getDrops().clear();
		EconomyResponse er;

		e.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));

		if (p.getKiller() instanceof Player) {
			p.playSound(p.getLocation(), Sound.GHAST_SCREAM, 1, 1);
			p.getKiller().playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

			if(Main.KillMessage.contains("<target>")) {
				Main.KillMessage = Main.KillMessage.replace("<target>", p.getName());
			}
			if(Main.DeathMessage.contains("<killer>")) {
				Main.DeathMessage = Main.DeathMessage.replace("<killer>", p.getKiller().getName());
			}
			p.sendMessage(Main.DeathMessage);
			p.getKiller().sendMessage(Main.KillMessage);
			Main.KillMessage = Main.KillMessage2;
			Main.DeathMessage = Main.DeathMessage2;

			Stats.addDeaths(p.getUniqueId().toString(), p.getName(), 1);
			Stats.addKills(p.getKiller().getUniqueId().toString(), p.getKiller().getName(), 1);

			er = Main.eco.depositPlayer(p.getKiller().getName(), Main.KillMoney);
			er = Main.eco.withdrawPlayer(p.getName(), Main.DeathMoney);

			setupscoreboard(p.getKiller());
		} else {
			Stats.addDeaths(p.getUniqueId().toString(), p.getName(), 1);
		}

		Main.onekitperlife.put(p, false);
		setupscoreboard(p);
	}

	@EventHandler
	public void onPickGapple(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		ItemStack is = e.getItem().getItemStack();
		if(is.getType().equals(Material.GOLDEN_APPLE)) {
			e.setCancelled(true);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
			e.getItem().remove();
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent e){
		e.setCancelled(true);
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e){
		Player p = e.getPlayer();
		defaultitem(p);
	}

	public void defaultitem(Player p){
		List<String> lore = new ArrayList<String>();

		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		ItemStack kitselector = new ItemStack(Material.COMPASS);
		ItemStack randomkit = new ItemStack(Material.DIAMOND);
		ItemMeta m = kitselector.getItemMeta();
		m.setDisplayName("§aKit Selector");
		kitselector.setItemMeta(m);
		m = randomkit.getItemMeta();
		m.setDisplayName("§bGet Random Kit");
		lore.add("Cost: 100");
		m.setLore(lore);
		randomkit.setItemMeta(m);
		p.getInventory().setItem(4,kitselector);
		p.getInventory().setItem(8,randomkit);
	}

	public void setupscoreboard(Player p){
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = scoreboard.registerNewObjective("dash", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName("§dFFA");

		int skill = Stats.getKills2(p.getName());
		int sdeath = Stats.getDeaths2(p.getName());
		double skd = (double)skill/(double)sdeath;
		int srank = Stats.getRank2(p.getName());
		double smoney = Main.eco.getBalance(p.getName());
		Score moneyTitle = obj.getScore("Money >>");
		moneyTitle.setScore(10);
		Score money = obj.getScore(String.format("%.1f", smoney));
		money.setScore(9);
		Score killsTitle = obj.getScore("Kills >>");
		killsTitle.setScore(8);
		Score kills = obj.getScore(String.valueOf(skill));
		kills.setScore(7);
		Score deathsTitle = obj.getScore("Deaths >>");
		deathsTitle.setScore(6);
		Score deaths = obj.getScore(String.valueOf(sdeath));
		deaths.setScore(5);
		Score kdTitle = obj.getScore("K/D >>");
		kdTitle.setScore(4);
		Score kd = obj.getScore(String.format("%.2f", skd));
		kd.setScore(3);
		Score rankTitle = obj.getScore("Rank >>");
		rankTitle.setScore(2);
		Score rank = obj.getScore(String.valueOf(srank));
		rank.setScore(1);

		p.setScoreboard(scoreboard);
	}

}
