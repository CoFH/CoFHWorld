package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenMinableLargeVein;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserLargeVein extends AbstractGenParserClusterCount {

	@Override
	@Nonnull
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("cluster-size");
		if (clusterSize <= 0) {
			log.warn("Invalid `cluster-size` for generator '{}'", name);
			throw new InvalidGeneratorException("Invalid `cluster-size`", genObject.getValue("cluster-size").origin());
		}

		boolean sparse = true, spindly = false;
		{
			sparse = genObject.hasPath("sparse") ? genObject.getBoolean("sparse") : sparse;
			spindly = genObject.hasPath("spindly") ? genObject.getBoolean("spindly") : spindly;
		}
		WorldGenMinableLargeVein vein = new WorldGenMinableLargeVein(resList, clusterSize, matList, sparse);
		return vein.setSpindly(spindly);
	}

}
