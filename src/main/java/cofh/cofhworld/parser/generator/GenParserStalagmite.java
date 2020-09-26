package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.generator.builders.BuilderStalagmite;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.ConditionData;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigOrigin;
import net.minecraft.util.Direction;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenParserStalagmite extends AbstractGenParserBlock {

	private final boolean stalactite;

	public GenParserStalagmite(boolean stalactite) {

		this.stalactite = stalactite;
	}

	@Override
	@Nonnull
	public WorldGen parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedBlock> resList, List<Material> matList) throws InvalidGeneratorException {

		ArrayList<Material> surfList = new ArrayList<>();
		{
			boolean has = genObject.hasPath("surface");
			if (has && !BlockData.parseMaterialList(genObject.getValue("surface"), surfList)) {
				ConfigOrigin origin = (has ? genObject.getValue("surface").origin() : genObject.origin());
				log.error("Invalid `surface` specified for generator '{}' on line {}!", generatorName, origin.lineNumber());
				throw new InvalidGeneratorException(has ? "Invalid `surface` specified" : "`surface` not spcified!", origin);
			} // TODO: make required?
		}
		BuilderStalagmite builder = new BuilderStalagmite(resList, matList);
		builder.setSurface(surfList);
		builder.setDirection(stalactite ? Direction.UP : Direction.DOWN);
		if (genObject.hasPath("height")) {
			builder.setHeight(NumberData.parseNumberValue(genObject.getValue("height")));
		}
		if (genObject.hasPath("size")) {
			builder.setSize(NumberData.parseNumberValue(genObject.getValue("size")));
		}
		if (genObject.hasPath("smooth")) {
			builder.setSmooth(ConditionData.parseConditionValue(genObject.getValue("smooth")));
		}
		if (genObject.hasPath("fat")) {
			builder.setFat(ConditionData.parseConditionValue(genObject.getValue("fat")));
		}
		if (genObject.hasPath("alt-sinc")) {
			builder.setAltSinc(ConditionData.parseConditionValue(genObject.getValue("alt-sinc")));
		}
		return builder.build();
	}

}
