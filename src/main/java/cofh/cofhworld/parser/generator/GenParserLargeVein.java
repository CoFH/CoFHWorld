package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderLargeVein;

public class GenParserLargeVein implements AbstractGenParserResource<BuilderLargeVein> {

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderLargeVein> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(BuilderLargeVein::new);

		fields.addRequiredField("vein-size", Type.NUMBER, BuilderLargeVein::setSize, "cluster-size");

		fields.addOptionalField("sparse", Type.CONDITION, BuilderLargeVein::setSparse);
		fields.addOptionalField("spindly", Type.CONDITION, BuilderLargeVein::setSpindly);
	}

}
