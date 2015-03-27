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

import java.util.HashSet;
import java.util.Set;

import me.happypikachu.SimpleCarts.SimpleCarts;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

public class SimpleCartsVehicleListener implements Listener {
	private SimpleCarts plugin;
	private Economy econ;
	static boolean allowUsage = false;
	static boolean isPunched = false;
	static boolean passThrough = false;
	private Set<String> hasPayed = new HashSet<String>();
	Vector lastvel = new Vector(0, 0, 0);
	public SimpleCartsVehicleListener(SimpleCarts plugin, Economy econ) {
		this.plugin = plugin;
		this.econ = econ;
	}	

	@EventHandler(priority=EventPriority.LOWEST)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if ((event.getVehicle() instanceof Minecart & event.getEntered() instanceof Player) && plugin.getConfig().getBoolean("Worlds." + event.getEntered().getWorld().getName())) {
			((Player)event.getEntered()).sendMessage(ChatColor.GREEN + plugin.getLocalization(((Player)event.getEntered()).getName(), "cartPunch"));
			if (econ != null) {
				if (plugin.getConfig().getBoolean("Economy.enable-vault")) {
					event.getVehicle().setVelocity(new Vector(0, 0, 0));
					if (((Player)event.getEntered()).isOp()) {
						((Player)event.getEntered()).sendMessage(String.format(ChatColor.GRAY + plugin.getLocalization(((Player)event.getEntered()).getName(), "vaultNotify"), econ.format(0)));
					} else {
						((Player)event.getEntered()).sendMessage(String.format(ChatColor.GRAY + plugin.getLocalization(((Player)event.getEntered()).getName(), "vaultNotify"), econ.format(plugin.getConfig().getInt("Economy.cost-per-ride")) + "."));
					}
				}
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		boolean abort = false;
		if ((event.getVehicle() instanceof Minecart & event.getVehicle().getPassenger() instanceof Player & event.getAttacker() instanceof Player) && plugin.getConfig().getBoolean("Worlds." + event.getAttacker().getWorld().getName())) {
			if (econ != null) {
				if (plugin.getConfig().getBoolean("Economy.enable-vault") & !hasPayed.contains(((Player)event.getAttacker()).getName())) {
					if (((Player) event.getAttacker()).isOp()) {
						EconomyResponse r = econ.withdrawPlayer(((Player)event.getAttacker()).getName(), 0);
						if (r.transactionSuccess()) {
							hasPayed.add(((Player)event.getAttacker()).getName());
							((Player)event.getAttacker()).sendMessage(String.format(plugin.getLocalization(((Player)event.getVehicle().getPassenger()).getName(), "vaultCharge"), econ.format(0)));
						} else {
							((Player)event.getAttacker()).sendMessage(String.format(ChatColor.RED + plugin.getLocalization(((Player)event.getVehicle().getPassenger()).getName(), "vaultError"), r.errorMessage));
							abort = true;
							((Minecart)event.getVehicle()).eject();
						}
					} else {
						EconomyResponse r = econ.withdrawPlayer(((Player)event.getAttacker()).getName(), plugin.getConfig().getInt("Economy.cost-per-ride"));
						if (r.transactionSuccess()) {
							hasPayed.add(((Player)event.getAttacker()).getName());
							((Player)event.getAttacker()).sendMessage(String.format(plugin.getLocalization(((Player)event.getVehicle().getPassenger()).getName(), "vaultCharge"), econ.format(plugin.getConfig().getInt("Economy.cost-per-ride"))));
						} else {
							((Player)event.getAttacker()).sendMessage(String.format(ChatColor.RED + plugin.getLocalization(((Player)event.getVehicle().getPassenger()).getName(), "vaultError"), r.errorMessage));
							abort = true;
							((Minecart)event.getVehicle()).eject();
						}
					}
				}
			}
			
			if (!abort) {
				Vector vel = event.getVehicle().getVelocity();
				Vector stop = new Vector(0, 0, 0);
				Location railBlock = event.getVehicle().getLocation();
				Location controlBlock = event.getVehicle().getLocation();
				railBlock.setY(Math.floor(railBlock.getY()));
				controlBlock.setY(Math.floor(controlBlock.getY()) - 1.0D);
				if (railBlock.getBlock().getTypeId() == 66 || railBlock.getBlock().getTypeId() == 27 || railBlock.getBlock().getTypeId() == 28) {
					Vector facing = event.getAttacker().getLocation().getDirection();
					if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.intersection") & vel.equals(stop)) {
						float yaw = (event.getAttacker().getLocation().getYaw() - 90.0F) % 360.0F;
						String cartDirection = getDirection(yaw);
						Block rail = railBlock.getBlock();
						Material type = rail.getType();
						if (plugin.debugMode.contains(((Player)event.getAttacker()).getName())) {
							((Player)event.getAttacker()).sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Direction selected: " + cartDirection);
						}
						Location targetRail = event.getVehicle().getLocation();
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
							event.getVehicle().setVelocity(facing);
							isPunched = true;
							if (plugin.debugMode.contains(((Player)event.getAttacker()).getName())) {
								((Player)event.getAttacker()).sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have started/destroyed the Minecart.");
							}
						}
					} else if (vel.equals(stop)) {
						event.getVehicle().setVelocity(facing);
						isPunched = true;
						if (plugin.debugMode.contains(((Player)event.getAttacker()).getName())) {
							((Player)event.getAttacker()).sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have started/destroyed the Minecart.");
						}
					} else if (plugin.getConfig().getBoolean("Minecart.punch-to-stop")) {
						event.getVehicle().setVelocity(stop);
						isPunched = false;
						if (plugin.debugMode.contains(((Player)event.getAttacker()).getName())) {
							((Player)event.getAttacker()).sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "You have stopped/destroyed the Minecart.");
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onVehicleUpdate(VehicleUpdateEvent event) {
		if ((event.getVehicle().getPassenger() instanceof Player & event.getVehicle() instanceof Minecart) && (plugin.getConfig().getBoolean("Worlds." + event.getVehicle().getPassenger().getWorld().getName()))) {
			boolean abort = false;
			if (econ != null) {
				if (plugin.getConfig().getBoolean("Economy.enable-vault") & !hasPayed.contains(((Player)event.getVehicle().getPassenger()).getName())) {
					event.getVehicle().setVelocity(new Vector(0, 0, 0));
				}
			}
			
			if (!abort) {
				Location railBlock = event.getVehicle().getLocation();
				Location controlBlock = event.getVehicle().getLocation();
				railBlock.setY(Math.floor(railBlock.getY()));
				controlBlock.setY(Math.floor(controlBlock.getY()) - 1.0D);
				boolean isPowered = false;
				if (controlBlock.getBlock().getTypeId() == 66 || controlBlock.getBlock().getTypeId() == 27 || controlBlock.getBlock().getTypeId() == 28) {
					railBlock.setY(Math.floor(railBlock.getY()) - 1.0D);
					controlBlock.setY(Math.floor(controlBlock.getY()) - 1.0D);
				}
				if (railBlock.getBlock().getTypeId() != 28 & (railBlock.getBlock().isBlockPowered() || controlBlock.getBlock().isBlockPowered())) {
					isPowered = true;
				}
				if (railBlock.getBlock().getTypeId() == 66 || railBlock.getBlock().getTypeId() == 27 || railBlock.getBlock().getTypeId() == 28) {
					Vector vel = event.getVehicle().getVelocity();
					double x = vel.getX();
					double y = vel.getY();
					double z = vel.getZ();
					if(!(plugin.getConfig().getDouble("Minecart.speed-multiplier") < 0.01D)) {
						double multiplier = Math.ceil(plugin.getConfig().getDouble("Minecart.speed-multiplier")) / 100.0D;
						if (multiplier > 5.0D) {
							multiplier = 1.0D;
						}
						((Minecart)event.getVehicle()).setMaxSpeed(0.9D * multiplier);
						if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.booster")) {
							if ((!plugin.getConfig().getBoolean("Redstone.booster")) || ((plugin.getConfig().getBoolean("Redstone.booster")) && (!isPowered))) {
								if (x > 0.0D) {
									x = 0.8D * multiplier;
								} else if (x < 0.0D) {
									x = -0.8D * multiplier;
								}
								if (y > 0.0D) {
									y = 0.8D * multiplier;
								} else if (y < 0.0D) {
									y = -0.8D * multiplier;
								}
								if (z > 0.0D) {
									z = 0.8D * multiplier;
								} else if (z < 0.0D) {
									z = -0.8D * multiplier;
								}
							}
						} else if (controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.brake")) {
							if ((!plugin.getConfig().getBoolean("Redstone.brake")) || ((plugin.getConfig().getBoolean("Redstone.brake")) && (!isPowered))) {
								if (x > 0.0D) {
									x = 0.2D * multiplier;
								} else if (x < 0.0D) {
									x = -0.2D * multiplier;
								}
								if (y > 0.0D) {
									y = 0.2D * multiplier;
								} else if (y < 0.0D) {
									y = -0.2D * multiplier;
								}
								if (z > 0.0D) {
									z = 0.2D * multiplier;
								} else if (z < 0.0D) {
									z = -0.2D * multiplier;
								}
							}
						} else {
							if (x > 0.0D) {
								x = 0.4D * multiplier;
							} else if (x < 0.0D) {
								x = -0.4D * multiplier;
							}
							if (y > 0.0D) {
								y = 0.4D * multiplier;
							} else if (y < 0.0D) {
								y = -0.4D * multiplier;
							}
							if (z > 0.0D) {
								z = 0.4D * multiplier;
							} else if (z < 0.0D) {
								z = -0.4D * multiplier;
							}
						}
						vel = new Vector(x, y, z);
						event.getVehicle().setVelocity(vel);
					}
					if (x != 0.0D || y != 0.0D || z != 0.0D) {
						lastvel = new Vector(x, y, z);
					}
					if (passThrough) {
						Vector revvel = new Vector(vel.getX() * -1.0D, vel.getY() * -1.0D, vel.getZ() * -1.0D);
						event.getVehicle().setVelocity(revvel);
						passThrough = false;
					}
					if (controlBlock.getBlock().getTypeId() != plugin.getConfig().getInt("BlockIDs.station")
							& controlBlock.getBlock().getTypeId() != plugin.getConfig().getInt("BlockIDs.ejector")
							& controlBlock.getBlock().getTypeId() != plugin.getConfig().getInt("BlockIDs.bouncer")
							& controlBlock.getBlock().getTypeId() != plugin.getConfig().getInt("BlockIDs.intersection")
							& controlBlock.getBlock().getTypeId() != plugin.getConfig().getInt("BlockIDs.elevator")) {
						allowUsage = true;
					} else {
						if ((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.station") & allowUsage)) {
							stationStop(railBlock, controlBlock, event, (Player)event.getVehicle().getPassenger(), isPowered);
						} else if (((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.station") ? 1 : 0) & (allowUsage ? 0 : 1)) != 0) { //here
							stationGo(railBlock, controlBlock, event, (Player)event.getVehicle().getPassenger(), isPowered, lastvel);
						}
						if ((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.ejector") & allowUsage)) {
							ejector(railBlock, controlBlock, event, (Player)event.getVehicle().getPassenger(), isPowered);
						}
						if ((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.bouncer") & allowUsage)) {
							bouncer(railBlock, controlBlock, vel, x, y, z, event, (Player)event.getVehicle().getPassenger(), isPowered);
						}
						if ((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.intersection") & allowUsage)) {
							intersection(controlBlock, event, (Player)event.getVehicle().getPassenger());
						}
						if ((controlBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.elevator") & allowUsage)) {
							elevator(controlBlock, railBlock, event, x, z, (Player)event.getVehicle().getPassenger(), isPowered, event.getVehicle().getWorld());
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
		Location hitBlock = event.getBlock().getLocation();
		if (hitBlock.getBlock().getTypeId() == 54 & plugin.getConfig().getBoolean("Collector.active")) {
			if (plugin.getConfig().getBoolean("Worlds." + event.getBlock().getWorld().getName())) {
				if ((event.getVehicle() instanceof Minecart)) {
					if (!plugin.getConfig().getBoolean("Collector.auto-dispose")) {
						ItemStack item = new ItemStack(328, 1);
						Chest chest = (Chest)hitBlock.getBlock().getState();
						chest.getInventory().addItem(item);
						chest.update();
					}
					if ((event.getVehicle().getPassenger() instanceof Entity)) {
						event.getVehicle().eject();
					}
					event.getVehicle().remove();
				} else if ((event.getVehicle() instanceof StorageMinecart)) {
					if (!plugin.getConfig().getBoolean("Collector.auto-dispose")) {
						ItemStack item = new ItemStack(342, 1);
						Chest chest = (Chest)hitBlock.getBlock().getState();
						chest.getInventory().addItem(item);
						chest.update();
					}
					event.getVehicle().remove();
				} else if ((event.getVehicle() instanceof PoweredMinecart)) {
					if (!plugin.getConfig().getBoolean("Collector.auto-dispose")) {
						ItemStack item = new ItemStack(343, 1);
						Chest chest = (Chest)hitBlock.getBlock().getState();
						chest.getInventory().addItem(item);
					}
					event.getVehicle().remove();
				}
			}
		} else if (((event.getVehicle().getPassenger() instanceof Player)) && ((event.getVehicle() instanceof Minecart)) && (plugin.getConfig().getBoolean("Worlds." + event.getVehicle().getPassenger().getWorld().getName()))) {
			Vector vel = event.getVehicle().getVelocity();
			if (vel.getX() == 0.0D & vel.getZ() == 0.0D) {
				isPunched = false;
				if ((plugin.debugMode.contains(((Player)event.getVehicle().getPassenger()).getName()) & hitBlock.getBlock().getTypeId() != 66 & hitBlock.getBlock().getTypeId() != 27 & hitBlock.getBlock().getTypeId() != 28)) {
					((Player)event.getVehicle().getPassenger()).sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with " + hitBlock.getBlock().getType() + ".");
				}
			}
		}
	}

	@EventHandler
		public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
			if (((event.getVehicle().getPassenger() instanceof Player)) && ((event.getVehicle() instanceof Minecart)) && (plugin.getConfig().getBoolean("Worlds." + event.getEntity().getWorld().getName()))) {
				Player p = (Player)event.getVehicle().getPassenger();
				if ((event.getEntity() instanceof Minecart & event.getEntity().getPassenger() instanceof Player)) {
					Player p2 = (Player)event.getEntity().getPassenger();
					if (plugin.debugMode.contains(p.getName())) {
						p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with " + p2.getName() + "'s cart.");
					}
				} else if ((event.getEntity() instanceof Boat & event.getEntity().getPassenger() instanceof Player)) {
					Player p2 = (Player)event.getEntity().getPassenger();
					if (plugin.debugMode.contains(p.getName())) {
						p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with " + p2.getName() + "'s... boat?");
					}
				} else if ((event.getEntity() instanceof Player)) {
					Player p2 = (Player)event.getEntity();
					if (p2.getVehicle() == null) {
						if (plugin.getConfig().getBoolean("Path.kick-players")) {
							if (plugin.debugMode.contains(p.getName())) {
								p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart passed through " + p2.getName() + ".");
							}
							p2.kickPlayer("[SimpleCarts] " + String.format(plugin.getLocalization(((Player)event.getVehicle().getPassenger()).getName(), "playerKick"), p.getName()));
							passThrough = true;
						} else if (plugin.debugMode.contains(p.getName())) {
							p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with " + p2.getName() + ".");
						}
					}
				} else if ((event.getEntity() instanceof StorageMinecart)) {
					if (plugin.debugMode.contains(p.getName()))
						p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with StorageMinecart.");
				} else if ((event.getEntity() instanceof PoweredMinecart)) {
					if (plugin.debugMode.contains(p.getName()))
						p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with PoweredMinecart.");
				} else if ((event.getEntity() instanceof Minecart)) {
					if (event.getEntity().getPassenger() == null) {
						if (plugin.getConfig().getBoolean("Path.remove-empty-carts")) {
							if (plugin.debugMode.contains(p.getName())) {
								p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart passed through Minecart.");
							}
							event.getEntity().remove();
							passThrough = true;
						} else if (plugin.debugMode.contains(p.getName())) {
							p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with Minecart.");
						}
					}
				} else if (plugin.getConfig().getBoolean("Path.remove-entities")) {
					if ((event.getEntity() instanceof Entity)) {
						if (plugin.debugMode.contains(p.getName())) {
							p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart passed through " + event.getEntity().toString().replaceFirst("Craft", "") + ".");
						}
						event.getEntity().remove();
					}
					passThrough = true;
				} else if (((event.getEntity() instanceof Entity)) && (plugin.debugMode.contains(p.getName()))) {
					p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart collided with " + event.getEntity().toString().replaceFirst("Craft", "") + ".");
				}
			}
 	 }

	@EventHandler(priority=EventPriority.LOWEST)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getAttacker() instanceof Player & event.getVehicle().getPassenger() instanceof Player & event.getVehicle() instanceof Minecart) {
			if (plugin.getConfig().getBoolean("Minecart.destroy-on-exit")) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onVehicleExit(VehicleExitEvent event) {
		if (((event.getVehicle().getPassenger() instanceof Player)) && ((event.getVehicle() instanceof Minecart)) && (plugin.getConfig().getBoolean("Worlds." + event.getVehicle().getPassenger().getWorld().getName()))) {
			if ((plugin.getConfig().getBoolean("Minecart.drop-on-exit")) && (!isPunched)) {
				event.getVehicle().getWorld().dropItem(event.getVehicle().getLocation(), new ItemStack(328, 1));
			}

			if (plugin.getConfig().getBoolean("Minecart.destroy-on-exit")) {
				event.getVehicle().remove();
			}
			isPunched = false;
			lastvel = new Vector(0, 0, 0);
			hasPayed.remove(((Player)event.getVehicle().getPassenger()).getName());
		}
	}
	
	public static String getDirection(float yaw) {
		yaw = (yaw - 90) % 360;
		if (yaw < 0.0F) {
			yaw += 360.0F;
		}
		if ((yaw >= 0.0F) && (yaw < 45.0F)) {
			return "North";
		}
		if ((yaw >= 45.0F) && (yaw < 135.0F)) {
			return "East";
		}
		if ((yaw >= 135.0F) && (yaw < 225.0F)) {
			return "South";
		}
		if ((yaw >= 225.0F) && (yaw < 315.0F)) {
			return "West";
		}
		if ((yaw >= 315.0F) && (yaw < 360.0F)) {
			return "North";
		}
		return null;
	}
	
	public void stationStop(Location railBlock, Location controlBlock, VehicleUpdateEvent event, Player p, boolean isPowered) {
		if ((!plugin.getConfig().getBoolean("Redstone.station")) && (!isPowered) || ((plugin.getConfig().getBoolean("Redstone.station")) && (!isPowered))) {
			Vector stop = new Vector(0, 0, 0);
			event.getVehicle().setVelocity(stop);
			allowUsage = false;
			isPunched = false;
			if (plugin.debugMode.contains(p.getName())) {
				p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart should be haulted by Station (" + controlBlock.getBlock().getTypeId() + ").");
			}
		}
	}

	public void stationGo(Location railBlock, Location controlBlock, VehicleUpdateEvent event, Player p, boolean isPowered, Vector lastvel) {
		if ((plugin.getConfig().getBoolean("Redstone.station") == false) && (isPowered) || ((plugin.getConfig().getBoolean("Redstone.station") == true) && (isPowered))) {
			event.getVehicle().setVelocity(lastvel);
			allowUsage = true;
			isPunched = true;
			if (plugin.debugMode.contains(p.getName())) {
				p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart should be launched by Station (" + controlBlock.getBlock().getTypeId() + ").");
			}
		}
	}

	public void ejector(Location railBlock, Location controlBlock, VehicleUpdateEvent event, Player p, boolean isPowered) {
		if ((!plugin.getConfig().getBoolean("Redstone.ejector")) || ((plugin.getConfig().getBoolean("Redstone.ejector")) && (!isPowered))) {
			event.getVehicle().eject();
			allowUsage = false;
			if (plugin.debugMode.contains(p.getName())) {
				p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Player should be ejected by Ejector (" + controlBlock.getBlock().getTypeId() + ").");
			}
		}
		if ((plugin.getConfig().getBoolean("Minecart.drop-on-eject")) && (!isPunched)) {
			event.getVehicle().getWorld().dropItem(event.getVehicle().getLocation(), new ItemStack(328, 1));
		}
		if (plugin.getConfig().getBoolean("Minecart.destroy-on-eject")) {
			event.getVehicle().remove();
		}
		isPunched = false;
		hasPayed.remove(((Player)p).getName());
	}

	public void bouncer(Location railBlock, Location controlBlock, Vector newvel, double x, double y, double z, VehicleUpdateEvent event, Player p, boolean isPowered) {
		if ((!plugin.getConfig().getBoolean("Redstone.bouncer")) || ((plugin.getConfig().getBoolean("Redstone.bouncer")) && (!isPowered))) {
			if ((newvel.getX() > 0.0D) || (newvel.getY() > 0.0D) || (newvel.getZ() > 0.0D)) {
				x = Math.abs(x) * -1.0D;
				y = Math.abs(y) * -1.0D;
				z = Math.abs(z) * -1.0D;
				newvel = new Vector(x, y, z);
				event.getVehicle().setVelocity(newvel);
			} else if ((newvel.getX() < 0.0D) || (newvel.getY() < 0.0D) || (newvel.getZ() < 0.0D)) {
				x = Math.abs(x);
				y = Math.abs(y);
				z = Math.abs(z);
				newvel = new Vector(x, y, z);
				event.getVehicle().setVelocity(newvel);
			}
			allowUsage = false;
			if (plugin.debugMode.contains(p.getName())) {
				p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Direction should be reversed by Bouncer (" + controlBlock.getBlock().getTypeId() + ").");
			}
		}
	}

	public void intersection(Location controlBlock, VehicleUpdateEvent event, Player p) {
		Vector stop = new Vector(0, 0, 0);
		event.getVehicle().setVelocity(stop);
		allowUsage = false;
		isPunched = false;
		if (plugin.debugMode.contains(p.getName())) {
			p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart should be stopped by Intersection (" + controlBlock.getBlock().getTypeId() + ").");
		}
		p.sendMessage(ChatColor.GREEN + plugin.getLocalization(p.getName(), "cartPunch"));
	}

  	public void elevator(Location controlBlock, Location railBlock, VehicleUpdateEvent event, double x, double z, Player p, boolean isPowered, World world) {
  		if ((!plugin.getConfig().getBoolean("Redstone.elevator")) || ((plugin.getConfig().getBoolean("Redstone.elevator")) && (!isPowered))) {
  			Location currentBlock = event.getVehicle().getLocation();
  			Location currentRail = event.getVehicle().getLocation();
  			double mapHeight = world.getMaxHeight();
  			for (double i = 0.0D; i < mapHeight; i++) {
  				double block = i;
  				double rail = i + 1.0D;
  				currentBlock.setY(block);
  				currentRail.setY(rail);
  				if ((currentBlock.getBlock().getTypeId() == plugin.getConfig().getInt("BlockIDs.elevator")) && (currentRail.getBlock().getTypeId() == 66 || currentRail.getBlock().getTypeId() == 27 || currentRail.getBlock().getTypeId() == 28) && (currentBlock.getY() != controlBlock.getY())) {
  					if ((!plugin.getConfig().getBoolean("Redstone.elevator")) || ((plugin.getConfig().getBoolean("Redstone.elevator")) && ((!currentRail.getBlock().isBlockPowered()) || (!currentBlock.getBlock().isBlockPowered())))) {
  						if (x != 0.0D) {
  							currentRail.setX(Math.round(currentRail.getX()));
  							currentRail.setY(currentRail.getY() + 0.5D);
  						}
  						if (z != 0.0D) {
  							currentRail.setZ(Math.round(currentRail.getZ()));
  							currentRail.setY(currentRail.getY() + 0.5D);
  						}
  						event.getVehicle().teleport(currentRail);
  						allowUsage = false;
  						if (plugin.debugMode.contains(p.getName())) {
  							p.sendMessage(ChatColor.RED + "[Debug] " + ChatColor.WHITE + "Minecart should be teleported by Elevator (" + controlBlock.getBlock().getTypeId() + ").");
  						}
  						break;
  					}
  				}
  			}
	    }
  	}
}