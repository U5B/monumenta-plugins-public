package com.playmonumenta.bossfights.bosses;

import com.playmonumenta.bossfights.BossBarManager;
import com.playmonumenta.bossfights.BossBarManager.BossHealthAction;
import com.playmonumenta.bossfights.Plugin;
import com.playmonumenta.bossfights.SpellManager;
import com.playmonumenta.bossfights.spells.Spell;
import com.playmonumenta.bossfights.spells.SpellBaseLaser;
import com.playmonumenta.bossfights.spells.SpellBlockBreak;
import com.playmonumenta.bossfights.spells.SpellChangeFloor;
import com.playmonumenta.bossfights.spells.SpellConditionalTeleport;
import com.playmonumenta.bossfights.spells.SpellFireball;
import com.playmonumenta.bossfights.spells.SpellKnockAway;
import com.playmonumenta.bossfights.spells.SpellMinionResist;
import com.playmonumenta.bossfights.utils.SerializationUtils;
import com.playmonumenta.bossfights.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class Azacor extends BossAbilityGroup {
	public static final String identityTag = "boss_azacor";
	public static final int detectionRange = 50;

	private final LivingEntity mBoss;
	private final Location mSpawnLoc;
	private final Location mEndLoc;
	private final Random mRand = new Random();

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new Azacor(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public Azacor(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellChangeFloor(plugin, mBoss, spawnLoc, 24, 3, Material.LAVA, 400),
			new SpellFireball(plugin, boss, detectionRange, 40, 1, 160, 2.0f, true, false,
			                  // Launch effect
			                  (Location loc) -> {
			                      loc.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
			                      loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 10, 0.4, 0.4, 0.4, 0);
			                  }),
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false,
			                   // Tick action per player
			                   (Player player, int ticks, boolean blocked) -> {
			                       player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 80f) * 1.5f);
			                       boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 80f) * 1.5f);
			                       player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                       boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                       if (ticks == 0) {
			                           boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4), true);
			                       }
			                   },
			                   // Particles generated by the laser
			                   (Location loc) -> {
			                       loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0.02, 0.02, 0.02, 0);
			                       loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.02, 0.02, 0.02, 0);
			                   },
			                   // Damage generated at the end of the attack
			                   (Player player, Location loc, boolean blocked) -> {
			                       loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
			                       loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 30, 0, 0, 0, 0.3);
			                       if (!blocked) {
			                           double newHealth = player.getHealth() - (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()*0.75);

			                           if (newHealth <= 0 && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
			                               // Kill the player, but allow totems to trigger
			                               player.damage(100);
			                           } else if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
			                               player.setHealth(newHealth);
			                           }
			                           player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2), true);
			                       } else {
			                           Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon tnt " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {Fuse:0}");
			                       }
			                   }),
			new SpellKnockAway(plugin, boss, 5, 20, 1.5f)
		));
		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(mBoss),
			// Teleport the boss to spawnLoc if he gets too far away from where he spawned
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 80),
			// Teleport the boss to spawnLoc if he is stuck in bedrock
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> b.getLocation().getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().add(0, 1, 0).getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().getBlock().getType() == Material.LAVA),
			new SpellMinionResist(mBoss, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30, 2), detectionRange, 5)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		int player_count = Utils.playersInRange(mSpawnLoc, detectionRange).size();
		events.put(100, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"I took his offer and I remain here. Even assassins cannot make me face death! What makes you think you can fare better?\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		events.put(75, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"I will bask in their screams!\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		events.put(50, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"Foolish mortals! Your efforts mean nothing. You cannot stop me. You will fall, just like the rest.\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		events.put(25, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"I wield powers beyond your comprehension. I will not be defeated by insects like you!\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		BossBarManager bossBar = new BossBarManager(boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, passiveSpells, detectionRange, bossBar);
	}

	private void randomMinion(String tellraw) {
		int rand = mRand.nextInt(4);
		int player_count = Utils.playersInRange(mSpawnLoc, detectionRange).size();
		double elite_health = 100.0 + player_count * 75.0;
		if (rand == 0) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:wither_skeleton " + mSpawnLoc.getX() + " " + mSpawnLoc.getY() + " " + mSpawnLoc.getZ() + " {Tags:[\"boss_tpbehind\",\"boss_generic\"],HurtByTimestamp:0,Attributes:[{Base:" + elite_health + "d,Name:\"generic.maxHealth\"},{Base:0.0d,Name:\"generic.knockbackResistance\"},{Base:0.27d,Name:\"generic.movementSpeed\"},{Base:0.0d,Name:\"generic.armor\"},{Base:0.0d,Name:\"generic.armorToughness\"},{Base:100.0d,Name:\"generic.followRange\"},{Base:12.0d,Name:\"generic.attackDamage\"}],Invulnerable:0b,FallFlying:0b,PortalCooldown:0,AbsorptionAmount:0.0f,DeathTime:0s,WorldUUIDMost:-1041596277173696703L,HandDropChances:[-200.1f,-200.1f],PersistenceRequired:0b,Spigot.ticksLived:133,Motion:[0.009843517136819713d,-0.0784000015258789d,-0.0730011114372536d],Leashed:0b,Health:" + elite_health + "f,Bukkit.updateLevel:2,LeftHanded:0b,Air:300s,OnGround:1b,Dimension:0,HandItems:[{id:\"minecraft:stone_sword\",Count:1b,tag:{display:{Name:\"{\\\"text\\\":\\\"§4§lCorrupted Watcher\\\\u0027s Sword\\\"}\"},Enchantments:[{lvl:4s,id:\"minecraft:smite\"},{lvl:5s,id:\"minecraft:unbreaking\"},{lvl:1s,id:\"minecraft:sweeping\"}],Damage:0}},{id:\"minecraft:golden_sword\",Count:1b,tag:{HideFlags:3,display:{Name:\"{\\\"text\\\":\\\"§4§lCorrupted Geomantic Dagger\\\"}\"},Enchantments:[{lvl:1s,id:\"minecraft:smite\"},{lvl:1s,id:\"minecraft:bane_of_arthropods\"},{lvl:12s,id:\"minecraft:unbreaking\"},{lvl:5s,id:\"minecraft:sharpness\"}],Damage:0,AttributeModifiers:[{UUIDMost:341186L,UUIDLeast:200116L,Amount:-2.4d,Slot:\"mainhand\",AttributeName:\"generic.attackSpeed\",Operation:0,Name:\"generic.attackSpeed\"},{UUIDMost:42236L,UUIDLeast:915731L,Amount:3.0d,Slot:\"mainhand\",AttributeName:\"generic.attackDamage\",Operation:0,Name:\"generic.attackDamage\"},{UUIDMost:197685L,UUIDLeast:254146L,Amount:3.0d,Slot:\"mainhand\",AttributeName:\"generic.armor\",Operation:0,Name:\"generic.armor\"}]}}],ArmorDropChances:[-200.1f,-200.1f,-200.1f,-200.1f],CustomName:\"{\\\"text\\\":\\\"§r§4§lSarin\\\\u0027tul the Unseen\\\"}\",Pos:[-720.0716621107997d,67.0d,-1455.2620444425788d],Fire:-1s,ArmorItems:[{id:\"minecraft:leather_boots\",Count:1b,tag:{display:{color:2110023,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Boots\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:projectile_protection\"}]}},{id:\"minecraft:leather_leggings\",Count:1b,tag:{display:{color:3033190,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Pants\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:feather_falling\"}]}},{id:\"minecraft:leather_chestplate\",Count:1b,tag:{display:{color:4088202,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Tunic\\\"}\"},Enchantments:[{lvl:20s,id:\"minecraft:blast_protection\"}],Damage:0}},{id:\"minecraft:player_head\",Count:1b,tag:{SkullOwner:{Id:\"ddb54214-900f-4067-9725-cd7c4ce52e93\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBlY2E4MTA0MTVjMzM2OGNkNTg1M2U2ODc2ZTQ1OGQyYWU0MjE0OTZmZDQzMTY5NzY5ZWE3MTI4ZTZhMTkifX19\"}]}},display:{Name:\"{\\\"text\\\":\\\"Blue Fire Demon\\\"}\"}}}],CanPickUpLoot:0b,HurtTime:0s,WorldUUIDLeast:-7560693509725274339L,Team:\"Azac\"}");
		} else if (rand == 1) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:skeleton " + mSpawnLoc.getX() + " " + mSpawnLoc.getY() + " " + mSpawnLoc.getZ() + " {Tags:[\"boss_generic\",\"boss_fireresist\"],HurtByTimestamp:0,Attributes:[{Base:" + elite_health + "d,Name:\"generic.maxHealth\"},{Base:0.0d,Name:\"generic.knockbackResistance\"},{Base:0.25d,Name:\"generic.movementSpeed\"},{Base:0.0d,Name:\"generic.armor\"},{Base:0.0d,Name:\"generic.armorToughness\"},{Base:100.0d,Name:\"generic.followRange\"},{Base:5.0d,Name:\"generic.attackDamage\"}],Invulnerable:0b,FallFlying:0b,PortalCooldown:0,AbsorptionAmount:0.0f,DeathTime:0s,WorldUUIDMost:-1041596277173696703L,HandDropChances:[-200.1f,-200.1f],PersistenceRequired:0b,Spigot.ticksLived:133,Motion:[0.009843517136819713d,-0.0784000015258789d,-0.0730011114372536d],Leashed:0b,Health:" + elite_health + "f,Bukkit.updateLevel:2,LeftHanded:0b,Air:300s,OnGround:1b,Dimension:0,HandItems:[{id:\"minecraft:bow\",Count:1b,tag:{display:{Name:\"{\\\"text\\\":\\\"§4§lDemonbreath\\\"}\"},Enchantments:[{lvl:7s,id:\"minecraft:power\"}],AttributeModifiers:[{UUIDMost:-7214717605117081503L,UUIDLeast:-8547664050005874273L,Amount:2.0d,Slot:\"mainhand\",AttributeName:\"generic.attackDamage\",Operation:0,Name:\"Modifier\"}]}},{id:\"minecraft:tipped_arrow\",Count:1b,tag:{CustomPotionColor:5017955,CustomPotionEffects:[{Ambient:0b,ShowIcon:1b,ShowParticles:1b,Duration:80,Id:25b,Amplifier:2b}],Potion:\"minecraft:empty\"}}],ArmorDropChances:[-200.1f,-200.1f,-200.1f,-200.1f],CustomName:\"{\\\"text\\\":\\\"§r§4§lZirin\\\\u0027kel the Presice\\\"}\",Pos:[-720.0716621107997d,67.0d,-1455.2620444425788d],Fire:-1s,ArmorItems:[{id:\"minecraft:leather_boots\",Count:1b,tag:{display:{color:806183,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Boots\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:projectile_protection\"}]}},{id:\"minecraft:leather_leggings\",Count:1b,tag:{display:{color:1411655,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Pants\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:feather_falling\"}]}},{id:\"minecraft:leather_chestplate\",Count:1b,tag:{display:{color:1882721,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Tunic\\\"}\"},Enchantments:[{lvl:20s,id:\"minecraft:blast_protection\"}]}},{id:\"minecraft:player_head\",Count:1b,tag:{SkullOwner:{Id:\"2af5dca6-d4ea-4c66-9ea2-453b152b75ae\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTU0MjY2MDI5ZjhmNWRhZTZmZjViMmNjZGU1MzBhNjY4YWRlNDg0YmM5NDMzNmFlZDI3NzMyNmVhMjgxOGM3In19fQ==\"}]}},display:{Name:\"{\\\"text\\\":\\\"Green Fire Demon\\\"}\"}}}],CanPickUpLoot:0b,HurtTime:0s,WorldUUIDLeast:-7560693509725274339L,Team:\"Azac\"}");
		} else if (rand == 2) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:wither_skeleton " + mSpawnLoc.getX() + " " + mSpawnLoc.getY() + " " + mSpawnLoc.getZ() + " {Tags:[\"boss_charger\",\"boss_generic\"],HurtByTimestamp:0,Attributes:[{Base:" + elite_health * 1.5 + "d,Name:\"generic.maxHealth\"},{Base:1.0d,Name:\"generic.knockbackResistance\"},{Base:0.23d,Name:\"generic.movementSpeed\"},{Base:0.0d,Name:\"generic.armor\"},{Base:0.0d,Name:\"generic.armorToughness\"},{Base:100.0d,Name:\"generic.followRange\"},{Base:10.0d,Name:\"generic.attackDamage\"}],Invulnerable:0b,FallFlying:0b,PortalCooldown:0,AbsorptionAmount:0.0f,DeathTime:0s,WorldUUIDMost:-1041596277173696703L,HandDropChances:[-200.1f,-200.1f],PersistenceRequired:0b,Spigot.ticksLived:133,Motion:[0.009843517136819713d,-0.0784000015258789d,-0.0730011114372536d],Leashed:0b,Health:" + elite_health * 1.5 + "f,Bukkit.updateLevel:2,LeftHanded:0b,Air:300s,OnGround:1b,Dimension:0,HandItems:[{id:\"minecraft:stone_axe\",Count:1b,tag:{display:{Name:\"{\\\"text\\\":\\\"§4§lSearing Wrath\\\"}\"},Enchantments:[{lvl:3s,id:\"minecraft:knockback\"}]}},{id:\"minecraft:shield\",Count:1b,tag:{HideFlags:34,BlockEntityTag:{id:\"banner\",Patterns:[{Pattern:\"gra\",Color:15},{Pattern:\"cbo\",Color:14},{Pattern:\"tts\",Color:14},{Pattern:\"bts\",Color:14},{Pattern:\"mc\",Color:1},{Pattern:\"flo\",Color:14}],Base:1},display:{Name:\"{\\\"text\\\":\\\"§4§lMagmahide Shield\\\"}\"},Damage:0,AttributeModifiers:[{UUIDMost:1894275489125451813L,UUIDLeast:-6646113075810549108L,Amount:-2.0d,Slot:\"mainhand\",AttributeName:\"generic.attackSpeed\",Operation:0,Name:\"Modifier\"},{UUIDMost:-2969066205530863996L,UUIDLeast:-6132519075155033987L,Amount:4.0d,Slot:\"mainhand\",AttributeName:\"generic.attackDamage\",Operation:0,Name:\"Modifier\"}]}}],ArmorDropChances:[-200.1f,-200.1f,-200.1f,-200.1f],CustomName:\"{\\\"text\\\":\\\"§r§4§lKazar\\\\u0027thun the Mighty\\\"}\",Pos:[-720.0716621107997d,67.0d,-1455.2620444425788d],Fire:-1s,ArmorItems:[{id:\"minecraft:leather_boots\",Count:1b,tag:{display:{color:6036496,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Boots\\\"}\"},Enchantments:[{lvl:10s,id:\"minecraft:projectile_protection\"}]}},{id:\"minecraft:leather_leggings\",Count:1b,tag:{display:{color:10695196,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Pants\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:feather_falling\"}],Damage:0}},{id:\"minecraft:leather_chestplate\",Count:1b,tag:{display:{color:13385507,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Tunic\\\"}\"},Enchantments:[{lvl:20s,id:\"minecraft:blast_protection\"}],Damage:0}},{id:\"minecraft:player_head\",Count:1b,tag:{SkullOwner:{Id:\"5c15e120-642a-47d0-a604-2df56b8c87f4\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzEzMDczOTdiYzMzMjdiYWU0OWIyOWViZmU5MmRiYTgzYjUxZDQ4MDI4NDUyMGRmZDRkMDcwMjUzNWNhMiJ9fX0=\"}]}},display:{Name:\"{\\\"text\\\":\\\"Fire Demon\\\"}\"}}}],CanPickUpLoot:0b,HurtTime:0s,WorldUUIDLeast:-7560693509725274339L,Team:\"Azac\"}");
		} else if (rand == 3) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon minecraft:wither_skeleton " + mSpawnLoc.getX() + " " + mSpawnLoc.getY() + " " + mSpawnLoc.getZ() + " {Tags:[\"boss_flamenova\",\"boss_generic\"],HurtByTimestamp:0,Attributes:[{Base:" + elite_health * 0.75 + "d,Name:\"generic.maxHealth\"},{Base:0.0d,Name:\"generic.knockbackResistance\"},{Base:0.29d,Name:\"generic.movementSpeed\"},{Base:0.0d,Name:\"generic.armor\"},{Base:0.0d,Name:\"generic.armorToughness\"},{Base:100.0d,Name:\"generic.followRange\"},{Base:5.0d,Name:\"generic.attackDamage\"}],Invulnerable:0b,FallFlying:0b,PortalCooldown:0,AbsorptionAmount:0.0f,DeathTime:0s,WorldUUIDMost:-1041596277173696703L,HandDropChances:[-200.1f,-200.1f],PersistenceRequired:0b,Spigot.ticksLived:133,Motion:[0.009843517136819713d,-0.0784000015258789d,-0.0730011114372536d],Leashed:0b,Health:" + elite_health + "f,Bukkit.updateLevel:2,LeftHanded:0b,Air:300s,OnGround:1b,Dimension:0,HandItems:[{id:\"minecraft:blaze_rod\",Count:1b,tag:{display:{Name:\"{\\\"text\\\":\\\"§5§lHell\\\\u0027s Fury\\\"}\"},Enchantments:[{lvl:3s,id:\"minecraft:sharpness\"},{lvl:2s,id:\"minecraft:knockback\"},{lvl:5s,id:\"minecraft:fire_aspect\"}],AttributeModifiers:[{UUIDMost:69502167760309831L,UUIDLeast:25155512585813018L,Amount:0.12d,Slot:\"mainhand\",AttributeName:\"generic.movementSpeed\",Operation:1,Name:\"generic.movementSpeed\"}]}},{id:\"minecraft:blaze_powder\",Count:1b,tag:{display:{Name:\"{\\\"text\\\":\\\"§4§lSoul of Conflagration\\\"}\"},Enchantments:[{lvl:3s,id:\"minecraft:fire_aspect\"}],AttributeModifiers:[{UUIDMost:-7769405553159353316L,UUIDLeast:-5980438255535144460L,Amount:0.16d,Slot:\"offhand\",AttributeName:\"generic.attackSpeed\",Operation:1,Name:\"Modifier\"},{UUIDMost:6881167597771049715L,UUIDLeast:-5128578317340387122L,Amount:1.5d,Slot:\"offhand\",AttributeName:\"generic.attackDamage\",Operation:0,Name:\"Modifier\"}]}}],ArmorDropChances:[-200.1f,-200.1f,-200.1f,-200.1f],CustomName:\"{\\\"text\\\":\\\"§r§4§lVerkan\\\\u0027tal the Cunning\\\"}\",Pos:[-720.0716621107997d,67.0d,-1455.2620444425788d],Fire:-1s,ArmorItems:[{id:\"minecraft:leather_boots\",Count:1b,tag:{display:{color:3284283,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Boots\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:projectile_protection\"}]}},{id:\"minecraft:leather_leggings\",Count:1b,tag:{display:{color:5386337,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Pants\\\"}\"},Enchantments:[{lvl:5s,id:\"minecraft:feather_falling\"}]}},{id:\"minecraft:leather_chestplate\",Count:1b,tag:{display:{color:6962813,Name:\"{\\\"text\\\":\\\"§c§lDemoncaller Tunic\\\"}\"},Enchantments:[{lvl:20s,id:\"minecraft:blast_protection\"}]}},{id:\"minecraft:player_head\",Count:1b,tag:{SkullOwner:{Id:\"69d62fc7-fcb3-4687-b076-8d1230d8344d\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlNjM3ZjFjYTczNWVmMzU0NjdhNjI3NTlkYWU0MWNmNGZmYjVjMmMxNGFhZDQyZWE4YWNlOWJjN2U2OGVjIn19fQ==\"}]}}}}],CanPickUpLoot:0b,HurtTime:0s,WorldUUIDLeast:-7560693509725274339L,Team:\"Azac\"}");
		}
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, tellraw);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int player_count = Utils.playersInRange(mBoss.getLocation(), detectionRange).size();
		int hp_del = 1024;
		int armor = (int)(Math.sqrt(player_count * 2) - 1);
		while (player_count > 0) {
			bossTargetHp = bossTargetHp + hp_del;
			hp_del = hp_del / 2;
			player_count--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		//launch event related spawn commands
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "effect @s minecraft:blindness 2 2");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"Azacor\",\"color\":\"dark_gray\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"The Dark Summoner\",\"color\":\"gray\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.7");
	}

	@Override
	public void death() {
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.enderdragon.death master @s ~ ~ ~ 100 0.8");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"No... it's not possible... I was promised...\",\"color\":\"dark_red\"}]");
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}
}
