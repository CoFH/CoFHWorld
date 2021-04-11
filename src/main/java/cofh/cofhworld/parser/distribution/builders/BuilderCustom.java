package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionCustom;

import javax.annotation.Nonnull;

public class BuilderCustom extends BuilderGenerator<DistributionCustom> {

	protected INumberProvider xOffset, yOffset, zOffset;

	public void setxOffset(INumberProvider xOffset) {

		this.xOffset = xOffset;
	}

	public void setyOffset(INumberProvider yOffset) {

		this.yOffset = new BoundedProvider(yOffset, 0, 255);
	}

	public void setzOffset(INumberProvider zOffset) {

		this.zOffset = zOffset;
	}

	@Nonnull
	@Override
	public DistributionCustom build() {

		return new DistributionCustom(featureName, generator, clusterCount, retrogen, xOffset, yOffset, zOffset);
	}
}
