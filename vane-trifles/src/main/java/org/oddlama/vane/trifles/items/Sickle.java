package org.oddlama.vane.trifles.items;

import org.bukkit.Material;
import org.oddlama.vane.util.BlockUtil;
import static org.oddlama.vane.util.ItemUtil.damage_item;
import static org.oddlama.vane.util.PlayerUtil.harvest_plant;
import static org.oddlama.vane.util.MaterialUtil.is_seeded_plant;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.Tag;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.oddlama.vane.annotation.item.VaneItem;
import org.oddlama.vane.core.item.CustomItem;
import org.oddlama.vane.core.item.CustomItemVariant;
import org.oddlama.vane.core.item.ItemVariantEnum;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.trifles.Trifles;

@VaneItem(name = "sickle")
public class Sickle extends CustomItem<Trifles, Sickle> {
	public static enum Variant implements ItemVariantEnum {
		WOODEN,
		STONE,
		IRON(false),
		GOLDEN,
		DIAMOND,
		NETHERITE;

		private boolean enabled;
		private Variant() { this(true); }
		private Variant(boolean enabled) {
			this.enabled = enabled;
		}

		@Override public String prefix() { return name().toLowerCase(); }
		@Override public boolean enabled() { return enabled; }
	}

	public static class SickleVariant extends CustomItemVariant<Trifles, Sickle, Variant> {
		public SickleVariant(Sickle parent, Variant variant) {
			super(parent, variant);

			final var recipe_key = recipe_key();
			if (variant == Variant.NETHERITE) {
				// TODO add_recipe(recipe_key, new SmithingRecipe(recipe_key, item(), item(Variant.DIAMOND), Material.NETHERITE_INGOT));
			} else {
				final var recipe = new ShapedRecipe(recipe_key, item())
					.shape(" mm",
						   "  m",
						   " s ")
					.setIngredient('s', Material.STICK);

				switch (variant) {
					case WOODEN:  recipe.setIngredient('m', new MaterialChoice(Tag.PLANKS)); break;
					case STONE:   recipe.setIngredient('m', new MaterialChoice(Tag.ITEMS_STONE_TOOL_MATERIALS)); break;
					case IRON:    recipe.setIngredient('m', Material.IRON_INGOT); break;
					case GOLDEN:  recipe.setIngredient('m', Material.GOLD_INGOT); break;
					case DIAMOND: recipe.setIngredient('m', Material.DIAMOND); break;

					case NETHERITE:
						// Can't happen
						break;
				}

				add_recipe(recipe_key, recipe);
			}
		}

		@Override
		public ItemStack modify_item_stack(ItemStack item) {
			double attack_speed = 0.0;
			double attack_damage = 0.0;
			switch (variant()) {
				case WOODEN:    attack_speed = 1.0; attack_damage = 2.0; break;
				case STONE:     attack_speed = 2.0; attack_damage = 3.0; break;
				case IRON:      attack_speed = 3.0; attack_damage = 4.0; break;
				case GOLDEN:    attack_speed = 5.0; attack_damage = 3.0; break;
				case DIAMOND:   attack_speed = 4.0; attack_damage = 5.0; break;
				case NETHERITE: attack_speed = 4.0; attack_damage = 6.0; break;
			}

			final var meta = item.getItemMeta();
			// TODO meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(""));
			// TODO meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, );
			item.setItemMeta(meta);
			return item;
		}
	}

	public Sickle(Context<Trifles> context) {
		super(context, Variant.class, Variant.values(), SickleVariant::new);
		// TODO attack speed and ....
		// TODO anti carrot behaviour in special listener for all custom items
		// TODO test grindstone repairing
		// TODO test disabling...
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void on_player_right_click_plant(final PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// Only seed when right clicking a plant
		final var root_block = event.getClickedBlock();
		final var plant_type = root_block.getType();
		if (!is_seeded_plant(plant_type)) {
			return;
		}

		// Get item variant
		final var player = event.getPlayer();
		final var item = player.getEquipment().getItemInMainHand();
		final var variant = this.<SickleVariant>variant_of(item);
		if (variant == null || !variant.enabled()) {
			return;
		}

		// TODO setting
		final var radius = 3;
		var total_harvested = 0;

		// Harvest surroundings
		for (var relative_pos : BlockUtil.NEAREST_RELATIVE_BLOCKS_FOR_RADIUS.get(radius - 1)) {
			final var block = relative_pos.relative(root_block);
			if (harvest_plant(player, block)) {
				++total_harvested;
			}
		}

		// Damage item if we harvested at least one plant
		if (total_harvested > 0) {
			damage_item(player, item, 1 + (int)(0.1 * total_harvested));
		}
	}
}
