package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderStructure;
import cofh.cofhworld.world.generator.WorldGenStructure;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class GenParserStructure implements IGeneratorParser<WorldGenStructure, BuilderStructure> {

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenStructure, BuilderStructure> fields) {

		fields.setConstructor(BuilderStructure::new);

		fields.addRequiredField("structure", Type.STRUCTURE_LIST, BuilderStructure::setTemplates);

		fields.addOptionalField("ignored-block", Type.MATERIAL_LIST, BuilderStructure::setIgnoredBlocks);
		fields.addOptionalField("ignore-entities", Type.RAW_BOOLEAN, BuilderStructure::setIgnoreEntities);

		fields.addOptionalField("integrity", Type.NUMBER, BuilderStructure::setIntegrity);

		fields.addOptionalField("rotation", Type.Enum.ofList(Rotation.class), BuilderStructure::setRotations);
		fields.addOptionalField("mirror", Type.Enum.ofList(Mirror.class), BuilderStructure::setMirrors);
	}

}
