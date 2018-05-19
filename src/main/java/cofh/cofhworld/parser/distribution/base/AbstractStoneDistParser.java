package cofh.cofhworld.parser.distribution.base;

import cofh.cofhworld.util.WeightedRandomBlock;
import net.minecraft.init.Blocks;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractStoneDistParser extends AbstractDistParser {

	@Override
	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

}
