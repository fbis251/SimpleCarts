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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.happypikachu.SimpleCarts.listeners.SimpleCartsBlockListener;
import me.happypikachu.SimpleCarts.listeners.SimpleCartsPlayerListener;
import me.happypikachu.SimpleCarts.listeners.SimpleCartsVehicleListener;
import me.happypikachu.SimpleCarts.util.Localization;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCarts extends JavaPlugin {
    private YamlConfiguration customLocale = null;
    private File customLocaleFile = null;
	public Set<String> debugMode = new HashSet<String>();
	public HashMap<String, String> playerLang = new HashMap<String, String>();
	public Economy econ = null;
	
	@Override
	public void onEnable() {
		//Copy config.yml and update header
		saveDefaultConfig();
		getConfig().options().header("SimpleCarts v" + getDescription().getVersion() + " Configuration" + 
				"\nby HappyPikachu -aka- FlyingPikachu" + 
				"\n" + 
			    "\nIf you experience a problem with this config when starting" +
			    "\nyour server, make sure that you're using spaces and not tabs." + 
			    "\nCheck that all apostrophes are escaped. For example, \"can't\"" + 
			    "\nbecomes \"can\\'t\"." +
			    "\n" + 
			    "\nThanks to nisovin, bergerkiller, DDoS, Lex Talionis, and V10lator." + 
				"\nSpecial thanks to all the plugin testers, particularly Emericasktr1." + 
				"\n");
		getConfig().options().copyHeader(true);
		for (World world: getServer().getWorlds()) {
			getConfig().addDefault("Worlds." + world.getName(), true);
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		//Copy locale files, default first
		saveDefaultLocale("locale_enUS.yml");
		saveDefaultLocale("locale_deDE.yml");
		saveDefaultLocale("locale_esES.yml");
		saveDefaultLocale("locale_frFR.yml");
		saveDefaultLocale("locale_ruRU.yml");
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!playerLang.containsKey(p.getName())) {
				if (getConfig().getBoolean("Locale.override") & Localization.isValidCode(getConfig().getString("Locale.iso-code"))) {
					playerLang.put(p.getName(), getConfig().getString("Locale.iso-code"));
				} else {
					String language = Locale.getDefault().getLanguage();
					String country = Locale.getDefault().getCountry();
					playerLang.put(p.getName(), language + country);
				}
			}
		}
		if (setupEconomy() & getConfig().getBoolean("Economy.enable-vault")) {
            getLogger().info(getLocalization(null, "vaultEnabled"));
        } else if (setupEconomy()) {
        	getLogger().info(getLocalization(null, "vaultDisabled"));
        }
		if (Math.ceil(getConfig().getDouble("Minecart.speed-multiplier")) > 500.0D) {
			getLogger().warning(getLocalization(null, "speedHigh"));
		}
		getCommand("sc").setExecutor(new SimpleCartsCommandExecutor(this));
		getServer().getPluginManager().registerEvents(new SimpleCartsBlockListener(this), this);
		getServer().getPluginManager().registerEvents(new SimpleCartsVehicleListener(this, econ), this);
		getServer().getPluginManager().registerEvents(new SimpleCartsPlayerListener(this, econ), this);
	}
	
	@Override
	public void onDisable() {
	}
	
    public void reloadLocale(String fileName) {
        customLocaleFile = new File(getDataFolder(), fileName);
        try {
			customLocale = Localization.loadYamlUTF8(customLocaleFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public YamlConfiguration getLocale(String fileName) {
    	if (customLocaleFile == null) {
    		this.reloadLocale(fileName);
    	} else {
    		if (!customLocaleFile.getName().equals(fileName)) {
    			this.reloadLocale(fileName);
    		}
    	}
        return customLocale;
    }
    
    public void saveLocale(String fileName) {
        if (customLocale == null || customLocaleFile == null) {
        return;
        }
        try {
            getLocale(fileName).save(customLocaleFile);
        } catch (IOException ex) {
            getLogger().severe("Could not save " + fileName);
        }
    }
    
    public void saveDefaultLocale(String fileName) {
    	if (!new File(getDataFolder(), fileName).exists()) {
        	saveResource(fileName, false);
        }
    }
	
	public String getLocalization(String playerName, String key) {
		String fileName = "";
		String language = Locale.getDefault().getLanguage();
		String country = Locale.getDefault().getCountry();
		//Set locale based on player
		if (playerName != null && playerLang.containsKey(playerName)) {
			fileName = "locale_" + playerLang.get(playerName) + ".yml";
		//Set locale based on config
		} else if (getConfig().getBoolean("Locale.override")) {
			fileName = "locale_" + getConfig().getString("Locale.iso-code").replaceAll("_", "") + ".yml";
		//Set locale based on machine
		} else if (getResource("locale_" + language + country + ".yml") != null) {
			fileName = "locale_" + language + country + ".yml";
		//Default to enUS
		} else {
			fileName = "locale_enUS.yml";
		}
		try {
			return getLocale(fileName).getString(key.toUpperCase());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "An error occured! Please notify the developer.";
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}