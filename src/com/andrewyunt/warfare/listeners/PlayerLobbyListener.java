package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.lobby.SignException;
import com.andrewyunt.warfare.menu.ShopMenu;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.lobby.SignDisplay;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerLobbyListener extends PlayerListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.setJoinMessage(null);

        Player player = event.getPlayer();

        // Send welcome message
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Welcome to " + ChatColor.GOLD + ChatColor.BOLD.toString() + "Warfare");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Teamspeak: " + ChatColor.GRAY + "ts.faithfulmc.com");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Website: " + ChatColor.GRAY + "www.faithfulmc.com");
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
            gp.updateHotbar();
            player.teleport(player.getLocation().getWorld().getSpawnLocation());
        }, 2L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();
        Warfare.getInstance().getPlayerManager().deletePlayer(Warfare.getInstance().getPlayerManager().getPlayer(player));
    }

    protected boolean handleHotbarClick(Player player, String itemName) {
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE))) {
            Warfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_KIT_SELECTOR_TITLE))) {
            Warfare.getInstance().getKitSelectorMenu().open(gp);
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE))) {
            Warfare.getInstance().getPlayMenu().open(gp);
            return true;
        }

        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteractHigh(PlayerInteractEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockDamageEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getY() < 0) {
            player.teleport(player.getLocation().getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        if (event.getLine(0) == null || event.getLine(1) == null || event.getLine(2) == null) {
            return;
        }

        if (!event.getLine(0).equalsIgnoreCase("[Leaderboard]")) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("warfare.sign.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create a leaderboard sign.");
            return;
        }

        SignDisplay.Type type = null;

        if (event.getLine(1).equalsIgnoreCase("kills")) {
            type = SignDisplay.Type.KILLS_LEADERBOARD;
        } else if (event.getLine(1).equalsIgnoreCase("wins")) {
            type = SignDisplay.Type.WINS_LEADERBOARD;
        } else {
            return;
        }

        int place;

        try {
            place = Integer.valueOf(event.getLine(2));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
            return;
        }

        if (place > 5) {
            player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
            return;
        }

        try {
            Warfare.getInstance().getSignManager().createSign(
                    event.getBlock().getLocation(),
                    type,
                    place,
                    false);
        } catch (SignException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}