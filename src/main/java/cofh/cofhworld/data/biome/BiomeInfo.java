package cofh.cofhworld.data.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Collection;
import java.util.Random;

public class BiomeInfo {

	private final Object data;
	private final boolean whitelist;
	private final Type type;
	private final int hash;

	public BiomeInfo(String name) {

		data = name;
		hash = name.hashCode();
		whitelist = true;
		type = Type.BiomeName;
	}

	public BiomeInfo(Object d, Type t, boolean wl) {

		data = d;
		hash = 0;
		whitelist = wl;
		type = t;
	}

	@SuppressWarnings ("unchecked")
	public boolean isBiomeEqual(Biome biome, Random rand) {

		boolean r = false;
		if (biome != null) {
			switch (type) {
				default:
					break;
				case BiomeName:
					String name = biome.getRegistryName().toString();
					r = name.hashCode() == hash && name.equals(data);
					break;
				case BiomeNameList:
					r = ((Collection<String>) data).contains(biome.getRegistryName().toString());
					break;
				case TemperatureCategory:
					r = biome.getTempCategory() == data;
					break;
				case TemperatureCategoryList:
					r = ((Collection<TempCategory>) data).contains(biome.getTempCategory());
					break;
				case DictionaryType:
					r = BiomeDictionary.hasType(biome, (BiomeDictionary.Type) data);
					break;
				case DictionaryTypeList:
					BiomeDictionary.Type[] d = (BiomeDictionary.Type[]) data;
					int c = 0, e = d.length;
					for (int i = 0; i < e; ++i) {
						if (BiomeDictionary.hasType(biome, d[i])) {
							++c;
						}
					}
					r = c == e;
					break;
				case RegistryName:
					ResourceLocation registry = biome.getRegistryName();
					r = registry.hashCode() == hash && registry.equals(data);
					break;
				case RegistryNameList:
					r = ((Collection<ResourceLocation>) data).contains(biome.getRegistryName());
					break;
			}
		}
		return r == whitelist;
	}

	public static enum Type {
		BiomeName,
		BiomeNameList,
		TemperatureCategory,
		TemperatureCategoryList,
		DictionaryType,
		DictionaryTypeList,
		RegistryName,
		RegistryNameList,
	}

}
