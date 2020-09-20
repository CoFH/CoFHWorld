package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenBoulder;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserBoulder implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "diameter" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("diameter");
		if (clusterSize <= 0) {
			log.warn("Invalid `diameter` for generator '{}'", name);
			throw new InvalidGeneratorException("Invalid `diameter`", genObject.getValue("diameter").origin());
		}

		WorldGenBoulder r = new WorldGenBoulder(resList, clusterSize, matList);
		{
			if (genObject.hasPath("size-variance")) {
				r.sizeVariance = genObject.getInt("size-variance");
			}
			if (genObject.hasPath("count")) {
				r.clusters = genObject.getInt("count");
			}
			if (genObject.hasPath("count-variance")) {
				r.clusterVariance = genObject.getInt("count-variance");
			}
			if (genObject.hasPath("hollow")) {
				r.hollow = genObject.getBoolean("hollow");
			}
			if (genObject.hasPath("hollow-size")) {
				r.hollowAmt = (float) genObject.getDouble("hollow-size");
			}
			if (genObject.hasPath("hollow-variance")) {
				r.hollowVar = (float) genObject.getDouble("hollow-variance");
			}
		}
		return r;
	}

}
