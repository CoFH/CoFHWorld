package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.WorldHeightBoundProvider;
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
			cave.setMaxLevel(new WorldHeightBoundProvider(maxHeight));
		}
		if (avgHeight != null) {
			cave.setIntermediateLevel(new WorldHeightBoundProvider(avgHeight));
		}
		if (minHeight != null) {
			cave.setMinLevel(new WorldHeightBoundProvider(minHeight));
		}
		if (caveCondition != null) {
			cave.setCaveCondition(caveCondition);
		}
		return cave;
	}
}
