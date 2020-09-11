package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.PlaneShape;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class WorldGenMinablePlate extends WorldGen {

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;

	private final INumberProvider radius;
	private INumberProvider height;

	private PlaneShape shape = PlaneShape.CIRCLE;
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

		DataHolder data = new DataHolder(pos);

		++y;
		int size = radius.intValue(world, rand, data);
		final PlaneShape shape = this.shape;
		int height = this.height.intValue(world, rand, data);

		boolean r = false;
		for (int posX = x - size; posX <= x + size; ++posX) {
			int areaX = posX - x;
			for (int posZ = z - size; posZ <= z + size; ++posZ) {
				int areaZ = posZ - z;

				if (shape.inArea(areaX, areaZ, size)) {
					for (int posY = y - height; slim ? posY < y + height : posY <= y + height; ++posY) {
						r |= generateBlock(world, rand, posX, posY, posZ, genBlock, cluster);
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

	public WorldGenMinablePlate setShape(PlaneShape shape) {

		this.shape = shape;
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
