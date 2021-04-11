package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionCave;

import javax.annotation.Nonnull;

public class BuilderCave extends BuilderGenerator<DistributionCave> {

	protected boolean ceiling;
	protected INumberProvider groundLevel;

	public void setCeiling(boolean ceiling) {

		this.ceiling = ceiling;
	}

	public void setGroundLevel(INumberProvider groundLevel) {

		this.groundLevel = groundLevel;
	}

	@Nonnull
	@Override
	public DistributionCave build() {

		DistributionCave cave = new DistributionCave(featureName, generator, ceiling, clusterCount, retrogen);
		if (groundLevel != null) {
			cave.setGroundLevel(new BoundedProvider(groundLevel, 0, 255));
		}
		return cave;
	}
}
