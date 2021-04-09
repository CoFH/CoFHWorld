package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderSpike;
import cofh.cofhworld.world.generator.WorldGenSpike;

public class GenParserSpike implements AbstractGenParserResource<WorldGenSpike, BuilderSpike> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenSpike, BuilderSpike> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderSpike::new);

		fields.addOptionalField("height", Type.NUMBER, BuilderSpike::setHeight);
		fields.addOptionalField("size", Type.NUMBER, BuilderSpike::setSize);

		fields.addOptionalField("variance-y", Type.NUMBER, BuilderSpike::setyVariance);

		fields.addOptionalField("layer-size", Type.NUMBER, BuilderSpike::setLayerSize);

		fields.addOptionalField("large-spike", Type.CONDITION, BuilderSpike::setLargeSpikes);
		fields.addOptionalField("large-spike-height-gain", Type.NUMBER, BuilderSpike::setLargeSpikeHeightGain);
	}

}
