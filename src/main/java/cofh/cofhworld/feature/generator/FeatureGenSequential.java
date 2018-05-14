package cofh.cofhworld.feature.generator;

import cofh.cofhworld.biome.BiomeInfo;
import cofh.cofhworld.biome.BiomeInfoSet;
import cofh.cofhworld.feature.IConfigurableFeatureGenerator;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class FeatureGenSequential extends FeatureBase {

	private final IConfigurableFeatureGenerator[] features;

	public FeatureGenSequential(String name, List<IConfigurableFeatureGenerator> features, GenRestriction biomeRes, boolean regen, GenRestriction dimRes) {

		super(name, biomeRes, regen, dimRes);
		this.features = features.toArray(new IConfigurableFeatureGenerator[features.size()]);
	}

	public FeatureBase addBiome(BiomeInfo biome) {

		super.addBiome(biome);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.addBiome(biome);
		}
		return this;
	}

	public FeatureBase addBiomes(BiomeInfoSet biomes) {

		super.addBiomes(biomes);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.addBiomes(biomes);
		}
		return this;
	}

	public FeatureBase setWithVillage(boolean inVillage) {

		super.setWithVillage(inVillage);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.setWithVillage(inVillage);
		}
		return this;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		boolean r = false;

		for (IConfigurableFeatureGenerator feature : features) {
			r |= feature.generateFeature(random, blockX, blockZ, world);
		}
		return false;
	}

}
