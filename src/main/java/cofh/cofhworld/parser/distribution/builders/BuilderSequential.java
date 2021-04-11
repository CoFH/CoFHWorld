package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.distribution.DistributionSequential;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderSequential extends BaseBuilder<DistributionSequential> {

	protected List<IConfigurableFeatureGenerator> features;

	public void setFeatures(List<IConfigurableFeatureGenerator> features) {

		this.features = features;
	}

	@Nonnull
	@Override
	public DistributionSequential build() {

		return new DistributionSequential(featureName, features, retrogen);
	}
}
