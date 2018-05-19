package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.util.WeightedRandomBlock;
import net.minecraft.init.Blocks;

import java.util.Collections;
import java.util.List;

public class DistParserDecoration extends DistParserSurface {

	@Override
	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Collections.singletonList(new WeightedRandomBlock(Blocks.GRASS, -1));
	}

	@Override
	protected String getDefaultGenerator() {

		return "decoration";
	}

}
