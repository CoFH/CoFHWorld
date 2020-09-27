package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderSequential;

public class GenParserSequential implements IGeneratorParser {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields.setBuilder(BuilderSequential::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderSequential::setGenerators);

		return fields;
	}

}
