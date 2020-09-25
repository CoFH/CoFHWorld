package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderSpout;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.parser.variables.ShapeData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserSpout implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "radius", "height" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		BuilderSpout builder = new BuilderSpout(resList, matList);

		builder.setRadius(NumberData.parseNumberValue(genObject.getValue("radius"), 0, 64));
		builder.setHeight(NumberData.parseNumberValue(genObject.getValue("height")));

		if (genObject.hasPath("shape")) {
			Shape2D shape = ShapeData.parse2DShapeEntry(genObject.getValue("shape"));
			if (shape != null) {
				builder.setShape(shape);
			}
		}
		return builder.build();
	}

}
