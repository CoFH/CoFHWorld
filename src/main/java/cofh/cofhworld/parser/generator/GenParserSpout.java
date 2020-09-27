package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderSpout;

public class GenParserSpout extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderSpout::new);

		fields.addRequiredField("radius", Type.NUMBER, BuilderSpout::setRadius);

		fields.addOptionalField("height", Type.NUMBER, BuilderSpout::setHeight);

		fields.addOptionalField("shape", Type.SHAPE_2D, BuilderSpout::setShape);

		return fields;
	}

}
