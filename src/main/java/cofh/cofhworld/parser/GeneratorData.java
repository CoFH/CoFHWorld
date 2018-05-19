package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomWorldGenerator;
import cofh.cofhworld.util.exceptions.InvalidGeneratorException;
import cofh.cofhworld.world.generator.WorldGenMulti;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class GeneratorData {

	public static WorldGenerator parseGenerator(String def, Config genObject, List<WeightedRandomBlock> defaultMaterial) throws InvalidGeneratorException {

		if (!genObject.hasPath("generator")) {
			throw new InvalidGeneratorException("No `generator` entry present", genObject.origin());
		}
		ConfigValue genData = genObject.root().get("generator");
		if (genData.valueType() == ConfigValueType.LIST) {
			List<? extends Config> list = genObject.getConfigList("generator");
			ArrayList<WeightedRandomWorldGenerator> gens = new ArrayList<>(list.size());
			for (Config genElement : list) {
				WorldGenerator gen = parseGeneratorData(def, genElement, defaultMaterial);
				int weight = genElement.hasPath("weight") ? genElement.getInt("weight") : 100;
				gens.add(new WeightedRandomWorldGenerator(gen, weight));
			}
			return new WorldGenMulti(gens);
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			return parseGeneratorData(def, genObject.getConfig("generator"), defaultMaterial);
		} else {
			log.error("Invalid data type for field 'generator'. > It must be an object or list.");
			throw new InvalidGeneratorException("Invalid data type", genData.origin());
		}
	}

	public static WorldGenerator parseGeneratorData(String def, Config genObject, List<WeightedRandomBlock> defaultMaterial) throws InvalidGeneratorException {

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
			throw new InvalidGeneratorException("Generator '" + name + "' is not registered!", genObject.origin());
		} else if (parser.isMeta() && name == def) { // name == def is not a bug, we are checking to see if `type` was absent
			throw new IllegalStateException("Default generator for a distribution is a meta generator!");
		}

		List<WeightedRandomBlock> resList = new ArrayList<>();
		if (!parser.isMeta() && !genObject.hasPath("block")) {
			log.error("Generators cannot generate blocks unless `block` is specified.");
			throw new InvalidGeneratorException("`block` not specified", genObject.origin());
		} else if (!BlockData.parseBlockList(genObject.getValue("block"), resList, true) && !parser.isMeta()) {
			throw new InvalidGeneratorException("`block` not valid", genObject.origin());
		}

		List<WeightedRandomBlock> matList = new ArrayList<>();
		if (!genObject.hasPath("material")) {
			log.debug("Using the default material list.");
			matList = defaultMaterial;
		} else if (!BlockData.parseBlockList(genObject.getValue("material"), matList, false)) {
			log.warn("Invalid material list! Using default list.");
			matList = defaultMaterial;
		}

		return parser.parseGenerator(parser.isMeta() ? def : name, genObject, log, resList, matList);
	}
}
