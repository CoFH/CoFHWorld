package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.DistributionCave;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserCave extends DistParserUniform {

	@Override
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		boolean ceiling = genObject.hasPath("ceiling") && genObject.getBoolean("ceiling");
		DistributionCave cave = new DistributionCave(featureName, gen, ceiling, numClusters, biomeRes, retrogen, dimRes);
		if (genObject.hasPath("ground-level")) {
			cave.setGroundLevel(NumberData.parseNumberValue(genObject.getValue("ground-level"), 0, 255));
		}
		return cave;
	}

}
