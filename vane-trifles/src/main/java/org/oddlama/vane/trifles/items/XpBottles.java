package org.oddlama.vane.trifles.items;

import static org.oddlama.vane.util.PlayerUtil.give_item;
import static org.oddlama.vane.util.PlayerUtil.remove_one_item_from_hand;
import static org.oddlama.vane.util.Util.exp_for_level;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.oddlama.vane.annotation.config.ConfigInt;
import org.oddlama.vane.annotation.item.VaneItemv2;
import org.oddlama.vane.core.Listener;
import org.oddlama.vane.core.itemv2.CustomItem;
import org.oddlama.vane.core.itemv2.CustomItemHelper;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.trifles.Trifles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class XpBottles extends Listener<Trifles> {
	public abstract static class XpBottle extends CustomItem<Trifles> {
		@ConfigInt(def = -1, desc = "Level capacity.")
		public int config_capacity;

		public XpBottle(Context<Trifles> context) {
			super(context);
		}

		@Override
		public Component displayName() {
			return super.displayName().color(NamedTextColor.YELLOW);
		}
	}

	@VaneItemv2(name = "small_xp_bottle", base = Material.HONEY_BOTTLE, model_data = 0x76000b, version = 1)
	public static class SmallXpBottle extends XpBottle {
		public SmallXpBottle(Context<Trifles> context) { super(context); }
		public int config_capacity_def() { return 10; }
	}

	@VaneItemv2(name = "medium_xp_bottle", base = Material.HONEY_BOTTLE, model_data = 0x76000c, version = 1)
	public static class MediumXpBottle extends XpBottle {
		public MediumXpBottle(Context<Trifles> context) { super(context); }
		public int config_capacity_def() { return 20; }
	}

	@VaneItemv2(name = "small_xp_bottle", base = Material.HONEY_BOTTLE, model_data = 0x76000d, version = 1)
	public static class LargeXpBottle extends XpBottle {
		public LargeXpBottle(Context<Trifles> context) { super(context); }
		public int config_capacity_def() { return 30; }
	}

	public List<XpBottle> bottles = new ArrayList<>();

	public XpBottles(Context<Trifles> context) {
		super(context.group("xp_bottles", "Several xp bottles storing a certain amount of experience."));
		bottles.add(new SmallXpBottle(get_context()));
		bottles.add(new MediumXpBottle(get_context()));
		bottles.add(new LargeXpBottle(get_context()));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void on_player_item_consume(final PlayerItemConsumeEvent event) {
		final var item = event.getItem();
		final var custom_item = get_module().core.item_registry().get(item);
		if (!(custom_item instanceof XpBottle bottle) || !bottle.enabled()) {
			return;
		}

		// Exchange items
		final var player = event.getPlayer();
		final var main_hand = item.equals(player.getInventory().getItemInMainHand());
		remove_one_item_from_hand(player, main_hand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
		give_item(player, CustomItemHelper.newStack("vane_trifles:empty_xp_bottle"));

		// Add player experience without applying mending effects
		get_module().last_xp_bottle_consume_time.put(player.getUniqueId(), System.currentTimeMillis());
		player.giveExp(exp_for_level(bottle.config_capacity), false);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

		// Do not consume actual base item
		event.setCancelled(true);
	}
}
