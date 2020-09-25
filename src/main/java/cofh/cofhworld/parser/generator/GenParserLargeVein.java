package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.parser.generator.builders.BuilderLargeVein;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserLargeVein extends AbstractGenParserClusterCount {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		BuilderLargeVein builder = new BuilderLargeVein(resList, matList);

		builder.setSize(NumberData.parseNumberValue(genObject.getValue("cluster-size")));

		if (genObject.hasPath("sparse")) {
			builder.setSparse(ConditionData.parseConditionValue(genObject.getValue("sparse")));
		}

		if (genObject.hasPath("spindly")) {
			builder.setSpindly(ConditionData.parseConditionValue(genObject.getValue("spindly")));
		}

		return builder.build();
	}

}
