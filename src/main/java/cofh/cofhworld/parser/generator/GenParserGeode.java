package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderGeode;

public class GenParserGeode implements AbstractGenParserResource<BuilderGeode> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderGeode> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(BuilderGeode::new);

		fields.addRequiredField("crust", Type.BLOCK_LIST, BuilderGeode::setOutline, "outline");

		fields.addOptionalField("hollow", Type.CONDITION, BuilderGeode::setHollow);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderGeode::setFiller, "fill-block");
	}

}
