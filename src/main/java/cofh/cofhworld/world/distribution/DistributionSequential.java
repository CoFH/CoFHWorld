package cofh.cofhworld.world.distribution;

import cofh.cofhworld.world.IFeatureGenerator;
import net.minecraft.world.ISeedReader;

import java.util.List;
import java.util.Random;

public class DistributionSequential extends Distribution {

	private final IFeatureGenerator[] features;

	public DistributionSequential(String name, List<IFeatureGenerator> features, boolean regen) {

		super(name, regen);
		this.features = features.toArray(new IFeatureGenerator[0]);
	}

	@Override
	public boolean generateFeature(Random random, int chunkX, int chunkZ, ISeedReader world, boolean newGen) {

		boolean r = false;
		if (super.generateFeature(random, chunkX, chunkZ, world, newGen)) {
			for (IFeatureGenerator feature : features) {
				r |= feature.generateFeature(random, chunkX, chunkZ, world, newGen);
			}
		}
		return r;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world) {

		return true;
	}

}
