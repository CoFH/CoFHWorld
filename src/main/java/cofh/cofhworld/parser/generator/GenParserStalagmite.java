package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderStalagmite;
import cofh.cofhworld.world.generator.WorldGenStalagmite;
import net.minecraft.util.Direction;

public class GenParserStalagmite implements AbstractGenParserResource<WorldGenStalagmite, BuilderStalagmite> {

	private final boolean stalactite;

	public GenParserStalagmite(boolean stalactite) {

		this.stalactite = stalactite;
	}

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenStalagmite, BuilderStalagmite> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(() -> {
			BuilderStalagmite builder = new BuilderStalagmite();
			builder.setDirection(stalactite ? Direction.UP : Direction.DOWN);
			return builder;
		});

		fields.addOptionalField("surface", Type.MATERIAL_LIST, BuilderStalagmite::setSurface);

		fields.addOptionalField("height", Type.NUMBER, BuilderStalagmite::setHeight);
		fields.addOptionalField("size", Type.NUMBER, BuilderStalagmite::setSize);
		fields.addOptionalField("smooth", Type.CONDITION, BuilderStalagmite::setSmooth);
		fields.addOptionalField("fat", Type.CONDITION, BuilderStalagmite::setFat);
		fields.addOptionalField("alt-sinc", Type.CONDITION, BuilderStalagmite::setAltSinc);
	}

}
