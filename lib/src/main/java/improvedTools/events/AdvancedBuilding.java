package improvedTools.events;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import improvedTools.ImprovedTools;
import improvedTools.tools.BuildersWand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AdvancedBuilding implements Listener {

	private static NamespacedKey isPlacing = new NamespacedKey(ImprovedTools.plugin, "isPlacing");

	@EventHandler(priority = EventPriority.LOWEST)
	public void wandBuilding(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getHand() != EquipmentSlot.OFF_HAND) {
			return;
		}

		Player player = (Player) event.getPlayer();

		if (player.getPersistentDataContainer().has(isPlacing)) {
			return;
		}

		if (player.getMainHand() == null) {
			return;
		}

		Block clickedBlock = event.getBlockAgainst();
		Block placedBlock = event.getBlock();
		if (event.getBlockReplacedState().getType() == placedBlock.getType()) {
			return;
		}

		Material clickedMaterial = clickedBlock.getType();

		Vector direction = placedBlock.getLocation().clone().subtract(clickedBlock.getLocation()).toVector();

		ItemStack item = player.getInventory().getItemInMainHand();

		if (item.getType() == Material.AIR) {
			return;
		}
		if (!item.hasItemMeta()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(BuildersWand.key)) {
			return;
		}

		ItemStack offHandItem = player.getInventory().getItemInOffHand();
		if (offHandItem == null || offHandItem.getType().isAir()) {
			player.sendActionBar(Component.text("Take block in offhand to place").color(NamedTextColor.RED));
			return;
		}

		if (!offHandItem.getType().isBlock()) {
			player.sendActionBar(Component.text("Take block in offhand to place").color(NamedTextColor.RED));
			return;
		}

		if (offHandItem.hasItemMeta() && !player.isSneaking()) {
			event.setCancelled(true);
			player.sendActionBar(
					Component.text("Sneak to place blocks with meta data. They may get lost.").color(NamedTextColor.RED));
			return;
		}
		if (offHandItem.getType().createBlockData() instanceof Door) {
			event.setCancelled(true);
			player.sendActionBar(Component.text("Can't place doors.").color(NamedTextColor.RED));
			return;
		}

		player.getPersistentDataContainer().set(isPlacing, PersistentDataType.INTEGER, 1);
		int range = meta.getPersistentDataContainer().get(BuildersWand.key, PersistentDataType.INTEGER);

		Location targetLoc = placedBlock.getLocation();
		Location againstLoc = clickedBlock.getLocation();

		int xMult = (int) ((1 - Math.abs(direction.getX())) * range);
		int yMult = (int) ((1 - Math.abs(direction.getY())) * range);
		int zMult = (int) ((1 - Math.abs(direction.getZ())) * range);
		int lowerBox = Math.max(-1, -yMult);
		int higherBox = yMult + (yMult + lowerBox);

		BlockData blockData = placedBlock.getBlockData();
		ItemStack removing = new ItemStack(offHandItem);
		removing.setAmount(1);

		Bukkit.getScheduler().runTask(ImprovedTools.plugin, new Runnable() {
			@Override
			public void run() {
				outer:
				for (int dx = -xMult; dx <= xMult; dx++) {
					for (int dy = lowerBox; dy <= higherBox; dy++) {
						for (int dz = -zMult; dz <= zMult; dz++) {
							if (offHandItem.getAmount() == 0 || offHandItem.getType() == Material.AIR) {
								break outer;
							}
							Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
							Material mat = targetBlock.getType();

							if (!(Tag.REPLACEABLE_PLANTS.isTagged(mat) || mat.isAir() || mat == Material.WATER
									|| targetBlock.isReplaceable())) {
								continue;
							}
							BlockData dataCopy = blockData.clone();
							if (mat == Material.WATER) {
								if (dataCopy instanceof Waterlogged) {
									Waterlogged waterlogged = (Waterlogged) dataCopy;
									Levelled targetData = (Levelled) targetBlock.getBlockData();
									if (targetData.getLevel() == 0) {
										waterlogged.setWaterlogged(true);
									}

								}
							}

							Block imaginaryClickedBlock = againstLoc.clone().add(dx, dy, dz).getBlock();
							if (imaginaryClickedBlock.getType() != clickedMaterial) {
								continue;
							}

							if (!targetBlock.canPlace(dataCopy)) {
								continue;
							}

							BlockPlaceEvent placeChecker = new BlockPlaceEvent(targetBlock, targetBlock.getState(),
									imaginaryClickedBlock, offHandItem, player, true, EquipmentSlot.OFF_HAND);
							placeChecker.callEvent();

							if (placeChecker.callEvent()) {
								targetBlock.setBlockData(dataCopy, true);
								targetBlock.getState().update(true, true);
								if (player.getGameMode() != GameMode.CREATIVE) {
									HashMap<Integer, ItemStack> left = player.getInventory().removeItem(removing);
									for (Entry<Integer, ItemStack> entry : left.entrySet()) {
										ItemStack val = entry.getValue();
										player.getInventory().removeItemAnySlot(val);
									}
								}
							}

						}
					}
				}

				if (player.getGameMode() != GameMode.CREATIVE) {
					if (item.getType().getMaxDurability() > 0) {
						Damageable damagable = (Damageable) meta;
						damagable.setDamage(damagable.getDamage() + 1);
						if (item.getType().getMaxDurability() > damagable.getDamage()) {
							item.setItemMeta(damagable);
						} else {
							item.setAmount(0);
						}
					}
				}
				player.getPersistentDataContainer().remove(isPlacing);

			}
		});
		event.setCancelled(true);

	}

	/*
	@EventHandler(priority = EventPriority.LOWEST)
	public void hoeTheLand(PlayerInteractEvent event) {
		if (event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
	
		Player player = (Player) event.getPlayer();
	
		if (event.getHand() == EquipmentSlot.OFF_HAND) {
			if (player.getMainHand() != null) {
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item.hasItemMeta()) {
					if (item.getItemMeta().getPersistentDataContainer().has(BuildersWand.key)) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	
		if (player.getPersistentDataContainer().has(isPlacing)) {
			return;
		}
	
		if (event.getHand() == EquipmentSlot.OFF_HAND) {
			return;
		}
	
		if (player.getActiveItem() == null) {
			return;
		}
	
		Block clickedBlock = event.getClickedBlock();
		Material clickedMaterial = clickedBlock.getType();
	
		ItemStack item = player.getInventory().getItem(event.getHand());
	
		if (item.getType() == Material.AIR) {
			return;
		}
		if (!item.hasItemMeta()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(BuildersWand.key)) {
			return;
		}
	
		ItemStack offHandItem = player.getInventory().getItemInOffHand();
		if (offHandItem == null || offHandItem.getType().isAir()) {
			player.sendActionBar(Component.text("Take block in offhand to place").color(NamedTextColor.RED));
			return;
		}
	
		if (!offHandItem.getType().isBlock()) {
			player.sendActionBar(Component.text("Take block in offhand to place").color(NamedTextColor.RED));
			return;
		}
	
		if (offHandItem.hasItemMeta() && !player.isSneaking()) {
			event.setCancelled(true);
			player.sendActionBar(
					Component.text("Sneak to place blocks with meta data. They may get lost.").color(NamedTextColor.RED));
			return;
		}
		if (offHandItem.getType().createBlockData() instanceof Door) {
			event.setCancelled(true);
			player.sendActionBar(Component.text("Can't place doors.").color(NamedTextColor.RED));
			return;
		}
	
		player.getPersistentDataContainer().set(isPlacing, PersistentDataType.INTEGER, 1);
		int range = meta.getPersistentDataContainer().get(BuildersWand.key, PersistentDataType.INTEGER);
	
		Location targetLoc = clickedBlock.getLocation();
		BlockFace face = event.getBlockFace();
	
		Location interactionPoint = event.getInteractionPoint().clone();
		interactionPoint.subtract(event.getClickedBlock().getLocation().toCenterLocation());
	
		if (!Tag.REPLACEABLE_PLANTS.isTagged(clickedMaterial)) {
			targetLoc.add(face.getDirection());
		}
	
		int xMult = (1 - Math.abs(face.getModX())) * range;
		int yMult = (1 - Math.abs(face.getModY())) * range;
		int zMult = (1 - Math.abs(face.getModZ())) * range;
		int lowerBox = Math.max(-1, -yMult);
		int higherBox = yMult + (yMult + lowerBox);
	
		BlockData blockData = offHandItem.getType().createBlockData();
		boolean isCopied = false;
		if (clickedBlock.getType() == offHandItem.getType()) {
			BlockData placedData = clickedBlock.getBlockData();
			if (checkNoCopyClasses(placedData.getClass())) {
				blockData = clickedBlock.getBlockData();
				isCopied = true;
			}
		}
		if (!isCopied) {
			if (blockData instanceof Orientable) {
				Orientable orientable = (Orientable) blockData;
				Axis axis = Axis.Z;
				if (face == BlockFace.UP || face == BlockFace.DOWN) {
					axis = Axis.Y;
				} else if (face == BlockFace.EAST || face == BlockFace.WEST) {
					axis = Axis.X;
				}
				orientable.setAxis(axis);
			}
	
			if (blockData instanceof Directional) {
				Directional directional = (Directional) blockData;
				if (directional.getFaces().contains(face.getOppositeFace())) {
					directional.setFacing(face.getOppositeFace());
				} else if (directional.getFaces().contains(player.getFacing().getOppositeFace())) {
					directional.setFacing(player.getFacing());
				} else {
	
					Location interactDirction = player.getEyeLocation().subtract(event.getInteractionPoint());
					interactDirction.setY(0);
					if (Math.abs(interactDirction.getX()) < Math.abs(interactDirction.getZ())) {
						interactDirction.setX(0);
					} else {
						interactDirction.setZ(0);
					}
	
					Set<BlockFace> faces = directional.getFaces();
					BlockFace targetFace = BlockFace.NORTH;
					Vector zeroVector = new Vector();
					for (BlockFace blockFace : faces) {
						if (blockFace.getDirection().getCrossProduct(interactDirction.toVector()).equals(zeroVector)) {
							Vector output = blockFace.getDirection().multiply(interactDirction.toVector());
							if (output.getX() >= 0 && output.getZ() >= 0) {
								targetFace = blockFace;
								break;
							}
						}
					}
					directional.setFacing(targetFace.getOppositeFace());
				}
	
			}
			if (blockData instanceof Bisected) {
	
				Half half = Half.BOTTOM;
				if (face == BlockFace.DOWN) {
					half = Half.TOP;
				}
				if (face != BlockFace.DOWN && face != BlockFace.UP) {
					if (interactionPoint.getY() > 0) {
						half = Half.TOP;
					}
				}
	
				Bisected stairs = (Bisected) blockData;
	
				stairs.setHalf(half);
			}
		}
	
		outer:
		for (int dx = -xMult; dx <= xMult; dx++) {
			for (int dy = lowerBox; dy <= higherBox; dy++) {
				for (int dz = -zMult; dz <= zMult; dz++) {
					if (offHandItem.getAmount() == 0 || offHandItem.getType() == Material.AIR) {
						break outer;
					}
					Block targetBlock = targetLoc.clone().add(dx, dy, dz).getBlock();
					Material mat = targetBlock.getType();
	
					if (!(Tag.REPLACEABLE_PLANTS.isTagged(mat) || mat.isAir() || mat == Material.WATER)) {
						continue;
					}
					BlockData dataCopy = blockData.clone();
					if (mat == Material.WATER) {
						if (dataCopy instanceof Waterlogged) {
							Waterlogged waterlogged = (Waterlogged) dataCopy;
							Levelled targetData = (Levelled) targetBlock.getBlockData();
							if (targetData.getLevel() == 0) {
								waterlogged.setWaterlogged(true);
							}
	
						}
					}
	
					Block facingBlock = targetLoc.clone().add(dx, dy, dz).subtract(face.getDirection()).getBlock();
					if (facingBlock.getType() != clickedMaterial) {
						continue;
					}
					BlockPlaceEvent placeChecker = new BlockPlaceEvent(targetBlock, targetBlock.getState(), facingBlock,
							offHandItem, player, true, EquipmentSlot.OFF_HAND);
	
					if (!targetBlock.canPlace(dataCopy)) {
						continue;
					}
					if (placeChecker.callEvent()) {
						targetBlock.setBlockData(dataCopy, true);
						targetBlock.getState().update(true, true);
						if (player.getGameMode() != GameMode.CREATIVE) {
							offHandItem.setAmount(offHandItem.getAmount() - 1);
						}
					}
				}
			}
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			if (meta instanceof Damageable) {
				Damageable damagable = (Damageable) meta;
				damagable.setDamage(damagable.getDamage() + 1);
				if (item.getType().getMaxDurability() >= damagable.getDamage()) {
					item.setItemMeta(damagable);
				} else {
					item.setType(Material.AIR);
				}
			}
		}
		player.getPersistentDataContainer().remove(isPlacing);
	}
	
	private boolean checkNoCopyClasses(Class<?> checkClass) {
		ArrayList<Class<?>> classes = new ArrayList<>();
		classes.add(Waterlogged.class);
		classes.add(Turtle.class);
		classes.add(TNT.class);
		classes.add(StructureBlock.class);
		classes.add(Snowable.class);
		classes.add(Snow.class);
		classes.add(SculkCatalyst.class);
		classes.add(Sapling.class);
		classes.add(RespawnAnchor.class);
		classes.add(Powerable.class);
		classes.add(Lightable.class);
		classes.add(Levelled.class);
		classes.add(Leaves.class);
		classes.add(Jukebox.class);
		classes.add(Jigsaw.class);
		classes.add(Hangable.class);
		classes.add(Farmland.class);
		classes.add(CaveVines.class);
		classes.add(Cake.class);
		classes.add(BubbleColumn.class);
		classes.add(BrewingStand.class);
		classes.add(Bisected.class);
		classes.add(Attachable.class);
		classes.add(AnaloguePowerable.class);
		classes.add(Ageable.class);
		for (Object o : classes) {
			if (!checkClass.isInstance(o)) {
				return false;
			}
		}
		return true;
	}
	*/

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		event.getPlayer().getPersistentDataContainer().remove(isPlacing);
	}

}
