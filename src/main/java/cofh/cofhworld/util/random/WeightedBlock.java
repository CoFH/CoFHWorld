package cofh.cofhworld.util.random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

import java.util.Collection;

/**
 * This class essentially allows for ores to be generated in clusters, with Features randomly choosing one or more blocks from a weighted list.
 *
 * @author King Lemming
 */
public final class WeightedBlock extends WeightedRandom.Item {

	public final Block block;
	public final int metadata;
	public final IBlockState state;

	public WeightedBlock(ItemStack ore) {

		this(ore, 100);
	}

	public WeightedBlock(ItemStack ore, int weight) {

		this(Block.getBlockFromItem(ore.getItem()), ore.getItemDamage(), weight);
	}

	public WeightedBlock(Block ore) {

		this(ore, 0, 100); // some blocks do not have associated items
	}

	public WeightedBlock(Block ore, int metadata) {

		this(ore, metadata, 100);
	}

	public WeightedBlock(Block ore, int metadata, int weight) {

		super(weight);
		this.block = ore;
		this.metadata = metadata;
		this.state = null;
	}

	public WeightedBlock(IBlockState ore, int weight) {

		super(weight);
		this.block = ore.getBlock();
		this.metadata = block.getMetaFromState(ore);
		this.state = ore;
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

}
