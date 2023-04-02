package improvedTools.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import improvedTools.ImprovedTools;
import improvedTools.tools.Leafblower;
import improvedTools.utils.BlockBreaking;

public class LeafBreak implements Listener {

	private static NamespacedKey isBlowing = new NamespacedKey(ImprovedTools.plugin, "isBlowingLeaves");

	@EventHandler
	public void anvilPrepare(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getPlayer().getActiveItem() == null) {
			return;
		}
		Player player = event.getPlayer();
		if (player.getPersistentDataContainer().has(isBlowing)) {
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
		if (!meta.getPersistentDataContainer().has(Leafblower.key)) {
			return;
		}
		
		Block eventBlock = event.getBlock();
		if (!(Tag.LEAVES.isTagged(eventBlock.getType()))) {			
			event.setCancelled(true);
			return;
		}

		player.getPersistentDataContainer().set(isBlowing, PersistentDataType.INTEGER, 1);
		int blockLimit = meta.getPersistentDataContainer().get(Leafblower.key, PersistentDataType.INTEGER);
		ItemStack copyTool = new ItemStack(item);
		copyTool.editMeta((copyToolMeta) -> copyToolMeta.getPersistentDataContainer().remove(Leafblower.key));

		boolean isTree = false;
		LinkedList<Block> treeBlocks = new LinkedList<>();
		treeBlocks.add(eventBlock);

		long milli = System.currentTimeMillis();
		HashSet<Block> addedBlocks = new HashSet<>();
		HashSet<Block> scanBlocks = new HashSet<>();
		HashSet<Block> pastBlocks = new HashSet<>();
		addedBlocks.add(eventBlock);
		do {
			pastBlocks = new HashSet<>();
			pastBlocks.addAll(scanBlocks);
			scanBlocks = new HashSet<>();
			LinkedList<Block> scanedBlocks = new LinkedList<>();
			scanBlocks.addAll(addedBlocks);
			addedBlocks = new HashSet<>();
			for (Block block : scanBlocks) {
				Location targetLoc = block.getLocation();
				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) {
								continue;
							}
							Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
							if (scanBlocks.contains(targetBlock)) {
								continue;
							}
							scanedBlocks.add(targetBlock);
							if (pastBlocks.contains(targetBlock)) {
								continue;
							}
							if (Tag.LEAVES.isTagged(targetBlock.getType())) {
								isTree = true;
								addedBlocks.add(targetBlock);
							}					
						}
					}
				}
			}
			treeBlocks.addAll(addedBlocks);
			if (treeBlocks.size() >= blockLimit) {
				break;
			}
			if (System.currentTimeMillis() - milli > 10000) {
				ImprovedTools.plugin.getLogger().warning("Treescan for axe took longer than 10 s. Stopping it");
				break;
			}
		} while (!addedBlocks.isEmpty());


		treeBlocks.remove(eventBlock);
		if (isTree) {
			ArrayList<ItemStack> dropedItems = new ArrayList<>();
			int blockCounter = 0;
			for (Block block : treeBlocks) {
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
						while (totalAmount>0) {
							ItemStack stack = new ItemStack(key);
							int stackSize = stack.getMaxStackSize();
							stackSize = Math.min(stackSize, totalAmount);
							stack.setAmount(stackSize);
							eventBlock.getWorld().dropItem(eventBlock.getLocation().toCenterLocation(), stack);
							totalAmount = totalAmount-stackSize;
						}											
					}
				}, 5*i);
				i++;
			}
		}

		player.getPersistentDataContainer().remove(isBlowing);

	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		event.getPlayer().getPersistentDataContainer().remove(isBlowing);
	}

}
