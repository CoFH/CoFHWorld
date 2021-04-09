package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderLargeVein;
import cofh.cofhworld.world.generator.WorldGenLargeVein;

public class GenParserLargeVein implements AbstractGenParserResource<WorldGenLargeVein, BuilderLargeVein> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenLargeVein, BuilderLargeVein> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderLargeVein::new);

		fields.addRequiredField("vein-size", Type.NUMBER, BuilderLargeVein::setSize, "cluster-size");

		fields.addOptionalField("sparse", Type.CONDITION, BuilderLargeVein::setSparse);
		fields.addOptionalField("spindly", Type.CONDITION, BuilderLargeVein::setSpindly);
	}

}
