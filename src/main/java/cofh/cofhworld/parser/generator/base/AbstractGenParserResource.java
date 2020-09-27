package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;

public class AbstractGenParserResource extends AbstractGenParserMaterial {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.addRequiredField("resource", Type.BLOCK_LIST, BaseBuilder::SET_RESOURCE, "block"); // can't reference instance methods on abstract classes

		return fields;
	}
}
