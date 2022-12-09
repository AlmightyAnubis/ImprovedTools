package improvedTools.events;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import improvedTools.ImprovedTools;

public class NonRepairable implements Listener{
	
	public static NamespacedKey key = new NamespacedKey(ImprovedTools.plugin, "unrepairable");
	
	@EventHandler
	public void anvilPrepare(PrepareAnvilEvent event) {
		AnvilInventory inv = event.getInventory();
		ItemStack first = inv.getFirstItem();
		ItemStack second= inv.getSecondItem();
		if (first != null) {
			if (first.hasItemMeta()) {
				ItemMeta meta = first.getItemMeta();
				if (meta.getPersistentDataContainer().has(key)) {
					event.setResult(null);
					return;
				}				
			}
		}
		if (second != null) {
			if (second.hasItemMeta()) {
				ItemMeta meta = second.getItemMeta();
				if (meta.getPersistentDataContainer().has(key)) {
					event.setResult(null);
					return;
				}				
			}
		}
		
	}
	

}
