package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.world.generator.WorldGen;

public interface AbstractGenParserResource<T extends IBuilder<? extends WorldGen>> extends AbstractGenParserMaterial<T> {

	@Override
	default void getFields(IGeneratorFieldRegistry<T> fields) {

		AbstractGenParserMaterial.super.getFields(fields);
		fields.addRequiredField("resource", Type.BLOCK_LIST, BaseBuilder::SET_RESOURCE, "block"); // can't reference instance methods on abstract classes
	}
}
