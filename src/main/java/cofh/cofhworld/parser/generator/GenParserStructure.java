package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderStructure;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.EnumData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenParserStructure implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "structure" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		ArrayList<WeightedNBTTag> tags = new ArrayList<>();
		if (genObject.hasPath("structure")) {
			ArrayList<WeightedString> files = new ArrayList<>();
			final String dir = FilenameUtils.getFullPath(genObject.origin().filename());
			if (StringData.parseStringList(genObject.getValue("structure"), files)) {
				if (files.size() == 0) {
					log.error("No structures provided for `structure` generator!");
					throw new InvalidGeneratorException("Empty `structure` array", genObject.getValue("structure").origin());
				}
				tags.ensureCapacity(files.size());
				for (WeightedString file : files) {
					String path = FilenameUtils.normalize(dir + file.value);
					try {
						if (!FilenameUtils.directoryContains(WorldProps.cannonicalWorldGenDir, path)) {
							throw new InvalidGeneratorException("Structure NBT file not contained in worldgen folder", genObject.getValue("structure").origin());
						}
					} catch (IOException e) {
						// this ... never actually can be thrown. FileNameUtils.directoryContains declares that it can, but has no path to do so
						// bizzare
					}
					try {
						FileInputStream stream = new FileInputStream(new File(path));
						tags.add(new WeightedNBTTag(file.itemWeight, CompressedStreamTools.readCompressed(stream)));
						IOUtils.closeQuietly(stream);
					} catch (IOException e) {
						throw new InvalidGeneratorException("Structure NBT file cannot be read.", genObject.getValue("structure").origin()).causedBy(e);
					}
				}
			} else {
				log.error("Tag `structure` invalid!");
				throw new InvalidGeneratorException("Invalid `structure` tag", genObject.getValue("structure").origin());
			}
		} else {
			log.error("Tag `structure` missing for `structure` generator!");
			throw new InvalidGeneratorException("Missing `structure` tag", genObject.origin());
		}

		BuilderStructure builder = new BuilderStructure();
		builder.setTemplates(tags);

		matList.clear();

		if (genObject.hasPath("ignored-block")) {
			if (!BlockData.parseMaterialList(genObject.getValue("ignored-block"), matList))
				log.warn("Error parsing `ignored-block`, a partial list will be used");
			builder.setIgnoredBlocks(matList);
		}
		if (genObject.hasPath("ignore-entities")) {
			builder.setIgnoreEntities(genObject.getBoolean("ignore-entities"));
		}

		if (genObject.hasPath("integrity")) {
			builder.setIntegrity(NumberData.parseNumberValue(genObject.getValue("integrity"), 0d, 1.01d)); // ensure we don't accidentally a .999...
		}

		if (genObject.hasPath("rotation")) {
			ArrayList<WeightedEnum<Rotation>> rots = new ArrayList<>(4);
			if (!EnumData.parseEnumList(genObject.getValue("rotation"), rots, Rotation.class)) {
				log.warn("Invalid `rotation` list, a partial list will be used.");
			}
			builder.setRotations(rots);
		}

		if (genObject.hasPath("mirror")) {
			ArrayList<WeightedEnum<Mirror>> mirror = new ArrayList<>(3);
			if (!EnumData.parseEnumList(genObject.getValue("mirror"), mirror, Mirror.class)) {
				log.warn("Invalid `mirror` list, a partial list will be used");
			}
			builder.setMirrors(mirror);
		}

		return builder.build();
	}

	@Override
	public boolean isMeta() {

		return true;
	}

}
