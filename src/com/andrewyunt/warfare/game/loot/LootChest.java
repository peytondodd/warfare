package com.andrewyunt.warfare.game.loot;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.andrewyunt.warfare.Warfare;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootChest {

	@Getter private Location location;
	@Getter private LootTier tier;
	@Getter private Island island;

	public LootChest(Location location, byte tier, Island island) {
		this.location = location;

		if (tier == 1) {
			this.tier = new LootTier.Tier1();
		} else if (tier == 2) {
			this.tier = new LootTier.Tier2();
		} else if (tier == 3) {
			this.tier = new LootTier.Tier3();
		}

		this.island = island;

		if (island != null) {
			island.addChest(this);
		}
	}
	
	private ItemStack getRandomLootItem(ItemStack[] group) {
		List<ItemStack> groupArray = Arrays.asList(group);
		Collections.shuffle(groupArray);
		return groupArray.iterator().next();
	}
	
	public void fill() {
		Block block = location.getBlock();
		BlockState blockState = block.getState();

		if (blockState instanceof Chest) {
			Chest chest = (Chest) blockState;
			Inventory inv = chest.getBlockInventory();
			List<ItemStack> lootItems = new ArrayList<>();

			// Add a random item from guaranteed groups for each tier and give island items
			if (tier instanceof LootTier.Tier3) {
				lootItems.add(getRandomLootItem(((LootTier.Tier3) tier).getGroup1Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier3) tier).getGroup1Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier3) tier).getGroup2Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier3) tier).getGroup3Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier3) tier).getGroup5Items()));

				for (LootType type : island.getChestItems().get(this)) {
					lootItems.add(((LootTier.Tier3) tier).getItem(type));
				}
			} else if (tier instanceof LootTier.Tier2) {
				lootItems.add(getRandomLootItem(((LootTier.Tier2) tier).getGroup1Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier2) tier).getGroup2Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier2) tier).getGroup3Items()));

				for (LootType type : island.getChestItems().get(this)) {
					lootItems.add(((LootTier.Tier2) tier).getItem(type));
				}
			} else if (tier instanceof LootTier.Tier1) {
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup1Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup2Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup3Items()));
			}

			// Give players items from two randomly chosen groups in the chest tier
			for (int i = 1; i < 3; i++) {
				List<ItemStack[]> groupArray = new ArrayList<>();

				if (tier instanceof LootTier.Tier1) {
					groupArray.add(((LootTier.Tier1) tier).getGroup1Items());
					groupArray.add(((LootTier.Tier1) tier).getGroup2Items());
					groupArray.add(((LootTier.Tier1) tier).getGroup3Items());
					groupArray.add(((LootTier.Tier1) tier).getGroup4Items());
					groupArray.add(((LootTier.Tier1) tier).getGroup5Items());
				} else if (tier instanceof LootTier.Tier2) {
					groupArray.add(((LootTier.Tier2) tier).getGroup1Items());
					groupArray.add(((LootTier.Tier2) tier).getGroup2Items());
					groupArray.add(((LootTier.Tier2) tier).getGroup3Items());
				} else if (tier instanceof LootTier.Tier3) {
					groupArray.add(((LootTier.Tier3) tier).getGroup1Items());
					groupArray.add(((LootTier.Tier3) tier).getGroup2Items());
					groupArray.add(((LootTier.Tier3) tier).getGroup3Items());
					groupArray.add(((LootTier.Tier3) tier).getGroup4Items());
				}

				// Shuffle the randomly chosen group
				Collections.shuffle(groupArray);

				// The array of items from the group converted to a list
				List<ItemStack> groupList = new LinkedList<>(Arrays.asList(groupArray.iterator().next()));

				// The number of items a player should receive from the randomly chosen group
				int random = -ThreadLocalRandom.current().nextInt(4 - 3 + 1) + 4;

				while (groupList.size() > random) {
					groupList.remove(groupList.size() - 1);
				}

				lootItems.addAll(groupList);
			}

			List<Integer> slots = new ArrayList<>();
			for (int i = 0; i < inv.getSize(); i ++){
			    slots.add(i);
            }
            Collections.shuffle(slots);

			// Randomize item arrangement in chest inventory
			for (ItemStack is : lootItems) {
				int randomSlot = slots.remove(0);
				inv.setItem(randomSlot, is);
			}

			// Update chest
			chest.update();
		}
	}
}