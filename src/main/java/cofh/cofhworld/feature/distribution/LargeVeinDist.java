package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LargeVeinDist implements IDistribution {

	final INumberProvider minY;
	final INumberProvider veinHeight, veinDiameter;
	final INumberProvider verticalDensity;
	final INumberProvider horizontalDensity;

	public LargeVeinDist(INumberProvider minY, INumberProvider height, INumberProvider diameter, INumberProvider vDensity, INumberProvider hDensity) {

		this.minY = minY;
		this.veinHeight = height;
		this.veinDiameter = diameter;
		this.verticalDensity = vDensity;
		this.horizontalDensity = hDensity;
	}

	public int getDensity(Random rand, int oreDistance, float oreDensity) {

		oreDensity = oreDensity * 0.01f * (oreDistance >> 1);
		int i = (int) oreDensity;
		if (i == 0) {
			++i;
		}
		int rnd = oreDistance / i;
		int r = 0;
		for (; i > 0; --i) {
			r += rand.nextInt(rnd);
		}
		return r;
	}

	@Override
	public boolean apply(Feature f, Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = f.getChunkCount().intValue(world, random, pos);
		final int blockY = minY.intValue(world, random, pos);
		final int veinDiameter = this.veinDiameter.intValue(world, random, pos);
		final int horizontalDensity = this.horizontalDensity.intValue(world, random, pos);
		final int veinHeight = this.veinHeight.intValue(world, random, pos);
		final int verticalDensity = this.verticalDensity.intValue(world, random, pos);

		Random dRand = new Random(world.getSeed());
		long l = (dRand.nextLong() / 2L) * 2L + 1L;
		long l1 = (dRand.nextLong() / 2L) * 2L + 1L;
		dRand.setSeed((blockX >> 4) * l + (blockZ >> 4) * l1 ^ world.getSeed());

		boolean generated = false;
		for (int i = count; i-- > 0; ) {

			int x = blockX + getDensity(dRand, veinDiameter, horizontalDensity);
			int y = blockY + getDensity(dRand, veinHeight, verticalDensity);
			int z = blockZ + getDensity(dRand, veinDiameter, horizontalDensity);
			if (!f.canGenerateInBiome(world, x, z, random)) {
				continue;
			}

			generated |= f.applyGenerator(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

	@Override
	public List<WeightedRandomBlock> defaultMaterials() {
		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	@Override
	public String defaultGenerator() {
		return "large-vein";
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String name, Config genObject, Logger log) {
			if (!(genObject.hasPath("min-height") && genObject.hasPath("vein-height"))) {
				log.error("Height parameters for 'fractal' template not specified in \"" + name + "\"");
				return null;
			}
			if (!(genObject.hasPath("vein-diameter"))) {
				log.error("veinDiameter parameter for 'fractal' template not specified in \"" + name + "\"");
				return null;
			}
			if (!(genObject.hasPath("vertical-density") && genObject.hasPath("horizontal-density"))) {
				log.error("Density parameters for 'fractal' template not specified in \"" + name + "\"");
				return null;
			}
			ConfigObject genData = genObject.root();
			INumberProvider minY = FeatureParser.parseNumberValue(genData.get("min-height"));
			INumberProvider h = FeatureParser.parseNumberValue(genData.get("vein-height"));
			INumberProvider d = FeatureParser.parseNumberValue(genData.get("vein-diameter"));
			INumberProvider vD = FeatureParser.parseNumberValue(genData.get("vertical-density"), 0, 100);
			INumberProvider hD = FeatureParser.parseNumberValue(genData.get("horizontal-density"), 0, 100);

			return new LargeVeinDist(minY, h, d, vD, hD);
		}
	}
}

