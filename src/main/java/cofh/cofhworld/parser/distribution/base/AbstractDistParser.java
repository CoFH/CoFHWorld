package cofh.cofhworld.parser.distribution.base;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;

public abstract class AbstractDistParser<T extends IConfigurableFeatureGenerator, B extends BuilderGenerator<T>> implements IDistributionParser<T, B> {

	protected AbstractDistParser() {

	}

	@Override
	public void getFields(IBuilderFieldRegistry<T, B> fields) {

		fields.addRequiredField("generator", Type.GENERATOR, BuilderGenerator::setGenerator);
		fields.addRequiredField("cluster-count", Type.NUMBER, BuilderGenerator::setClusterCount);

		fields.addOptionalField("retrogen", Type.RAW_BOOLEAN, BaseBuilder::setRetrogen);
	}

}
