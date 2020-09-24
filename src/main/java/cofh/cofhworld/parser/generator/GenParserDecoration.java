package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenDecoration;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserDecoration implements IGeneratorParser {

	private static String[] FIELDS = new String[] { "block", "cluster-size" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public WorldGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException {

		int clusterSize = genObject.getInt("cluster-size"); // TODO: another name?
		if (clusterSize <= 0) {
			throw new InvalidGeneratorException("Invalid cluster size for generator '" + name + "'", genObject.getValue("cluster-size").origin());
		}

		ArrayList<WeightedBlock> list = new ArrayList<>();
		if (!genObject.hasPath("surface")) {
			log.debug("Entry does not specify surface for 'decoration' generator. Using grass.");
			list.add(new WeightedBlock(Blocks.GRASS));
		} else {
			if (!BlockData.parseBlockList(genObject.getValue("surface"), list, false)) {
				log.warn("Entry specifies invalid surface for 'decoration' generator! Using grass!");
				list.clear();
				list.add(new WeightedBlock(Blocks.GRASS));
			}
		}
		WorldGenDecoration r = new WorldGenDecoration(resList, clusterSize, matList, list);
		if (genObject.hasPath("see-sky-condition")) {
			r.setSeeSky(ConditionData.parseConditionValue(genObject.getValue("see-sky-condition")));
		} else if (genObject.hasPath("see-sky")) {
			r.setSeeSky(genObject.getBoolean("see-sky"));
		}
		if (genObject.hasPath("check-stay-condition")) {
			r.setCheckStay(ConditionData.parseConditionValue(genObject.getValue("check-stay-condition")));
		} else if (genObject.hasPath("check-stay")) {
			r.setCheckStay(genObject.getBoolean("check-stay"));
		}
		if (genObject.hasPath("stack-height")) {
			r.setStackHeight(NumberData.parseNumberValue(genObject.getValue("stack-height")));
		}
		if (genObject.hasPath("x-variance")) {
			r.setXVar(NumberData.parseNumberValue(genObject.getValue("x-variance"), 0, 15));
		}
		if (genObject.hasPath("y-variance")) {
			r.setYVar(NumberData.parseNumberValue(genObject.getValue("y-variance")));
		}
		if (genObject.hasPath("z-variance")) {
			r.setZVar(NumberData.parseNumberValue(genObject.getValue("z-variance"), 0, 15));
		}
		return r;
	}

}
