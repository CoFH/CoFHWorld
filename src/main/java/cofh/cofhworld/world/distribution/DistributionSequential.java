package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.biome.BiomeInfo;
import cofh.cofhworld.data.biome.BiomeInfoSet;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class DistributionSequential extends Distribution {

	private final IConfigurableFeatureGenerator[] features;

	public DistributionSequential(String name, List<IConfigurableFeatureGenerator> features, GenRestriction biomeRes, boolean regen, GenRestriction dimRes) {

		super(name, biomeRes, regen, dimRes);
		this.features = features.toArray(new IConfigurableFeatureGenerator[features.size()]);
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
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		boolean r = false;

		for (IConfigurableFeatureGenerator feature : features) {
			r |= feature.generateFeature(random, blockX, blockZ, world);
		}
		return false;
	}

}
