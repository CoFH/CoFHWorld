package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderSurface;
import cofh.cofhworld.world.distribution.DistributionSurface;

public class DistParserSurface extends AbstractDistParser<DistributionSurface, BuilderSurface> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionSurface, BuilderSurface> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderSurface::new);

		fields.addOptionalField("material", Type.MATERIAL_LIST, BuilderSurface::setMaterial);
		fields.addOptionalField("top-block", Type.RAW_BOOLEAN, BuilderSurface::setTopBlock);
	}

}
