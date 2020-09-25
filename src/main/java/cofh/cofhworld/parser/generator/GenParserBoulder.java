package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderBoulder;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserBoulder implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "diameter" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		BuilderBoulder builder = new BuilderBoulder(resList, matList);

		builder.setSize(NumberData.parseNumberValue(genObject.getValue("diameter")));

		if (genObject.hasPath("quantity")) {
			builder.setQuantity(NumberData.parseNumberValue(genObject.getValue("quantity")));
		}
		if (genObject.hasPath("hollow")) {
			builder.setHollow(ConditionData.parseConditionValue(genObject.getValue("hollow")));
		}
		if (genObject.hasPath("hollow-size")) {
			builder.setHollowAmt(NumberData.parseNumberValue(genObject.getValue("hollow-size")));
		}

		if (genObject.hasPath("variance.x")) {
			builder.setxVar(NumberData.parseNumberValue(genObject.getValue("variance.x")));
		}
		if (genObject.hasPath("variance.y")) {
			builder.setyVar(NumberData.parseNumberValue(genObject.getValue("variance.y")));
		}
		if (genObject.hasPath("variance.z")) {
			builder.setzVar(NumberData.parseNumberValue(genObject.getValue("variance.z")));
		}
		return builder.build();
	}

}
