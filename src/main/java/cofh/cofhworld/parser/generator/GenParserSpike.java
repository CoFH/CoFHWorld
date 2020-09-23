package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
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
	public WorldGen parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		WorldGenSpike r = new WorldGenSpike(resList, matList);
		{
			if (genObject.hasPath("height")) {
				r.height = NumberData.parseNumberValue(genObject.getValue("height"));
			}
			if (genObject.hasPath("size")) {
				r.size = NumberData.parseNumberValue(genObject.getValue("size"));
			}
			if (genObject.hasPath("y-variance")) {
				r.yVariance = NumberData.parseNumberValue(genObject.getValue("y-variance"));
			}
			if (genObject.hasPath("layer-size")) {
				r.layerSize = NumberData.parseNumberValue(genObject.getValue("layer-size"));
			}
			if (genObject.hasPath("large-spikes")) {
				r.largeSpikes = ConditionData.parseConditionValue(genObject.getValue("large-spikes"));
			}
			if (genObject.hasPath("large-spike-height-gain")) {
				r.largeSpikeHeightGain = NumberData.parseNumberValue(genObject.getValue("large-spike-height-gain"));
			}
		}
		return r;
	}

}
