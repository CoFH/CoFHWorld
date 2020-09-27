package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderLargeVein;

public class GenParserLargeVein extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderLargeVein::new);

		fields.addRequiredField("vein-size", Type.NUMBER, BuilderLargeVein::setSize, "cluster-size");

		fields.addOptionalField("sparse", Type.CONDITION, BuilderLargeVein::setSparse);
		fields.addOptionalField("spindly", Type.CONDITION, BuilderLargeVein::setSpindly);

		return fields;
	}

}
