package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.world.generator.WorldGen;

public interface AbstractGenParserResource<T extends WorldGen, B extends IBuilder<T>> extends AbstractGenParserMaterial<T, B> {

	@Override
	default void getFields(IBuilderFieldRegistry<T, B> fields) {

		AbstractGenParserMaterial.super.getFields(fields);
		fields.addRequiredField("resource", Type.BLOCK_LIST, BaseBuilder::SET_RESOURCE, "block"); // can't reference instance methods on abstract classes
	}
}
