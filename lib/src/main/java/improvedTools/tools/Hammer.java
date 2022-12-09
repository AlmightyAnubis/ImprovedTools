package improvedTools.tools;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import improvedTools.ImprovedTools;
import improvedTools.events.NonRepairable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Hammer {

	public static NamespacedKey key = new NamespacedKey(ImprovedTools.plugin, "hammerRange");

	public static ItemStack getItem() { return getItem(Material.DIAMOND_PICKAXE, 1, 100); }

	public static ItemStack getItem(int range) {
		return getItem(Material.DIAMOND_PICKAXE, range, 100);
	}

	public static ItemStack getItem(Material material, int range) {
		return getItem(material, range, 100);
	}

	public static ItemStack getItem(Material material, int range, int toolHealth) {
		if (range <= 0 || range > 20) {
			ImprovedTools.plugin.getLogger().warning(range + " is out of the range of 1 to 20. Adjusted to 1.");
			range = 1;
		}
		ItemStack hammer = new ItemStack(material);
		Damageable meta = (Damageable) hammer.getItemMeta();
		meta.setDamage(Material.DIAMOND_PICKAXE.getMaxDurability() - toolHealth);
		meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
		meta.addEnchant(Enchantment.DURABILITY, 3, true);
		meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, true);
		meta.getPersistentDataContainer().set(NonRepairable.key, PersistentDataType.INTEGER, 1);
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, range);
		meta.displayName(Component.text("Hammer").color(NamedTextColor.LIGHT_PURPLE));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Breaks blocks in ").color(NamedTextColor.WHITE));
		lore.add(Component.text((range*2+1) + "x" + (range*2+1)).color(NamedTextColor.GREEN));
		lore.add(Component.text("area").color(NamedTextColor.WHITE));
		meta.lore(lore);
		hammer.setItemMeta(meta);
		return hammer;
	}

}
