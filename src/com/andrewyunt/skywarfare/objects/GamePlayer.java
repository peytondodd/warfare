/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.andrewyunt.skywarfare.objects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import com.andrewyunt.skywarfare.SkyWarfare;

public class GamePlayer {
	
	private UUID uuid;
	private CustomClass customClass;
	private int coins, earnedCoins, wins, energy;
	private boolean cooldown, hasSpeed, loaded, spectating;
	private Set<Purchasable> purchases = new HashSet<Purchasable>();
	private Set<CustomClass> customClasses = new HashSet<CustomClass>();
	private DynamicScoreboard dynamicScoreboard;
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;
		
		// Set up scoreboard
		dynamicScoreboard = new DynamicScoreboard(ChatColor.YELLOW + "" + ChatColor.BOLD + "MEGATW");
		getBukkitPlayer().setScoreboard(dynamicScoreboard.getScoreboard());
		
		// Register health objective
		Objective healthObjective = dynamicScoreboard.getScoreboard().registerNewObjective(ChatColor.RED + "❤", "health");
		healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
	}
	
	public UUID getUUID() {
		
		return uuid;
	}
	
	public Player getBukkitPlayer() {
		
		return Bukkit.getServer().getPlayer(uuid);
	}
	
	public void setCustomClass(CustomClass customClass) {
		
		this.customClass = customClass;
	}
	
	public CustomClass getCustomClass() {
		
		return customClass;
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setEarnedCoins(int earnedCoins) {
		
		this.earnedCoins = earnedCoins;
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setWins(int wins) {
		
		this.wins = wins;
	}
	
	public int getWins() {
		
		return wins;
	}
	
	public void setCooldown(boolean cooldown) {
		
		this.cooldown = cooldown;
	}
	
	public boolean isCooldown() {
		
		return cooldown;
	}
	
	public void setHasSpeed(boolean hasSpeed) {
		
		this.hasSpeed = hasSpeed;
	}
	
	public boolean hasSpeed() {
		
		return hasSpeed;
	}
	
	public void addEnergy(int energy) {
		
		this.energy = this.energy + energy;
	}
	
	public int getEnergy() {
		
		return energy;
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public boolean isInGame() {
		
		return SkyWarfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public GamePlayer getLastDamager() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setSpectating(boolean spectating) {
		
		this.spectating = spectating;
		
		if (spectating) {
			Player player = getBukkitPlayer();
			
			player.setAllowFlight(true);
			player.setFireTicks(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15), true);
			
			updateDynamicScoreboard();
			
			Location loc = SkyWarfare.getInstance().getArena().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded())
				chunk.load();
			
			loc.setY(loc.getY() + 1);
			
			player.teleport(loc, TeleportCause.COMMAND);
		}
	}
	
	public boolean isSpectating() {
		
		return spectating;
	}
	
	public Set<Purchasable> getPurchases() {
		
		return purchases;
	}
	
	public boolean hasPurchased(Purchasable purchasable) {
		
		for (Purchasable purchase : purchases)
			if (purchase.toString().equals(purchasable.toString()))
				return true;
		
		return false;
	}
	
	public Set<CustomClass> getCustomClasses() {
		
		return customClasses;
	}
	
	public CustomClass getCustomClass(String name) {
		
		for (CustomClass customClass : customClasses)
			if (customClass.getName().equalsIgnoreCase(name))
				return customClass;
		
		return null;
	}
	
	public DynamicScoreboard getDynamicScoreboard() {
		
		return dynamicScoreboard;
	}
	
	public void updateDynamicScoreboard() {
		// TODO Auto-generated method stub
		
	}
	
	public void updateHotbar() {
		
		Player bp = getBukkitPlayer();
		
		ItemStack shop = new ItemStack(Material.EMERALD, 1);
		ItemMeta shopMeta = shop.getItemMeta();
		shopMeta.setDisplayName(ChatColor.GREEN + "Shop");
		shop.setItemMeta(shopMeta);
		bp.getInventory().setItem(0, shop);
	}
}