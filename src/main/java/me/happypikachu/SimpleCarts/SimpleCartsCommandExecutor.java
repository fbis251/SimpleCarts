/*
* This file is part of SimpleCarts.
*
* SimpleCarts is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SimpleCarts is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SimpleCarts. If not, see <http://www.gnu.org/licenses/>.
*/
package me.happypikachu.SimpleCarts;

import me.happypikachu.SimpleCarts.util.Localization;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class SimpleCartsCommandExecutor implements CommandExecutor {
	private SimpleCarts plugin;
	public SimpleCartsCommandExecutor(SimpleCarts plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sc") || cmd.getName().equalsIgnoreCase("simplecarts")) {
			if(args.length == 0) {
				PluginDescriptionFile pdFile = plugin.getDescription();
				sender.sendMessage(ChatColor.RED + "SimpleCarts v" + pdFile.getVersion() + ChatColor.GRAY + " " + pdFile.getAuthors().toString());
				sender.sendMessage(ChatColor.WHITE + plugin.getLocalization(sender.getName(), "pluginDescription"));
				sender.sendMessage(ChatColor.GRAY + String.format(plugin.getLocalization(sender.getName(), "cmd"), ChatColor.WHITE + "/sc help" + ChatColor.GRAY));
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("i") || args[0].equalsIgnoreCase("info")) {
					sender.sendMessage(ChatColor.RED + String.format(plugin.getLocalization(sender.getName(), "titleHelp"), "SimpleCarts"));
					sender.sendMessage("/sc " + ChatColor.YELLOW + "b" + ChatColor.RESET + "locks: " + ChatColor.GRAY + plugin.getLocalization(sender.getName(), "cmdBlocks"));
                    sender.sendMessage("/sc " + ChatColor.YELLOW + "d" + ChatColor.RESET + "ebug: " + ChatColor.GRAY + plugin.getLocalization(sender.getName(), "cmdDebug"));
                    sender.sendMessage("/sc " + ChatColor.YELLOW + "e" + ChatColor.RESET + "ject: " + ChatColor.GRAY + plugin.getLocalization(sender.getName(), "cmdEject"));
                    sender.sendMessage("/sc " + ChatColor.YELLOW + "h" + ChatColor.RESET + "elp: " + ChatColor.GRAY + plugin.getLocalization(sender.getName(), "cmdHelp"));
                    sender.sendMessage("/sc " + ChatColor.YELLOW + "l" + ChatColor.RESET + "ang <iso>: " + ChatColor.GRAY + plugin.getLocalization(sender.getName(), "cmdLang"));
					return true;
				}
				if (args[0].equalsIgnoreCase("b") || args[0].equalsIgnoreCase("blocks")) {
					sender.sendMessage(ChatColor.RED + String.format(plugin.getLocalization(null, "titleBlocks"), "SimpleCarts"));
					if (sender instanceof Player) {
						if (plugin.getConfig().getBoolean("Worlds." + ((Player)sender).getWorld().getName())) {
							sender.sendMessage(ChatColor.GRAY + plugin.getLocalization(sender.getName(), "pluginEnabled"));
						} else {
							sender.sendMessage(ChatColor.GRAY + plugin.getLocalization(sender.getName(), "pluginDisabled"));
						}
					}
					sender.sendMessage("Booster: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.booster"));
					sender.sendMessage("Bouncer: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.bouncer"));
					sender.sendMessage("Brake: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.brake"));
					sender.sendMessage("Ejector: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.ejector"));
					sender.sendMessage("Elevator: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.elevator"));
					sender.sendMessage("Intersection: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.intersection"));
					sender.sendMessage("Station: " + ChatColor.GRAY + plugin.getConfig().getInt("BlockIDs.station"));
					return true;
				}
				if (sender instanceof Player & (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("debug"))) {
					if (!plugin.debugMode.contains(sender.getName())) {
						if (plugin.playerLang.get(((Player)sender).getName()) == "enUS") {
							sender.sendMessage(ChatColor.RED + plugin.getLocalization(sender.getName(), "denyLanguage"));
						} else {
							plugin.debugMode.add(sender.getName());
							sender.sendMessage(String.format(ChatColor.RED + "[Debug] " + ChatColor.WHITE + plugin.getLocalization(sender.getName(), "notificationsEnabled"), "SimpleCarts"));
						}
					} else {
						if (plugin.playerLang.get(((Player)sender).getName()) == "enUS" & plugin.debugMode.contains(sender.getName())) {
							plugin.debugMode.remove(sender.getName());
							sender.sendMessage(ChatColor.RED + plugin.getLocalization(sender.getName(), "denyLanguage"));
						} else {
							plugin.debugMode.remove(sender.getName());
							sender.sendMessage(String.format(ChatColor.RED + "[Debug] " + ChatColor.WHITE + plugin.getLocalization(sender.getName(), "notificationsDisabled"), "SimpleCarts"));
						}
					}
					return true;
				} else if (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("debug")) {
					sender.sendMessage(plugin.getLocalization(null, "denyConsole"));
					return true;
				}
				if (sender instanceof Player & (args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("eject"))) {
					Player p = (Player)sender;
					if (p.getVehicle() instanceof Minecart) {
						p.getVehicle().eject();
					} else {
						sender.sendMessage(ChatColor.RED + plugin.getLocalization(sender.getName(), "cartNull"));
					}
					return true;
				} else if (args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("eject")) {
					sender.sendMessage(plugin.getLocalization(null, "denyConsole"));
					return true;
				}
				if (sender instanceof Player & (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("lang"))) {
					sender.sendMessage(ChatColor.AQUA + "http://tinyurl.com/sc-codes");
					return true;
				} else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("lang")) {
					sender.sendMessage(plugin.getLocalization(null, "denyConsole"));
					return true;
				}
				defaultResponse(sender);
				return true;
			}
			if (args.length > 1) {
				if (sender instanceof Player & (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language"))) {
					String language = args[1].replaceAll("_", "").substring(0, 2).toLowerCase();
	            	String country = args[1].replaceAll("_", "").substring(2, 4).toUpperCase();
					if (Localization.isValidCode(language + country)) {
						/*if (!plugin.playerLang.containsKey(((Player)sender).getName())) {
							plugin.playerLang.put(((Player)sender).getName(), language + country);
						} else {*/
							plugin.playerLang.remove(((Player) sender).getName());
							plugin.playerLang.put(((Player)sender).getName(), language + country);
						//}
						
	            		Player p = (Player) sender;
	            		//derp
	            		//plugin.getLogger().info(String.format(ChatColor.GREEN + plugin.getLocalization(sender.getName(), "newLang"), language + country));
	            		//plugin.getLogger().info(String.format(plugin.getLocalization(sender.getName(), "altLang"), Localization.getTranslator(plugin.playerLang.get(((Player)sender).getName()))));
						p.sendMessage(String.format(ChatColor.GREEN + plugin.getLocalization(sender.getName(), "newLang"), language + country));
						p.sendMessage(String.format(plugin.getLocalization(sender.getName(), "altLang"), Localization.getTranslator(plugin.playerLang.get(((Player)sender).getName()))));
						return true;
					}
					sender.sendMessage(ChatColor.AQUA + "http://tinyurl.com/sc-codes");
					return true;
				} else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) {
					sender.sendMessage(plugin.getLocalization(null, "denyConsole"));
					return true;
				}
				defaultResponse(sender);
				return true;
			}
		}
		return false;
	}
	
	private Boolean defaultResponse(CommandSender sender) {
		PluginDescriptionFile pdFile = plugin.getDescription();
		sender.sendMessage(ChatColor.RED + "SimpleCarts v" + pdFile.getVersion() + ChatColor.GRAY + " " + pdFile.getAuthors().toString());
		sender.sendMessage(ChatColor.WHITE + plugin.getLocalization(sender.getName(), "pluginDescription"));
		sender.sendMessage(ChatColor.GRAY + String.format(plugin.getLocalization(sender.getName(), "cmd"), ChatColor.WHITE + "/sc help" + ChatColor.GRAY));
		return true;
	}
}