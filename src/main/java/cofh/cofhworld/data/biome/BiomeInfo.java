package cofh.cofhworld.data.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
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
		type = Type.RegistryName;
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
				case Category:
					String name = biome.getCategory().getName();
					r = name.hashCode() == hash && name.equals(data);
					break;
				case CategoryList:
					r = ((Collection<String>) data).contains(biome.getCategory().getName());
					break;
//				case TemperatureCategory:
//					r = biome.getTempCategory() == data;
//					break;
//				case TemperatureCategoryList:
//					r = ((Collection<TempCategory>) data).contains(biome.getTempCategory());
//					break;
				case DictionaryType:
					r = BiomeDictionary.hasType(WorldGenRegistries.BIOME.getOptionalKey(biome).get(), (BiomeDictionary.Type) data);
					break;
				case DictionaryTypeList:
					BiomeDictionary.Type[] d = (BiomeDictionary.Type[]) data;
					int c = 0, e = d.length;
					for (int i = 0; i < e; ++i) {
						if (BiomeDictionary.hasType(WorldGenRegistries.BIOME.getOptionalKey(biome).get(), d[i])) {
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
		Category,
		CategoryList,
		TemperatureCategory,
		TemperatureCategoryList,
		DictionaryType,
		DictionaryTypeList,
		RegistryName,
		RegistryNameList,
	}

}
