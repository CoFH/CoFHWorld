package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserMaterial;
import cofh.cofhworld.parser.generator.builders.BuilderSmallTree;
import cofh.cofhworld.world.generator.WorldGenSmallTree;

public class GenParserSmallTree implements AbstractGenParserMaterial<WorldGenSmallTree, BuilderSmallTree> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenSmallTree, BuilderSmallTree> fields) {

		AbstractGenParserMaterial.super.getFields(fields);
		fields.setConstructor(BuilderSmallTree::new);

		fields.addRequiredField("trunk", Type.BLOCK_LIST, BuilderSmallTree::setResource, "resource", "block");

		fields.addOptionalField("leaves", Type.BLOCK_LIST, BuilderSmallTree::setLeaves);
		fields.addOptionalField("leaves-variance", Type.CONDITION, BuilderSmallTree::setLeafVariance);

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderSmallTree::setSurface);

		fields.addOptionalField("height", Type.NUMBER, BuilderSmallTree::setSize);

		fields.addOptionalField("tree-checks", Type.CONDITION, BuilderSmallTree::setTreeChecks);
		fields.addOptionalField("relaxed-growth", Type.CONDITION, BuilderSmallTree::setRelaxedGrowth);
		fields.addOptionalField("water-loving", Type.CONDITION, BuilderSmallTree::setWaterLoving);
	}

}
