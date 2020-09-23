package cofh.cofhworld.parser;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedWorldGenerator;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenMulti;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import java.util.ArrayList;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class GeneratorData {

	public static WorldGen parseGenerator(String def, Config genObject) throws IGeneratorParser.InvalidGeneratorException {

		if (!genObject.hasPath("generator")) {
			throw new IGeneratorParser.InvalidGeneratorException("No `generator` entry present", genObject.origin());
		}
		ConfigValue genData = genObject.getValue("generator");
		if (genData.valueType() == ConfigValueType.LIST) {
			List<? extends Config> list = genObject.getConfigList("generator");
			ArrayList<WeightedWorldGenerator> gens = new ArrayList<>(list.size());
			for (Config genElement : list) {
				WorldGen gen = parseGeneratorData(def, genElement);
				int weight = genElement.hasPath("weight") ? genElement.getInt("weight") : 100;
				gens.add(new WeightedWorldGenerator(gen, weight));
			}
			return new WorldGenMulti(gens);
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			return parseGeneratorData(def, genObject.getConfig("generator"));
		} else {
			log.error("Invalid data type for field 'generator'. > It must be an object or list.");
			throw new IGeneratorParser.InvalidGeneratorException("Invalid data type", genData.origin());
		}
	}

	public static WorldGen parseGeneratorData(String def, Config genObject) throws IGeneratorParser.InvalidGeneratorException {

		String name = def;
		if (genObject.hasPath("type")) {
			name = genObject.getString("type");
			if (null == FeatureParser.getGenerator(name)) {
				log.warn("Unknown generator '{}'! using '{}'", name, def);
				name = def;
			}
		}

		IGeneratorParser parser = FeatureParser.getGenerator(name);
		if (parser == null) {
			throw new IGeneratorParser.InvalidGeneratorException("Generator '" + name + "' is not registered!", genObject.origin());
		} else if (parser.isMeta() && name == def) { // name == def is not a bug, we are checking to see if `type` was absent
			throw new IllegalStateException("Default generator for a distribution is a meta generator!");
		}

		boolean missedFields = false;
		for (String field : parser.getRequiredFields()) {
			if (!genObject.hasPath(field)) {
				log.error("Missing required setting `{}` for generator type '{}' on line {}.", field, name, genObject.origin().lineNumber());
				missedFields = true;
			}
		}
		if (missedFields) {
			throw new IGeneratorParser.InvalidGeneratorException("Missing fields", genObject.origin());
		}

		List<WeightedBlock> resList = new ArrayList<>();
		if (!parser.isMeta() && !genObject.hasPath("block")) {
			log.error("Generators cannot generate blocks unless `block` is specified.");
			throw new IGeneratorParser.InvalidGeneratorException("`block` not specified", genObject.origin());
		} else if (!BlockData.parseBlockList(genObject.root().get("block"), resList) && !parser.isMeta()) {
			throw new IGeneratorParser.InvalidGeneratorException("`block` not valid", genObject.getValue("block").origin());
		}

		List<Material> matList = new ArrayList<>();
		if (!parser.isMeta() && !genObject.hasPath("material")) {
			log.error("Generators cannot generate blocks unless `material` is specified.");
			throw new IGeneratorParser.InvalidGeneratorException("`material` not specified", genObject.origin());
		} else if (!BlockData.parseMaterialList(genObject.root().get("material"), matList) && !parser.isMeta()) {
			throw new IGeneratorParser.InvalidGeneratorException("`material` not valid", genObject.getValue("material").origin());
		}

		WorldGen r = parser.parseGenerator(parser.isMeta() ? def : name, genObject, log, resList, matList);

		if (genObject.hasPath("offset.x")) {
			r.setXVar(NumberData.parseNumberValue(genObject.getValue("offset.x"), -128, 128));
		}
		if (genObject.hasPath("offset.y")) {
			r.setYVar(NumberData.parseNumberValue(genObject.getValue("offset.y")));
		}
		if (genObject.hasPath("offset.z")) {
			r.setZVar(NumberData.parseNumberValue(genObject.getValue("offset.z"), -128, 128));
		}
		return r;
	}
}
