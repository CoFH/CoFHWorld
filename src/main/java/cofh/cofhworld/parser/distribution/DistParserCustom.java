package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderCustom;
import cofh.cofhworld.world.distribution.DistributionCustom;

public class DistParserCustom extends AbstractDistParser<DistributionCustom, BuilderCustom> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionCustom, BuilderCustom> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderCustom::new);

		fields.addRequiredField("x-offset", Type.NUMBER, BuilderCustom::setxOffset);
		fields.addRequiredField("y-offset", Type.NUMBER, BuilderCustom::setyOffset);
		fields.addRequiredField("z-offset", Type.NUMBER, BuilderCustom::setzOffset);
	}

}
