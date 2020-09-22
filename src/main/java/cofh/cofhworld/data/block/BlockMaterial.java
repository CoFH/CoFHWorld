package cofh.cofhworld.data.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class BlockMaterial extends Material {

	final private Block block;
	final protected boolean inclusive;

	public BlockMaterial(Block block, boolean inclusive) {

		this.block = block;
		this.inclusive = inclusive;
	}

	@Override
	public boolean test(BlockState blockState) {

		return blockState.getBlock() == block.delegate.get() == inclusive;
	}
}
