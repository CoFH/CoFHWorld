package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.biome.BiomeInfoSet;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class DistributionSequential extends Distribution {

	private final IConfigurableFeatureGenerator[] features;

	public DistributionSequential(String name, List<IConfigurableFeatureGenerator> features, boolean regen) {

		super(name, regen);
		this.features = features.toArray(new IConfigurableFeatureGenerator[0]);
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

	public Distribution setStructureRestriction(GenRestriction restriction) {

		super.setStructureRestriction(restriction);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.setStructureRestriction(restriction);
		}
		return this;
	}

	public Distribution addStructures(String[] structures) {

		super.addStructures(structures);
		for (IConfigurableFeatureGenerator feature : features) {
			feature.addStructures(structures);
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

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {

		boolean r = false;

		for (IConfigurableFeatureGenerator feature : features) {
			r |= feature.generateFeature(random, blockX, blockZ, world);
		}
		return r;
	}

}
