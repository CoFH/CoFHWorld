package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WorldGenSpout extends WorldGenerator {

	private static enum Shape {

		CIRCLE {

			@Override
			public boolean inArea(int x, int z, int radius) {

				return x*x + z*z <= radius * radius;
			}
		}, SQUARE {

			@Override
			public boolean inArea(int x, int z, int radius) {

				return true;
			}
		};

		public abstract boolean inArea(int x, int z, int radius);
	}

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;

	private final INumberProvider radius;
	private final INumberProvider height;

	private Shape shape = Shape.CIRCLE;

	public WorldGenSpout(List<WeightedBlock> resource, List<WeightedBlock> material, INumberProvider radius, INumberProvider height) {

		cluster = resource;
		this.radius = radius;
		this.height = height;
		genBlock = material.toArray(new WeightedBlock[material.size()]);
	}

	public WorldGenSpout setShape(String shape) {

		this.shape = Shape.valueOf(shape.toUpperCase(Locale.US));
		return this;
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int xCenter = pos.getX();
		int yCenter = pos.getY();
		int zCenter = pos.getZ();

		int height = this.height.intValue(world, rand, pos);
		boolean r = false;
		for (int y = 0; y < height; ++y) {
			int radius = this.radius.intValue(world, rand, pos.add(0, y, 0));
			for (int x = -radius; x <= radius; ++x) {
				for (int z = -radius; z <= radius; ++z) {
					if (shape.inArea(x, z, radius)) {
						r |= WorldGenMinableCluster.generateBlock(world, rand, xCenter + x, yCenter + y, zCenter + z, genBlock, cluster);
					}
				}
			}
		}

		return r;
	}

}
