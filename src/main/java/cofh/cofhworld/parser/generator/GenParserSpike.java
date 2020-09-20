package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenSpike;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserSpike extends AbstractGenParserBlock {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) {

		WorldGenSpike r = new WorldGenSpike(resList, matList);
		{
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
		}
		return r;
	}

}
