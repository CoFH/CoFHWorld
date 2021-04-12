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
		fields.addOptionalField("max-height", Type.NUMBER, BuilderCave::setMaxLevel, "ground-level");
		fields.addOptionalField("min-height", Type.NUMBER, BuilderCave::setMinLevel);
		fields.addOptionalField("average-height", Type.NUMBER, BuilderCave::setAvgLevel);
		fields.addOptionalField("cave-condition", Type.CONDITION, BuilderCave::setCaveCondition);
	}

}
