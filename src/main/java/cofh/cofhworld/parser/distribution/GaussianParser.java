package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.FeatureBase;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.FeatureGenGaussian;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class GaussianParser extends UniformParser {

	@Override
	protected FeatureBase getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.hasPath("center-height") && genObject.hasPath("spread"))) {
			log.error("Height parameters for 'normal' template not specified in \"" + featureName + "\"");
			return null;
		}
		ConfigObject genData = genObject.root();
		INumberProvider centerHeight = NumberData.parseNumberValue(genData.get("center-height"));
		INumberProvider spread = NumberData.parseNumberValue(genData.get("spread"));
		INumberProvider rolls = genObject.hasPath("smoothness") ? NumberData.parseNumberValue(genData.get("smoothness")) : new ConstantProvider(2);

		return new FeatureGenGaussian(featureName, gen, numClusters, rolls, centerHeight, spread, biomeRes, retrogen, dimRes);
	}

}
