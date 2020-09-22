package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all ints with INumberProvider
 */
@Deprecated
public class WorldGenSmallTree extends WorldGen {

	private final List<WeightedBlock> leaves;
	private final List<WeightedBlock> trunk;
	private final Material[] material;
	public Material[] genSurface = null;

	public int minHeight = 5;
	public int heightVariance = 3;
	public boolean treeChecks = true;
	public boolean leafVariance = true;
	public boolean relaxedGrowth = false;
	public boolean waterLoving = false;

	public WorldGenSmallTree(List<WeightedBlock> resource, List<WeightedBlock> leaf, List<Material> materials) {

		leaves = leaf;
		trunk = resource;
		material = materials.toArray(new Material[0]);
	}

	protected int getLeafRadius(int height, int level, boolean check) {

		if (check) {
			if (level >= 1 + height - 2) {
				return 2;
			} else {
				return relaxedGrowth ? 0 : 1;
			}
		}

		if (level >= 1 + height - 4) {
			return 1 - ((level - height) / 2);
		} else {
			return 0;
		}
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int treeHeight = (heightVariance <= 1 ? 0 : rand.nextInt(heightVariance)) + minHeight;
		int worldHeight = world.getHeight();
		BlockState state;
		BlockPos offsetPos;

		if (y + treeHeight + 1 <= worldHeight) {
			int xOffset;
			int yOffset;
			int zOffset;

			if (!canGenerateInBlock(world, x, y - 1, z, genSurface)) {
				return false;
			}

			if (y < worldHeight - treeHeight - 1) {
				if (treeChecks) {
					for (yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {

						int radius = getLeafRadius(treeHeight, yOffset - y, true);

						if (yOffset >= 0 & yOffset < worldHeight) {
							if (radius == 0) {
								offsetPos = new BlockPos(x, yOffset, z);
								state = world.getBlockState(offsetPos);
								if (!(state.getBlock().isIn(BlockTags.LEAVES) || state.getBlock().isAir(state, world, offsetPos) || state.getMaterial().isReplaceable() || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos) || canGenerateInBlock(world, offsetPos,
										material))) {
									return false;
								}

								if (!waterLoving && yOffset >= y + 1) {
									radius = 1;
									for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
										for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
											offsetPos = new BlockPos(xOffset, yOffset, zOffset);
											state = world.getBlockState(offsetPos);

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
										state = world.getBlockState(offsetPos);

										if (!(state.getBlock().isIn(BlockTags.LEAVES) || state.getBlock().isAir(state, world, offsetPos) || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos) || canGenerateInBlock(world, offsetPos,
												material))) {
											return false;
										}
									}
								}
							}
						} else {
							return false;
						}
					}

					if (!canGenerateInBlock(world, x, y - 1, z, genSurface)) {
						return false;
					}
					offsetPos = new BlockPos(x, y - 1, z);
					state = world.getBlockState(offsetPos);
					state.getBlock().onPlantGrow(state, world, offsetPos, new BlockPos(x, y, z));
				}

				boolean r = false;

				for (yOffset = y; yOffset <= y + treeHeight; ++yOffset) {

					int var12 = yOffset - (y + treeHeight);
					int radius = getLeafRadius(treeHeight, yOffset - y, false);
					if (radius <= 0) {
						continue;
					}

					for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
						int xPos = xOffset - x, t;
						xPos = (xPos + (t = xPos >> 31)) ^ t;

						for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
							int zPos = zOffset - z;
							zPos = (zPos + (t = zPos >> 31)) ^ t;
							offsetPos = new BlockPos(xOffset, yOffset, zOffset);
							state = world.getBlockState(offsetPos);

							if (((xPos != radius | zPos != radius) || (!leafVariance || (rand.nextInt(2) != 0 && var12 != 0))) && ((treeChecks && (state.getBlock().isIn(BlockTags.LEAVES) || state.getBlock().isAir(state, world, offsetPos) || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos))) || canGenerateInBlock(world, offsetPos,
									material))) {
								r |= generateBlock(world, rand, xOffset, yOffset, zOffset, leaves);
							}
						}
					}
				}

				for (yOffset = 0; yOffset < treeHeight; ++yOffset) {
					offsetPos = new BlockPos(x, y + yOffset, z);
					state = world.getBlockState(offsetPos);

					if ((treeChecks && (state.getBlock().isAir(state, world, offsetPos) || state.getBlock().isIn(BlockTags.LEAVES) || state.getMaterial().isReplaceable())) || canGenerateInBlock(world, offsetPos,
							material)) {
						r |= generateBlock(world, rand, x, yOffset + y, z, trunk);
					}
				}

				return r;
			}
		}
		return false;
	}

}
