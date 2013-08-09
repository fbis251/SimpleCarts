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
import java.util.HashSet;
import java.util.Set;

import me.happypikachu.SimpleCarts.SimpleCarts;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

public class SimpleCartsPlayerListener implements Listener {
    private SimpleCarts plugin;
    public HashMap<String, Integer> lastControlBlockCreated = new HashMap<String, Integer>();
    private Economy econ;
    static boolean allowUsage = false;
    static boolean isPunched = false;
    static boolean passThrough = false;
    private Set<String> hasPayed = new HashSet<String>();
    Vector lastvel = new Vector(0, 0, 0);

    public SimpleCartsPlayerListener(SimpleCarts plugin, Economy econ) {
        this.plugin = plugin;
        this.econ = econ;
    }
    
    
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Minecart minecart;
        if ( (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && event.getPlayer().getVehicle() instanceof Minecart) {
            minecart = (Minecart)event.getPlayer().getVehicle();
        } else {
            return;
        }
        
        boolean abort = false;
        Player player = (Player)event.getPlayer();

        if (plugin.getConfig().getBoolean("Worlds." + event.getPlayer().getWorld().getName())) {
            if (econ != null) {
                if (plugin.getConfig().getBoolean("Economy.enable-vault") & !hasPayed.contains(player.getName())) {
                    if (player.isOp()) {
                        EconomyResponse r = econ.withdrawPlayer(player.getName(), 0);
                        if (r.transactionSuccess()) {
                            hasPayed.add(player.getName());
                            player.sendMessage(String.format(plugin.getLocalization(((Player)minecart.getPassenger()).getName(), "vaultCharge"), econ.format(0)));
                        } else {
                            player.sendMessage(String.format(ChatColor.RED + plugin.getLocalization(((Player)minecart.getPassenger()).getName(), "vaultError"), r.errorMessage));
                            abort = true;
                            minecart.eject();
                        }
                    } else {
                        EconomyResponse r = econ.withdrawPlayer(player.getName(), plugin.getConfig().getInt("Economy.cost-per-ride"));
                        if (r.transactionSuccess()) {
                            hasPayed.add(player.getName());
                            player.sendMessage(String.format(plugin.getLocalization(((Player)minecart.getPassenger()).getName(), "vaultCharge"), econ.format(plugin.getConfig().getInt("Economy.cost-per-ride"))));
                        } else {
                            player.sendMessage(String.format(ChatColor.RED + plugin.getLocalization(((Player)minecart.getPassenger()).getName(), "vaultError"), r.errorMessage));
                            abort = true;
                            minecart.eject();
                        }
                    }
                }
            }
            
            if (!abort) {
                Vector vel = minecart.getVelocity();
                Vector stop = new Vector(0, 0, 0);
                Location railBlock = minecart.getLocation();
                Location controlBlock = minecart.getLocation();
                railBlock.setY(Math.floor(railBlock.getY()));
                controlBlock.setY(Math.floor(controlBlock.getY()) - 1.0D);
                if (railBlock.getBlock().getTypeId() == 66 || railBlock.getBlock().getTypeId() == 27 || railBlock.getBlock().getTypeId() == 28) {
                    Vector facing = player.getLocation().getDirection();
                    if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.intersection") & vel.equals(stop)) {
                        float yaw = (player.getLocation().getYaw() - 90.0F) % 360.0F;
                        String cartDirection = SimpleCartsVehicleListener.getDirection(yaw);
                        Block rail = railBlock.getBlock();
                        Material type = rail.getType();
                        if (plugin.debugMode.contains(player.getName())) {
                            player.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Direction selected: " + cartDirection);
                        }
                        Location targetRail = minecart.getLocation();
                        BlockFace targetDirection = null;
                        if (cartDirection == "North") {
                            targetRail.setX(Math.floor(targetRail.getX() - 1.0D));
                            targetDirection = BlockFace.NORTH;
                        } else if (cartDirection == "East") {
                            targetRail.setZ(Math.floor(targetRail.getZ() - 1.0D));
                            targetDirection = BlockFace.EAST;
                        } else if (cartDirection == "South") {
                            targetRail.setX(Math.floor(targetRail.getX() + 1.0D));
                            targetDirection = BlockFace.SOUTH;
                        } else if (cartDirection == "West") {
                            targetRail.setZ(Math.floor(targetRail.getZ() + 1.0D));
                            targetDirection = BlockFace.WEST;
                        }
                        if (targetDirection != null & (targetRail.getBlock().getTypeId() == 66 || targetRail.getBlock().getTypeId() == 27 || targetRail.getBlock().getTypeId() == 28) && (type == Material.RAILS || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL)) {
                            byte olddata = rail.getData();
                            Rails r = (Rails)type.getNewData(olddata);
                            r.setDirection(targetDirection, r.isOnSlope());
                            byte newdata = r.getData();
                            if (olddata != newdata) {
                                rail.setData(newdata);
                            }
                            minecart.setVelocity(facing);
                            isPunched = true;
                            if (plugin.debugMode.contains(player.getName())) {
                                player.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have started/destroyed the Minecart.");
                            }
                        }
                    } else if (vel.equals(stop)) {
                        minecart.setVelocity(facing);
                        isPunched = true;
                        if (plugin.debugMode.contains(player.getName())) {
                            player.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have started/destroyed the Minecart.");
                        }
                    } else if (plugin.getConfig().getBoolean("Minecart.punch-to-stop")) {
                        minecart.setVelocity(stop);
                        isPunched = false;
                        if (plugin.debugMode.contains(player.getName())) {
                            player.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have stopped/destroyed the Minecart.");
                        }
                    }
                }
            }
        }
    }
}