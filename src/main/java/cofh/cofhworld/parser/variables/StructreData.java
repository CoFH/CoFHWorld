package cofh.cofhworld.parser.variables;

import cofh.cofhworld.init.WorldProps;
import cofh.cofhworld.parser.IGeneratorParser.InvalidGeneratorException;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import com.typesafe.config.ConfigValue;
import net.minecraft.nbt.CompressedStreamTools;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StructreData {

	public static List<WeightedNBTTag> parseStructureList(ConfigValue stringEntry) throws InvalidGeneratorException {

		ArrayList<WeightedNBTTag> tags = new ArrayList<>();

		List<WeightedString> files = new ArrayList<>();
		if (StringData.parseStringList(stringEntry, files)) {
				throw new InvalidGeneratorException("Invalid `structure` tag", stringEntry.origin());
		}
		final String dir = FilenameUtils.getFullPath(stringEntry.origin().filename());
		{
			if (files.size() == 0) {
				throw new InvalidGeneratorException("Empty `structure` tag", stringEntry.origin());
			}
			tags.ensureCapacity(files.size());
			for (WeightedString file : files) {
				String path = FilenameUtils.normalize(dir + file.value);
				try {
					if (!FilenameUtils.directoryContains(WorldProps.canonicalWorldGenDir, path)) {
						throw new InvalidGeneratorException("Structure NBT file not contained in worldgen folder", stringEntry.origin());
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
					throw new InvalidGeneratorException("Structure NBT file cannot be read.", stringEntry.origin()).causedBy(e);
				}
			}
		}

		return tags;
	}
}
