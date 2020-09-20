package cofh.cofhworld.util.random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * This class essentially allows for ores to be generated in clusters, with Features randomly choosing one or more blocks from a weighted list.
 *
 * @author King Lemming
 */
public final class WeightedBlock extends WeightedRandom.Item {

	public static final WeightedBlock AIR = new WeightedBlock(Blocks.AIR);

	public final Block block;
	public final int metadata;
	public final IBlockState state;
	private final List<WeightedNBTTag> data;

	public WeightedBlock(ItemStack ore) {

		this(ore, 100);
	}

	public WeightedBlock(ItemStack ore, int weight) {

		this(Block.getBlockFromItem(ore.getItem()), ore.getItemDamage(), null, weight);
	}

	public WeightedBlock(Block ore) {

		this(ore, 0, null, 100); // some blocks do not have associated items
	}

	public WeightedBlock(Block ore, int metadata) {

		this(ore, metadata, null, 100);
	}

	public WeightedBlock(Block ore, int metadata, List<WeightedNBTTag> data, int weight) {

		super(weight);
		this.block = ore;
		this.metadata = metadata;
		this.state = null;
		this.data = data;
	}

	public WeightedBlock(IBlockState ore, List<WeightedNBTTag> data, int weight) {

		super(weight);
		this.block = ore.getBlock();
		this.metadata = block.getMetaFromState(ore);
		this.state = ore;
		this.data = data;
	}

	public static boolean isBlockContained(Block block, int metadata, Collection<WeightedBlock> list) {

		for (WeightedBlock rb : list) {
			if (block.equals(rb.block) && (metadata == -1 || rb.metadata == -1 || rb.metadata == metadata)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBlockContained(Block block, int metadata, WeightedBlock[] list) {

		for (WeightedBlock rb : list) {
			if (block.equals(rb.block) && (metadata == -1 || rb.metadata == -1 || rb.metadata == metadata)) {
				return true;
			}
		}
		return false;
	}

	public IBlockState getState() {

		return state == null ? block.getStateFromMeta(metadata) : state;
	}

	public NBTTagCompound getData(Random rand, NBTTagCompound source) {

		NBTTagCompound data = getData(rand);
		if (data != null) {
			data.removeTag("x");
			data.removeTag("y");
			data.removeTag("z");
			source.merge(data);
		}
		return source;
	}

	@Nullable
	private NBTTagCompound getData(Random rand) {

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
