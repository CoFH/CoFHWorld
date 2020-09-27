package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUnderMaterial;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DistParserUnderMaterial extends AbstractDistParser {

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		ArrayList<Material> surface = null;
		if (genObject.hasPath("surface")) {
			surface = new ArrayList<>();
			if (!BlockData.parseMaterialList(genObject.getValue("surface"), surface)) {
				log.warn("Invalid surface list! A partial list will be used.");
			}
		} else if (genObject.hasPath("fluid")) {
			surface = new ArrayList<>();
			if (!BlockData.parseMaterialList(genObject.getValue("fluid"), surface)) {
				log.warn("Invalid fluid list! A partial list will be used.");
			}
		} else if (genObject.hasPath("ceiling")) {
			surface = new ArrayList<>();
			if (!BlockData.parseMaterialList(genObject.getValue("ceiling"), surface)) {
				log.warn("Invalid ceiling list! A partial list will be used.");
			}
		}

		// does this logic actually need a material?
		List<Material> materials = new ArrayList<>();
		if (genObject.hasPath("material")) {
			if (!BlockData.parseMaterialList(genObject.getValue("material"), materials)) {
				log.warn("Invalid material list! A partial list will be used.");
			}
		}
		return new DistributionUnderMaterial(featureName, gen, materials, surface, numClusters, retrogen);
	}

}
