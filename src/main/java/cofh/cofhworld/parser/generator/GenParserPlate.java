package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderPlate;

public class GenParserPlate extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderPlate::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderPlate::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderPlate::setHeight);
		fields.addOptionalField("slim", Type.CONDITION, BuilderPlate::setSlim);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderPlate::setShape);

		return fields;
	}

}
