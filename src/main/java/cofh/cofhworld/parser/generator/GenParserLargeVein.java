package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenMinableLargeVein;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserLargeVein extends AbstractGenParserClusterCount {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("cluster-size");
		if (clusterSize <= 0) {
			log.warn("Invalid `cluster-size` for generator '{}'", name);
			throw new InvalidGeneratorException("Invalid `cluster-size`", genObject.getValue("cluster-size").origin());
		}

		ICondition sparse = ConstantCondition.TRUE, spindly = ConstantCondition.FALSE;
		{
			sparse = genObject.hasPath("sparse") ? ConditionData.parseConditionValue(genObject.getValue("sparse")) : sparse;
			spindly = genObject.hasPath("spindly") ? ConditionData.parseConditionValue(genObject.getValue("spindly")) : spindly;
		}
		WorldGenMinableLargeVein vein = new WorldGenMinableLargeVein(resList, clusterSize, matList);
		return vein.setSparse(sparse).setSpindly(spindly);
	}

}
