package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderConsecutive;

public class GenParserConsecutive implements IGeneratorParser {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields.setBuilder(BuilderConsecutive::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderConsecutive::setGenerators);

		return fields;
	}

}
