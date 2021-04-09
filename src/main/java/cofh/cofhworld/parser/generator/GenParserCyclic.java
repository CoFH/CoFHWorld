package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderCyclic;
import cofh.cofhworld.world.generator.WorldGenCyclic;

public class GenParserCyclic implements IGeneratorParser<WorldGenCyclic, BuilderCyclic> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenCyclic, BuilderCyclic> fields) {

		fields.setConstructor(BuilderCyclic::new);

		fields.addRequiredField("generators", Type.GENERATOR_LIST, BuilderCyclic::setGenerators);
	}

}
