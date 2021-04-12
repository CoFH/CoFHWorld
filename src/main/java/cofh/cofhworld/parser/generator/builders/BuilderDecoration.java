package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.world.generator.WorldGenDecoration;

import java.util.List;

public class BuilderDecoration extends BuilderSize<WorldGenDecoration> {

	private static final ICondition SEE_SKY = WorldValueCondition.CAN_SEE_SKY, CHECK_STAY = WorldValueCondition.BLOCK_CAN_PLACE;
	private static final INumberProvider ONE = new ConstantProvider(1);

	private List<Material> surface = null;

	private ICondition seeSky = SEE_SKY;
	private ICondition checkStay = CHECK_STAY;

	private INumberProvider stackHeight = ONE;

	public void setSurface(List<Material> surface) {

		this.surface = surface;
	}

	public void setSeeSky(ICondition seeSky) {

		this.seeSky = seeSky;
	}

	public void setCheckStay(ICondition checkStay) {

		this.checkStay = checkStay;
	}

	public void setStackHeight(INumberProvider stackHeight) {

		this.stackHeight = stackHeight;
	}

	@Override
	public WorldGenDecoration build() {

		return new WorldGenDecoration(resource, size, material, surface, seeSky, checkStay, stackHeight);
	}
}
