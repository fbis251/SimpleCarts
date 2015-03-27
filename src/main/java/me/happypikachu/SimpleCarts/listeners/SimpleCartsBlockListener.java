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
package me.happypikachu.SimpleCarts.listeners;

import java.util.HashMap;

import me.happypikachu.SimpleCarts.SimpleCarts;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class SimpleCartsBlockListener implements Listener {
	private SimpleCarts plugin;
	public HashMap<String, Integer> lastControlBlockCreated = new HashMap<String, Integer>();
	public SimpleCartsBlockListener(SimpleCarts plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		if (plugin.getConfig().getBoolean("Worlds." + event.getBlock().getWorld().getName())) {
			if (event.getBlock().getTypeId() == 23 & event.getItem().getTypeId() == 328) {
				if (plugin.getConfig().getBoolean("Dispenser.active") & plugin.getConfig().getBoolean("Dispenser.auto-refill")) {
					Dispenser dispenser = (Dispenser)event.getBlock().getState();
					dispenser.getInventory().addItem(event.getItem());
				} else if (!plugin.getConfig().getBoolean("Dispenser.active")) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getTypeId() == 66 || event.getBlockPlaced().getTypeId() == 27 || event.getBlockPlaced().getTypeId() == 28) {
			Location controlBlock = event.getBlockPlaced().getLocation();
			controlBlock.setY(Math.floor(controlBlock.getY()) - 1.0D);
				if (!lastControlBlockCreated.containsKey(event.getPlayer().getName()) || controlBlock.getBlock().getTypeId() != lastControlBlockCreated.get(event.getPlayer().getName())) {
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.booster")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createBooster"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.booster"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.bouncer")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createBouncer"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.bouncer"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.brake")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createBrake"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.brake"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.ejector")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createEjector"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.ejector"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.elevator")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createElevator"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.elevator"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.intersection")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createIntersection"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.intersection"));
					}
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.station")) {
						event.getPlayer().sendMessage(ChatColor.GRAY + plugin.getLocalization(event.getPlayer().getName(), "createStation"));
						lastControlBlockCreated.put(event.getPlayer().getName(), plugin.getConfig().getInt("BlockIDs.station"));
					}
				}
			
		}
	}
}