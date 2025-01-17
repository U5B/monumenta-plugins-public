package com.playmonumenta.plugins.gallery.effects;

import com.playmonumenta.plugins.effects.AbilityCooldownDecrease;
import com.playmonumenta.plugins.effects.EffectManager;
import com.playmonumenta.plugins.gallery.GalleryPlayer;

public class GalleryEnlightenmentEffect extends GalleryConsumableEffect {

	public GalleryEnlightenmentEffect() {
		super(GalleryEffectType.ENLIGHTENMENT);
	}

	@Override
	public void tick(GalleryPlayer player, boolean oneSecond, boolean twoHertz, int ticks) {
		super.tick(player, oneSecond, twoHertz, ticks);
		if (twoHertz) {
			EffectManager.getInstance().addEffect(player.getPlayer(), "GalleryEnlightenmentEffect", new AbilityCooldownDecrease(20, 0.2).displays(false));
		}
	}
}
