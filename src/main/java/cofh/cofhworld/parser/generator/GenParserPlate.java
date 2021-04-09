package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderPlate;
import cofh.cofhworld.world.generator.WorldGenPlate;

public class GenParserPlate implements AbstractGenParserResource<WorldGenPlate, BuilderPlate> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenPlate, BuilderPlate> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderPlate::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderPlate::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderPlate::setHeight);
		fields.addOptionalField("slim", Type.CONDITION, BuilderPlate::setSlim);
		fields.addOptionalField("mirror-below", Type.CONDITION, BuilderPlate::setMirror);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderPlate::setShape);
	}

}
