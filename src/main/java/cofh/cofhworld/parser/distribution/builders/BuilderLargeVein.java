package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionLargeVein;

import javax.annotation.Nonnull;

public class BuilderLargeVein extends BuilderGenerator<DistributionLargeVein> {

	protected INumberProvider minY, height, diameter, verticalDensity, horizontalDensity;

	public void setMinY(INumberProvider minY) {

		this.minY = minY;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setDiameter(INumberProvider diameter) {

		this.diameter = diameter;
	}

	public void setVerticalDensity(INumberProvider verticalDensity) {

		this.verticalDensity = new BoundedProvider(verticalDensity, 0, 100);
	}

	public void setHorizontalDensity(INumberProvider horizontalDensity) {

		this.horizontalDensity = new BoundedProvider(horizontalDensity, 0, 100);
	}

	@Nonnull
	@Override
	public DistributionLargeVein build() {

		return new DistributionLargeVein(featureName, generator, clusterCount, minY, retrogen, height, diameter, verticalDensity, horizontalDensity);
	}
}
