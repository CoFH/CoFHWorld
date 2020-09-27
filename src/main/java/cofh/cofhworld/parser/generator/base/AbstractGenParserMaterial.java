package cofh.cofhworld.parser.generator.base;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.world.generator.WorldGen;

public interface AbstractGenParserMaterial<T extends IBuilder<? extends WorldGen>> extends IGeneratorParser<T> {

	@Override
	default void getFields(IGeneratorFieldRegistry<T> fields) {

		fields.addRequiredField("material", Type.MATERIAL_LIST, BaseBuilder::SET_MATERIAL); // can't reference instance methods on abstract classes
	}

}
