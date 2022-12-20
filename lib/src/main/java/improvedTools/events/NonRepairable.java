package improvedTools.events;

import java.util.LinkedList;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;

import improvedTools.ImprovedTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NonRepairable implements Listener {

	private static NamespacedKey key = new NamespacedKey(ImprovedTools.plugin, "unrepairable");

	@EventHandler
	public void anvilPrepare(PrepareResultEvent event) {
		if (event.getResult() == null) {
			return;
		}
		Inventory inv = event.getInventory();
		ItemStack[] stacks = inv.getContents();
		for (int i = 0; i < stacks.length; i++) {
			ItemStack itemStack = stacks[i];
			if (itemStack != null) {
				if (itemStack.hasItemMeta()) {
					ItemMeta meta = itemStack.getItemMeta();
					if (meta.getPersistentDataContainer().has(key)) {
						event.setResult(null);
						return;
					}
				}
			}
		}
	}

	public static void makeUnrepaireble(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
		LinkedList<Component> lore = new LinkedList<>();
		if (meta.hasLore()) {
			lore = new LinkedList<>(meta.lore());
		}
		lore.add(Component.text("Can't be modified").color(NamedTextColor.DARK_RED));
		meta.lore(lore);
		stack.setItemMeta(meta);
	}

}
