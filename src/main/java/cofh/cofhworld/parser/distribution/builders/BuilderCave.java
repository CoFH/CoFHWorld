package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionCave;

import javax.annotation.Nonnull;

public class BuilderCave extends BuilderGenerator<DistributionCave> {

	protected boolean ceiling;
	protected INumberProvider minHeight, avgHeight, maxHeight;
	protected ICondition caveCondition;

	public void setCeiling(boolean ceiling) {

		this.ceiling = ceiling;
	}

	public void setMinLevel(INumberProvider level) {

		this.minHeight = level;
	}

	public void setAvgLevel(INumberProvider level) {

		this.avgHeight = level;
	}

	public void setMaxLevel(INumberProvider level) {

		this.maxHeight = level;
	}

	public void setCaveCondition(ICondition condition) {

		this.caveCondition = condition;
	}

	@Nonnull
	@Override
	public DistributionCave build() {

		DistributionCave cave = new DistributionCave(featureName, generator, ceiling, clusterCount, retrogen);
		if (maxHeight != null) {
			cave.setMaxLevel(new BoundedProvider(maxHeight, 0, 255));
		}
		if (avgHeight != null) {
			cave.setIntermediateLevel(new BoundedProvider(avgHeight, 0, 255));
		}
		if (minHeight != null) {
			cave.setMinLevel(new BoundedProvider(minHeight, 0, 255));
		}
		if (caveCondition != null) {
			cave.setCaveCondition(caveCondition);
		}
		return cave;
	}
}
