package com.playmonumenta.plugins.commands;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.ItemStatUtils;
import com.playmonumenta.plugins.utils.ItemStatUtils.Region;
import com.playmonumenta.plugins.utils.ItemUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/*
 * NOTICE!
 * If this enchantment gets changed, make sure someone updates the Python item replacement code to match!
 * Constants and new enchantments included!
 * This most likely means @NickNackGus or @Combustible
 * If this does not happen, your changes will NOT persist across weekly updates!
 */
public class ReforgeHeldItem extends GenericCommand {

	public static void register() {
		registerPlayerCommand("reforgehelditem", "monumenta.command.reforgehelditem", ReforgeHeldItem::run);
	}

	private static void run(CommandSender sender, Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		// If the player is in creative, reforge for free.
		if (player.getGameMode() == GameMode.CREATIVE) {
			if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
				CommandAPI.fail("Player must have a Shattered item in their main hand!");
			} else if (ItemStatUtils.isShattered(item)) {
				ItemStatUtils.reforge(item);
				player.sendMessage("Your item has been reforged!");
				if (sender != player) {
					sender.sendMessage("Successfully reforged the player's held item");
				}
				return;
			}
		}
		if (player.hasMetadata("PlayerCanReforge")) {
			player.removeMetadata("PlayerCanReforge", Plugin.getInstance());
			if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
				CommandAPI.fail("Player must have a Shattered item in their main hand!");
			} else if (ItemStatUtils.isShattered(item)) {
				Region region = ItemStatUtils.getRegion(item);
				int cost = ItemUtils.getReforgeCost(item);
				PlayerInventory inventory = player.getInventory();
				if (region == Region.SHULKER_BOX) {
					// ItemRegion.SHULKER_BOX currently only exists to allow shulker boxes to be
					// reforged with either King's Valley or Celsian Isles currency.
					// We need to figure out a more permanent solution to this at some point.
					if (player.getWorld().getName().equals("Project_Epic-valley")) {
						// King's Valley: Use XP
						region = Region.VALLEY;
					} else if (player.getWorld().getName().equals("Project_Epic-isles")) {
						// Celsian Isles: Use CS
						region = Region.ISLES;
					} else {
						// This shouldn't happen, but if it does, default to XP
						region = Region.VALLEY;
					}

				}
				if (region == Region.VALLEY) {
					ItemStack cxp = CalculateReforge.mCXP.clone();
					ItemStack hxp = CalculateReforge.mHXP.clone();
					if (inventory.containsAtLeast(cxp, cost)) {
						cxp.setAmount(cost);
						inventory.removeItem(cxp);
					} else if (inventory.containsAtLeast(hxp, cost / 64) && inventory.containsAtLeast(cxp, cost % 64)) {
						hxp.setAmount(cost / 64);
						cxp.setAmount(cost % 64);
						inventory.removeItem(hxp);
						inventory.removeItem(cxp);
					} else {
						if (sender != player) {
							player.sendMessage(ChatColor.RED + "You can't afford that");
							CommandAPI.fail("Player doesn't have enough currency");
						} else {
							CommandAPI.fail("You can't afford that");
						}
						return;
					}
				} else if (region == Region.ISLES) {
					ItemStack ccs = CalculateReforge.mCCS.clone();
					ItemStack hcs = CalculateReforge.mHCS.clone();
					if (inventory.containsAtLeast(ccs, cost)) {
						ccs.setAmount(cost);
						inventory.removeItem(ccs);
					} else if (inventory.containsAtLeast(hcs, cost / 64) && inventory.containsAtLeast(ccs, cost % 64)) {
						hcs.setAmount(cost / 64);
						ccs.setAmount(cost % 64);
						inventory.removeItem(hcs);
						inventory.removeItem(ccs);
					} else {
						if (sender != player) {
							player.sendMessage(ChatColor.RED + "You can't afford that");
							CommandAPI.fail("Player doesn't have enough currency");
						} else {
							CommandAPI.fail("You can't afford that");
						}
						return;
					}
				} else {
					player.sendMessage("Something went wrong");
					CommandAPI.fail("Invalid ItemRegion");
					return;
				}
				ItemStatUtils.reforge(item);
				player.sendMessage("Your item has been reforged!");
				if (sender != player) {
					sender.sendMessage("Successfully reforged the player's held item");
				}
			} else {
				if (sender != player) {
					CommandAPI.fail("Player must have a Shattered item in their main hand!");
				} else {
					CommandAPI.fail("You must have a Shattered item in your main hand!");
				}
			}
		} else {
			if (sender != player) {
				CommandAPI.fail("Player doesn't have the metadata tag to use this command");
			} else {
				CommandAPI.fail("You don't have the metadata tag to use this command");
			}
		}
	}
}
