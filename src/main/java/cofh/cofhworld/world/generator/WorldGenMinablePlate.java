package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class WorldGenMinablePlate extends WorldGenerator {

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;
	private final INumberProvider radius;
	private INumberProvider height;
	private boolean slim;

	public WorldGenMinablePlate(List<WeightedBlock> resource, int clusterSize, List<WeightedBlock> block) {

		this(resource, new UniformRandomProvider(clusterSize, clusterSize + 2), block);
	}

	public WorldGenMinablePlate(List<WeightedBlock> resource, INumberProvider clusterSize, List<WeightedBlock> block) {

		cluster = resource;
		radius = clusterSize;
		genBlock = block.toArray(new WeightedBlock[block.size()]);
		setHeight(1).setSlim(false);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		INumberProvider.DataHolder data = new INumberProvider.DataHolder(pos);

		++y;
		int size = radius.intValue(world, rand, data);
		final int dist = size * size;
		int height = this.height.intValue(world, rand, data);

		boolean r = false;
		for (int posX = x - size; posX <= x + size; ++posX) {
			int xDist = posX - x;
			xDist *= xDist;
			for (int posZ = z - size; posZ <= z + size; ++posZ) {
				int zSize = posZ - z;

				if (zSize * zSize + xDist <= dist) {
					for (int posY = y - height; slim ? posY < y + height : posY <= y + height; ++posY) {
						r |= WorldGenMinableCluster.generateBlock(world, rand, posX, posY, posZ, genBlock, cluster);
					}
				}
			}
		}

		return r;
	}

	public WorldGenMinablePlate setSlim(boolean slim) {

		this.slim = slim;
		return this;
	}

	public WorldGenMinablePlate setHeight(int height) {

		this.height = new ConstantProvider(height);
		return this;
	}

	public WorldGenMinablePlate setHeight(INumberProvider height) {

		this.height = height;
		return this;
	}

}
