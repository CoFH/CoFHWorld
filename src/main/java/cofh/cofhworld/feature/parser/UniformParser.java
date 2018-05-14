package cofh.cofhworld.feature.parser;

import cofh.cofhworld.feature.IConfigurableFeatureGenerator;
import cofh.cofhworld.feature.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.feature.IFeatureGenerator;
import cofh.cofhworld.feature.IFeatureParser;
import cofh.cofhworld.feature.generator.FeatureBase;
import cofh.cofhworld.feature.generator.FeatureGenUniform;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UniformParser extends SequentialParser {

	protected final List<WeightedRandomBlock> defaultMaterial;

	public UniformParser() {

		defaultMaterial = generateDefaultMaterial();
	}

	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	@Override
	public FeatureBase getFeature(String featureName, Config genObject, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		INumberProvider numClusters = FeatureParser.parseNumberValue(genObject.getValue("cluster-count"), 0, Long.MAX_VALUE);

		WorldGenerator generator = FeatureParser.parseGenerator(getDefaultGenerator(), genObject, defaultMaterial);
		if (generator == null) {
			log.warn("Invalid generator for '{}'!", featureName);
			return null;
		}

		return getFeature(featureName, genObject, generator, numClusters, biomeRes, retrogen, dimRes, log);
	}

	protected FeatureBase getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.hasPath("min-height") && genObject.hasPath("max-height"))) {
			log.error("Height parameters for 'uniform' template not specified in \"" + featureName + "\"");
			return null;
		}

		INumberProvider minHeight = FeatureParser.parseNumberValue(genObject.root().get("min-height"));
		INumberProvider maxHeight = FeatureParser.parseNumberValue(genObject.root().get("max-height"));

		return new FeatureGenUniform(featureName, gen, numClusters, minHeight, maxHeight, biomeRes, retrogen, dimRes);
	}

	protected String getDefaultGenerator() {

		return "cluster";
	}

}
