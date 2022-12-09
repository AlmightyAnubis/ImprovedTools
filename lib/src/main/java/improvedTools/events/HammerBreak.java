package improvedTools.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import improvedTools.ImprovedTools;
import improvedTools.tools.Hammer;

public class HammerBreak implements Listener {

	private static NamespacedKey isBreaking = new NamespacedKey(ImprovedTools.plugin, "isBreakingBlocks");
	
	@EventHandler
	public void anvilPrepare(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getPlayer().getActiveItem() == null) {
			return;
		}
		Player player = event.getPlayer();
		if (player.getPersistentDataContainer().has(isBreaking)) {
			return;
		}		
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			return;
		}
		if (!item.hasItemMeta()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(Hammer.key)) {
			return;
		}
		player.getPersistentDataContainer().set(isBreaking, PersistentDataType.INTEGER, 1);
		int range = meta.getPersistentDataContainer().get(Hammer.key, PersistentDataType.INTEGER);
		ItemStack copyTool = new ItemStack(item);
		copyTool.getItemMeta().addAttributeModifier(Attribute.GENERIC_LUCK,
				new AttributeModifier("text", range, Operation.ADD_NUMBER));
		copyTool.editMeta((copyToolMeta) -> copyToolMeta.getPersistentDataContainer().remove(Hammer.key));
		Location targetLoc = event.getBlock().getLocation();
		BlockFace face = player.getTargetBlockFace(5);
		int xMult = (1 - Math.abs(face.getModX())) * range;
		int yMult = (1 - Math.abs(face.getModY())) * range;
		int zMult = (1 - Math.abs(face.getModZ())) * range;
		for (int dx = -xMult; dx <= xMult; dx++) {
			for (int dy = -yMult; dy <= yMult; dy++) {
				for (int dz = -zMult; dz <= zMult; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) {
						continue;
					}
					Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
					if (!targetBlock.isValidTool(item)) {
						continue;
					}
					if (targetBlock.getState() instanceof TileState) {
						continue;
					}
					
					BlockBreakEvent breakChecker = new BlockBreakEvent(targetBlock, player);
					
					
					if (breakChecker.callEvent()) {						
						targetBlock.breakNaturally(copyTool);
					}
				}
			}
		}
		player.getPersistentDataContainer().remove(isBreaking);

	}
	
	@EventHandler
	public void anvilPrepare(PlayerJoinEvent event) {
		event.getPlayer().getPersistentDataContainer().remove(isBreaking);
	}

}
