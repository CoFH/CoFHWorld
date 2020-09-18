package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.PlaneShape;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.EnumData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.world.generator.WorldGenSpout;
import com.typesafe.config.Config;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
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
			// TODO: abstract into IShapedGenerator
			PlaneShape shape = null;
			Rotation rot = null;
			Mirror mirror = null;
			if (genObject.hasPath("shape")) {
				WeightedEnum<PlaneShape> val = EnumData.parseEnumEntry(genObject.getValue("shape"), PlaneShape.class);
				if (val != null) {
					shape = val.value;
				}
			}
			if (genObject.hasPath("shape-rotation")) {
				WeightedEnum<Rotation> val = EnumData.parseEnumEntry(genObject.getValue("shape-rotation"), Rotation.class);
				if (val != null) {
					rot = val.value;
				}
			}
			if (genObject.hasPath("shape-mirror")) {
				WeightedEnum<Mirror> val = EnumData.parseEnumEntry(genObject.getValue("shape-mirror"), Mirror.class);
				if (val != null) {
					mirror = val.value;
				}
			}
			r.setShape(shape, rot, mirror);
		}
		return r;
	}

}
