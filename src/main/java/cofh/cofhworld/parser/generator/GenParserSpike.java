package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderSpike;

public class GenParserSpike extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderSpike::new);

		fields.addOptionalField("height", Type.NUMBER, BuilderSpike::setHeight);
		fields.addOptionalField("size", Type.NUMBER, BuilderSpike::setSize);

		fields.addOptionalField("variance-y", Type.NUMBER, BuilderSpike::setyVariance);

		fields.addOptionalField("layer-size", Type.NUMBER, BuilderSpike::setLayerSize);

		fields.addOptionalField("large-spike", Type.CONDITION, BuilderSpike::setLargeSpikes);
		fields.addOptionalField("large-spike-height-gain", Type.NUMBER, BuilderSpike::setLargeSpikeHeightGain);

		return fields;
	}

}
