package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.PlaneShape;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.EnumData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenPlate;
import com.typesafe.config.Config;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserPlate implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "radius" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		INumberProvider clusterSize = NumberData.parseNumberValue(genObject.getValue("radius"), 0, 32);

		WorldGenPlate r = new WorldGenPlate(resList, clusterSize, matList);
		{
			if (genObject.hasPath("height")) {
				r.setHeight(NumberData.parseNumberValue(genObject.getValue("height"), 0, 64));
			}
			if (genObject.hasPath("slim")) {
				r.setSlim(genObject.getBoolean("slim"));
			}
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
		}
		return r;
	}

}
