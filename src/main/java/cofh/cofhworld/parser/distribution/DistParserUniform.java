package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.distribution.builders.BuilderUniform;
import cofh.cofhworld.world.distribution.DistributionUniform;

public class DistParserUniform extends AbstractDistParser<DistributionUniform, BuilderUniform> {

	@Override
	public void getFields(IBuilderFieldRegistry<DistributionUniform, BuilderUniform> fields) {

		super.getFields(fields);
		fields.setConstructor(BuilderUniform::new);

		fields.addRequiredField("min-height", Type.NUMBER, BuilderUniform::setMinHeight);
		fields.addRequiredField("max-height", Type.NUMBER, BuilderUniform::setMaxHeight);
	}
}
