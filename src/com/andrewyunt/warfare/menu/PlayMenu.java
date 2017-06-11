package com.andrewyunt.warfare.menu;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Party;
import com.andrewyunt.warfare.lobby.Server;
import com.andrewyunt.warfare.utilities.Utils;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PlayMenu implements Listener, InventoryHolder {

    @Getter private final Inventory inventory;

    private final int SIZE = 5 * 9;
    private final int QUICK_JOIN_SOLO_SLOT = 39;
    private final int QUICK_JOIN_TEAMS_SLOT = 41;
    private final ItemStack QUICK_JOIN_SOLO_ITEM = new ItemBuilder(Material.IRON_SWORD).displayName(ChatColor.GOLD + "Quick Join Solo").lore(ChatColor.GRAY + "Click to join a solo game").build();
    private final ItemStack QUICK_JOIN_TEAMS_ITEM = new ItemBuilder(Material.IRON_SWORD).displayName(ChatColor.GOLD + "Quick Join Teams").lore(ChatColor.GRAY + "Click to join a teams game").build();
    private final ItemStack PANE = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).displayName(" ").build();

    private Map<Integer, Server> serversBySlot = new HashMap<>();
    private List<Server> inventoryServers = new ArrayList<>();
    private List<Server> quickJoinServers = new ArrayList<>();

    public PlayMenu() {
        inventory = Bukkit.createInventory(this, SIZE, ChatColor.GOLD + ChatColor.BOLD.toString() + "Game Menu");
        Bukkit.getScheduler().runTaskTimerAsynchronously(Warfare.getInstance(), () -> {
            List<Server> serverList = Warfare.getInstance().getStorageManager().getServers();
            inventoryServers = new ArrayList<>(serverList).stream().filter(server -> server.getMaxPlayers() > 0 && server.getGameStage() == Game.Stage.WAITING || server.getGameStage() == Game.Stage.COUNTDOWN).collect(Collectors.toList());
            inventoryServers.sort(Comparator.comparingInt(server -> (server.getGameStage().getOrder() * 1000) - server.getOnlinePlayers()));
            quickJoinServers = new ArrayList<>(serverList).stream().filter(server -> server.getMaxPlayers() > 0 &&  server.getGameStage() == Game.Stage.COUNTDOWN || server.getGameStage() == Game.Stage.WAITING)
                    .collect(Collectors.toList());
            quickJoinServers.sort(Comparator.comparingInt(server -> (server.getGameStage().ordinal() * 1000) - server.getOnlinePlayers()));
            Bukkit.getScheduler().runTask(Warfare.getInstance(), () -> inventory.setContents(getContents()));
        }, 0, 2);
    }

    public ItemStack[] getContents() {
        ItemStack[] itemStacks = new ItemStack[SIZE];
        /*
        for (int i = 0; i < 9; i++) {
            itemStacks[i] = PANE.clone();
        }
        for (int i = 9; i < 45; i = i + 9) {
            itemStacks[i] = PANE.clone();
            itemStacks[i + 8] = PANE.clone();
        }
        for (int i = 45; i < 54; i++) {
            itemStacks[i] = PANE.clone();
        }
        */


        serversBySlot.clear();

        int i = 0;
        for (Server server: inventoryServers) {
            ItemStack itemStack = createServerItem(server);
            if(itemStack != null){
                itemStacks[i] = itemStack;
                serversBySlot.put(i, server);
                i++;
            }
        }

        itemStacks[QUICK_JOIN_SOLO_SLOT] = QUICK_JOIN_SOLO_ITEM.clone();
        itemStacks[QUICK_JOIN_TEAMS_SLOT] = QUICK_JOIN_TEAMS_ITEM.clone();

        return itemStacks;
    }

    public ItemStack createServerItem(Server server) {
        Server.ServerType serverType = server.getServerType();
        if ((serverType == Server.ServerType.TEAMS || serverType == Server.ServerType.SOLO) && server.getGameStage().ordinal() < Game.Stage.END.ordinal()) {
            return new ItemBuilder(Material.STAINED_GLASS_PANE, 1, server.getGameStage().getDyeColor().getData())
                    .displayName(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 30))
                    .lore(
                            ChatColor.GOLD + "  Server: " + ChatColor.GRAY + server.getName(),
                            ChatColor.GOLD + "  Players: " + ChatColor.WHITE + " (" + server.getOnlinePlayers() + "/" + server.getMaxPlayers() + ")",
                            ChatColor.GOLD + "  Server Type: " + ChatColor.GRAY + " " + (serverType == Server.ServerType.TEAMS ? "Teams" : "Solo"),
                            ChatColor.GOLD + "  Map Name: " + ChatColor.GRAY + " " + server.getMapName(),
                            ChatColor.GOLD + "  Stage: " + ChatColor.GRAY + server.getGameStage().getDisplay(),
                            ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 30)
                    )
                    .build();
        }
        return null;
    }

    public void open(GamePlayer player) {
        player.getBukkitPlayer().openInventory(getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv != null && inv.getHolder() == this) {
            event.setCancelled(true);

            int slot = event.getSlot();

            Player player = (Player) event.getWhoClicked();

            PlayersEntity playerEntity;
            Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());
            if (party == null) {
                playerEntity = new SinglePlayerEntity(player.getUniqueId());
            } else {
                if (Objects.equals(party.getLeader(), player.getUniqueId())) {
                    playerEntity = new PartyPlayerEntity(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "You must be the party leader to do this");
                    Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                    return;
                }
            }

            if (slot == QUICK_JOIN_SOLO_SLOT) {
                Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                for (Server server : quickJoinServers) {
                    if (server.getServerType() != Server.ServerType.SOLO) {
                        continue;
                    }
                    int size = playerEntity.size();
                    int amount = size == 1 ? 1 : size + 2;
                    if (server.getOnlinePlayers() + amount <= server.getMaxPlayers()) {
                        playerEntity.sendToServer(server.getName());
                        if (playerEntity instanceof PartyPlayerEntity) {
                            Warfare.getInstance().getStorageManager().setPartyServer(party, server.getName());
                        }
                        server.setOnlinePlayers(server.getOnlinePlayers() + size);
                        return;
                    }
                }
                player.sendMessage(ChatColor.RED + "There are currently no available solo games");
            } else if (slot == QUICK_JOIN_TEAMS_SLOT) {
                    Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                    for (Server server: quickJoinServers) {
                        if (server.getServerType() != Server.ServerType.TEAMS) {
                            continue;
                        }
                        int size = playerEntity.size();
                        int amount = size == 1 ? 1 : size + 2;
                        if (server.getOnlinePlayers() + amount <= server.getMaxPlayers()) {
                            playerEntity.sendToServer(server.getName());
                            if (playerEntity instanceof PartyPlayerEntity) {
                                Warfare.getInstance().getStorageManager().setPartyServer(party, server.getName());
                            }
                            server.setOnlinePlayers(server.getOnlinePlayers() + size);
                            return;
                        }
                    }
                    player.sendMessage(ChatColor.RED + "There are currently no available team games");
            } else {
                Server server = serversBySlot.get(slot);
                if (server != null) {
                    playerEntity.sendToServer(server.getName());
                    if (playerEntity instanceof PartyPlayerEntity) {
                        Warfare.getInstance().getStorageManager().setPartyServer(party, server.getName());
                    }
                    Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                }
            }
        }
    }

    public abstract class PlayersEntity {
        protected UUID player;

        public PlayersEntity(UUID player) {
            this.player = player;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(player);
        }

        public abstract void sendToServer(String servername);
        public abstract int size();
    }

    public class SinglePlayerEntity extends PlayersEntity {
        public SinglePlayerEntity(UUID player) {
            super(player);
        }

        public int size() {
            return 1;
        }

        public void sendToServer(String servername) {
            Utils.sendPlayerToServer(Bukkit.getPlayer(player), servername);
        }
    }

    public class PartyPlayerEntity extends PlayersEntity{
        public PartyPlayerEntity(UUID partyLeader) {
            super(partyLeader);
        }

        public boolean hasFailed() {
            return Bukkit.getPlayer(player) == null || Warfare.getInstance().getPartyManager().getParty(player) == null;
        }

        public int size() {
            return Warfare.getInstance().getPartyManager().getParty(player).getMembers().size();
        }

        public void sendToServer(String servername) {
            Utils.sendPartyToServer(Bukkit.getPlayer(player), Warfare.getInstance().getPartyManager().getParty(player), servername);
        }
    }
}