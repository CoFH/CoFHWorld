package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenSmallTree;
import com.typesafe.config.Config;
import net.minecraft.block.Blocks;
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
			list = new ArrayList<>();
			if (!BlockData.parseBlockList(genObject.getValue("leaves"), list)) {
				log.warn("Entry specifies invalid leaves for 'smalltree' generator!");
				list.clear();
			}
		} else {
			log.info("Entry does not specify leaves for 'smalltree' generator! There are none!");
		}

		WorldGenSmallTree r = new WorldGenSmallTree(resList, list, matList);
		{
			if (blocks.size() > 0) {
				r.genSurface = blocks.toArray(new Material[0]);
			}

			if (genObject.hasPath("min-height")) {
				r.minHeight = genObject.getInt("min-height");
			}
			if (genObject.hasPath("height-variance")) {
				r.heightVariance = genObject.getInt("height-variance");
			}

			if (genObject.hasPath("tree-checks")) {
				r.treeChecks = genObject.getBoolean("tree-checks");
			}
			if (genObject.hasPath("relaxed-growth")) {
				r.relaxedGrowth = genObject.getBoolean("relaxed-growth");
			}
			if (genObject.hasPath("water-loving")) {
				r.waterLoving = genObject.getBoolean("water-loving");
			}
			if (genObject.hasPath("leaf-variance")) {
				r.leafVariance = genObject.getBoolean("leaf-variance");
			}
		}
		return r;
	}

}
