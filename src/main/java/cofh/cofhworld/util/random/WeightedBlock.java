package cofh.cofhworld.util.random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.WeightedRandom;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class essentially allows for ores to be generated in clusters, with Features randomly choosing one or more blocks from a weighted list.
 *
 * @author King Lemming
 */
public final class WeightedBlock extends WeightedRandom.Item {

	public static final WeightedBlock AIR = new WeightedBlock(Blocks.AIR);

	// TODO: wildcard?
	public final Block block;
	public final BlockState state;
	private final List<WeightedNBTTag> data;

	public WeightedBlock(Block ore) {

		this(100, ore, null);
	}

	public WeightedBlock(BlockState ore) {

		this(100, ore, null);
	}


	public WeightedBlock(int weight, Block ore, List<WeightedNBTTag> data) {

		super(weight);
		this.block = ore;
		this.state = null;
		this.data = data;
	}

	public WeightedBlock(int weight, BlockState ore, List<WeightedNBTTag> data) {

		super(weight);
		this.block = ore.getBlock();
		this.state = ore;
		this.data = data;
	}

	public BlockState getState() {

		return state == null ? block.getDefaultState() : state;
	}

	public CompoundNBT getData(Random rand, CompoundNBT source) {

		CompoundNBT data = getData(rand);
		if (data != null) {
			data.remove("x");
			data.remove("y");
			data.remove("z");
			source.merge(data);
		}
		return source;
	}

	@Nullable
	private CompoundNBT getData(Random rand) {

		if (data == null) {
			return null;
		}
		int size = data.size();
		if (size == 0) {
			return null;
		}
		if (size > 1) {
			return WeightedRandom.getRandomItem(rand, data).getCompoundTag();
		}
		return data.get(0).getCompoundTag();
	}

}
