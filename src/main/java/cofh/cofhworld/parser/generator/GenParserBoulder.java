package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderBoulder;

public class GenParserBoulder extends AbstractGenParserResource {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(BuilderBoulder::new);

		fields.addRequiredField("diameter", Type.NUMBER, BuilderBoulder::setSize);

		fields.addOptionalField("quantity", Type.NUMBER, BuilderBoulder::setQuantity);

		fields.addOptionalField("hollow", Type.CONDITION, BuilderBoulder::setHollow);
		fields.addOptionalField("hollow-size", Type.NUMBER, BuilderBoulder::setHollowAmt);
		fields.addOptionalField("filler", Type.BLOCK_LIST, BuilderBoulder::setFiller);

		fields.addOptionalField("variance.x", Type.NUMBER, BuilderBoulder::setxVar);
		fields.addOptionalField("variance.y", Type.NUMBER, BuilderBoulder::setyVar);
		fields.addOptionalField("variance.z", Type.NUMBER, BuilderBoulder::setzVar);

		return fields;
	}

}
