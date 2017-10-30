package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenStalagmite implements IGenerator {

	protected final List<WeightedRandomBlock> cluster;
	protected final WeightedRandomBlock[] baseBlock;
	protected WeightedRandomBlock[] genBlock;
	public int minHeight = 7;
	public int heightVariance = 4;
	public int sizeVariance = 2;
	public int heightMod = 5;
	public int genSize = 0;
	public boolean smooth = false;
	public boolean fat = true;
	public boolean altSinc = false;

	public WorldGenStalagmite(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> block) {

		cluster = resource;
		baseBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	protected int getHeight(int x, int z, int size, Random rand, int height) {

		if (smooth) {
			if ((x * x + z * z) * 4 >= size * size * 5) {
				return 0;
			}

			final double lim = (altSinc ? 600f : (fat ? 1f : .5f) * 400f) / size;
			final double pi = Math.PI;
			double r;
			r = Math.sqrt((r = ((x * lim) / pi)) * r + (r = ((z * lim) / pi)) * r) * pi / 180;
			if (altSinc && r < 1) {
				r = Math.sqrt((size * 2 * lim) / pi) * pi / 180;
			}
			if (r == 0) {
				return height;
			}
			if (!altSinc) {
				return (int) Math.round(height * (fat ? Math.sin(r) / r : Math.sin(r = r * pi) / r));
			}
			double sinc = (Math.sin(r) / r);
			return (int) Math.round(height * (sinc * 2 + (Math.sin(r = r * (pi * 4)) / r)) / 2 + rand.nextGaussian() * .75);
		} else {
			int absx = x < 0 ? -x : x, absz = (z < 0 ? -z : z);
			int dist = fat ? (absx < absz ? absz + absx / 2 : absx + absz / 2) : absx + absz;
			if (dist == 0) {
				return height;
			}
			int v = 1 + height / dist;
			return v > 1 ? rand.nextInt(v) : 0;
		}
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();
		while (world.isAirBlock(new BlockPos(xStart, yStart, zStart)) && yStart > 0) {
			--yStart;
		}

		if (!WorldGenMinableCluster.canGenerateInBlock(world, xStart, yStart++, zStart, baseBlock)) {
			return false;
		}

		int maxHeight = (heightVariance > 0 ? rand.nextInt(heightVariance) : 0) + minHeight;

		int size = (genSize > 0 ? genSize : maxHeight / heightMod);
		if (sizeVariance > 0) {
			size += rand.nextInt(sizeVariance);
		}
		boolean r = false;
		for (int x = -size; x <= size; ++x) {
			for (int z = -size; z <= size; ++z) {
				if (!WorldGenMinableCluster.canGenerateInBlock(world, xStart + x, yStart - 1, zStart + z, baseBlock)) {
					continue;
				}
				int height = getHeight(x, z, size, rand, maxHeight);
				for (int y = 0; y < height; ++y) {
					r |= WorldGenMinableCluster.generateBlock(world, xStart + x, yStart + y, zStart + z, genBlock, cluster);
				}
			}
		}
		return r;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			return commonParse(new WorldGenStalagmite(resList, matList), generatorName, genObject, log);
		}

		protected IGenerator commonParse(WorldGenStalagmite r, String generatorName, Config genObject, Logger log) {

			// TODO: these names need revised
			ArrayList<WeightedRandomBlock> list = new ArrayList<>();
			if (!genObject.hasPath("gen-body")) {
				log.info("Entry does not specify gen body for 'stalagmite' generator. Using air.");
				list.add(new WeightedRandomBlock(Blocks.AIR));
			} else {
				if (!FeatureParser.parseResList(genObject.root().get("gen-body"), list, false)) {
					log.warn("Entry specifies invalid gen body for 'stalagmite' generator! Using air!");
					list.clear();
					list.add(new WeightedRandomBlock(Blocks.AIR));
				}
			}

			r.genBlock = list.toArray(new WeightedRandomBlock[list.size()]);

			if (genObject.hasPath("min-height")) {
				r.minHeight = genObject.getInt("min-height");
			}
			if (genObject.hasPath("height-variance")) {
				r.heightVariance = genObject.getInt("height-variance");
			}
			if (genObject.hasPath("size-variance")) {
				r.sizeVariance = genObject.getInt("size-variance");
			}
			if (genObject.hasPath("height-mod")) {
				r.heightMod = genObject.getInt("height-mod");
			}
			if (genObject.hasPath("gen-size")) {
				r.genSize = genObject.getInt("gen-size");
			}
			if (genObject.hasPath("smooth")) {
				r.smooth = genObject.getBoolean("smooth");
			}
			if (genObject.hasPath("fat")) {
				r.fat = genObject.getBoolean("fat");
			}
			if (genObject.hasPath("alt-sinc")) {
				r.altSinc = genObject.getBoolean("alt-sinc");
			}

			return r;
		}
	}
}
