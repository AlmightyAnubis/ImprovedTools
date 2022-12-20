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
import net.kyori.adventure.text.format.TextDecoration;

public class Treefeller {

	public static NamespacedKey key = new NamespacedKey(ImprovedTools.plugin, "axeMaxSize");

	public static ItemStack getItem() { return getItem(Material.DIAMOND_AXE, 100, 100); }

	public static ItemStack getItem(int range) {
		return getItem(Material.DIAMOND_AXE, range, 100);
	}

	public static ItemStack getItem(Material material, int range) {
		return getItem(material, range, 100);
	}

	public static ItemStack getItem(Material material, int range, int toolHealth) {
		if (range <= 0 || range > 100000) {
			ImprovedTools.plugin.getLogger().warning(range + " is out of the range of 1 to 100k. Adjusted to 50.");
			range = 1;
		}
		ItemStack hammer = new ItemStack(material);
		Damageable meta = (Damageable) hammer.getItemMeta();
		meta.setDamage(hammer.getType().getMaxDurability() - toolHealth);
		meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
		//meta.addEnchant(Enchantment.DURABILITY, 3, true);
		meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, true);
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, range);
		meta.displayName(Component.text("\uD83E\uDE93 Treefeller Axe \uD83E\uDE93").color(NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Maximum Blocks broken").color(NamedTextColor.WHITE));
		lore.add(Component.text(range).color(NamedTextColor.GREEN));
		meta.lore(lore);
		hammer.setItemMeta(meta);
		NonRepairable.makeUnrepaireble(hammer);
		return hammer;
	}

}
