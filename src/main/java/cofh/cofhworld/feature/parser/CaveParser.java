package cofh.cofhworld.feature.parser;

import cofh.cofhworld.feature.generator.FeatureBase;
import cofh.cofhworld.feature.generator.FeatureBase.GenRestriction;
import cofh.cofhworld.feature.generator.FeatureGenCave;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class CaveParser extends UniformParser {

	@Override
	protected FeatureBase getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		boolean ceiling = genObject.hasPath("ceiling") && genObject.getBoolean("ceiling");
		return new FeatureGenCave(featureName, gen, ceiling, numClusters, biomeRes, retrogen, dimRes);
	}

}
