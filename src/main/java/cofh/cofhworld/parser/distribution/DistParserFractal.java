package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.DistributionLargeVein;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserFractal extends DistParserUniform {

	@Override
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.hasPath("min-height") && genObject.hasPath("vein-height"))) {
			log.error("Height parameters for 'fractal' template not specified in \"" + featureName + "\"");
			return null;
		}
		if (!(genObject.hasPath("vein-diameter"))) {
			log.error("veinDiameter parameter for 'fractal' template not specified in \"" + featureName + "\"");
			return null;
		}
		if (!(genObject.hasPath("vertical-density") && genObject.hasPath("horizontal-density"))) {
			log.error("Density parameters for 'fractal' template not specified in \"" + featureName + "\"");
			return null;
		}
		ConfigObject genData = genObject.root();
		INumberProvider minY = NumberData.parseNumberValue(genData.get("min-height"));
		INumberProvider h = NumberData.parseNumberValue(genData.get("vein-height"));
		INumberProvider d = NumberData.parseNumberValue(genData.get("vein-diameter"));
		INumberProvider vD = NumberData.parseNumberValue(genData.get("vertical-density"), 0, 100);
		INumberProvider hD = NumberData.parseNumberValue(genData.get("horizontal-density"), 0, 100);

		return new DistributionLargeVein(featureName, gen, numClusters, minY, biomeRes, retrogen, dimRes, h, d, vD, hD);
	}

	@Override
	protected String getDefaultGenerator() {

		return "large-vein";
	}

}
