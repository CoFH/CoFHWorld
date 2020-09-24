package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenMinableCluster;
import cofh.cofhworld.world.generator.WorldGenSparseMinableCluster;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserCluster extends AbstractGenParserClusterCount {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		INumberProvider clusterSize = NumberData.parseNumberValue(genObject.getValue("cluster-size"), 0, 64);

		if (sparse) {
			return new WorldGenSparseMinableCluster(resList, clusterSize, matList);
		}
		return new WorldGenMinableCluster(resList, clusterSize, matList);
	}

}
