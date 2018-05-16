package cofh.cofhworld.decoration.parser;

import cofh.cofhworld.decoration.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomNBTTag;
import cofh.cofhworld.util.WeightedRandomString;
import cofh.cofhworld.util.exceptions.InvalidGeneratorException;
import cofh.cofhworld.world.generator.WorldGenStructure;
import com.typesafe.config.Config;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StructureParser implements IGeneratorParser {

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) throws InvalidGeneratorException {

		ArrayList<WeightedRandomNBTTag> tags = new ArrayList<>();
		if (genObject.hasPath("structure")) {
			ArrayList<WeightedRandomString> files = new ArrayList<>();
			final String dir = FilenameUtils.getFullPath(genObject.origin().filename());
			if (FeatureParser.parseWeightedStringList(genObject.getValue("structure"), files)) {
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

		if (genObject.hasPath("ignored-block") && !FeatureParser.parseResList(genObject.getValue("ignored-block"), resList, false)) {
			log.warn("Error parsing `ignored-block`, generating all template blocks instead");
			resList.clear();
		}

		boolean ignoreEntities = false;
		if (genObject.hasPath("ignore-entities")) {
			ignoreEntities = genObject.getBoolean("ignore-entities");
		}

		WorldGenStructure gen = new WorldGenStructure(tags, resList, ignoreEntities);

		return gen;
	}

	@Override
	public boolean isMeta() {

		return true;
	}

}
