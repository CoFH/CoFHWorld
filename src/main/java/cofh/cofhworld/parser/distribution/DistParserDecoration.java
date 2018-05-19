package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.world.generator.WorldGenDecoration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistParserDecoration extends DistParserSurface {

	@Override
	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Collections.singletonList(new WeightedRandomBlock(Blocks.GRASS, -1));
	}

	@Override
	protected String getDefaultGenerator() {

		return "decoration";
	}

}
