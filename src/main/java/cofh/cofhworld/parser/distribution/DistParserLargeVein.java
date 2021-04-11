package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderLargeVein;
import cofh.cofhworld.world.distribution.DistributionLargeVein;

public class DistParserLargeVein extends AbstractDistParser<DistributionLargeVein, BuilderLargeVein> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionLargeVein, BuilderLargeVein> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderLargeVein::new);

		fields.addRequiredField("min-height", Type.NUMBER, BuilderLargeVein::setMinY);
		fields.addRequiredField("vein-height", Type.NUMBER, BuilderLargeVein::setHeight);
		fields.addRequiredField("vein-diameter", Type.NUMBER, BuilderLargeVein::setDiameter);
		fields.addRequiredField("vertical-density", Type.NUMBER, BuilderLargeVein::setHorizontalDensity);
		fields.addRequiredField("horizontal-density", Type.NUMBER, BuilderLargeVein::setVerticalDensity);
	}

}
