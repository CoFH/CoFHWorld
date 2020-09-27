package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderSpout;

public class GenParserSpout implements AbstractGenParserResource<BuilderSpout> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderSpout> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(BuilderSpout::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderSpout::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderSpout::setHeight);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderSpout::setShape);
	}

}
