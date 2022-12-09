package improvedTools.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import improvedTools.ImprovedTools;
import improvedTools.tools.Hammer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GetToolCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arg) {
		Player target = null;
		if (sender != null) {
			target = (Player) sender;
		}
		if (arg.length >= 2) {
			String name = arg[1];
			Player player = Bukkit.getPlayer(name);
			if (player != null) {
				target = player;
			}
		}
		if (target == null) {
			if (sender == null) {
				ImprovedTools.plugin.getLogger().severe("Missing target");
			}else {
				sender.sendMessage(Component.text("Missing target").color(NamedTextColor.RED));
			}
			return true;
		}
		String toolName = arg[0];
		ItemStack stack = null;
		switch (toolName) {
		case "hammer":
			if (arg.length >= 3) {
				int range = Integer.valueOf(arg[2]);
				if (arg.length >= 4) {
					int durablility = Integer.valueOf(arg[3]);
					stack = Hammer.getItem(Material.DIAMOND_PICKAXE, range, durablility);
				}else {
					stack = Hammer.getItem(range);
				}
			}else {
				stack = Hammer.getItem();
			}			
			
			break;
		}
		if (stack != null) {
			target.getInventory().addItem(stack);
			return true;
		}
		
		return false;
	}

}
