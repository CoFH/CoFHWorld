package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.world.IFeatureGenerator;
import cofh.cofhworld.world.distribution.DistributionSequential;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderSequential extends BaseBuilder<DistributionSequential> {

	protected List<IFeatureGenerator> features;

	public void setFeatures(List<IFeatureGenerator> features) {

		this.features = features;
	}

	@Nonnull
	@Override
	public DistributionSequential build() {

		return new DistributionSequential(featureName, features, retrogen);
	}
}
