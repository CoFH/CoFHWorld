package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.IBuilder.BuilderFields;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder.FeatureNameData;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static cofh.cofhworld.CoFHWorld.log;

public interface IDistributionParser<T extends IConfigurableFeatureGenerator, B extends IBuilder<T>> {

	void getFields(IBuilderFieldRegistry<T, B> fields);

	static void addFeatureRestrictions(IConfigurableFeatureGenerator feature, Config genObject, Logger log) throws InvalidDistributionException {

		{ // structures
			final String FIELD = "structures";

			GenRestriction structureRes = GenRestriction.NONE;
			if (genObject.hasPath(FIELD)) {
				log.trace("'{}' has structure restrictions", feature.getFeatureName());
				ConfigValue data = genObject.getValue(FIELD);

				if (data.valueType() == ConfigValueType.STRING) {
					structureRes = GenRestriction.get(genObject.getString(FIELD));
					log.trace("'{}' has explicit structure restriction type {}", feature.getFeatureName(), structureRes.name());

					if (structureRes != GenRestriction.NONE) {
						log.error("Invalid structure restriction `{}` on '{}'. Must be an object to meaningfully function", structureRes.name().toLowerCase(Locale.US),
								feature.getFeatureName());
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else //
					if (data.valueType() == ConfigValueType.OBJECT) {
						structureRes = GenRestriction.get(genObject.getString(FIELD + ".restriction"));
						log.trace("'{}' has explicit structure restriction type {}", feature.getFeatureName(), structureRes.name());

						ArrayList<WeightedString> structures = new ArrayList<>();
						if (StringData.parseStringList(genObject.getValue(FIELD + ".value"), structures)) {
							log.trace("'{}' has structure restriction for values {}", feature.getFeatureName(), structures);
							//Structure<?> structure = Registry.STRUCTURE_FEATURE.getOrDefault(new ResourceLocation(s.toLowerCase(Locale.ROOT)));
							feature.addStructures(structures.stream().map(str -> str.value).distinct().
									map(str -> new ResourceLocation(str.toLowerCase(Locale.ROOT))).toArray(ResourceLocation[]::new));
						} else {
							log.error("Invalid structure list on '{}'. No values added!", feature.getFeatureName());
						}
					}
			}
			feature.setStructureRestriction(structureRes);
		}
		{ // biomes
			final String FIELD = "biome";

			GenRestriction biomeRes = GenRestriction.NONE;
			if (genObject.hasPath(FIELD)) {
				log.trace("'{}' has biome restrictions", feature.getFeatureName());
				ConfigValue data = genObject.getValue(FIELD);

				if (data.valueType() == ConfigValueType.STRING) {
					biomeRes = GenRestriction.get(genObject.getString(FIELD));
					log.trace("'{}' has explicit biome restriction type {}", feature.getFeatureName(), biomeRes.name());

					if (biomeRes != GenRestriction.NONE) {
						log.error("Invalid biome restriction `{}` on '{}'. Must be an object to meaningfully function", biomeRes.name().toLowerCase(Locale.US),
								feature.getFeatureName());
						throw new InvalidDistributionException("Invalid value for string", data.origin());
					}
				} else //
					if (data.valueType() == ConfigValueType.OBJECT) {
						biomeRes = GenRestriction.get(genObject.getString(FIELD + ".restriction"));
						log.trace("'{}' has explicit biome restriction type {}", feature.getFeatureName(), biomeRes.name());
						feature.addBiomes(BiomeData.parseBiomeRestrictions(genObject.getConfig(FIELD)));
					}
			}
			feature.setBiomeRestriction(biomeRes);
		}
		{// dimensions
			final String FIELD = "dimension";

			GenRestriction dimRes = GenRestriction.NONE;
			if (genObject.hasPath(FIELD)) {
				log.trace("'{}' has dimension restrictions", feature.getFeatureName());
				ConfigValue data = genObject.getValue(FIELD);
				ConfigList restrictionList = null;

				switch (data.valueType()) {
					case STRING:
						dimRes = GenRestriction.get(genObject.getString(FIELD));
						log.trace("'{}' has explicit dimension restriction type {}", feature.getFeatureName(), dimRes.name());
						if (dimRes != GenRestriction.NONE) {
							log.error("Invalid dimension restriction `{}` on '{}'. Must be an object to meaningfully function", dimRes.name().toLowerCase(Locale.US),
									feature.getFeatureName());
							throw new InvalidDistributionException("Invalid value for string", data.origin());
						}
						break;
					case OBJECT:
						dimRes = GenRestriction.get(genObject.getString(FIELD + ".restriction"));
						log.trace("'{}' has explicit dimension restriction type {}", feature.getFeatureName(), dimRes.name());
						restrictionList = genObject.getList(FIELD + ".value");
						break;
					case LIST:
						dimRes = GenRestriction.WHITELIST;
						log.trace("'{}' has implicit dimension restriction type {}", feature.getFeatureName(), dimRes.name());
						restrictionList = genObject.getList(FIELD);
						break;
					case NULL:
						break;
					default:
						log.error("Feature '{}' has unknown dimension restriction of type `{}` on line {}", feature.getFeatureName(), data.valueType().toString(),
								data.origin().lineNumber());
						break;
				}
				if (restrictionList != null) {
					Registry<Dimension> reg = LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getServerConfiguration().
							getDimensionGeneratorSettings().func_236224_e_();

					for (int i = 0; i < restrictionList.size(); i++) { // TODO: allow dimension type? multiple dimensions can have the same type
						ConfigValue val = restrictionList.get(i);
						log.trace("'{}' has dimension restriction for value {}", feature.getFeatureName(), val.unwrapped());

						if (val.valueType() == ConfigValueType.STRING) {
							ResourceLocation dimName = new ResourceLocation(String.valueOf(val.unwrapped()));
							if (!reg.keySet().contains(dimName)) {
								log.warn("Potentially invalid dimension entry `{}` on line {}. No dimension with that identifier is registered. " +
												"Adding restriction anyway.",
										dimName, val.origin().lineNumber());
							}
							feature.addDimension(dimName);
						} else //
							if (val.valueType() != ConfigValueType.NULL) {
								// skip over accidental (and intentional, i guess? we can't tell.) nulls from multiple sequential commas
								log.error("Invalid dimension entry type `{}` on line {}. String required.", val.valueType().name(), data.origin().lineNumber());
								throw new InvalidDistributionException("Invalid value for dimension id, expected string got " + val.valueType().name(), data.origin());
							}
					}
				}
			}
			feature.setDimensionRestriction(dimRes);
		}

	}

	/**
	 * Parse a {@link Config} for registration}.
	 *
	 * @param featureName
	 * 		The name of the feature to register.
	 * @param genObject
	 * 		The JsonObject to parse.
	 *
	 * @return The {@link IFeatureGenerator} to be registered with an IFeatureHandler
	 */
	static IFeatureGenerator parseFeature(String featureName, Config genObject) throws InvalidDistributionException {

		String name;
		{
			final String FIELD = "distribution";

			if (genObject.hasPath(FIELD)) {
				ConfigValue typeVal = genObject.getValue(FIELD);
				if (typeVal.valueType() == ConfigValueType.STRING) {
					name = String.valueOf(typeVal.unwrapped());
				} else if (typeVal.valueType() == ConfigValueType.OBJECT && genObject.hasPath(FIELD + ".name")) {
					name = genObject.getString(FIELD + ".name");
				} else {
					throw new InvalidDistributionException("Feature `distribution` entry not valid", genObject.origin());
				}
			} else {
				throw new InvalidDistributionException("Feature `distribution` entry is not specified!", genObject.origin());
			}
		}

		BuilderFields<? extends IConfigurableFeatureGenerator> genData = FeatureParser.getDistribution(name);
		if (genData == null) {
			throw new InvalidDistributionException("Distribution '" + name + "' is not registered!", genObject.origin());
		}

		IConfigurableFeatureGenerator feature;
		try {
			feature = genData.parse(genObject, field -> {
				log.error("Missing required setting `{}` for generator type '{}' on line {}.", field, name, genObject.origin().lineNumber());
			}, (field, origin) -> {
				if (field.length() > 0 & (field.charAt(0) == '_' || InvalidDistributionException.EXTERNAL_FIELDS.contains(field))) return false;
				log.warn("Unknown setting `{}` for generator type '{}' on line {}", field, name, origin.lineNumber());
				return false;
			}, FeatureNameData.of(featureName)
			);
		} catch (InvalidConfigurationException e) {
			throw new InvalidDistributionException(e.getMessage(), e.origin());
		}

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

	class InvalidDistributionException extends InvalidConfigurationException {

		private static List<String> EXTERNAL_FIELDS = Arrays.asList("distribution", "enabled", "biome", "dimension", "structures", "chunk-chance");

		public InvalidDistributionException(String cause, ConfigOrigin origin) {

			super(cause, origin);
		}

		public InvalidDistributionException causedBy(Throwable cause) {

			this.initCause(cause);
			return this;
		}

	}

}
