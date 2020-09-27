package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderAdvLake;

public class GenParserLake implements AbstractGenParserResource<BuilderAdvLake> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderAdvLake> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(BuilderAdvLake::new);

		fields.addOptionalField("outline", Type.BLOCK_LIST, BuilderAdvLake::setOutline, "outline-block");
		fields.addOptionalField("outline-condition", Type.CONDITION, BuilderAdvLake::setOutlineCondition);

		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderAdvLake::setFiller, "gap-block");
	}

}
