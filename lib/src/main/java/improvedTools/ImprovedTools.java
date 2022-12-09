package improvedTools;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import improvedTools.commands.GetToolCommand;
import improvedTools.commands.GetToolCommandCompleter;
import improvedTools.events.HammerBreak;
import improvedTools.events.NonRepairable;

public class ImprovedTools extends JavaPlugin {

	public FileConfiguration config = getConfig();

	public static JavaPlugin plugin;
	
	public static String commandName = "getTool";

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		getServer().getPluginManager().registerEvents(new NonRepairable(), this);
		getServer().getPluginManager().registerEvents(new HammerBreak(), this);
		
		getCommand(commandName).setExecutor(new GetToolCommand());
		getCommand(commandName).setTabCompleter(new GetToolCommandCompleter());
		
		
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
	}

}
