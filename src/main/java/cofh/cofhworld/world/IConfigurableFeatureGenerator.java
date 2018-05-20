package cofh.cofhworld.world;

import cofh.cofhworld.data.biome.BiomeInfo;
import cofh.cofhworld.data.biome.BiomeInfoSet;
import net.minecraft.world.World;

import java.util.Random;

public interface IConfigurableFeatureGenerator extends IFeatureGenerator {

	enum GenRestriction {
		NONE, BLACKLIST, WHITELIST;

		public static GenRestriction get(String restriction) {

			if (restriction.equalsIgnoreCase("blacklist")) {
				return BLACKLIST;
			}
			if (restriction.equalsIgnoreCase("whitelist")) {
				return WHITELIST;
			}
			return NONE;
		}
	}

	boolean generateFeature(Random random, int blockX, int blockZ, World world);

	IConfigurableFeatureGenerator setWithVillage(boolean inVillage);

	IConfigurableFeatureGenerator setRarity(int rarity);

	IConfigurableFeatureGenerator addBiome(BiomeInfo biome);

	IConfigurableFeatureGenerator addBiomes(BiomeInfoSet biomes);

	IConfigurableFeatureGenerator addDimension(int dimID);

	GenRestriction getBiomeRestriction();

	GenRestriction getDimensionRestriction();

}
