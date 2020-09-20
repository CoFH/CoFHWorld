package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.List;

public class DistParserDecoration extends DistParserSurface {

	@Override
	protected List<WeightedBlock> generateDefaultMaterial() {

		return Collections.singletonList(new WeightedBlock(Blocks.GRASS));
	}

	@Override
	protected String getDefaultGenerator() {

		return "decoration";
	}

}
