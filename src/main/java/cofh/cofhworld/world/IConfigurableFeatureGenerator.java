package cofh.cofhworld.world;

import cofh.cofhworld.data.biome.BiomeInfoSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ISeedReader;

import java.util.Random;

public interface IConfigurableFeatureGenerator extends IFeatureGenerator {

	enum GenRestriction {
		NONE, BLACKLIST, WHITELIST;

		public static GenRestriction get(String restriction) {

			if ("blacklist".equalsIgnoreCase(restriction)) {
				return BLACKLIST;
			}
			if ("whitelist".equalsIgnoreCase(restriction)) {
				return WHITELIST;
			}
			return NONE;
		}
	}

	boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world);

	IConfigurableFeatureGenerator setRarity(int rarity);

	IConfigurableFeatureGenerator setStructureRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addStructures(ResourceLocation[] structures);

	IConfigurableFeatureGenerator setBiomeRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addBiomes(BiomeInfoSet biomes);

	IConfigurableFeatureGenerator setDimensionRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addDimension(ResourceLocation dimID);

}
