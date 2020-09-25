package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.generator.builders.BuilderDungeon;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
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

		BuilderDungeon builder = new BuilderDungeon(resList, matList);

		ArrayList<WeightedBlock> list = new ArrayList<>();
		if (!BlockData.parseBlockList(genObject.getValue("spawner"), list)) {
			log.warn("Entry specifies invalid spawner list for 'dungeon' generator on line {}! Using vanilla's without configuration!",
					genObject.getValue("spawner").origin().lineNumber());
			list.clear();
			list.add(new WeightedBlock(Blocks.SPAWNER));
		}

		builder.setSpawners(list);

		if (genObject.hasPath("floor")) {
			list = new ArrayList<>();
			if (BlockData.parseBlockList(genObject.getValue("floor"), list)) {
				builder.setFloor(list);
			} else {
				log.warn("Entry specifies invalid block list for `floor` on line {}! Using walls.", genObject.getValue("floor").origin().lineNumber());
			}
		}
		if (genObject.hasPath("chest")) {
			resList = new ArrayList<>();
			if (BlockData.parseBlockList(genObject.getValue("chest"), resList)) {
				builder.setChests(resList);
			} else {
				log.warn("Entry specifies invalid blocks for `chest` on line {}! Using default.", genObject.getValue("chest").origin().lineNumber());
			}
		}
		if (genObject.hasPath("filler")) {
			resList = new ArrayList<>();
			if (BlockData.parseBlockList(genObject.getValue("filler"), resList)) {
				builder.setFiller(resList);
			} else {
				log.warn("Entry specifies invalid blocks for `fill-block` on line {}! Using default.", genObject.getValue("chest").origin().lineNumber());
			}
		}
		if (genObject.hasPath("chest-count")) {
			builder.setChestCount(NumberData.parseNumberValue(genObject.getValue("chest-count")));
		}
		if (genObject.hasPath("chest-attempts")) {
			builder.setChestAttempts(NumberData.parseNumberValue(genObject.getValue("chest-attempts"), 1, 5));
		}

		if (genObject.hasPath("check-hole")) {
			builder.setHoleCondition(ConditionData.parseConditionValue(genObject.getValue("check-hole")));
		}
		if (genObject.hasPath("check-hole-count")) {
			builder.setHoleCount(ConditionData.parseConditionValue(genObject.getValue("check-hole-count")));
		}

		if (genObject.hasPath("height")) {
			builder.setHeight(NumberData.parseNumberValue(genObject.getValue("height")));
		}

		if (genObject.hasPath("radius-x")) {
			builder.setRadiusX(NumberData.parseNumberValue(genObject.getValue("radius-x")));
		}
		if (genObject.hasPath("radius-z")) {
			builder.setRadiusZ(NumberData.parseNumberValue(genObject.getValue("radius-z")));
		}
		return builder.build();
	}

}
