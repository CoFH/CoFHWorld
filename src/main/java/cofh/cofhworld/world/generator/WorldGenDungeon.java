package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

public class WorldGenDungeon extends WorldGen {

	private final WeightedBlock[] material;
	private final List<WeightedBlock> spawners;
	private final List<WeightedBlock> walls;
	public List<WeightedBlock> chests;
	public List<WeightedBlock> floor;
	public int minWidthX = 2, maxWidthX = 3;
	public int minWidthZ = 2, maxWidthZ = 3;
	public int minHeight = 3, maxHeight = 3;
	public int minHoles = 1, maxHoles = 5;
	public int maxChests = 2, maxChestTries = 3;

	public WorldGenDungeon(List<WeightedBlock> blocks, List<WeightedBlock> material, List<WeightedBlock> spawners) {

		this.material = material.toArray(new WeightedBlock[0]);
		walls = blocks;
		floor = walls;
		this.spawners = spawners;
		try {
			chests = Collections.singletonList(new WeightedBlock(Blocks.CHEST.getDefaultState(), JsonToNBT.getTagFromJson("{LootTable:\"minecraft:chests/simple_dungeon\"}"), 100));
		} catch (NBTException e) {
			throw new AssertionError("Oops.", e);
		}
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();
		if (yStart <= 2) {
			return false;
		}

		int height = nextInt(rand, maxHeight - minHeight + 1) + minHeight;
		int xWidth = nextInt(rand, maxWidthX - minWidthX + 1) + minWidthX;
		int zWidth = nextInt(rand, maxWidthZ - minWidthZ + 1) + minWidthZ;
		int holes = 0;
		int x, y, z;

		int floor = yStart - 1, ceiling = yStart + height + 1;

		for (x = xStart - xWidth - 1; x <= xStart + xWidth + 1; ++x) {
			for (z = zStart - zWidth - 1; z <= zStart + zWidth + 1; ++z) {
				for (y = floor; y <= ceiling; ++y) {

					if (y == floor && !canGenerateInBlock(world, x, y, z, material)) {
						return false;
					}

					if (y == ceiling && !canGenerateInBlock(world, x, y, z, material)) {
						return false;
					}

					if ((abs(x - xStart) == xWidth + 1 || abs(z - zStart) == zWidth + 1) && y == yStart && world.isAirBlock(new BlockPos(x, y, z))
							&& world.isAirBlock(new BlockPos(x, y + 1, z))) {
						++holes;
					}
				}
			}
		}

		if (holes < minHoles || holes > maxHoles) {
			return false;
		}

		for (x = xStart - xWidth - 1; x <= xStart + xWidth + 1; ++x) {
			for (z = zStart - zWidth - 1; z <= zStart + zWidth + 1; ++z) {
				for (y = yStart + height; y >= floor; --y) {

					l: if (y != floor) {
						if ((abs(x - xStart) != xWidth + 1 && abs(z - zStart) != zWidth + 1)) {
							world.setBlockToAir(new BlockPos(x, y, z));
						} else if (!canGenerateInBlock(world, x, y - 1, z, material)) {
							world.setBlockToAir(new BlockPos(x, y, z));
						} else {
							break l;
						}
						continue;
					}
					if (canGenerateInBlock(world, x, y, z, material)) {
						if (y == floor) {
							generateBlock(world, rand, x, y, z, this.floor);
						} else {
							generateBlock(world, rand, x, y, z, walls);
						}
					}
				}
			}
		}

		WeightedBlock chest = selectBlock(rand, chests);

		for (int i = maxChests; i-- > 0;) {
			for (int j = maxChestTries; j-- > 0;) {
				x = xStart + nextInt(rand, xWidth * 2 + 1) - xWidth;
				z = zStart + nextInt(rand, zWidth * 2 + 1) - zWidth;
				BlockPos checkPos = new BlockPos(x, yStart,  z);

				if (world.isAirBlock(checkPos)) {
					int walls = 0;

					if (isWall(world, x - 1, yStart, z)) {
						++walls;
					}

					if (isWall(world, x + 1, yStart, z)) {
						++walls;
					}

					if (isWall(world, x, yStart, z - 1)) {
						++walls;
					}

					if (isWall(world, x, yStart, z + 1)) {
						++walls;
					}

					if (walls >= 1 && walls <= 2) {
						setBlock(world, checkPos, chest);

						break;
					}
				}
			}
		}

		generateBlock(world, rand, xStart, yStart, zStart, spawners);

		return true;
	}

	private static int nextInt(Random rand, int v) {

		if (v <= 1) {
			return 0;
		}
		return rand.nextInt(v);
	}

	private boolean isWall(World world, int x, int y, int z) {

		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		for (int j = 0, e = walls.size(); j < e; ++j) {
			WeightedBlock genBlock = walls.get(j);
			if (state.equals(genBlock.getState()))
				return true;
		}
		return false;
	}

}
