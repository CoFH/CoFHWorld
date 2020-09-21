package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.biome.BiomeInfo;
import cofh.cofhworld.data.biome.BiomeInfoSet;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class DistributionSequential extends Distribution {

	private final IConfigurableFeatureGenerator[] features;

	public DistributionSequential(String name, List<IConfigurableFeatureGenerator> features, boolean regen) {

		super(name, regen);
		this.features = features.toArray(new IConfigurableFeatureGenerator[features.size()]);
	}

	public Distribution setBiomeRestriction(GenRestriction restriction) {

		super.setBiomeRestriction(restriction);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.setBiomeRestriction(restriction);
		}
		return this;
	}

	public Distribution setDimensionRestriction(GenRestriction restriction) {

		super.setDimensionRestriction(restriction);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.setDimensionRestriction(restriction);
		}
		return this;
	}

	public Distribution addBiome(BiomeInfo biome) {

		super.addBiome(biome);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.addBiome(biome);
		}
		return this;
	}

	public Distribution addBiomes(BiomeInfoSet biomes) {

		super.addBiomes(biomes);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.addBiomes(biomes);
		}
		return this;
	}

	public Distribution setWithVillage(boolean inVillage) {

		super.setWithVillage(inVillage);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.setWithVillage(inVillage);
		}
		return this;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {

		boolean r = false;

		for (IConfigurableFeatureGenerator feature : features) {
			r |= feature.generateFeature(random, blockX, blockZ, world);
		}
		return false;
	}

}
