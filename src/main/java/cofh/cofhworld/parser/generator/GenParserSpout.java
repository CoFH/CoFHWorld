package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenSpout;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserSpout implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "radius", "height" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		INumberProvider radius = NumberData.parseNumberValue(genObject.getValue("radius"), 0, 8);
		INumberProvider height = NumberData.parseNumberValue(genObject.getValue("height"), 0, Integer.MAX_VALUE);

		WorldGenSpout r = new WorldGenSpout(resList, matList, radius, height);
		{
			if (genObject.hasPath("shape")) {
				r.setShape(genObject.getString("shape"));
			}
		}
		return r;
	}

}
