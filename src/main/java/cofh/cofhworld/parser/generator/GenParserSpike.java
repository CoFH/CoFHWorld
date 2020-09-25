package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.generator.builders.BuilderSpike;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserSpike extends AbstractGenParserBlock {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		BuilderSpike r = new BuilderSpike(resList, matList);
		{
			if (genObject.hasPath("height")) {
				r.setHeight(NumberData.parseNumberValue(genObject.getValue("height")));
			}
			if (genObject.hasPath("size")) {
				r.setSize(NumberData.parseNumberValue(genObject.getValue("size")));
			}
			if (genObject.hasPath("variance-y")) {
				r.setyVariance(NumberData.parseNumberValue(genObject.getValue("variance-y")));
			}
			if (genObject.hasPath("layer-size")) {
				r.setLayerSize(NumberData.parseNumberValue(genObject.getValue("layer-size")));
			}
			if (genObject.hasPath("large-spikes")) {
				r.setLargeSpikes(ConditionData.parseConditionValue(genObject.getValue("large-spikes")));
			}
			if (genObject.hasPath("large-spike-height-gain")) {
				r.setLargeSpikeHeightGain(NumberData.parseNumberValue(genObject.getValue("large-spike-height-gain")));
			}
		}
		return r.build();
	}

}
