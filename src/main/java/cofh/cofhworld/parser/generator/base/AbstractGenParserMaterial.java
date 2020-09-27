package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;

public abstract class AbstractGenParserMaterial implements IGeneratorParser {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields.addRequiredField("material", Type.MATERIAL_LIST, BaseBuilder::SET_MATERIAL); // can't instance reference methods on abstract classes

		return fields;
	}

}
