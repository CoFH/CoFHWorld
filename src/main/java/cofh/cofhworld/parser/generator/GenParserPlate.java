package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderPlate;

public class GenParserPlate implements AbstractGenParserResource<BuilderPlate> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderPlate> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(BuilderPlate::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderPlate::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderPlate::setHeight);
		fields.addOptionalField("slim", Type.CONDITION, BuilderPlate::setSlim);
		fields.addOptionalField("mirror-below", Type.CONDITION, BuilderPlate::setMirror);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderPlate::setShape);
	}

}
