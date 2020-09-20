package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.GeneratorData;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenConsecutive;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserConsecutive implements IGeneratorParser {

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
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		ArrayList<WorldGen> gens;

		ConfigValue genData = genObject.getValue("generators");
		if (genData.valueType() == ConfigValueType.LIST) {
			List<? extends Config> list = genObject.getConfigList("generators");
			gens = new ArrayList<>(list.size());
			for (Config genElement : list) {
				gens.add(GeneratorData.parseGenerator(name, genElement.atKey("generator"), matList));
			}
		} else if (genData.valueType() == ConfigValueType.OBJECT) {
			gens = new ArrayList<>(1);
			gens.add(GeneratorData.parseGenerator(name, genObject.getConfig("generators").atKey("generator"), matList));
		} else {
			log.error("Invalid object type for generator on line {}.", genData.origin().lineNumber());
			throw new InvalidGeneratorException("Invalid object type", genData.origin());
		}

		return new WorldGenConsecutive(gens);
	}
}
