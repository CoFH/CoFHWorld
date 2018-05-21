package cofh.cofhworld.parser;

import cofh.cofhworld.parser.variables.BiomeData;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.IFeatureGenerator;
import com.google.gson.JsonObject;
import com.typesafe.config.*;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Locale;

public interface IDistributionParser {

	static void addFeatureRestrictions(IConfigurableFeatureGenerator feature, Config genObject, Logger log) throws InvalidDistributionException {

		{ // biomes
			GenRestriction biomeRes = GenRestriction.NONE;
			if (genObject.hasPath("biome")) {
				ConfigValue data = genObject.getValue("biome");
				if (data.valueType() == ConfigValueType.STRING) {
					biomeRes = GenRestriction.get(genObject.getString("biome"));
					if (biomeRes != GenRestriction.NONE) {
						log.error("Invalid biome restriction {2} on '{1}'. Must be an object to meaningfully function",
								feature.getFeatureName(), biomeRes.name().toLowerCase(Locale.US));
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else if (data.valueType() == ConfigValueType.OBJECT) {
					biomeRes = GenRestriction.get(genObject.getString("biome.restriction"));
					feature.addBiomes(BiomeData.parseBiomeRestrictions(genObject.getConfig("biome")));
				}
			}
			feature.setBiomeRestriction(biomeRes);
		}
		{// dimensions
			GenRestriction dimRes = GenRestriction.NONE;
			if (genObject.hasPath("dimension")) {
				String field = "dimension";
				ConfigValue data = genObject.getValue("dimension");
				ConfigList restrictionList = null;
				switch (data.valueType()) {
				case STRING:
					dimRes = GenRestriction.get(genObject.getString("dimension"));
					if (dimRes != GenRestriction.NONE) {
						log.error("Invalid dimension restriction {2} on '{1}'. Must be an object to meaningfully function",
								feature.getFeatureName(), dimRes.name().toLowerCase(Locale.US));
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
					break;
				case OBJECT:
					dimRes = GenRestriction.get(genObject.getString(field + ".restriction"));
					field += ".value";
					// continue
				case LIST:
					restrictionList = genObject.getList(field);
					break;
				case NUMBER:
					dimRes = GenRestriction.WHITELIST;
					feature.addDimension(genObject.getNumber(field).intValue());
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
			feature.setDimensionRestriction(dimRes);
		}
	}

	String[] getRequiredFields();

	/**
	 * Parse a {@link JsonObject} for registration}.
	 *
	 * @param featureName The name of the feature to register.
	 * @param genObject   The JsonObject to parse.
	 * @param log         The {@link Logger} to log debug/error/etc. messages to.
	 * @return The {@link IFeatureGenerator} to be registered with an IFeatureHandler
	 */
	@Nonnull
	default IFeatureGenerator parseFeature(String featureName, Config genObject, Logger log) throws InvalidDistributionException {

		boolean retrogen = false;
		if (genObject.hasPath("retrogen")) {
			retrogen = genObject.getBoolean("retrogen");
		}

		IConfigurableFeatureGenerator feature = getFeature(featureName, genObject, retrogen, log);

		{
			if (genObject.hasPath("chunk-chance")) {
				int rarity = MathHelper.clamp(genObject.getInt("chunk-chance"), 1, 1000000000);
				feature.setRarity(rarity);
			}
			addFeatureRestrictions(feature, genObject, log);
			if (genObject.hasPath("in-village")) {
				feature.setWithVillage(genObject.getBoolean("in-village"));
			}
		}
		return feature;
	};

	@Nonnull
	IConfigurableFeatureGenerator getFeature(String featureName, Config genObject, boolean retrogen, Logger log) throws InvalidDistributionException;

	class InvalidDistributionException extends Exception {

		private final ConfigOrigin origin;

		public InvalidDistributionException(String cause, ConfigOrigin origin) {

			super(cause);
			this.origin = origin;
		}

		public ConfigOrigin origin() {

			return this.origin;
		}

		public InvalidDistributionException causedBy(Throwable cause) {

			this.initCause(cause);
			return this;
		}

	}

}
