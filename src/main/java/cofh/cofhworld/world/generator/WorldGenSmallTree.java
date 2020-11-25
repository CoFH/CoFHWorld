package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSmallTree extends WorldGen {

	private final List<WeightedBlock> leaves;
	private final List<WeightedBlock> trunk;
	private final Material[] material;
	private final Material[] surface;
	private final INumberProvider height;

	private final ICondition treeChecks;
	private final ICondition waterLoving; // TODO: more work on this logic
	private final ICondition relaxedGrowth;

	private final ICondition leafVariance;

	public WorldGenSmallTree(List<WeightedBlock> resource, List<WeightedBlock> leaf, List<Material> materials, Material[] surface,
			INumberProvider height, ICondition treeChecks, ICondition waterLoving, ICondition relaxedGrowth, ICondition leafVariance) {

		leaves = leaf;
		trunk = resource;
		material = materials.toArray(new Material[0]);
		this.surface = surface;
		this.height = height;
		this.treeChecks = treeChecks;
		this.waterLoving = waterLoving;
		this.relaxedGrowth = relaxedGrowth;
		this.leafVariance = leafVariance;
	}

	protected int getLeafRadius(int height, int level, Boolean check) {

		if (check != null) {
			if (level >= 1 + height - 2) {
				return 2;
			} else {
				return check == Boolean.TRUE ? 0 : 1;
			}
		}

		if (level >= 1 + height - 4) {
			return 1 - ((level - height) / 2);
		} else {
			return 0;
		}
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		final int x = data.getPosition().getX();
		final int y = data.getPosition().getY();
		final int z = data.getPosition().getZ();

		final Boolean checkValue = relaxedGrowth.checkCondition(world, rand, data) ? Boolean.TRUE : Boolean.FALSE;
		final boolean waterLoving = this.waterLoving.checkCondition(world, rand, data.setValue("relaxed-growth", checkValue));

		final int treeHeight = height.intValue(world, rand, data.setValue("water-loving", waterLoving));
		final int worldHeight = world.getHeight();
		data.setValue("height", treeHeight);

		BlockState state;
		BlockPos offsetPos;
		int xOffset;
		int yOffset;
		int zOffset;

		if (y >= worldHeight - treeHeight - 1 || !canGenerateInBlock(world, x, y - 1, z, surface)) {
			return false;
		}

		if (treeChecks.checkCondition(world, rand, data)) {
			data.setValue("performing-check", true);

			for (yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {

				int radius = getLeafRadius(treeHeight, yOffset - y, checkValue);

				if (y < treeHeight) {
					offsetPos = new BlockPos(x, yOffset, z);
					state = getBlockState(world, x, yOffset, z);
					if (!(state.getBlock().isIn(BlockTags.LEAVES) || state.getBlock().isAir(state, world, offsetPos) || state.getMaterial().isReplaceable() ||
							state.getBlock().canBeReplacedByLogs(state, world, offsetPos) || canGenerateInBlock(world, offsetPos, material))) {
						return false;
					}
				}

				if (radius == 0) {
					if (!waterLoving && yOffset >= y + 1) {
						radius = 1;
						for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
							for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
								state = getBlockState(world, xOffset, yOffset, zOffset);

								if (state.getMaterial().isLiquid()) {
									return false;
								}
							}
						}
					}
				} else {
					for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
						for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
							offsetPos = new BlockPos(xOffset, yOffset, zOffset);
							state = getBlockState(world, xOffset, yOffset, zOffset);

							if (!(state.getBlock().isIn(BlockTags.LEAVES) || state.getBlock().isAir(state, world, offsetPos) ||
									state.getBlock().canBeReplacedByLeaves(state, world, offsetPos) || canGenerateInBlock(world, offsetPos, material))) {
								return false;
							}
						}
					}
				}
			}

			if (y < 1 || !canGenerateInBlock(world, x, y - 1, z, surface)) { // may have triggered other generation (TODO: validate for 1.15+)
				return false;
			}

			offsetPos = new BlockPos(x, y - 1, z);
			state = getBlockState(world, x, y - 1, z);
			state.getBlock().onPlantGrow(state, world, offsetPos, new BlockPos(x, y, z));
			data.removeValue("performing-check");
		} else {
			data.setValue("performing-check", true);

			for (yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {

				int radius = getLeafRadius(treeHeight, yOffset - y, checkValue);

				if (y < treeHeight) {
					if (!(canGenerateInBlock(world, x, yOffset, z, material))) {
						return false;
					}
				}

				if (radius == 0) {
					if (!waterLoving && yOffset >= y + 1) {
						radius = 1;
						for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
							for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
								state = getBlockState(world, xOffset, yOffset, zOffset);

								if (state.getMaterial().isLiquid()) {
									return false;
								}
							}
						}
					}
				} else {
					for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
						for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
							if (!(canGenerateInBlock(world, xOffset, yOffset, zOffset, material))) {
								return false;
							}
						}
					}
				}
			}

			if (!canGenerateInBlock(world, x, y - 1, z, surface)) { // may have triggered other generation (TODO: validate for 1.15+)
				return false;
			}
			data.removeValue("performing-check");
		}

		boolean r = false;

		for (yOffset = y; yOffset <= y + treeHeight; ++yOffset) {
			data.setValue("layer", yOffset - y);

			final int radius = getLeafRadius(treeHeight, yOffset - y, null);
			if (radius <= 0) {
				continue;
			}
			data.setValue("radius", radius);

			for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
				data.setValue("layer-x", xOffset - x);

				for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {

					if (leafVariance.checkCondition(world, rand, data.setValue("layer-z", zOffset - z))) {
						r |= generateBlock(world, rand, xOffset, yOffset, zOffset, leaves); // area already validated
					}
				}
			}
		}

		for (yOffset = 0; yOffset < treeHeight; ++yOffset) {
			r |= generateBlock(world, rand, x, yOffset + y, z, trunk); // area already validated, always replace leaves
		}

		return r;
	}

}
