package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.generator.builders.BuilderGeode;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import net.minecraft.block.Blocks;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserGeode extends AbstractGenParserBlock {

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) {

		ArrayList<WeightedBlock> list = new ArrayList<>();
		if (!genObject.hasPath("crust")) { // TODO: require?
			log.debug("Entry does not specify crust for 'geode' generator. Using stone.");
			list.add(new WeightedBlock(Blocks.STONE));
		} else {
			if (!BlockData.parseBlockList(genObject.getValue("crust"), list)) {
				if (list.size() > 0) {
					log.warn("Entry specifies invalid crust for 'geode' generator! A partial list will be used!");
				} else {
					log.warn("Entry specifies invalid crust for 'geode' generator! Using stone!");
					list.add(new WeightedBlock(Blocks.STONE));
				}
			}
		}
		BuilderGeode builder = new BuilderGeode(resList, matList);
		builder.setOutline(list);
		if (genObject.hasPath("hollow")) {
			builder.setHollow(ConditionData.parseConditionValue(genObject.getValue("hollow")));
		}
		if (genObject.hasPath("filler")) {
			list = new ArrayList<>();
			if (!BlockData.parseBlockList(genObject.getValue("filler"), list)) {
				log.warn("Entry specifies invalid filler for 'geode' generator! Not filling!");
			} else {
				builder.setFiller(list);
			}
		}
		return builder.build();
	}

}
