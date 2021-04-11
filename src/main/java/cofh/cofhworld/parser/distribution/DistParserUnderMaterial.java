package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderUnderMaterial;
import cofh.cofhworld.world.distribution.DistributionUnderMaterial;

public class DistParserUnderMaterial extends AbstractDistParser<DistributionUnderMaterial, BuilderUnderMaterial> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionUnderMaterial, BuilderUnderMaterial> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderUnderMaterial::new);

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderUnderMaterial::setSurface, "fluid", "ceiling");
		fields.addOptionalField("material", Type.MATERIAL_LIST, BuilderUnderMaterial::setMaterial);
	}

}
