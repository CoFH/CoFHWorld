package cofh.cofhworld.parser.distribution.base;

import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Blocks;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractStoneDistParser extends AbstractDistParser {

	@Override
	protected List<WeightedBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedBlock(Blocks.STONE));
	}

}
