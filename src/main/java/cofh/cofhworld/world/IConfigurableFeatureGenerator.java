package cofh.cofhworld.world;

import cofh.cofhworld.data.biome.BiomeInfoSet;
import net.minecraft.world.IWorld;

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

	boolean generateFeature(Random random, int blockX, int blockZ, IWorld world);

	IConfigurableFeatureGenerator setRarity(int rarity);

	IConfigurableFeatureGenerator setStructureRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addStructures(String[] structures);

	IConfigurableFeatureGenerator setBiomeRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addBiomes(BiomeInfoSet biomes);

	IConfigurableFeatureGenerator setDimensionRestriction(GenRestriction restriction);

	IConfigurableFeatureGenerator addDimension(int dimID);

}
