package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.IBuilder.BuilderFields;
import cofh.cofhworld.parser.IGeneratorParser.InvalidGeneratorException;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedWorldGenerator;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenMulti;
import com.typesafe.config.*;

import java.util.ArrayList;

import static cofh.cofhworld.CoFHWorld.log;

public class GeneratorData {

	public static ArrayList<WorldGen> parseGenerators(ConfigValue genData) throws IGeneratorParser.InvalidGeneratorException {

		ArrayList<WorldGen> gens = new ArrayList<>(1);

		if (genData.valueType() == ConfigValueType.LIST) {
			ConfigList list = (ConfigList) genData;
			gens.ensureCapacity(list.size());
			for (ConfigValue genElement : list) {
				if (genElement.valueType() == ConfigValueType.OBJECT) {
					Config elementConfig = ((ConfigObject) genElement).toConfig();
					WorldGen gen = parseGeneratorData(elementConfig);
					gens.add(gen);
				} else {
					log.error("Invalid data type for field 'generator'. > It must be an object.");
					throw new IGeneratorParser.InvalidGeneratorException("Invalid data type", genElement.origin());
				}
			}
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			gens.add(parseGeneratorData(((ConfigObject) genData).toConfig()));
		} else {
			log.error("Invalid data type for field 'generator'. > It must be an object or list.");
			throw new IGeneratorParser.InvalidGeneratorException("Invalid data type", genData.origin());
		}

		return gens;
	}

	public static WorldGen parseGenerator(ConfigValue genData) throws IGeneratorParser.InvalidGeneratorException {

		if (genData.valueType() == ConfigValueType.LIST) {
			ConfigList list = (ConfigList) genData;
			ArrayList<WeightedWorldGenerator> gens = new ArrayList<>(list.size());
			for (ConfigValue genElement : list) {
				if (genElement.valueType() == ConfigValueType.OBJECT) {
					Config elementConfig = ((ConfigObject) genElement).toConfig();
					WorldGen gen = parseGeneratorData(elementConfig);
					int weight = elementConfig.hasPath("weight") ? elementConfig.getInt("weight") : 100;
					gens.add(new WeightedWorldGenerator(gen, weight));
				} else {
					log.error("Invalid data type for field 'generator'. > It must be an object.");
					throw new IGeneratorParser.InvalidGeneratorException("Invalid data type", genElement.origin());
				}
			}
			return new WorldGenMulti(gens);
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			return parseGeneratorData(((ConfigObject) genData).toConfig());
		} else {
			log.error("Invalid data type for field 'generator'. > It must be an object or list.");
			throw new IGeneratorParser.InvalidGeneratorException("Invalid data type", genData.origin());
		}
	}

	public static WorldGen parseGeneratorData(Config genObject) throws IGeneratorParser.InvalidGeneratorException {

		String name;
		if (genObject.hasPath("type")) {
			ConfigValue typeVal = genObject.getValue("type");
			if (typeVal.valueType() == ConfigValueType.STRING) {
				name = String.valueOf(typeVal.unwrapped());
			} else if (typeVal.valueType() == ConfigValueType.OBJECT && genObject.hasPath("type.name")) {
				name = genObject.getString("type.name");
			} else {
				throw new IGeneratorParser.InvalidGeneratorException("Generator `type` entry not valid", genObject.origin());
			}
		} else {
			throw new IGeneratorParser.InvalidGeneratorException("Generator `type` entry is not specified!", genObject.origin());
		}

		BuilderFields<? extends WorldGen> genData = FeatureParser.getGenerator(name);
		if (genData == null) {
			throw new IGeneratorParser.InvalidGeneratorException("Generator '" + name + "' is not registered!", genObject.origin());
		}

		WorldGen r;
		try {
			r = genData.parse(genObject, field -> {
				log.error("Missing required setting `{}` for generator type '{}' on line {}.", field, name, genObject.origin().lineNumber());
			}, (field, origin) -> {
				if (field.length() > 0 & (field.charAt(0) == '_' || "type".equals(field))) return false;
				log.warn("Unknown setting `{}` for generator type '{}' on line {}", field, name, origin.lineNumber());
				return false;
			});
		} catch (InvalidConfigurationException e) {
			throw new InvalidGeneratorException(e.getMessage(), e.origin());
		}

		if (genObject.hasPath("offset.x")) {
			r.setOffsetX(NumberData.parseNumberValue(genObject.getValue("offset.x"), -128, 128));
		}
		if (genObject.hasPath("offset.y")) {
			r.setOffsetY(NumberData.parseNumberValue(genObject.getValue("offset.y")));
		}
		if (genObject.hasPath("offset.z")) {
			r.setOffsetZ(NumberData.parseNumberValue(genObject.getValue("offset.z"), -128, 128));
		}
		return r;
	}
}
