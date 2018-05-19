package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.world.generator.WorldGenAdvLakes;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GenParserLake implements IGeneratorParser {

	@Override
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

		boolean useMaterial = false;
		{
			useMaterial = genObject.hasPath("use-material") ? genObject.getBoolean("use-material") : useMaterial;
		}
		WorldGenAdvLakes r = new WorldGenAdvLakes(resList, useMaterial ? matList : null);
		{
			ArrayList<WeightedRandomBlock> list = new ArrayList<>();
			if (genObject.hasPath("outline-block")) {
				if (!BlockData.parseBlockList(genObject.root().get("outline-block"), list, true)) {
					log.warn("Entry specifies invalid outline-block for 'lake' generator! Not outlining!");
				} else {
					r.setOutlineBlock(list);
				}
				list = new ArrayList<>();
			}
			if (genObject.hasPath("gap-block")) {
				if (!BlockData.parseBlockList(genObject.getValue("gap-block"), list, true)) {
					log.warn("Entry specifies invalid gap block for 'lake' generator! Not filling!");
				} else {
					r.setGapBlock(list);
				}
			}
			if (genObject.hasPath("solid-outline")) {
				r.setSolidOutline(genObject.getBoolean("solid-outline"));
			}
			if (genObject.hasPath("total-outline")) {
				r.setTotalOutline(genObject.getBoolean("total-outline"));
			}
		}
		return r;
	}

}
