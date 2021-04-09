package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderBoulder;
import cofh.cofhworld.world.generator.WorldGenBoulder;

public class GenParserBoulder implements AbstractGenParserResource<WorldGenBoulder, BuilderBoulder> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenBoulder, BuilderBoulder> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderBoulder::new);

		fields.addRequiredField("diameter", Type.NUMBER, BuilderBoulder::setSize);

		fields.addOptionalField("quantity", Type.NUMBER, BuilderBoulder::setQuantity);

		fields.addOptionalField("hollow", Type.CONDITION, BuilderBoulder::setHollow);
		fields.addOptionalField("hollow-size", Type.NUMBER, BuilderBoulder::setHollowAmt);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderBoulder::setFiller);

		fields.addOptionalField("variance.x", Type.NUMBER, BuilderBoulder::setxVar);
		fields.addOptionalField("variance.y", Type.NUMBER, BuilderBoulder::setyVar);
		fields.addOptionalField("variance.z", Type.NUMBER, BuilderBoulder::setzVar);
	}

}
