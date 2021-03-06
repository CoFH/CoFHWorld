package cofh.cofhworld.parser.generator;

import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.EnumData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.generator.WorldGenStructure;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.feature.WorldGenerator;
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

	// @formatter:off
	protected static ArrayList<WeightedEnum<Rotation>> ALL_ROTATION = Lists.newArrayList(
			new WeightedEnum<>(Rotation.NONE, 1),
			new WeightedEnum<>(Rotation.CLOCKWISE_90, 1),
			new WeightedEnum<>(Rotation.CLOCKWISE_180, 1),
			new WeightedEnum<>(Rotation.COUNTERCLOCKWISE_90, 1)
	);
	// @formatter:on
	protected static ArrayList<WeightedEnum<Mirror>> NO_MIRROR = new ArrayList<>();

	private static String[] FIELDS = new String[] { "structure" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		ArrayList<WeightedNBTTag> tags = new ArrayList<>();
		if (genObject.hasPath("structure")) {
			ArrayList<WeightedString> files = new ArrayList<>();
			final String dir = FilenameUtils.getFullPath(genObject.origin().filename());
			if (StringData.parseStringList(genObject.getValue("structure"), files)) {
				if (files.size() == 0) {
					log.error("No structures provided for `structure` geenrator!");
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

		resList.clear();

		if (genObject.hasPath("ignored-block") && !BlockData.parseBlockList(genObject.getValue("ignored-block"), resList, false)) {
			log.warn("Error parsing `ignored-block`, generating all template blocks instead");
			resList.clear();
		}

		boolean ignoreEntities = false;
		if (genObject.hasPath("ignore-entities")) {
			ignoreEntities = genObject.getBoolean("ignore-entities");
		}

		WorldGenStructure gen = new WorldGenStructure(tags, resList, ignoreEntities);

		if (genObject.hasPath("integrity")) {
			gen.setIntegrity(NumberData.parseNumberValue(genObject.getValue("integrity"), 0d, 1.01d)); // ensure we don't accidentally a .999...
		}

		ArrayList<WeightedEnum<Rotation>> rots = ALL_ROTATION;
		if (genObject.hasPath("rotation")) {
			rots = new ArrayList<>(4);
			if (!EnumData.parseEnumList(genObject.getValue("rotation"), rots, Rotation.class)) {
				rots.clear();
			}
		}

		ArrayList<WeightedEnum<Mirror>> mirror = NO_MIRROR;
		if (genObject.hasPath("mirror")) {
			mirror = new ArrayList<>(3);
			if (!EnumData.parseEnumList(genObject.getValue("mirror"), mirror, Mirror.class)) {
				mirror.clear();
			}
		}

		gen.setDetails(rots, mirror);

		return gen;
	}

	@Override
	public boolean isMeta() {

		return true;
	}

}
