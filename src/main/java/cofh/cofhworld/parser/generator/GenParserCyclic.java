package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderCyclic;

public class GenParserCyclic implements IGeneratorParser<BuilderCyclic> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderCyclic> fields) {

		fields.setBuilder(BuilderCyclic::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderCyclic::setGenerators);
	}

}
