package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderCave;
import cofh.cofhworld.world.distribution.DistributionCave;

public class DistParserCave extends AbstractDistParser<DistributionCave, BuilderCave> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionCave, BuilderCave> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderCave::new);

		fields.addOptionalField("ceiling", Type.RAW_BOOLEAN, BuilderCave::setCeiling);
		fields.addOptionalField("ground-level", Type.NUMBER, BuilderCave::setGroundLevel);
	}

}
