package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenAdvLakes;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserLake extends AbstractGenParserBlock {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		WorldGenAdvLakes r = new WorldGenAdvLakes(resList, matList);
		{
			ArrayList<WeightedBlock> list = new ArrayList<>();
			if (genObject.hasPath("outline-block")) {
				if (!BlockData.parseBlockList(genObject.getValue("outline-block"), list)) {
					log.warn("Entry specifies invalid outline-block for 'lake' generator! Not outlining!");
				} else {
					r.setOutlineBlock(list);
				}
				list = new ArrayList<>();
			}
			if (genObject.hasPath("gap-block")) {
				if (!BlockData.parseBlockList(genObject.getValue("gap-block"), list)) {
					log.warn("Entry specifies invalid gap block for 'lake' generator! Not filling!");
				} else {
					r.setGapBlock(list);
				}
			}
			if (genObject.hasPath("outline-condition")) {
				r.setOutlineCondition(ConditionData.parseConditionValue(genObject.getValue("outline-condition")));
			}
		}
		return r;
	}

}
