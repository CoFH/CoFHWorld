package cofh.cofhworld.parser.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.base.AbstractGenParserBlock;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenStalagmite;
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

		ArrayList<Material> list = new ArrayList<>();
		{
			boolean has = genObject.hasPath("surface");
			if (!has || !BlockData.parseMaterialList(genObject.getValue("surface"), list)) {
				ConfigOrigin origin = (has ? genObject.getValue("surface").origin() : genObject.origin());
				log.error("Invalid `surface` specified for generator '{}' on line {}!", generatorName, origin.lineNumber());
				throw new InvalidGeneratorException(has ? "Invalid `surface` specified" : "`surface` not spcified!", origin);
			}
		}
		WorldGenStalagmite r = new WorldGenStalagmite(resList, matList, list, stalactite ? Direction.UP : Direction.DOWN);
		{
			if (genObject.hasPath("min-height")) {
				r.minHeight = genObject.getInt("min-height");
			}
			if (genObject.hasPath("height-variance")) {
				r.heightVariance = genObject.getInt("height-variance");
			}
			if (genObject.hasPath("size-variance")) {
				r.sizeVariance = genObject.getInt("size-variance");
			}
			if (genObject.hasPath("height-mod")) {
				r.heightMod = genObject.getInt("height-mod");
			}
			if (genObject.hasPath("gen-size")) {
				r.genSize = genObject.getInt("gen-size");
			}
			if (genObject.hasPath("smooth")) {
				r.smooth = genObject.getBoolean("smooth");
			}
			if (genObject.hasPath("fat")) {
				r.fat = genObject.getBoolean("fat");
			}
			if (genObject.hasPath("alt-sinc")) {
				r.altSinc = genObject.getBoolean("alt-sinc");
			}
		}
		return r;
	}

}
