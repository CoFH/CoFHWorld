package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderDecoration;
import cofh.cofhworld.world.generator.WorldGenDecoration;

public class GenParserDecoration implements AbstractGenParserResource<WorldGenDecoration, BuilderDecoration> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenDecoration, BuilderDecoration> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(BuilderDecoration::new);

		fields.addRequiredField("quantity", Type.NUMBER, BuilderDecoration::setSize);

		fields.addOptionalField("stack-height", Type.NUMBER, BuilderDecoration::setStackHeight);

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderDecoration::setSurface);

		fields.addOptionalField("see-sky", Type.CONDITION, BuilderDecoration::setSeeSky);

		fields.addOptionalField("check-stay", Type.CONDITION, BuilderDecoration::setCheckStay);
	}

}
