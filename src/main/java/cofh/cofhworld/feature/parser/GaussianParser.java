package cofh.cofhworld.feature.parser;

import cofh.cofhworld.feature.generator.FeatureBase;
import cofh.cofhworld.feature.generator.FeatureBase.GenRestriction;
import cofh.cofhworld.feature.generator.FeatureGenGaussian;
import cofh.cofhworld.init.FeatureParser;
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
		INumberProvider centerHeight = FeatureParser.parseNumberValue(genData.get("center-height"));
		INumberProvider spread = FeatureParser.parseNumberValue(genData.get("spread"));
		INumberProvider rolls = genObject.hasPath("smoothness") ? FeatureParser.parseNumberValue(genData.get("smoothness")) : new ConstantProvider(2);

		return new FeatureGenGaussian(featureName, gen, numClusters, rolls, centerHeight, spread, biomeRes, retrogen, dimRes);
	}

}
