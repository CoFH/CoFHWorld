package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenLargeVein;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserLargeVein extends AbstractGenParserClusterCount {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		INumberProvider clusterSize = NumberData.parseNumberValue(genObject.getValue("cluster-size"));

		ICondition sparse = ConstantCondition.TRUE, spindly = ConstantCondition.FALSE;
		{
			sparse = genObject.hasPath("sparse") ? ConditionData.parseConditionValue(genObject.getValue("sparse")) : sparse;
			spindly = genObject.hasPath("spindly") ? ConditionData.parseConditionValue(genObject.getValue("spindly")) : spindly;
		}
		WorldGenLargeVein vein = new WorldGenLargeVein(resList, clusterSize, matList);
		return vein.setSparse(sparse).setSpindly(spindly);
	}

}
