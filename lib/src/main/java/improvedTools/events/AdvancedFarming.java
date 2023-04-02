package improvedTools.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import improvedTools.ImprovedTools;
import improvedTools.tools.AdvancedHoe;
import improvedTools.tools.Hammer;

public class AdvancedFarming implements Listener {

	private static NamespacedKey isHoeing = new NamespacedKey(ImprovedTools.plugin, "isFarming");

	@EventHandler
	public void callectThePlants(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		if (event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY) {
			return;
		}

		Player player = (Player) event.getPlayer();

		ItemStack item = player.getInventory().getItem(event.getHand());

		if (item.getType() == Material.AIR) {
			return;
		}
		if (!item.hasItemMeta()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(AdvancedHoe.key)) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block clickedBlock = event.getClickedBlock();
			Material type = clickedBlock.getType();
			if (player.getPersistentDataContainer().has(isHoeing)) {
				return;
			}
			if (Tag.CROPS.isTagged(type)) {
				collectDrops(event, player, clickedBlock, type, item, meta);
				return;
			} else if (type == Material.DIRT || type == Material.DIRT_PATH || type == Material.GRASS_BLOCK) {
				hoeDirt(event, player, clickedBlock, item, meta);
				return;
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			event.setUseInteractedBlock(Result.DENY);
		}

	}

	private void hoeDirt(PlayerInteractEvent event, Player player, Block clickedBlock, ItemStack item, ItemMeta meta) {

		player.getPersistentDataContainer().set(isHoeing, PersistentDataType.INTEGER, 1);
		int range = meta.getPersistentDataContainer().get(AdvancedHoe.key, PersistentDataType.INTEGER);
		ItemStack copyTool = new ItemStack(item);

		copyTool.editMeta((copyToolMeta) -> copyToolMeta.getPersistentDataContainer().remove(Hammer.key));

		Location targetLoc = clickedBlock.getLocation();
		BlockFace face = player.getTargetBlockFace(5);
		int xMult = (1 - Math.abs(face.getModX())) * range;
		int yMult = (1 - Math.abs(face.getModY())) * range;
		int zMult = (1 - Math.abs(face.getModZ())) * range;
		int lowerBox = Math.max(-1, -yMult);
		int higherBox = yMult + (yMult + lowerBox);

		for (int dx = -xMult; dx <= xMult; dx++) {
			for (int dy = lowerBox; dy <= higherBox; dy++) {
				for (int dz = -zMult; dz <= zMult; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) {
						continue;
					}
					Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
					Material mat = targetBlock.getType();
					if (!(mat == Material.DIRT || mat == Material.DIRT_PATH || mat == Material.GRASS_BLOCK)) {
						continue;
					}
					PlayerInteractEvent breakChecker =
							new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, clickedBlock, face);
					if (!targetBlock.canPlace(Material.FARMLAND.createBlockData())) {
						continue;
					}
					if (breakChecker.callEvent()) {
						targetBlock.setBlockData(Material.FARMLAND.createBlockData());
					}
				}
			}
		}
		player.getPersistentDataContainer().remove(isHoeing);
	}

	private void collectDrops(PlayerInteractEvent event, Player player, Block clickedBlock, Material type, ItemStack item,
			ItemMeta meta) {

		player.getPersistentDataContainer().set(isHoeing, PersistentDataType.INTEGER, 1);
		int range = meta.getPersistentDataContainer().get(AdvancedHoe.key, PersistentDataType.INTEGER);
		ItemStack copyTool = new ItemStack(item);

		copyTool.editMeta((copyToolMeta) -> copyToolMeta.getPersistentDataContainer().remove(AdvancedHoe.key));

		Location targetLoc = clickedBlock.getLocation();
		BlockFace face = player.getTargetBlockFace(5);
		int xMult = (1 - Math.abs(face.getModX())) * range;
		int yMult = (1 - Math.abs(face.getModY())) * range;
		int zMult = (1 - Math.abs(face.getModZ())) * range;
		int lowerBox = Math.max(-1, -yMult);
		int higherBox = yMult + (yMult + lowerBox);

		ArrayList<Block> blocksForBreaking = new ArrayList<>();
		boolean gotItem = false;
		for (int dx = -xMult; dx <= xMult; dx++) {
			for (int dy = lowerBox; dy <= higherBox; dy++) {
				for (int dz = -zMult; dz <= zMult; dz++) {
					Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
					if (!Tag.CROPS.isTagged(type)) {
						continue;
					}
					if (!(targetBlock.getBlockData() instanceof Ageable)) {
						continue;
					}
					Ageable ageable = (Ageable) targetBlock.getBlockData();
					if (ageable.getAge() != ageable.getMaximumAge()) {
						continue;
					}

					BlockBreakEvent breakChecker = new BlockBreakEvent(targetBlock, player);
					if (breakChecker.callEvent()) {
						blocksForBreaking.add(targetBlock);
						gotItem = true;
					}
				}
			}
		}

		ArrayList<ItemStack> dropedItems = new ArrayList<>();
		HashMap<Material, Integer> neededSeeds = new HashMap<>();
		for (Block block : blocksForBreaking) {
			BlockBreakEvent breakChecker = new BlockBreakEvent(block, player);
			if (breakChecker.callEvent()) {
				Collection<ItemStack> drops = block.getDrops(copyTool, player);
				dropedItems.addAll(drops);
				Ageable ageable = (Ageable) block.getBlockData();
				ageable.setAge(0);
				block.setBlockData(ageable);
				Material seedType = block.getType();
				switch (seedType) {
				case WHEAT:
					seedType = Material.WHEAT_SEEDS;
					break;
				case BEETROOTS:
					seedType = Material.BEETROOT_SEEDS;
					break;
				case CARROTS:
					seedType = Material.CARROT;
					break;
				case POTATOES:
					seedType = Material.POTATO;
					break;
				default:
					break;
				}
				neededSeeds.put(seedType, neededSeeds.getOrDefault(seedType, 0) + 1);
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

		for (Entry<Material, Integer> entry : neededSeeds.entrySet()) {
			ItemStack key = new ItemStack(entry.getKey());
			Integer val = entry.getValue();

			if (itemMap.containsKey(key)) {
				itemMap.put(key, Math.max(0, itemMap.get(key) - val));
			}
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
						clickedBlock.getWorld().dropItem(clickedBlock.getLocation().toCenterLocation(), stack);
						totalAmount = totalAmount - stackSize;
					}
				}
			}, 5 * i);
			i++;
		}

		if (gotItem) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				if (meta instanceof Damageable) {
					Damageable damagable = (Damageable) meta;
					damagable.setDamage(damagable.getDamage() + 1);
					item.setItemMeta(damagable);
				}
			}
		}

		player.getPersistentDataContainer().remove(isHoeing);
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		event.getPlayer().getPersistentDataContainer().remove(isHoeing);
	}

}
