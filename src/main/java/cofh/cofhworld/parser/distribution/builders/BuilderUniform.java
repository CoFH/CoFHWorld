package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionUniform;

import javax.annotation.Nonnull;

public class BuilderUniform extends BuilderGenerator<DistributionUniform> {

	protected INumberProvider minHeight, maxHeight;

	public void setMinHeight(INumberProvider minHeight) {

		this.minHeight = minHeight;
	}

	public void setMaxHeight(INumberProvider maxHeight) {

		this.maxHeight = maxHeight;
	}

	@Nonnull
	@Override
	public DistributionUniform build() {

		return new DistributionUniform(featureName, generator, clusterCount, minHeight, maxHeight, retrogen);
	}
}
