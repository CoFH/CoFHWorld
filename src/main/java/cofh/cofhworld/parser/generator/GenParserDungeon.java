package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenDungeon;
import com.typesafe.config.Config;
import net.minecraft.block.Blocks;
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
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		ArrayList<WeightedBlock> mobList = new ArrayList<>();
		if (!BlockData.parseBlockList(genObject.getValue("spawner"), mobList)) {
			log.warn("Entry specifies invalid spawner list for 'dungeon' generator on line {}! Using vanilla's without configuration!",
					genObject.getValue("spawner").origin().lineNumber());
			mobList.clear();
			mobList.add(new WeightedBlock(Blocks.SPAWNER));
		}

		WorldGenDungeon r = new WorldGenDungeon(resList, matList, mobList);
		{
			if (genObject.hasPath("floor")) {
				resList = new ArrayList<>();
				if (BlockData.parseBlockList(genObject.getValue("floor"), resList)) {
					r.floor = resList;
				} else {
					log.warn("Entry specifies invalid block list for `floor` on line {}! Using walls.", genObject.getValue("floor").origin().lineNumber());
				}
			}
			if (genObject.hasPath("chest")) {
				resList = new ArrayList<>();
				if (BlockData.parseBlockList(genObject.getValue("chest"), resList)) {
					r.chests = resList;
				} else {
					log.warn("Entry specifies invalid blocks for `chest` on line {}! Using default.", genObject.getValue("chest").origin().lineNumber());
				}
			}
			if (genObject.hasPath("fill-block")) {
				resList = new ArrayList<>();
				if (BlockData.parseBlockList(genObject.getValue("fill-block"), resList)) {
					r.filler = resList;
				} else {
					log.warn("Entry specifies invalid blocks for `fill-block` on line {}! Using default.", genObject.getValue("chest").origin().lineNumber());
				}
			}
			if (genObject.hasPath("chest-count")) {
				r.chestCount = NumberData.parseNumberValue(genObject.getValue("chest-count"));
			}
			if (genObject.hasPath("chest-attempts")) {
				r.chestAttempts = NumberData.parseNumberValue(genObject.getValue("chest-attempts"), 1, 5);
			}

			if (genObject.hasPath("check-hole")) {
				r.holeCondition = ConditionData.parseConditionValue(genObject.getValue("check-hole"));
			}
			if (genObject.hasPath("check-hole-count")) {
				r.validHoleCount = ConditionData.parseConditionValue(genObject.getValue("check-hole-count"));
			}

			if (genObject.hasPath("height")) {
				r.height = NumberData.parseNumberValue(genObject.getValue("height"));
			}

			if (genObject.hasPath("radius-x")) {
				r.radiusX = NumberData.parseNumberValue(genObject.getValue("radius-x"));
			}
			if (genObject.hasPath("radius-z")) {
				r.radiusZ = NumberData.parseNumberValue(genObject.getValue("radius-z"));
			}
		}
		return r;
	}

}
