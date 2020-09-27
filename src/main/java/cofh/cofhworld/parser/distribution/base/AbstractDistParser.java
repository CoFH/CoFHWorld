package cofh.cofhworld.parser.distribution.base;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.GeneratorData;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.parser.IGeneratorParser.InvalidGeneratorException;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public abstract class AbstractDistParser implements IDistributionParser {

	private final String[] FIELDS = new String[] { "generator", "cluster-count" };

	protected AbstractDistParser() {

	}

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public final Distribution getFeature(String featureName, Config genObject, boolean retrogen, Logger log) throws InvalidDistributionException {

		INumberProvider numClusters = NumberData.parseNumberValue(genObject.getValue("cluster-count"), 0, Long.MAX_VALUE);

		WorldGen generator;
		try {
			if (!genObject.hasPath("generator")) {
				throw new IGeneratorParser.InvalidGeneratorException("No `generator` entry present", genObject.origin());
			}
			generator = GeneratorData.parseGenerator(genObject.getValue("generator"));
		} catch (InvalidGeneratorException e) {
			log.warn("Invalid generator for '{}' on line {}!", featureName, e.origin().lineNumber());
			throw new InvalidDistributionException("Invalid generator", e.origin()).causedBy(e);
		}

		return getFeature(featureName, genObject, generator, numClusters, retrogen, log);
	}

	@Nonnull
	protected abstract Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log);

}
