package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.GeneratorData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUniform;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.exceptions.InvalidGeneratorException;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class DistParserUniform extends DistParserSequential {

	protected final List<WeightedRandomBlock> defaultMaterial;

	public DistParserUniform() {

		defaultMaterial = generateDefaultMaterial();
	}

	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	@Override
	public Distribution getFeature(String featureName, Config genObject, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		INumberProvider numClusters = NumberData.parseNumberValue(genObject.getValue("cluster-count"), 0, Long.MAX_VALUE);

		WorldGenerator generator;
		try {
			generator = GeneratorData.parseGenerator(getDefaultGenerator(), genObject, defaultMaterial);
		} catch (InvalidGeneratorException e) {
			log.warn("Invalid generator for '{}' on line {}!", featureName, e.origin().lineNumber());
			return null;
		}

		return getFeature(featureName, genObject, generator, numClusters, biomeRes, retrogen, dimRes, log);
	}

	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.hasPath("min-height") && genObject.hasPath("max-height"))) {
			log.error("Height parameters for 'uniform' template not specified in \"" + featureName + "\"");
			return null;
		}

		INumberProvider minHeight = NumberData.parseNumberValue(genObject.root().get("min-height"));
		INumberProvider maxHeight = NumberData.parseNumberValue(genObject.root().get("max-height"));

		return new DistributionUniform(featureName, gen, numClusters, minHeight, maxHeight, biomeRes, retrogen, dimRes);
	}

	protected String getDefaultGenerator() {

		return "cluster";
	}

}
