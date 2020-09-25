package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserClusterCount;
import cofh.cofhworld.parser.generator.builders.BuilderCluster;
import cofh.cofhworld.parser.generator.builders.BuilderCluster.Type;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class GenParserCluster extends AbstractGenParserClusterCount {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		return new BuilderCluster(resList, matList).
				setClusterSize(NumberData.parseNumberValue(genObject.getValue("cluster-size"), 0, 64)).
				setType(sparse ? Type.SPARSE : Type.TINY).build();
	}

}
