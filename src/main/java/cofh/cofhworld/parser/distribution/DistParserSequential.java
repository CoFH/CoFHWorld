package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.distribution.builders.BuilderSequential;
import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.world.distribution.DistributionSequential;

public class DistParserSequential implements IDistributionParser<DistributionSequential, BuilderSequential> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionSequential, BuilderSequential> fields) {

		fields.setConstructor(BuilderSequential::new);

		fields.addOptionalField("retrogen", Type.RAW_BOOLEAN, BaseBuilder::setRetrogen);

		fields.addRequiredField("features", Type.FEATURE_LIST, BuilderSequential::setFeatures);
	}

}
