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
package com.andrewyunt.warfare.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Skill;

/**
 * The listener class used for skills which holds methods to listen on events.
 * 
 * @author Andrew Yunt
 */
public class PlayerSkillListener implements Listener {

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
			return;

		Player damaged = (Player) event.getEntity();
		GamePlayer damagedGP = null;

		try {
			damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (damagedGP.getSelectedSkill() != Skill.RESISTANCE)
			return;

		if (Math.random() > 0.20D)
			return;
		
		damaged.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 1));
	}
	
	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;

		GamePlayer killedGP = null;

		try {
			killedGP = Warfare.getInstance().getPlayerManager().getPlayer(event.getEntity());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		GamePlayer lastDamagerGP = killedGP.getLastDamager();
		
		if (lastDamagerGP == null)
			return;
		
		Player lastDamager = lastDamagerGP.getBukkitPlayer();
		
		if (lastDamagerGP.getSelectedSkill() == Skill.JUGGERNAUT) {
			
			lastDamager.setMaxHealth(lastDamager.getMaxHealth() + 2);
		}
		
		if (lastDamagerGP.getSelectedSkill() == Skill.CONSUMPTION)
			lastDamager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 2));
	}
	
	@EventHandler
	private void onInventoryOpen(InventoryOpenEvent event) {
		
		if (event.getInventory().getType() != InventoryType.CHEST)
			return;
		
		Player player = (Player) event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.getSelectedSkill() == Skill.GUARD)
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
	}
	
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent event) {
		
		Player player = (Player) event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (event.getInventory().getType() == InventoryType.ENCHANTING)
			gp.setEnergy(gp.getEnergy());
		else if (event.getInventory().getType() == InventoryType.CHEST)
			if (gp.getSelectedSkill() == Skill.GUARD)
				player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
	}
	
	@EventHandler
	private void onProjectileLaunch(ProjectileLaunchEvent event) {
		
		Projectile projectile = event.getEntity();
		
		if (projectile.getType() != EntityType.ARROW)
			return;
		
		ProjectileSource ps = projectile.getShooter();
		
		if (!(ps instanceof Player))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer((Player) ps);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.getSelectedSkill() != Skill.FLAME)
			return;
		
		if (Math.random() <= 0.10D)
			projectile.setFireTicks(Integer.MAX_VALUE);
	}
}