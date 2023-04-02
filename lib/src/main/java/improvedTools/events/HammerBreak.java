package improvedTools.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
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
import improvedTools.utils.BlockBreaking;

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
		boolean isPickaxe = item.getType().toString().toLowerCase().contains("pickaxe");
		boolean isShovel = item.getType().toString().toLowerCase().contains("shovel");

		ItemStack copyTool = new ItemStack(item);
		
		copyTool.editMeta((copyToolMeta) -> copyToolMeta.getPersistentDataContainer().remove(Hammer.key));
		Location targetLoc = event.getBlock().getLocation();
		BlockFace face = player.getTargetBlockFace(5);
		int xMult = (1 - Math.abs(face.getModX())) * range;
		int yMult = (1 - Math.abs(face.getModY())) * range;
		int zMult = (1 - Math.abs(face.getModZ())) * range;
		int lowerBox = Math.max(-1, -yMult);
		int higherBox = yMult + (yMult + lowerBox);

		LinkedList<Block> blocksForBreaking = new LinkedList<>();
		for (int dx = -xMult; dx <= xMult; dx++) {
			for (int dy = lowerBox; dy <= higherBox; dy++) {
				for (int dz = -zMult; dz <= zMult; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) {
						continue;
					}
					Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();

					if (!targetBlock.isValidTool(item)) {
						continue;
					}

					if (!Tag.MINEABLE_PICKAXE.isTagged(targetBlock.getType()) && isPickaxe) {
						continue;
					}

					if (!Tag.MINEABLE_SHOVEL.isTagged(targetBlock.getType()) && isShovel) {
						continue;
					}

					if (targetBlock.getState() instanceof TileState) {
						continue;
					}

					blocksForBreaking.add(targetBlock);
				}
			}
		}

		blocksForBreaking.remove(event.getBlock());
		ArrayList<ItemStack> dropedItems = new ArrayList<>();
		int blockCounter = 0;
		for (Block block : blocksForBreaking) {
			BlockBreakEvent breakChecker = new BlockBreakEvent(block, player);
			if (breakChecker.callEvent()) {
				Collection<ItemStack> drops = block.getDrops(copyTool, player);
				dropedItems.addAll(drops);				
				BlockBreaking.breakBlock(block, blockCounter<20? 3:0 );
				blockCounter++;
			}
		}
		HashMap<ItemStack, Integer> itemMap = new HashMap<>();
		for (ItemStack itemStack : dropedItems) {
			int i = itemStack.getAmount();
			ItemStack copyStack = new ItemStack(itemStack);
			copyStack.setAmount(1);
			if (itemMap.containsKey(copyStack)) {
				i += itemMap.get(copyStack);
			}
			itemMap.put(copyStack, i);
		}

		int i = 0;
		for (Entry<ItemStack, Integer> entry : itemMap.entrySet()) {			
			ItemStack key = entry.getKey();
			Integer val = entry.getValue();

			Bukkit.getScheduler().runTaskLater(ImprovedTools.plugin, new Runnable() {

				@Override
				public void run() {
					int totalAmount = val;
					while (totalAmount > 0) {
						ItemStack stack = new ItemStack(key);
						int stackSize = stack.getMaxStackSize();
						stackSize = Math.min(stackSize, totalAmount);
						stack.setAmount(stackSize);
						event.getBlock().getWorld().dropItem(event.getBlock().getLocation().toCenterLocation(), stack);
						totalAmount = totalAmount - stackSize;
					}
				}
			}, 5 * i);
			i++;
		}

		player.getPersistentDataContainer().remove(isBreaking);

	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		event.getPlayer().getPersistentDataContainer().remove(isBreaking);
	}

}
