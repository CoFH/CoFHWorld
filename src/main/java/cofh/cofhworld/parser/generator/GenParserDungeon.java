package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserMaterial;
import cofh.cofhworld.parser.generator.builders.BuilderDungeon;

public class GenParserDungeon implements AbstractGenParserMaterial<BuilderDungeon> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderDungeon> fields) {

		AbstractGenParserMaterial.super.getFields(fields);
		fields.setBuilder(BuilderDungeon::new);

		fields.addRequiredField("wall", Type.BLOCK_LIST, BuilderDungeon::setResource, "block");

		fields.addOptionalField("floor", Type.BLOCK_LIST, BuilderDungeon::setFloor);
		fields.addOptionalField("spawner", Type.BLOCK_LIST, BuilderDungeon::setSpawners);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderDungeon::setFiller);
		fields.addOptionalField("chest", Type.BLOCK_LIST, BuilderDungeon::setChests);

		fields.addOptionalField("chest-count", Type.NUMBER, BuilderDungeon::setChestCount);
		fields.addOptionalField("chest-attempts", Type.NUMBER, BuilderDungeon::setChestAttempts);

		fields.addOptionalField("check-hole", Type.CONDITION, BuilderDungeon::setHoleCondition);
		fields.addOptionalField("check-hole-count", Type.CONDITION, BuilderDungeon::setHoleCount);

		fields.addOptionalField("height", Type.NUMBER, BuilderDungeon::setHeight);
		fields.addOptionalField("radius-x", Type.NUMBER, BuilderDungeon::setRadiusX);
		fields.addOptionalField("radius-z", Type.NUMBER, BuilderDungeon::setRadiusZ);
	}

}
