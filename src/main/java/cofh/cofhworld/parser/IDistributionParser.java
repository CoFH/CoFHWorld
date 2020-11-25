package cofh.cofhworld.parser;

import cofh.cofhworld.parser.variables.BiomeData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Locale;

public interface IDistributionParser {

	static void addFeatureRestrictions(IConfigurableFeatureGenerator feature, Config genObject, Logger log) throws InvalidDistributionException {

		{ // structures
			GenRestriction structureRes = GenRestriction.NONE;
			if (genObject.hasPath("structures")) {
				log.trace("'{}' has structure restrictions", feature.getFeatureName());
				ConfigValue data = genObject.getValue("structures");
				if (data.valueType() == ConfigValueType.STRING) {
					structureRes = GenRestriction.get(genObject.getString("structures"));
					log.trace("'{}' has explicit structure restriction type {}", feature.getFeatureName(), structureRes.name());
					if (structureRes != GenRestriction.NONE) {
						log.error("Invalid structure restriction `{}` on '{}'. Must be an object to meaningfully function", structureRes.name().toLowerCase(Locale.US), feature.getFeatureName());
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else if (data.valueType() == ConfigValueType.OBJECT) {
					structureRes = GenRestriction.get(genObject.getString("structures.restriction"));
					log.trace("'{}' has explicit structure restriction type {}", feature.getFeatureName(), structureRes.name());
					ArrayList<WeightedString> structures = new ArrayList<>();
					if (StringData.parseStringList(genObject.getValue("structures.value"), structures)) {
						log.trace("'{}' has structure restriction for values {}", feature.getFeatureName(), structures);
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
				log.trace("'{}' has biome restrictions", feature.getFeatureName());
				ConfigValue data = genObject.getValue("biome");
				if (data.valueType() == ConfigValueType.STRING) {
					biomeRes = GenRestriction.get(genObject.getString("biome"));
					log.trace("'{}' has explicit biome restriction type {}", feature.getFeatureName(), biomeRes.name());
					if (biomeRes != GenRestriction.NONE) {
						log.error("Invalid biome restriction `{}` on '{}'. Must be an object to meaningfully function", biomeRes.name().toLowerCase(Locale.US), feature.getFeatureName());
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else if (data.valueType() == ConfigValueType.OBJECT) {
					biomeRes = GenRestriction.get(genObject.getString("biome.restriction"));
					log.trace("'{}' has explicit biome restriction type {}", feature.getFeatureName(), biomeRes.name());
					feature.addBiomes(BiomeData.parseBiomeRestrictions(genObject.getConfig("biome")));
				}
			}
			feature.setBiomeRestriction(biomeRes);
		}
		{// dimensions
			GenRestriction dimRes = GenRestriction.NONE;
			if (genObject.hasPath("dimension")) {
				log.trace("'{}' has dimension restrictions", feature.getFeatureName());
				String field = "dimension";
				ConfigValue data = genObject.getValue("dimension");
				ConfigList restrictionList = null;
				switch (data.valueType()) {
					case STRING:
						dimRes = GenRestriction.get(genObject.getString("dimension"));
						log.trace("'{}' has explicit dimension restriction type {}", feature.getFeatureName(), dimRes.name());
						if (dimRes != GenRestriction.NONE) {
							log.error("Invalid dimension restriction `{}` on '{}'. Must be an object to meaningfully function", dimRes.name().toLowerCase(Locale.US), feature.getFeatureName());
							throw new InvalidDistributionException("Invalid value for string", data.origin());
						}
						break;
					case OBJECT:
						dimRes = GenRestriction.get(genObject.getString(field + ".restriction"));
						log.trace("'{}' has explicit dimension restriction type {}", feature.getFeatureName(), dimRes.name());
						field += ".value";
						// continue
					case LIST:
						restrictionList = genObject.getList(field);
						break;
					case NUMBER:
						dimRes = GenRestriction.WHITELIST;
						log.trace("'{}' has implicit dimension restriction type {} for value {}", feature.getFeatureName(), dimRes.name(), data.unwrapped());
						feature.addDimension(genObject.getNumber(field).intValue());
						break;
				}
				if (restrictionList != null) {
					Registry<Dimension> reg = LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).func_244267_aX().getRegistry(Registry.DIMENSION_KEY);
					for (int i = 0; i < restrictionList.size(); i++) { // TODO: allow dimension type? multiple dimensions can have the same type
						ConfigValue val = restrictionList.get(i);
						log.trace("'{}' has dimension restriction for value {}", feature.getFeatureName(), val.unwrapped());
						if (val.valueType() == ConfigValueType.STRING) {
							ResourceLocation dimName = new ResourceLocation(String.valueOf(val.unwrapped()));
							if (reg.keySet().contains(dimName)) {
								feature.addDimension(reg.getId(reg.getOrDefault(dimName)));
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
	 * Parse a {@link Config} for registration}.
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
			log.trace("'{}' has retrogen setting {}", featureName, retrogen);
		} else
			log.trace("'{}' will not retrogen", featureName);

		IConfigurableFeatureGenerator feature = getFeature(featureName, genObject, retrogen, log);

		{
			if (genObject.hasPath("chunk-chance")) {
				int rarity = MathHelper.clamp(genObject.getInt("chunk-chance"), 1, 1000000000);
				log.trace("'{}' has rarity setting {}", featureName, rarity);
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
