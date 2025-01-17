package com.playmonumenta.plugins.depths;

import com.playmonumenta.plugins.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum DepthsTree {
	DAWNBRINGER("Dawnbringer", DepthsUtils.DAWNBRINGER, Material.SUNFLOWER, "Bestows passive and active buffs to allies including speed, damage, resistance, and healing."),
	EARTHBOUND("Earthbound", DepthsUtils.EARTHBOUND, Material.LEATHER_CHESTPLATE, "Resolute tank with capabilities of taking aggro and granting resistance to self, armed with minor crowd control."),
	FLAMECALLER("Flamecaller", DepthsUtils.FLAMECALLER, Material.FIRE_CHARGE, "Caster of strong burst AOE abilities and potent damage over time."),
	FROSTBORN("Frostborn", DepthsUtils.FROSTBORN, Material.ICE, "Manipulates the flow of combat by debuffing enemies with ice generating abilities and high damage potential."),
	SHADOWDANCER("Shadowdancer", DepthsUtils.SHADOWDANCER, Material.IRON_SWORD, "Skilled in single target melee damage, especially against bosses and elites."),
	STEELSAGE("Steelsage", DepthsUtils.STEELSAGE, Material.CROSSBOW, "Master of ranged abilities with dual AOE and single target damage capabilities."),
	WINDWALKER("Windwalker", DepthsUtils.WINDWALKER, Material.FEATHER, "An arsenal of movement abilities and crowd control, allowing precise maneuvers and quick escapes.");

	private final String mDisplayName;
	private final TextColor mColor;
	private final Material mMaterial;
	private final String mDescription;

	DepthsTree(String displayName, int color, Material material, String description) {
		mDisplayName = displayName;
		mColor = TextColor.color(color);
		mMaterial = material;
		mDescription = description;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public TextColor getColor() {
		return mColor;
	}

	public Component getNameComponent() {
		return Component.text(mDisplayName, mColor).decoration(TextDecoration.ITALIC, false);
	}

	public ItemStack createItem() {
		return GUIUtils.createBasicItem(mMaterial, 1, getNameComponent(), mDescription, NamedTextColor.GRAY, 30, true);
	}

}
