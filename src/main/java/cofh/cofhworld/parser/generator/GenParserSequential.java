package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderSequential;

public class GenParserSequential implements IGeneratorParser<BuilderSequential> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderSequential> fields) {

		fields.setBuilder(BuilderSequential::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderSequential::setGenerators);
	}

}
