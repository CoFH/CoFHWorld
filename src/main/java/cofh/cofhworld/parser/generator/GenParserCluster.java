package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenMinableCluster;
import cofh.cofhworld.world.generator.WorldGenSparseMinableCluster;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GenParserCluster extends AbstractGenParserClusterCount {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("cluster-size");
		if (clusterSize <= 0) {
			log.warn("Invalid `cluster-size` for generator '{}'", name);
			throw new InvalidGeneratorException("Invalid `cluster-size`", genObject.getValue("cluster-size").origin());
		}

		if (sparse) {
			return new WorldGenSparseMinableCluster(resList, clusterSize, matList);
		}
		return new WorldGenMinableCluster(resList, clusterSize, matList);
	}

}
