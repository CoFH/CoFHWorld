package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import cofh.cofhworld.util.numbers.SkellamRandomProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DecorationGen implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final WeightedRandomBlock[] genBlock;
	private final WeightedRandomBlock[] onBlock;
	private final INumberProvider clusterSize;
	private boolean seeSky = true;
	private boolean checkStay = true;
	private INumberProvider stackHeight = new ConstantProvider(1);
	private INumberProvider xVar = new SkellamRandomProvider(8);
	private INumberProvider yVar = new SkellamRandomProvider(4);
	private INumberProvider zVar = new SkellamRandomProvider(8);

	public DecorationGen(List<WeightedRandomBlock> blocks, INumberProvider clusterSize, List<WeightedRandomBlock> material, List<WeightedRandomBlock> on) {

		this.cluster = blocks;
		this.clusterSize = clusterSize;
		genBlock = material == null ? null : material.toArray(new WeightedRandomBlock[material.size()]);
		onBlock = on == null ? null : on.toArray(new WeightedRandomBlock[on.size()]);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		final int clusterSize = this.clusterSize.intValue(world, rand, pos);

		boolean r = false;
		for (int l = clusterSize; l-- > 0; ) {
			int x = xStart + xVar.intValue(world, rand, pos);
			int y = yStart + yVar.intValue(world, rand, pos);
			int z = zStart + zVar.intValue(world, rand, pos);

			if (!world.isBlockLoaded(new BlockPos(x, y, z))) {
				++l;
				continue;
			}

			if ((!seeSky || world.canSeeSky(new BlockPos(x, y, z))) && ClusterGen.canGenerateInBlock(world, x, y - 1, z, onBlock) && ClusterGen.canGenerateInBlock(world, x, y, z, genBlock)) {

				WeightedRandomBlock block = ClusterGen.selectBlock(world, cluster);
				int stack = stackHeight.intValue(world, rand, pos);
				do {
					// TODO: checkStay logic
					if (!checkStay /*|| block.block.canBlockStay(world, x, y, z) Moved to BlockBush...*/) {
						r |= world.setBlockState(new BlockPos(x, y, z), block.getState(), 2);
					} else {
						break;
					}
					++y;
					if (!ClusterGen.canGenerateInBlock(world, x, y, z, genBlock)) {
						break;
					}
				} while (--stack > 0);
			}
		}
		return r;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			int clusterSize = genObject.getInt("cluster-size"); // TODO: another name?
			if (clusterSize <= 0) {
				log.warn("Invalid cluster size for generator '{}'", name);
				return null;
			}

			ArrayList<WeightedRandomBlock> list = new ArrayList<>();
			ConfigObject genData = genObject.root();
			if (!genObject.hasPath("surface")) {
				log.info("Entry does not specify surface for 'decoration' generator. Using grass.");
				list.add(new WeightedRandomBlock(Blocks.GRASS));
			} else {
				if (!FeatureParser.parseResList(genData.get("surface"), list, false)) {
					log.warn("Entry specifies invalid surface for 'decoration' generator! Using grass!");
					list.clear();
					list.add(new WeightedRandomBlock(Blocks.GRASS));
				}
			}

			DecorationGen r = new DecorationGen(resList, new ConstantProvider(clusterSize), matList, list);
			if (genObject.hasPath("see-sky")) {
				r.seeSky = genObject.getBoolean("see-sky");
			}
			if (genObject.hasPath("check-stay")) {
				r.checkStay = genObject.getBoolean("check-stay");
			}
			if (genObject.hasPath("stack-height")) {
				r.stackHeight = FeatureParser.parseNumberValue(genData.get("stack-height"));
			}
			if (genObject.hasPath("x-variance")) {
				r.xVar = FeatureParser.parseNumberValue(genData.get("x-variance"), 1, 15);
			}
			if (genObject.hasPath("y-variance")) {
				r.yVar = FeatureParser.parseNumberValue(genData.get("y-variance"), 0, 15);
			}
			if (genObject.hasPath("z-variance")) {
				r.zVar = FeatureParser.parseNumberValue(genData.get("z-variance"), 1, 15);
			}
			return r;
		}
	}
}
