package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.*;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenStructure extends WorldGen {

	private final PlacementSettings placementSettings = new PlacementSettings();
	private final Template template;

	private final List<WeightedNBTTag> templates;

	private List<WeightedEnum<Rotation>> rotations;
	private List<WeightedEnum<Mirror>> mirrors;

	private INumberProvider integrity = new ConstantProvider(2f); // 1++

	public WorldGenStructure(List<WeightedNBTTag> templates, List<WeightedBlock> ignoredBlocks, boolean ignoreEntities) {

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
			placementSettings.addProcessor(new BlockIgnoreStructureProcessor(Collections.EMPTY_LIST) {

				@Nullable
				@Override
				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {

					for (WeightedBlock ignoredBlock : ignoredBlocks) {
						if (ignoredBlock.getState().equals(current.state))
							return null;
					}
					return current;
				}
			});
		} else if (ignoredBlocks.size() > 0) {
			final WeightedBlock ignoredBlock = ignoredBlocks.get(0);
			placementSettings.addProcessor(new BlockIgnoreStructureProcessor(Collections.EMPTY_LIST) {

				@Nullable
				@Override
				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {

					return ignoredBlock.getState().equals(current.state) ? null : current;
				}
			});
		}
		placementSettings.setIgnoreEntities(ignoreEntities);
	}

	public WorldGenStructure setIntegrity(INumberProvider itg) {

		integrity = itg;
		l: {
			for (StructureProcessor processor : placementSettings.getProcessors()) {
				if (processor instanceof IntegrityProcessor)
					break l;
			}
			placementSettings.addProcessor(new IntegrityProcessor(0) {
				public BlockInfo process(IWorldReader world, BlockPos offset, BlockInfo original, BlockInfo current, PlacementSettings settings) {
					Random rand = settings.getRandom(current.pos);
					return WorldGenStructure.this.integrity.doubleValue(world, rand, new DataHolder(current.pos)) <= rand.nextFloat() ? null : current;
				}
			});
		}
		return this;
	}

	public WorldGenStructure setDetails(List<WeightedEnum<Rotation>> rot, List<WeightedEnum<Mirror>> mir) {

		switch (rot.size()) {
			case 1:
				placementSettings.setRotation(rot.get(0).value);
			case 0:
				rot = null;
			default:
				rotations = rot;
		}

		switch (mir.size()) {
			case 1:
				placementSettings.setMirror(mir.get(0).value);
			case 0:
				mir = null;
			default:
				mirrors = mir;
		}
		return this;
	}

	@Override
	public boolean generate(IWorld world, Random random, BlockPos pos) {

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

		BlockPos start = template.getZeroPositionWithTransform(pos, settings.getMirror(), settings.getRotation());

		template.addBlocksToWorld(world, start, settings, 20);

		return true; // we probably did something. templates don't actually feed back information like that
	}

}
