package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.EnumData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomEnum;
import cofh.cofhworld.util.WeightedRandomNBTTag;
import cofh.cofhworld.util.WeightedRandomString;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenParserStructure implements IGeneratorParser {

	protected static ArrayList<WeightedRandomEnum<Rotation>> ALL_ROTATION = Lists.newArrayList(
			new WeightedRandomEnum<Rotation>(Rotation.NONE, 1),
			new WeightedRandomEnum<Rotation>(Rotation.CLOCKWISE_90, 1),
			new WeightedRandomEnum<Rotation>(Rotation.CLOCKWISE_180, 1),
			new WeightedRandomEnum<Rotation>(Rotation.COUNTERCLOCKWISE_90, 1)
			);
	protected static ArrayList<WeightedRandomEnum<Mirror>> NO_MIRROR = new ArrayList<>();

	private static String[] FIELDS = new String[] { "structure" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) throws InvalidGeneratorException {

		ArrayList<WeightedRandomNBTTag> tags = new ArrayList<>();
		if (genObject.hasPath("structure")) {
			ArrayList<WeightedRandomString> files = new ArrayList<>();
			final String dir = FilenameUtils.getFullPath(genObject.origin().filename());
			if (StringData.parseStringList(genObject.getValue("structure"), files)) {
				if (files.size() == 0) {
					log.error("No structures provided for `structure` geenrator!");
					throw new InvalidGeneratorException("Empty `structure` array", genObject.getValue("structure").origin());
				}
				tags.ensureCapacity(files.size());
				for (WeightedRandomString file : files) {
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
						tags.add(new WeightedRandomNBTTag(file.itemWeight, CompressedStreamTools.readCompressed(stream)));
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

		ArrayList<WeightedRandomEnum<Rotation>> rots = ALL_ROTATION;
		if (genObject.hasPath("rotation")) {
			rots = new ArrayList<>(4);
			if (!EnumData.parseEnumList(genObject.getValue("rotation"), rots, Rotation.class)) {
				rots.clear();
			}
		}

		ArrayList<WeightedRandomEnum<Mirror>> mirror = NO_MIRROR;
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
