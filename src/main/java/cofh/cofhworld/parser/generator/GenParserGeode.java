package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderGeode;

public class GenParserGeode extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderGeode::new);

		fields.addOptionalField("crust", Type.BLOCK_LIST, BuilderGeode::setOutline, "outline"); // TODO: require?

		fields.addOptionalField("hollow", Type.CONDITION, BuilderGeode::setHollow);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderGeode::setFiller, "fill-block");

		return fields;
	}

}
