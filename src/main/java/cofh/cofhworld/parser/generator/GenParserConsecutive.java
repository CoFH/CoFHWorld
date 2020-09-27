package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderConsecutive;

public class GenParserConsecutive implements IGeneratorParser<BuilderConsecutive> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderConsecutive> fields) {

		fields.setBuilder(BuilderConsecutive::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderConsecutive::setGenerators);
	}

}
