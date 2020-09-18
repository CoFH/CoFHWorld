package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenDungeon;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserDungeon implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "material", "spawner" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) {

		ArrayList<WeightedBlock> mobList = new ArrayList<>();
		if (!BlockData.parseBlockList(genObject.getValue("spawner"), mobList, false)) {
			log.warn("Entry specifies invalid spawner list for 'dungeon' generator on line {}! Using vanilla's without configuration!",
					genObject.getValue("spawner").origin().lineNumber());
			mobList.clear();
			mobList.add(new WeightedBlock(Blocks.MOB_SPAWNER));
		}

		WorldGenDungeon r = new WorldGenDungeon(resList, matList, mobList);

		if (genObject.hasPath("floor")) {
			resList = new ArrayList<>();
			if (BlockData.parseBlockList(genObject.getValue("floor"), resList, false)) {
				r.floor = resList;
			} else {
				log.warn("Entry specifies invalid block list for `floor` on line {}! Using walls.", genObject.getValue("floor").origin().lineNumber());
			}
		}
		{
			if (genObject.hasPath("chest")) {
				ArrayList<WeightedBlock> lootList = new ArrayList<>();
				if (BlockData.parseBlockList(genObject.getValue("chest"), lootList, false)) {
					r.chests = lootList;
				} else {
					log.warn("Entry specifies invalid blocks for `chest` on line {}! Using default.", genObject.getValue("chest").origin().lineNumber());
				}
			}
			if (genObject.hasPath("maxChests")) {
				r.maxChests = genObject.getInt("maxChests");
			}
			if (genObject.hasPath("chestAttempts")) {
				r.maxChestTries = MathHelper.clamp(genObject.getInt("chestAttempts"), 1, 5);
			}

			if (genObject.hasPath("minHoles")) {
				r.minHoles = genObject.getInt("minHoles");
			}
			if (genObject.hasPath("maxHoles")) {
				r.maxHoles = genObject.getInt("maxHoles");
			}

			if (genObject.hasPath("minHeight")) {
				r.minHeight = genObject.getInt("minHeight");
			}
			if (genObject.hasPath("maxHeight")) {
				r.maxHeight = genObject.getInt("maxHeight");
			}

			if (genObject.hasPath("minWidthX")) {
				r.minWidthX = genObject.getInt("minWidthX");
			}
			if (genObject.hasPath("maxWidthX")) {
				r.maxWidthX = genObject.getInt("maxWidthX");
			}
			if (genObject.hasPath("minWidthZ")) {
				r.minWidthZ = genObject.getInt("minWidthZ");
			}
			if (genObject.hasPath("maxWidthZ")) {
				r.maxWidthZ = genObject.getInt("maxWidthZ");
			}
		}
		return r;
	}

}
