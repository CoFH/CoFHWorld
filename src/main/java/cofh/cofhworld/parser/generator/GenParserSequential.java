package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderSequential;
import cofh.cofhworld.world.generator.WorldGenSequential;

public class GenParserSequential implements IGeneratorParser<WorldGenSequential, BuilderSequential> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenSequential, BuilderSequential> fields) {

		fields.setConstructor(BuilderSequential::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderSequential::setGenerators);
	}

}
