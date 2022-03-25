package com.playmonumenta.plugins.graves;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.graves.GraveItem;
import com.playmonumenta.plugins.graves.GraveManager;
import com.playmonumenta.plugins.itemstats.ItemStatManager;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.ItemUtils;
import de.tr7zw.nbtapi.NBTEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;



public class DeathSort {
	private static final String DEATH_SORT_TAG = "DeathSortTag";

	//Makes the item seem like it was picked up by the player animation wise, and then deletes it
	private static void simulatePickup(Item item, Player player) {

		//Prevent the item from being picked up normally
		item.setCanMobPickup(false);
		item.setCanPlayerPickup(false);

		//Make the item despawn in the next tick
		NBTEntity nbte = new NBTEntity(item);
		nbte.setShort("Age", (short) 11999);

		//Play pickup animation and sound
		player.playPickupItemAnimation(item);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25f, (float) FastUtils.randomDoubleInRange(1, 2));
	}

	public static int pickupItem(Player player, Item entity, GraveItem graveItem) {
		PlayerInventory inventory = player.getInventory();
		ItemStack item = entity.getItemStack();

		//Attempt to place this in an empty inventory slot where it previously was
		if (player.getScoreboardTags().contains(DEATH_SORT_TAG)
			    && graveItem.mSlot != null
			    && insertIntoSlot(player, entity, graveItem.mSlot)) {
			return 0; //Nothing left to do, this item is handled
		}

		//Attempt to place this in an empty inventory slot that was empty before death
		if (pickupIntoEmptySlot(player, entity, false)) {
			return 0; //Nothing left to do, this item is handled
		}

		//Attempt to place this in any empty slot
		int slot = inventory.firstEmpty();
		if (slot != -1) {
			inventory.setItem(slot, item);
			simulatePickup(entity, player);
			return 0;
		}

		// Couldn't place the item, return how many are left
		return item.getAmount();
	}

	/**
	 * Attempts to put the given grave item into the given slot if the slot is empty. Will not attempt to fill an existing stack in the slot.
	 *
	 * @return Whether teh item was put into the slot
	 */
	private static boolean insertIntoSlot(Player player, Item entity, int slot) {
		PlayerInventory inventory = player.getInventory();
		ItemStack invItem = inventory.getItem(slot);
		if (invItem != null && !invItem.getType().isAir()) {
			return false;
		}
		ItemStack item = entity.getItemStack();
		//Put the item back in this slot, unless it was an armor slot and the item was curse of binding
		if (slot < 36 || (!item.containsEnchantment(Enchantment.BINDING_CURSE) && !ItemUtils.isShatteredWearable(item))) {
			if (ItemUtils.isArmor(item) && slot >= 36 && slot <= 39) {
				player.getWorld().playSound(player.getLocation(), ItemUtils.getArmorEquipSound(item.getType()), 0.75f, 1);
			}

			//Simulate item pickup
			inventory.setItem(slot, item);
			InventoryUtils.scheduleDelayedEquipmentSlotCheck(Plugin.getInstance(), player, slot);
			ItemStatManager.PlayerItemStats playerItemStats = Plugin.getInstance().mItemStatManager.getPlayerItemStats(player);
			if (playerItemStats != null) {
				playerItemStats.updateStats(player, true);
			}
			simulatePickup(entity, player);

			return true;
		}
		return false;
	}

	/**
	 * Attempts to pick up the given item inta an empty inventory slot that has been empty before any recent deaths if death sort is enabled.
	 * Or more accurately, into a slot not currently "reserved" for items in recent graves.
	 * Will attempt to stack into existing slots first if there are recent graves or if doNothingIfNoRecentGraves is false.
	 *
	 * @return Whether the item was picked up completely
	 */
	private static boolean pickupIntoEmptySlot(Player player, Item entity, boolean doNothingIfNoRecentGraves) {
		PlayerInventory inventory = player.getInventory();

		Set<Integer> reservedItemSlots = null;
		if (player.getScoreboardTags().contains(DEATH_SORT_TAG)) {
			GraveManager graveManager = GraveManager.getInstance(player);
			if (graveManager != null) {
				Instant considerGravesAfter = Instant.now().minus(30, ChronoUnit.MINUTES);
				reservedItemSlots = graveManager.getGraves().stream()
					.filter(grave -> grave.getDeathTime().isAfter(considerGravesAfter))
					.flatMap(grave -> grave.getItems().stream())
					.map(i -> i.mSlot)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			}
		}

		if (doNothingIfNoRecentGraves && (reservedItemSlots == null || reservedItemSlots.isEmpty())) {
			return false;
		}

		// Combine item with the same item if it can be stacked into it
		ItemStack item = entity.getItemStack();
		for (int slot = 0; slot < 36; slot++) {
			ItemStack invItem = inventory.getItem(slot);
			if (invItem != null && !invItem.getType().isAir() && invItem.getAmount() < invItem.getMaxStackSize() && invItem.isSimilar(item)) {
				if (invItem.getAmount() + item.getAmount() <= invItem.getMaxStackSize()) {
					// Full amount can be combined
					invItem.add(item.getAmount());
					simulatePickup(entity, player);
					return true;
				} else {
					// Partial amount can be combined
					item.setAmount(invItem.getAmount() + item.getAmount() - invItem.getMaxStackSize());
					invItem.setAmount(invItem.getMaxStackSize());
					entity.setItemStack(item);
				}
			}
		}

		// Attempt to place this in an empty inventory slot that was empty before recent deaths
		if (reservedItemSlots != null && !reservedItemSlots.isEmpty()) {
			for (int slot = 0; slot < 36; slot++) {
				ItemStack invItem = inventory.getItem(slot);
				if (!reservedItemSlots.contains(slot) && (invItem == null || invItem.getType().isAir())) {
					//Found an empty slot that was empty before death

					//Simulate item pickup
					inventory.setItem(slot, entity.getItemStack());
					InventoryUtils.scheduleDelayedEquipmentSlotCheck(Plugin.getInstance(), player, slot);
					simulatePickup(entity, player);

					return true;
				}
			}
		}

		return false;
	}

}