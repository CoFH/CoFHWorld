package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.MaterialPropertyMaterial;
import cofh.cofhworld.data.block.TagMaterial;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.random.RandomCondition;
import cofh.cofhworld.data.condition.world.MaterialCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenAdvLakes;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BuilderAdvLake extends BaseBuilder<WorldGenAdvLakes> {

	private static final List<WeightedBlock> GAP_BLOCK = Collections.singletonList(WeightedBlock.AIR_NORM);
	private static final ICondition OUTLINE = new BinaryCondition(
			new BinaryCondition(
					new ComparisonCondition(
							new DataProvider("layer"),
							new DataProvider("fill-height"),
							"LESS_THAN"),
					new RandomCondition(),
					"AND"),
			new MaterialCondition(Collections.singletonList(new MaterialPropertyMaterial(true, "SOLID"))),
			"AND");
	private static final ICondition RESURFACE_CONDITION = new BinaryCondition(
			new MaterialCondition(Collections.singletonList(TagMaterial.of(Collections.singleton(Tags.Blocks.DIRT.getName()), true))),
			new WorldValueCondition("IS_BLOCK_LIT_FROM_SKY"),
			"AND");
	private static final ICondition GAP_CONDITION = new MaterialCondition(Collections.singletonList(new MaterialPropertyMaterial(false, "LIQUID")));

	private ICondition gapCondition = GAP_CONDITION;
	private List<WeightedBlock> filler = GAP_BLOCK;

	private ICondition resurfaceCondition = RESURFACE_CONDITION;

	private List<WeightedBlock> outline = null;
	private ICondition outlineCondition = OUTLINE;

	public void setGapCondition(ICondition gapCondition) {

		this.gapCondition = gapCondition;
	}

	public void setFiller(List<WeightedBlock> blocks) {

		if (blocks.size() > 0) this.filler = blocks;
	}

	public void setResurfaceCondition(ICondition resurfaceCondition) {

		this.resurfaceCondition = resurfaceCondition;
	}

	public void setOutlineCondition(ICondition outline) {

		this.outlineCondition = outline;
	}

	public void setOutline(List<WeightedBlock> blocks) {

		if (blocks.size() > 0) this.outline = blocks;
	}

	@Nonnull
	@Override
	public WorldGenAdvLakes build() {

		return new WorldGenAdvLakes(resource, material, gapCondition, filler, resurfaceCondition, outline, outlineCondition);
	}
}
