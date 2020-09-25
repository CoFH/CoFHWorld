package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderDecoration;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserDecoration implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "quantity" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		BuilderDecoration builder = new BuilderDecoration(resList, matList);

		builder.setSize(NumberData.parseNumberValue(genObject.getValue("quantity")));

		ArrayList<Material> list = new ArrayList<>();
		if (genObject.hasPath("surface")) {
			if (!BlockData.parseMaterialList(genObject.getValue("surface"), list)) {
				log.warn("Entry specifies invalid surface for 'decoration' generator! A partial list will be used!");
			}
			builder.setSurface(list);
		}
		if (genObject.hasPath("see-sky")) {
			builder.setSeeSky(ConditionData.parseConditionValue(genObject.getValue("see-sky")));
		}
		if (genObject.hasPath("check-stay")) {
			builder.setCheckStay(ConditionData.parseConditionValue(genObject.getValue("check-stay")));
		}
		if (genObject.hasPath("stack-height")) {
			builder.setStackHeight(NumberData.parseNumberValue(genObject.getValue("stack-height")));
		}
		return builder.build();
	}

}
