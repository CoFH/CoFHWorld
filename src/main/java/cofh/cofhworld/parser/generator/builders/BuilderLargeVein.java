package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.world.generator.WorldGenLargeVein;

public class BuilderLargeVein extends BuilderSize<WorldGenLargeVein> {

	private ICondition sparse = ConstantCondition.TRUE, spindly = ConstantCondition.FALSE;

	public void setSparse(ICondition sparse) {

		this.sparse = sparse;
	}

	public void setSpindly(ICondition spindly) {

		this.spindly = spindly;
	}

	@Override
	public WorldGenLargeVein build() {

		return new WorldGenLargeVein(resource, size, material, sparse, spindly);
	}
}
