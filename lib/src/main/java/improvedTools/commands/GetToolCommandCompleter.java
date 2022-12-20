package improvedTools.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class GetToolCommandCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> suggestions = new ArrayList<>();
		if (args.length == 1) {
			suggestions.add("hammer");
			suggestions.add("axe");
			suggestions.add("excavator");
			suggestions.add("hoe");
			suggestions.add("wand");
			suggestions.add("leafblower");
			suggestions.removeIf((text) -> !text.startsWith(args[0]));
		}
		if (args.length == 2) {
			Bukkit.getOnlinePlayers().forEach((player) -> suggestions.add(player.getName()));
		}
		if (args.length > 2) {
			if (args[0].equals("axe") || args[0].equals("leafblower")) {
				for (int i = 1; i < 10; i++) {
					suggestions.add(String.valueOf(i*50));
				}
			}else {
				for (int i = 1; i < 10; i++) {
				suggestions.add(String.valueOf(i));
			}
			}
			
			
			
		}	
		return suggestions;		
	}

}
