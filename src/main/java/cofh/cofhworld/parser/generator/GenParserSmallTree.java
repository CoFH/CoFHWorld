package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserMaterial;
import cofh.cofhworld.parser.generator.builders.BuilderSmallTree;

public class GenParserSmallTree extends AbstractGenParserMaterial {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderSmallTree::new);

		fields.addRequiredField("trunk", Type.BLOCK_LIST, BuilderSmallTree::setResource, "resource", "block");

		fields.addOptionalField("leaves", Type.BLOCK_LIST, BuilderSmallTree::setLeaves);
		fields.addOptionalField("leaves-variance", Type.CONDITION, BuilderSmallTree::setLeafVariance);

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderSmallTree::setSurface);

		fields.addOptionalField("height", Type.NUMBER, BuilderSmallTree::setSize);

		fields.addOptionalField("tree-checks", Type.CONDITION, BuilderSmallTree::setTreeChecks);
		fields.addOptionalField("relaxed-growth", Type.CONDITION, BuilderSmallTree::setTreeChecks);
		fields.addOptionalField("water-loving", Type.CONDITION, BuilderSmallTree::setWaterLoving);

		return fields;
	}

}
