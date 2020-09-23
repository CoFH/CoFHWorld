package cofh.cofhworld.parser;

import cofh.cofhworld.parser.variables.BiomeData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.IFeatureGenerator;
import com.google.gson.JsonObject;
import com.typesafe.config.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Locale;

public interface IDistributionParser {

	static void addFeatureRestrictions(IConfigurableFeatureGenerator feature, Config genObject, Logger log) throws InvalidDistributionException {

		{ // structures
			GenRestriction structureRes = GenRestriction.NONE;
			if (genObject.hasPath("structures")) {
				ConfigValue data = genObject.getValue("structures");
				if (data.valueType() == ConfigValueType.STRING) {
					structureRes = GenRestriction.get(genObject.getString("structures"));
					if (structureRes != GenRestriction.NONE) {
						log.error("Invalid structure restriction `{}` on '{}'. Must be an object to meaningfully function", structureRes.name().toLowerCase(Locale.US), feature.getFeatureName());
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else if (data.valueType() == ConfigValueType.OBJECT) {
					structureRes = GenRestriction.get(genObject.getString("structures.restriction"));
					ArrayList<WeightedString> structures = new ArrayList<>();
					if (StringData.parseStringList(genObject.getValue("structures.value"), structures)) {
						feature.addStructures(structures.stream().map(str -> str.value).distinct().toArray(String[]::new));
					} else {
						log.error("Invalid structure list on '{}'. No values added!", feature.getFeatureName());
					}
				}
			}
			feature.setStructureRestriction(structureRes);
		}
		{ // biomes
			GenRestriction biomeRes = GenRestriction.NONE;
			if (genObject.hasPath("biome")) {
				ConfigValue data = genObject.getValue("biome");
				if (data.valueType() == ConfigValueType.STRING) {
					biomeRes = GenRestriction.get(genObject.getString("biome"));
					if (biomeRes != GenRestriction.NONE) {
						log.error("Invalid biome restriction `{}` on '{}'. Must be an object to meaningfully function", biomeRes.name().toLowerCase(Locale.US), feature.getFeatureName());
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
							log.error("Invalid dimension restriction `{}` on '{}'. Must be an object to meaningfully function", dimRes.name().toLowerCase(Locale.US), feature.getFeatureName());
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
						if (val.valueType() == ConfigValueType.STRING) {
							ResourceLocation dimName = new ResourceLocation(String.valueOf(val.unwrapped()));
							if (Registry.DIMENSION_TYPE.containsKey(dimName)) {
								feature.addDimension(Registry.DIMENSION_TYPE.getValue(dimName).get().getId());
							} else {
								log.error("Invalid dimension entry `{}` on line {}. No dimension with that identifier is registered.", dimName, val.origin().lineNumber());
							}
						} else if (val.valueType() == ConfigValueType.NUMBER) {
							feature.addDimension(((Number) val.unwrapped()).intValue());
							// don't bother validating registration; dimensions can be created while a world is running
						} else if (val.valueType() != ConfigValueType.NULL) {
							// skip over accidental (and intentional, i guess? we can't tell.) nulls from multiple sequential commas
							log.error("Invalid dimension entry type `{}` on line {}. Number required.", val.valueType().name(), data.origin().lineNumber());
							throw new InvalidDistributionException("Invalid value for dimension id, expected number got " + val.valueType().name(), data.origin());
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
		}
		return feature;
	}

	;

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
