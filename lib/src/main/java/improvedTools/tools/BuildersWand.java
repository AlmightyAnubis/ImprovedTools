package improvedTools.tools;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import improvedTools.ImprovedTools;
import improvedTools.events.NonRepairable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BuildersWand {

	public static NamespacedKey key = new NamespacedKey(ImprovedTools.plugin, "wandRange");

	public static ItemStack getItem() { return getItem(Material.DIAMOND_SWORD, 1, 100); }

	public static ItemStack getItem(int range) {
		return getItem(Material.STICK, range, 100);
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
		meta.setDamage(hammer.getType().getMaxDurability() - toolHealth);
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),"no damage", -1, Operation.MULTIPLY_SCALAR_1,EquipmentSlot.HAND));
		
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, range);
		meta.displayName(Component.text("Builders Wand").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Place blocks in ").color(NamedTextColor.WHITE));
		lore.add(Component.text((range*2+1) + "x" + (range*2+1)).color(NamedTextColor.GREEN));
		lore.add(Component.text("area").color(NamedTextColor.WHITE));
		lore.add(Component.empty());
		lore.add(Component.text("Put block in offhand").color(NamedTextColor.WHITE));
		lore.add(Component.text("to build").color(NamedTextColor.WHITE));
		meta.lore(lore);
		hammer.setItemMeta(meta);
		NonRepairable.makeUnrepaireble(hammer);
		return hammer;
	}

}
