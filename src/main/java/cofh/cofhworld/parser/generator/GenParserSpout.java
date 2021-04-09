package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderSpout;
import cofh.cofhworld.world.generator.WorldGenSpout;

public class GenParserSpout implements AbstractGenParserResource<WorldGenSpout, BuilderSpout> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenSpout, BuilderSpout> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderSpout::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderSpout::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderSpout::setHeight);
		fields.addOptionalField("mirror-below", Type.CONDITION, BuilderSpout::setMirror);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderSpout::setShape);
	}

}
