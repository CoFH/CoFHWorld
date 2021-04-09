package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderGeode;
import cofh.cofhworld.world.generator.WorldGenGeode;

public class GenParserGeode implements AbstractGenParserResource<WorldGenGeode, BuilderGeode> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenGeode, BuilderGeode> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderGeode::new);

		fields.addRequiredField("crust", Type.BLOCK_LIST, BuilderGeode::setOutline, "outline");

		fields.addOptionalField("hollow", Type.CONDITION, BuilderGeode::setHollow);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderGeode::setFiller, "fill-block");
	}

}
