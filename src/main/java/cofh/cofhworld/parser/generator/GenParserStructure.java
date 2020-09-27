package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderStructure;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class GenParserStructure implements IGeneratorParser {

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields.setBuilder(BuilderStructure::new);

		fields.addRequiredField("structure", Type.STRUCTURE_LIST, BuilderStructure::setTemplates);

		fields.addOptionalField("ignored-block", Type.MATERIAL_LIST, BuilderStructure::setIgnoredBlocks);
		fields.addOptionalField("ignore-entities", Type.RAW_BOOLEAN, BuilderStructure::setIgnoreEntities);

		fields.addOptionalField("integrity", Type.NUMBER, BuilderStructure::setIntegrity);

		fields.addOptionalField("rotation", Type.Enum.ofList(Rotation.class), BuilderStructure::setRotations);
		fields.addOptionalField("mirror", Type.Enum.ofList(Mirror.class), BuilderStructure::setMirrors);

		return fields;
	}

}
