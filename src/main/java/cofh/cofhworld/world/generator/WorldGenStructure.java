package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.IntegrityProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenStructure extends WorldGen {

	private final PlacementSettings placementSettings = new PlacementSettings();
	private final Template template;

	private final List<WeightedNBTTag> templates;

	private final List<WeightedEnum<Rotation>> rotations;
	private final List<WeightedEnum<Mirror>> mirrors;

	public WorldGenStructure(List<WeightedNBTTag> templates, List<Material> ignoredBlocks, boolean ignoreEntities,
			List<WeightedEnum<Rotation>> rotations, List<WeightedEnum<Mirror>> mirrors, final INumberProvider integrity) {

		if (templates.size() > 1) {
			this.template = null;
			this.templates = templates;
			Template template = new Template();
			for (WeightedNBTTag tag : templates)
				template.read(tag.getCompoundTag()); // crash here if any are invalid
		} else {
			this.templates = null;
			template = new Template();
			template.read(templates.get(0).getCompoundTag());
		}
		if (ignoredBlocks.size() > 1) {
			final Material[] ignoringBlocks = ignoredBlocks.toArray(new Material[0]);
			placementSettings.addProcessor(new BlockIgnoreStructureProcessor(Collections.emptyList()) {

				@Nullable
				@Override
				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {

					for (Material ignoredBlock : ignoringBlocks) {
						if (ignoredBlock.test(current.state))
							return null;
					}
					return current;
				}
			});
		} else if (ignoredBlocks.size() > 0) {
			final Material ignoredBlock = ignoredBlocks.get(0);
			placementSettings.addProcessor(new BlockIgnoreStructureProcessor(Collections.emptyList()) {

				@Nullable
				@Override
				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {

					return ignoredBlock.test(current.state) ? null : current;
				}
			});
		}
		placementSettings.setIgnoreEntities(ignoreEntities);

		this.rotations = rotations;
		this.mirrors = mirrors;
		if (integrity != null) {
			placementSettings.addProcessor(new IntegrityProcessor(0) {

				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {

					Random rand = settings.getRandom(current.pos);
					return integrity.doubleValue(world, rand, new DataHolder(current.pos)) <= rand.nextFloat() ? null : current;
				}
			});
		}
	}

	@Override
	public boolean generate(IWorld world, Random random, final DataHolder data) {

		Template template = this.template;
		if (templates != null) {
			template = new Template();
			template.read(WeightedRandom.getRandomItem(random, templates).getCompoundTag());
		}

		PlacementSettings settings = this.placementSettings.copy();

		// WeightedRandomLong to supply `setSeed` instead of the worldgen random?
		settings.setRandom(random);

		if (rotations != null) {
			settings.setRotation(WeightedRandom.getRandomItem(random, rotations).value);
		}
		if (mirrors != null) {
			settings.setMirror(WeightedRandom.getRandomItem(random, mirrors).value);
		}

		BlockPos start = template.getZeroPositionWithTransform(data.getPosition(), settings.getMirror(), settings.getRotation());

		return template.addBlocksToWorld(world, start, settings, 20);
	}

}
