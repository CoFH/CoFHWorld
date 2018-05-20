package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.GeneratorData;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.world.generator.WorldGenSequential;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GenParserSequential implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "generators" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	public boolean isMeta() {

		return true;
	}

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) throws InvalidGeneratorException {

		ArrayList<WorldGenerator> gens;

		ConfigValue genData = genObject.getValue("generators");
		if (genData.valueType() == ConfigValueType.LIST) {
			List<? extends Config> list = genObject.getConfigList("generators");
			gens = new ArrayList<>(list.size());
			for (Config genElement : list) {
				WorldGenerator gen = GeneratorData.parseGenerator(name, genElement.atKey("generator"), matList);
				gens.add(gen);
			}
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			gens = new ArrayList<>(1);
			WorldGenerator gen = GeneratorData.parseGenerator(name, genObject.getConfig("generators").atKey("generator"), matList);
			gens.add(gen);
		} else {
			log.error("Invalid object type for generator on line {}.", genData.origin().lineNumber());
			throw new InvalidGeneratorException("Invalid object type", genData.origin());
		}

		return new WorldGenSequential(gens);
	}

}
