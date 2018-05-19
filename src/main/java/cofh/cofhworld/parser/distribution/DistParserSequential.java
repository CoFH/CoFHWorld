package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.DistributionData;
import cofh.cofhworld.parser.variables.BiomeData;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.IFeatureGenerator;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionSequential;
import cofh.cofhworld.init.FeatureParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Locale;

public class DistParserSequential implements IDistributionParser {

	@Override
	public IFeatureGenerator parseFeature(String featureName, Config genObject, Logger log) {

		boolean retrogen = false;
		if (genObject.hasPath("retrogen")) {
			retrogen = genObject.getBoolean("retrogen");
		}
		GenRestriction biomeRes = GenRestriction.NONE;
		if (genObject.hasPath("biome")) {
			ConfigValue data = genObject.getValue("biome");
			if (data.valueType() == ConfigValueType.STRING) {
				biomeRes = GenRestriction.get(genObject.getString("biome"));
				if (biomeRes != GenRestriction.NONE) {
					log.error("Invalid biome restriction %2$s on '%1$s'. Must be an object to meaningfully function", featureName, biomeRes.name().toLowerCase(Locale.US));
					return null;
				}
			} else if (data.valueType() == ConfigValueType.OBJECT) {
				biomeRes = GenRestriction.get(genObject.getString("biome.restriction"));
			}
		}
		GenRestriction dimRes = GenRestriction.NONE;
		if (genObject.hasPath("dimension")) {
			ConfigValue data = genObject.getValue("dimension");
			switch (data.valueType()) {
			case STRING:
				dimRes = GenRestriction.get(genObject.getString("dimension"));
				if (dimRes != GenRestriction.NONE) {
					log.error("Invalid dimension restriction %2$s on '%1$s'. Must be an object to meaningfully function", featureName, dimRes.name().toLowerCase(Locale.US));
					return null;
				}
				break;
			case OBJECT:
				dimRes = GenRestriction.get(genObject.getString("dimension.restriction"));
				break;
			case LIST:
			case NUMBER:
				dimRes = GenRestriction.WHITELIST;
			}
		}

		IConfigurableFeatureGenerator feature = getFeature(featureName, genObject, biomeRes, retrogen, dimRes, log);

		if (feature != null) {
			if (genObject.hasPath("chunk-chance")) {
				int rarity = MathHelper.clamp(genObject.getInt("chunk-chance"), 1, 1000000000);
				feature.setRarity(rarity);
			}
			addFeatureRestrictions(feature, genObject);
			if (genObject.hasPath("in-village")) {
				feature.setWithVillage(genObject.getBoolean("in-village"));
			}
		}
		return feature;
	}

	@Override
	public Distribution getFeature(String featureName, Config genObject, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		ArrayList<IConfigurableFeatureGenerator> features = new ArrayList<>();

		int i = 0;
		for (Config featureConf : genObject.getConfigList("features")) {
			String template = DistributionData.parseFeatureType(featureConf);
			IDistributionParser parser = FeatureParser.getDistribution(template);
			if (parser != null) {
				IConfigurableFeatureGenerator feature = parser.getFeature(featureName + '$' + ++i, featureConf, biomeRes, retrogen, dimRes, log);
				if (feature == null) {
					log.error("Template '{}' has failed to parse its entry!", template);
					return null;
				}
				features.add(feature);
			} else {
				log.error("Unknown template '{}'", template);
				return null;
			}
		}

		return new DistributionSequential(featureName, features, biomeRes, retrogen, dimRes);
	}

	public static void addFeatureRestrictions(IConfigurableFeatureGenerator feature, Config genObject) {

		if (feature.getBiomeRestriction() != GenRestriction.NONE) {
			feature.addBiomes(BiomeData.parseBiomeRestrictions(genObject.getConfig("biome")));
		}
		if (feature.getDimensionRestriction() != GenRestriction.NONE) {
			String field = "dimension";
			ConfigValue data = genObject.getValue(field);
			ConfigList restrictionList = null;
			switch (data.valueType()) {
			case OBJECT:
				field += ".value";
			case LIST:
				restrictionList = genObject.getList(field);
				break;
			case NUMBER:
				feature.addDimension(genObject.getNumber(field).intValue());
				break;
			default:
				// unreachable
				break;
			}
			if (restrictionList != null) {
				for (int i = 0; i < restrictionList.size(); i++) {
					ConfigValue val = restrictionList.get(i);
					if (val.valueType() == ConfigValueType.NUMBER) {
						feature.addDimension(((Number) val.unwrapped()).intValue());
					}
				}
			}
		}
	}

}
