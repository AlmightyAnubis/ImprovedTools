package improvedTools.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BlockBreaking {

	/**
	 * Joins {@code components} using {@code separator}.
	 *
	 * @param block the block
	 * @param flags the flags
	 * <p class="atab">
	 * +1 -> particle<br>
	 * +2 -> sound<br>
	 * +4 -><br>
	 * </p>
	 * @param particleAmount Amount of particles to spawn
	 * @return a text component
	 */
	public static void breakBlock(Block block, int flags, int particleAmount) {
		Material material = block.getType();
		World world = block.getWorld();
		Location location = block.getLocation().toCenterLocation();		
		try {
			// Particle
			if ((flags & 1) == 1) {
				Particle particle = Particle.ITEM_CRACK;
				world.spawnParticle(particle, location, particleAmount, 0.5, 0.5, 0.5, 0, new ItemStack(material));
			}
			// Sound
			if ((flags >> 1 & 1) == 1) {
				world.playSound(location, block.getBlockSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1, 1);
			}
			if ((flags >> 2 & 1) == 1) {
				world.createExplosion(location, 1, false, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		block.setType(Material.AIR);

	}
	
	/**
	 * Joins {@code components} using {@code separator}.
	 *
	 * @param block the block
	 * @param flags the flags
	 * <p class="atab">
	 * +1 -> particle<br>
	 * +2 -> sound<br>
	 * +4 -><br>
	 * </p>
	 * @return a text component
	 */
	public static void breakBlock(Block block, int flags) {
		breakBlock(block, flags, 40);
	}

}
