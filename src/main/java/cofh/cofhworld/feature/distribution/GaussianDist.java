package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.ConstantProvider;
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

public class GaussianDist implements IDistribution {

	final INumberProvider rolls;
	final INumberProvider meanY;
	final INumberProvider maxVar;

	public GaussianDist(INumberProvider smoothness, INumberProvider meanY, INumberProvider maxVar) {

		this.rolls = smoothness;
		this.meanY = meanY;
		this.maxVar = maxVar;
	}

	@Override
	public boolean apply(Feature f, Random random, int blockX, int blockZ, World world) {
		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = f.getChunkCount().intValue(world, random, pos);
		final int meanY = this.meanY.intValue(world, random, pos);

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int y = meanY;
			final int maxVar = this.maxVar.intValue(world, random, pos);
			if (maxVar > 1) {
				final int rolls = this.rolls.intValue(world, random, pos);
				for (int v = 0; v < rolls; ++v) {
					y += random.nextInt(maxVar);
				}
				y = Math.round(y - (maxVar * (rolls * .5f)));
			}
			int z = blockZ + random.nextInt(16);
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
		return "cluster";
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String name, Config genObject, Logger log) {
			if (!(genObject.hasPath("center-height") && genObject.hasPath("spread"))) {
				log.error("Height parameters for 'normal' template not specified in \"" + name + "\"");
				return null;
			}
			ConfigObject genData = genObject.root();
			INumberProvider centerHeight = FeatureParser.parseNumberValue(genData.get("center-height"));
			INumberProvider spread = FeatureParser.parseNumberValue(genData.get("spread"));
			INumberProvider rolls = genObject.hasPath("smoothness") ? FeatureParser.parseNumberValue(genData.get("smoothness")) : new ConstantProvider(2);

			return new GaussianDist(rolls, centerHeight, spread);
		}
	}

}
