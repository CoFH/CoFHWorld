package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.world.generator.WorldGenMinablePlate;
import com.typesafe.config.Config;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GenParserPlate implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "radius" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("radius");
		if (clusterSize <= 0) {
			log.warn("Invalid `radius` for generator '{}'", name);
			throw new InvalidGeneratorException("Invalid `radius`", genObject.getValue("radius").origin());
		}

		WorldGenMinablePlate r = new WorldGenMinablePlate(resList, MathHelper.clamp(clusterSize, 0, 32), matList);
		{
			if (genObject.hasPath("height")) {
				r.setHeight(NumberData.parseNumberValue(genObject.getValue("height"), 0, 64));
			}
			if (genObject.hasPath("slim")) {
				r.setSlim(genObject.getBoolean("slim"));
			}
		}
		return r;
	}

}
