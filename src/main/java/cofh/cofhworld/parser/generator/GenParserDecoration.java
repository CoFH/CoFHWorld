package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderDecoration;

public class GenParserDecoration extends AbstractGenParserResource {

	private static String[] FIELDS = new String[] { "block", "material", "quantity" };

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderDecoration::new);

		fields.addRequiredField("quantity", Type.NUMBER, BuilderDecoration::setSize);

		fields.addOptionalField("stack-height", Type.NUMBER, BuilderDecoration::setStackHeight);

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderDecoration::setSurface);

		fields.addOptionalField("see-sky", Type.CONDITION, BuilderDecoration::setSeeSky);

		fields.addOptionalField("check-stay", Type.CONDITION, BuilderDecoration::setCheckStay);

		return fields;
	}

}
