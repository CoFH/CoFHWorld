package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.world.generator.WorldGen;

public interface AbstractGenParserMaterial<T extends WorldGen, B extends IBuilder<T>> extends IGeneratorParser<T, B> {

	@Override
	default void getFields(IBuilderFieldRegistry<T, B> fields) {

		fields.addRequiredField("material", Type.MATERIAL_LIST, BaseBuilder::SET_MATERIAL); // can't reference instance methods on abstract classes
	}

}
