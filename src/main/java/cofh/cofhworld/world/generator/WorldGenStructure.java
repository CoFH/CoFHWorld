package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomEnum;
import cofh.cofhworld.util.WeightedRandomNBTTag;
import cofh.cofhworld.util.numbers.INumberProvider;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import java.util.List;
import java.util.Random;

public class WorldGenStructure extends WorldGenerator {

	private final PlacementSettings placementSettings = new PlacementSettings();
	private final Template template = new Template();

	private final List<WeightedRandomNBTTag> templates;

	private final List<WeightedRandomBlock> ignoredBlocks;

	private List<WeightedRandomEnum<Rotation>> rots;
	private List<WeightedRandomEnum<Mirror>> mirrors;

	private INumberProvider integrity;

	public WorldGenStructure(List<WeightedRandomNBTTag> templates, List<WeightedRandomBlock> ignoredBlocks, boolean ignoreEntities) {

		if (templates.size() > 1) {
			this.templates = templates;
		} else {
			this.templates = null;
			template.read(templates.get(0).getCompoundTag());
		}
		if (ignoredBlocks.size() > 1) {
			this.ignoredBlocks = ignoredBlocks;
		} else {
			this.ignoredBlocks = null;
			if (ignoredBlocks.size() > 0) {
				placementSettings.setReplacedBlock(ignoredBlocks.get(0).block);
			}
		}
		placementSettings.setIgnoreEntities(ignoreEntities);
	}



	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		if (templates != null) {
			template.read(WeightedRandom.getRandomItem(random, templates).getCompoundTag());
		}

		placementSettings.setRandom(random);

		if (rots != null) {
			placementSettings.setRotation(WeightedRandom.getRandomItem(random, rots).value);
		}
		if (mirrors != null) {
			placementSettings.setMirror(WeightedRandom.getRandomItem(random, mirrors).value);
		}

		if (ignoredBlocks != null) {
			placementSettings.setReplacedBlock(WeightedRandom.getRandomItem(random, ignoredBlocks).block);
		}

		BlockPos start = template.getZeroPositionWithTransform(pos, placementSettings.getMirror(), placementSettings.getRotation());

		BlockPos checkValue = start;

		BlockPos blockpos = template.transformedSize(placementSettings.getRotation());
		int l = 256;

		for (int i1 = 0; i1 < blockpos.getX(); ++i1) {
			for (int j1 = 0; j1 < blockpos.getZ(); ++j1) {
				l = Math.min(l, world.getHeight(pos.getX() + i1, pos.getZ() + j1));
			}
		}

		placementSettings.setIntegrity(integrity.floatValue(world, random, checkValue));

		template.addBlocksToWorld(world, start, placementSettings, 20);


		return false;
	}

}
