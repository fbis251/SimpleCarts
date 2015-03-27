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
package me.happypikachu.SimpleCarts.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Localization {
	
	public static String getTranslator(String code) {
		if (code.replaceAll("_", "").equalsIgnoreCase("deDE")) {
			return "KJA1997";
		} else if (code.replaceAll("_", "").equalsIgnoreCase("enUS")) {
			return "HappyPikachu";
		} else if (code.replaceAll("_", "").equalsIgnoreCase("esES")) {
			return "MrCayke";
		} else if (code.replaceAll("_", "").equalsIgnoreCase("frFR")) {
			return "Warzouz";
        } else if (code.replaceAll("_", "").equalsIgnoreCase("ruRU")) {
            return "fablerion";
        } else if (code.replaceAll("_", "").equalsIgnoreCase("plPL")) {
            return "Bartolus99";
		}
		return "Missingno";
	}
	
	public static boolean isValidCode(String code) {
		if (code.replaceAll("_", "").equalsIgnoreCase("deDE")
				|| code.replaceAll("_", "").equalsIgnoreCase("enUS")
				|| code.replaceAll("_", "").equalsIgnoreCase("esES")
                || code.replaceAll("_", "").equalsIgnoreCase("frFR") 
                || code.replaceAll("_", "").equalsIgnoreCase("plPL")
				|| code.replaceAll("_", "").equalsIgnoreCase("ruRU")) {
			return true;
		}
		return false;
	}
	
	public static YamlConfiguration loadYamlUTF8(File file) throws InvalidConfigurationException, IOException {
		StringBuilder sb = new StringBuilder((int) file.length());
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		char[] buf = new char[1024];
		int l;
		while ((l = in.read(buf, 0, buf.length)) > -1) {
			sb = sb.append(buf, 0, l);
		}
		in.close();
		
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.loadFromString(sb.toString());
		
		return yaml;
	}
}