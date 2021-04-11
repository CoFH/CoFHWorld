package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionGaussian;

import javax.annotation.Nonnull;

public class BuilderGaussian extends BuilderGenerator<DistributionGaussian> {

	private static final ConstantProvider TWO = new ConstantProvider(2);

	protected INumberProvider centerHeight, spread, smoothness = TWO;

	public void setCenterHeight(INumberProvider centerHeight) {

		this.centerHeight = centerHeight;
	}

	public void setSpread(INumberProvider spread) {

		this.spread = spread;
	}

	public void setSmoothness(INumberProvider smoothness) {

		this.smoothness = smoothness;
	}

	@Nonnull
	@Override
	public DistributionGaussian build() {

		return new DistributionGaussian(featureName, generator, clusterCount, smoothness, centerHeight, spread, retrogen);
	}
}
