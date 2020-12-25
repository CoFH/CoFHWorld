package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenGeode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BuilderGeode extends BaseBuilder<WorldGenGeode> {

	private static final List<WeightedBlock> FILLER = Collections.singletonList(WeightedBlock.AIR_NORM);

	private List<WeightedBlock> outline;
	private List<WeightedBlock> filler = FILLER;
	private ICondition hollow = ConstantCondition.FALSE;

	public void setOutline(List<WeightedBlock> outline) {

		this.outline = outline;
	}

	public void setFiller(List<WeightedBlock> filler) {

		if (filler.size() > 0) this.filler = filler;
	}

	public void setHollow(ICondition hollow) {

		this.hollow = hollow;
	}

	@Nonnull
	@Override
	public WorldGenGeode build() {

		return new WorldGenGeode(resource, material, outline, filler, hollow);
	}
}
