package cofh.cofhworld.parser.variables;

import cofh.cofhworld.biome.BiomeInfo;
import cofh.cofhworld.biome.BiomeInfoRarity;
import cofh.cofhworld.biome.BiomeInfoSet;
import com.typesafe.config.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class BiomeData {

	public static BiomeInfoSet parseBiomeRestrictions(Config biomeEntry) {

		BiomeInfoSet set;
		ConfigValue data = biomeEntry.getValue("value");
		if (data.valueType() == ConfigValueType.LIST) {
			ConfigList restrictionList = (ConfigList) data;
			set = new BiomeInfoSet(restrictionList.size());
			for (int i = 0, e = restrictionList.size(); i < e; i++) {
				BiomeInfo info = parseBiomeEntry(restrictionList.get(i));
				if (info != null) {
					set.add(info);
				}
			}
		} else {
			set = new BiomeInfoSet(1);
			BiomeInfo info = parseBiomeEntry(data);
			if (info != null) {
				set.add(info);
			}
		}
		return set;
	}

	public static BiomeInfo parseBiomeEntry(ConfigValue biomeEntry) {

		BiomeInfo info = null;
		switch (biomeEntry.valueType()) {
			case NULL:
				log.debug("Null biome entry. Ignoring.");
				break;
			case OBJECT:
				Config obj = ((ConfigObject) biomeEntry).toConfig();
				String type = obj.getString("type");
				boolean wl = !obj.hasPath("whitelist") || obj.getBoolean("whitelist");
				ConfigValue value = obj.getValue("entry");
				List<String> array = value.valueType() == ConfigValueType.LIST ? obj.getStringList("entry") : null;
				String entry = array != null ? null : (String) value.unwrapped();
				int rarity = obj.hasPath("rarity") ? obj.getInt("rarity") : -1;

				l:
				if (type.equalsIgnoreCase("name")) {
					if (array != null) {
						List<String> names = array;
						if (rarity > 0) {
							info = new BiomeInfoRarity(names, 4, true, rarity);
						} else {
							info = new BiomeInfo(names, 4, true);
						}
					} else {
						if (rarity > 0) {
							info = new BiomeInfoRarity(entry, rarity);
						} else {
							info = new BiomeInfo(entry);
						}
					}
				} else {
					Object data;
					int t;
					if (type.equalsIgnoreCase("dictionary")) {
						if (array != null) {
							ArrayList<BiomeDictionary.Type> tags = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; k++) {
								tags.add(BiomeDictionary.Type.getType(array.get(k)));
							}
							data = tags.toArray(new BiomeDictionary.Type[tags.size()]);
							t = 6;
						} else {
							data = BiomeDictionary.Type.getType(entry);
							t = 2;
						}
					} else if (type.equalsIgnoreCase("id")) {
						if (array != null) {
							ArrayList<ResourceLocation> ids = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; ++k) {
								ids.add(new ResourceLocation(array.get(k)));
							}
							data = ids;
							t = 8;
						} else {
							data = new ResourceLocation(entry);
							t = 7;
						}
					} else if (type.equalsIgnoreCase("temperature")) {
						if (array != null) {
							ArrayList<Biome.TempCategory> temps = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; k++) {
								temps.add(Biome.TempCategory.valueOf(array.get(k)));
							}
							data = EnumSet.copyOf(temps);
							t = 5;
						} else {
							data = Biome.TempCategory.valueOf(entry);
							t = 1;
						}
					} else {
						log.warn("Biome entry of unknown type");
						break l;
					}
					if (data != null) {
						if (rarity > 0) {
							info = new BiomeInfoRarity(data, t, wl, rarity);
						} else {
							info = new BiomeInfo(data, t, wl);
						}
					}
				}
				break;
			case STRING:
				info = new BiomeInfo((String) biomeEntry.unwrapped());
				break;
			default:
				log.error("Unknown biome type in at line {}", biomeEntry.origin().lineNumber());
		}
		return info;
	}

}
