package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import cofh.cofhworld.util.numbers.UniformRandomProvider;
import com.typesafe.config.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class WorldGenMinablePlate implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final WeightedRandomBlock[] genBlock;
	private final INumberProvider radius;
	private INumberProvider height = new ConstantProvider(1);
	private boolean slim = false;

	public WorldGenMinablePlate(List<WeightedRandomBlock> resource, int clusterSize, List<WeightedRandomBlock> block) {

		cluster = resource;
		radius = new UniformRandomProvider(clusterSize, clusterSize+2);
		genBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		++y;
		int size = radius.intValue(world, rand, pos);
		final int dist = size * size;
		int height = this.height.intValue(world, rand, pos);

		boolean r = false;
		for (int posX = x - size; posX <= x + size; ++posX) {
			int xDist = posX - x;
			xDist *= xDist;
			for (int posZ = z - size; posZ <= z + size; ++posZ) {
				int zSize = posZ - z;

				if (zSize * zSize + xDist <= dist) {
					for (int posY = y - height; slim ? posY < y + height : posY <= y + height; ++posY) {
						r |= WorldGenMinableCluster.generateBlock(world, posX, posY, posZ, genBlock, cluster);
					}
				}
			}
		}

		return r;
	}

	public static class Parser implements IGeneratorParser {
		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			int clusterSize = genObject.getInt("radius");
			if (clusterSize <= 0) {
				log.warn("Invalid radius for generator '{}'", name);
				return null;
			}

			WorldGenMinablePlate r = new WorldGenMinablePlate(resList, MathHelper.clamp(clusterSize, 0, 32), matList);
			if (genObject.hasPath("height")) {
				r.height = FeatureParser.parseNumberValue(genObject.root().get("height"), 0, 64);
			}
			if (genObject.hasPath("slim")) {
				r.slim = genObject.getBoolean("slim");
			}
			return r;
		}
	}
}
