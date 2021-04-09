package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderAdvLake;
import cofh.cofhworld.world.generator.WorldGenAdvLakes;

public class GenParserLake implements AbstractGenParserResource<WorldGenAdvLakes, BuilderAdvLake> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenAdvLakes, BuilderAdvLake> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderAdvLake::new);

		fields.addOptionalField("outline", Type.BLOCK_LIST, BuilderAdvLake::setOutline, "outline-block");
		fields.addOptionalField("outline-condition", Type.CONDITION, BuilderAdvLake::setOutlineCondition);

		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderAdvLake::setFiller, "gap-block");
	}

}
