package improvedTools;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import improvedTools.commands.GetToolCommand;
import improvedTools.commands.GetToolCommandCompleter;
import improvedTools.events.AdvancedBuilding;
import improvedTools.events.AdvancedFarming;
import improvedTools.events.HammerBreak;
import improvedTools.events.LeafBreak;
import improvedTools.events.NonRepairable;
import improvedTools.events.TreeBreak;

public class ImprovedTools extends JavaPlugin {

	public FileConfiguration config = getConfig();

	public static JavaPlugin plugin;
	
	public static String commandName = "getTool";

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		getServer().getPluginManager().registerEvents(new NonRepairable(), this);
		
		getServer().getPluginManager().registerEvents(new TreeBreak(), this);
		getServer().getPluginManager().registerEvents(new LeafBreak(), this);
		
		getServer().getPluginManager().registerEvents(new HammerBreak(), this);
		
		getServer().getPluginManager().registerEvents(new AdvancedFarming(), this);
		
		getServer().getPluginManager().registerEvents(new AdvancedBuilding(), this);
		
		
		getCommand(commandName).setExecutor(new GetToolCommand());
		getCommand(commandName).setTabCompleter(new GetToolCommandCompleter());
		
		
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
	}

}
