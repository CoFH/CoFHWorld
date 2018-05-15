package cofh.cofhworld.decoration.parser;

import cofh.cofhworld.decoration.IGeneratorParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.exceptions.InvalidGeneratorException;
import cofh.cofhworld.world.generator.WorldGenMinableLargeVein;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LargeVeinParser implements IGeneratorParser {

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) throws InvalidGeneratorException {

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
