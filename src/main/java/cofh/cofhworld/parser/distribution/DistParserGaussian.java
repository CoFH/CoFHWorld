package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderGaussian;
import cofh.cofhworld.world.distribution.DistributionGaussian;

public class DistParserGaussian extends AbstractDistParser<DistributionGaussian, BuilderGaussian> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionGaussian, BuilderGaussian> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderGaussian::new);

		fields.addRequiredField("center-height", Type.NUMBER, BuilderGaussian::setCenterHeight);
		fields.addRequiredField("spread", Type.NUMBER, BuilderGaussian::setSpread);
		fields.addOptionalField("smoothness", Type.NUMBER, BuilderGaussian::setSmoothness);
	}

}
