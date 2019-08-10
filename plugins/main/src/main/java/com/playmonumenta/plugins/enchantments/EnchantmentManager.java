package com.playmonumenta.plugins.enchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.enchantments.evasions.AbilityEvasion;
import com.playmonumenta.plugins.enchantments.evasions.Evasion;
import com.playmonumenta.plugins.enchantments.evasions.MeleeEvasion;
import com.playmonumenta.plugins.enchantments.evasions.SecondWind;
import com.playmonumenta.plugins.utils.ItemUtils;

public class EnchantmentManager {
	/* NOTE:
	 *
	 * It is important that the specified slots do not overlap. That means you can have an item property
	 * that works in MAINHAND and ARMOR, but not one that has both MAINHAND and INVENTORY because that item
	 * will be checked twice.
	 */
	public enum ItemSlot {
		MAINHAND,
		OFFHAND,
		HAND,      // Includes both offhand and mainhand
		ARMOR,     // Does NOT include offhand!
		INVENTORY, // Includes everything, including armor, offhand, and hotbar
	}

	/*
	 * Keep a map of which properties apply to which slots
	 * This can dramatically reduce the number of checks done when looking through player inventories.
	 * Most items only apply to armor or mainhand, and we don't need to check every single item in the
	 * player's inventory * against these properties every time the player's inventory changes
	 */
	private final Map<ItemSlot, List<BaseEnchantment>> mProperties = new EnumMap<ItemSlot, List<BaseEnchantment>>(ItemSlot.class);
	private final List<BaseEnchantment> mSpawnedProperties = new ArrayList<BaseEnchantment>();

	public void load(Collection<String> forbiddenItemLore) {
		mProperties.clear();
		mSpawnedProperties.clear();

		final List<BaseEnchantment> init = new ArrayList<BaseEnchantment>();

		// Passive enchantments
		init.add(new Regeneration());
		init.add(new Darksight());
		init.add(new Radiant());
		init.add(new Gills());
		init.add(new Gilded());
		init.add(new Festive());
		init.add(new Clucking());
		init.add(new Oinking());
		init.add(new Stylish());
		init.add(new Colorful());
		init.add(new Hope());
		init.add(new Frost());
		init.add(new Intuition());
		init.add(new LifeDrain());
		init.add(new Evasion());
		init.add(new MeleeEvasion());
		init.add(new AbilityEvasion());
		init.add(new Resurrection());
		init.add(new DivineAura());
		init.add(new SecondWind());

		// Active enchantments
		init.add(new Chaotic());
		init.add(new IceAspect());
		init.add(new Slayer());
		init.add(new Duelist());
		init.add(new Thunder());
		init.add(new Inferno());
		init.add(new Impact());
		init.add(new Sniper());
		init.add(new PointBlank());
		init.add(new Decay());
		init.add(new Sapper());
		init.add(new Multitool());
		init.add(new HexEater());
		init.add(new ThrowingKnife());
		init.add(new Current());
		init.add(new InstantDrink());
		init.add(new JunglesNourishment());
		init.add(new Adrenaline());

		// Curses
		init.add(new CurseOfCorruption());

		// Forbidden items (dynamically set based on server config)
		if (forbiddenItemLore != null && !forbiddenItemLore.isEmpty()) {
			for (String str : forbiddenItemLore) {
				init.add(new ForbiddenItem(str));
			}
		}

		/* Build the map of which slots have which properties */
		for (BaseEnchantment property : init) {
			for (ItemSlot slot : property.validSlots()) {
				List<BaseEnchantment> slotList = mProperties.get(slot);
				if (slotList == null) {
					slotList = new ArrayList<BaseEnchantment>();
				}
				slotList.add(property);
				mProperties.put(slot, slotList);
			}

			if (property.hasOnSpawn()) {
				mSpawnedProperties.add(property);
			}
		}
	}

	private static ItemStack[] _getItems(PlayerInventory inv, ItemSlot slot) {
		switch (slot) {
		case MAINHAND:
			return new ItemStack[] {inv.getItemInMainHand()};
		case OFFHAND:
			return new ItemStack[] {inv.getItemInOffHand()};
		case ARMOR:
			return inv.getArmorContents();
		case INVENTORY:
			return inv.getContents();
		case HAND:
			return new ItemStack[] {inv.getItemInMainHand(), inv.getItemInOffHand()};
		}

		return null;
	}

	/*
	 * Build a map containing all of the ItemProperty's that apply to a player and the
	 * cumulative level of each property
	 *
	 * propertyMap is passed in by reference and updated by this function
	 *
	 * NOTE: propertyMap should already be clear'ed and the properties removed from the player
	 */
	public void getItemProperties(Map<BaseEnchantment, Integer>propertyMap, Player player) {
		final PlayerInventory inv = player.getInventory();

		/* Step over the slots for which we have item properties */
		for (Map.Entry<ItemSlot, List<BaseEnchantment>> entry : mProperties.entrySet()) {
			ItemSlot slot = entry.getKey();
			List<BaseEnchantment> slotProperties = entry.getValue();

			if (slotProperties != null && !(slotProperties.isEmpty())) {
				/* Step over the item(s) the player has in that slot */
				for (ItemStack item : _getItems(inv, slot)) {
					if (item != null) {
						/* Step over the properties that apply to that slot */
						for (BaseEnchantment property : slotProperties) {
							if ((slot == ItemSlot.OFFHAND || slot == ItemSlot.MAINHAND) && ItemUtils.isWearable(item.getType())) {
								// Prevents armor items being held in mainhand / offhand counting towards enchantment level
								continue;
							}

							/*
							 * If this particular property applies levels,
							 * add them to the running count UNLESS it is an
							 * evasion or second wind armor piece in hand
							 */
							int level = property.getLevelFromItem(item, player, slot);
							if (level > 0) {
								Integer currentLevel = propertyMap.get(property);
								if (currentLevel != null) {
									currentLevel += level;
								} else {
									currentLevel = level;
								}
								propertyMap.put(property, currentLevel);
							}
						}
					}
				}
			}
		}
	}

	/*
	 * Check if the newly spawned item entity matches any of the spawned item properties
	 */
	public void ItemSpawnEvent(Plugin plugin, Item item) {
		if (item != null) {
			ItemStack stack = item.getItemStack();
			if (stack != null) {
				for (BaseEnchantment property : mSpawnedProperties) {
					int level = property.getLevelFromItem(stack);
					if (level > 0) {
						property.onSpawn(plugin, item, level);
					}
				}
			}
		}
	}
}
