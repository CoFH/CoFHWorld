package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeodeGen implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final List<WeightedRandomBlock> outline;
	private final WeightedRandomBlock[] genBlock;
	private List<WeightedRandomBlock> fillBlock;
	private boolean hollow;
	private INumberProvider width = new ConstantProvider(16);
	private INumberProvider height = new ConstantProvider(8);

	public GeodeGen(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> material, List<WeightedRandomBlock> cover) {

		cluster = resource;
		genBlock = material.toArray(new WeightedRandomBlock[material.size()]);
		outline = cover;
		fillBlock = null;
		hollow = false;
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		final int height = this.height.intValue(world, rand, pos);
		final int width = this.width.intValue(world, rand, pos);

		int heightOff = height / 2;
		int widthOff = width / 2;
		xStart -= widthOff;
		zStart -= widthOff;

		if (yStart <= heightOff) {
			return false;
		}

		yStart -= heightOff;
		boolean[] spawnBlock = new boolean[width * width * height];
		boolean[] hollowBlock = new boolean[width * width * height];

		int W = width - 1, H = height - 1;

		for (int i = 0, e = rand.nextInt(4) + 4; i < e; ++i) {
			double xSize = rand.nextDouble() * 6.0D + 3.0D;
			double ySize = rand.nextDouble() * 4.0D + 2.0D;
			double zSize = rand.nextDouble() * 6.0D + 3.0D;
			double xCenter = rand.nextDouble() * (width - xSize - 2.0D) + 1.0D + xSize / 2.0D;
			double yCenter = rand.nextDouble() * (height - ySize - 4.0D) + 2.0D + ySize / 2.0D;
			double zCenter = rand.nextDouble() * (width - zSize - 2.0D) + 1.0D + zSize / 2.0D;
			double minDist = hollow ? rand.nextGaussian() * 0.15 + 0.4 : 0;

			for (int x = 1; x < W; ++x) {
				for (int z = 1; z < W; ++z) {
					for (int y = 1; y < H; ++y) {
						double xDist = (x - xCenter) / (xSize / 2.0D);
						double yDist = (y - yCenter) / (ySize / 2.0D);
						double zDist = (z - zCenter) / (zSize / 2.0D);
						double dist = xDist * xDist + yDist * yDist + zDist * zDist;

						if (dist < 1.0D) {
							spawnBlock[(x * width + z) * height + y] = true;
						}
						if (hollow && dist <= minDist) {
							hollowBlock[(x * width + z) * height + y] = true;
						}
					}
				}
			}
		}

		int x;
		int y;
		int z;

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					boolean flag = (fillBlock != null && hollowBlock[(x * width + z) * height + y]) || spawnBlock[(x * width + z) * height + y] || ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

					if (flag && !ClusterGen.canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, genBlock)) {
						return false;
					}
				}
			}
		}

		boolean r = false;
		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y]) {
						boolean t = ClusterGen.generateBlock(world, xStart + x, yStart + y, zStart + z, cluster);
						r |= t;
						if (!t) {
							spawnBlock[(x * width + z) * height + y] = false;
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (fillBlock != null && hollowBlock[(x * width + z) * height + y]) {
						r |= ClusterGen.generateBlock(world, xStart + x, yStart + y, zStart + z, fillBlock);
					} else {
						boolean flag = !spawnBlock[(x * width + z) * height + y] && ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

						if (flag) {
							r |= ClusterGen.generateBlock(world, xStart + x, yStart + y, zStart + z, outline);
						}
					}
				}
			}
		}

		return r;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			ArrayList<WeightedRandomBlock> list = new ArrayList<>();
			if (!genObject.hasPath("crust")) {
				if (WorldProps.verboseLogging) {
					log.warn("Using default 'crust' setting for GeodeGen on feature {}", name);
				}
				list.add(new WeightedRandomBlock(Blocks.STONE));
			} else {
				if (!FeatureParser.parseResList(genObject.root().get("crust"), list, true)) {
					log.warn("Parsing 'crust' setting for GeodeGen on feature {} failed; using default value", name);
					list.clear();
					list.add(new WeightedRandomBlock(Blocks.OBSIDIAN));
				}
			}
			GeodeGen r = new GeodeGen(resList, matList, list);
			if (genObject.hasPath("hollow")) {
				r.hollow = genObject.getBoolean("hollow");
			}
			if (genObject.hasPath("filler")) {
				list = new ArrayList<>();
				if (!FeatureParser.parseResList(genObject.getValue("filler"), list, true)) {
					log.warn("Parsing 'filler' setting for GeodeGen on feature {} failed; not filling", name);
				} else {
					r.fillBlock = list;
				}
			}
			return r;
		}
	}
}
