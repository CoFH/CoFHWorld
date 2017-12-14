package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class SpikeGen implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final WeightedRandomBlock[] genBlock;
	public boolean largeSpikes = true;
	public int largeSpikeChance = 60;
	public int minHeight = 7;
	public int heightVariance = 4;
	public int sizeVariance = 2;
	public int positionVariance = 3;
	public int minLargeSpikeHeightGain = 10;
	public int largeSpikeHeightVariance = 30;
	public int largeSpikeFillerSize = 1;

	public SpikeGen(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> block) {

		cluster = resource;
		genBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		while (world.isAirBlock(new BlockPos(xStart, yStart, zStart)) && yStart > 2) {
			--yStart;
		}

		if (!ClusterGen.canGenerateInBlock(world, xStart, yStart, zStart, genBlock)) {
			return false;
		}

		int height = rand.nextInt(heightVariance) + minHeight, originalHeight = height;
		int size = height / (minHeight / 2) + rand.nextInt(sizeVariance);
		if (size > 1 && positionVariance > 0) {
			yStart += rand.nextInt(positionVariance + 1) - 1;
		}

		if (largeSpikes && size > 1 && (largeSpikeChance <= 0 || rand.nextInt(largeSpikeChance) == 0)) {
			height += minLargeSpikeHeightGain + rand.nextInt(largeSpikeHeightVariance);
		}

		int offsetHeight = height - originalHeight;

		for (int y = 0; y < height; ++y) {
			float layerSize;
			if (y >= offsetHeight) {
				layerSize = (1.0F - (float) (y - offsetHeight) / (float) originalHeight) * size;
			} else {
				layerSize = largeSpikeFillerSize;
			}
			int width = MathHelper.ceil(layerSize);

			for (int x = -width; x <= width; ++x) {
				float xDist = MathHelper.abs(x) - 0.25F;

				for (int z = -width; z <= width; ++z) {
					float zDist = MathHelper.abs(z) - 0.25F;

					if ((x == 0 && z == 0 || xDist * xDist + zDist * zDist <= layerSize * layerSize) && (x != -width && x != width && z != -width && z != width || rand.nextFloat() <= 0.75F)) {

						ClusterGen.generateBlock(world, xStart + x, yStart + y, zStart + z, genBlock, cluster);

						if (y != 0 && width > 1) {
							ClusterGen.generateBlock(world, xStart + x, yStart - y + offsetHeight, zStart + z, genBlock, cluster);
						}
					}
				}
			}
		}
		return true;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			SpikeGen r = new SpikeGen(resList, matList);
			if (genObject.hasPath("min-height")) {
				r.minHeight = genObject.getInt("min-height");
			}
			if (genObject.hasPath("height-variance")) {
				r.heightVariance = genObject.getInt("height-variance");
			}
			if (genObject.hasPath("size-variance")) {
				r.sizeVariance = genObject.getInt("size-variance");
			}
			if (genObject.hasPath("position-variance")) {
				r.positionVariance = genObject.getInt("position-variance");
			}
			// TODO: these fields need addressed. combined into a sub-object?
			if (genObject.hasPath("large-spikes")) {
				r.largeSpikes = genObject.getBoolean("large-spikes");
			}
			if (genObject.hasPath("large-spike-chance")) {
				r.largeSpikeChance = genObject.getInt("large-spike-chance");
			}
			if (genObject.hasPath("min-large-spike-height-gain")) {
				r.minLargeSpikeHeightGain = genObject.getInt("min-large-spike-height-gain");
			}
			if (genObject.hasPath("large-spike-height-variance")) {
				r.largeSpikeHeightVariance = genObject.getInt("large-spike-height-variance");
			}
			if (genObject.hasPath("large-spike-filler-size")) {
				r.largeSpikeFillerSize = genObject.getInt("large-spike-filler-size");
			}
			return r;
		}
	}
}
