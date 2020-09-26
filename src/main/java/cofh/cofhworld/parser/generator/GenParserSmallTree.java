package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.generator.builders.BuilderSmallTree;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserSmallTree extends AbstractGenParserBlock {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		ArrayList<WeightedBlock> list = new ArrayList<>();
		ArrayList<Material> blocks = new ArrayList<>();
		if (genObject.hasPath("surface")) {
			if (!BlockData.parseMaterialList(genObject.getValue("surface"), blocks)) {
				log.warn("Entry specifies invalid surface for 'smalltree' generator! A partial list will be used!");
			}
		}

		if (genObject.hasPath("leaves")) {
			if (!BlockData.parseBlockList(genObject.getValue("leaves"), list)) {
				log.warn("Entry specifies invalid leaves for 'smalltree' generator!");
			}
		} else {
			log.info("Entry does not specify leaves for 'smalltree' generator! There are none!");
		}

		BuilderSmallTree r = new BuilderSmallTree(resList, matList);
		r.setLeaves(list);
		if (blocks.size() > 0) {
			r.setSurface(blocks.toArray(new Material[0]));
		}

		if (genObject.hasPath("height")) {
			r.setHeight(NumberData.parseNumberValue(genObject.getValue("height")));
		}

		if (genObject.hasPath("tree-checks")) {
			r.setTreeChecks(ConditionData.parseConditionValue(genObject.getValue("tree-checks")));
		}
		if (genObject.hasPath("relaxed-growth")) {
			r.setRelaxedGrowth(ConditionData.parseConditionValue(genObject.getValue("relaxed-growth")));
		}
		if (genObject.hasPath("water-loving")) {
			r.setWaterLoving(ConditionData.parseConditionValue(genObject.getValue("water-loving")));
		}
		if (genObject.hasPath("leaf-variance")) {
			r.setLeafVariance(ConditionData.parseConditionValue(genObject.getValue("leaf-variance")));
		}
		return r.build();
	}

}
