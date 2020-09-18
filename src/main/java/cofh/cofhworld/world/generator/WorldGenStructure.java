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
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace vanilla Template logic with custom code for better control and features
 */
@Deprecated
public class WorldGenStructure extends WorldGen {

	private final PlacementSettings placementSettings = new PlacementSettings();
	private final Template template;

	private final List<WeightedNBTTag> templates;

	private final List<WeightedBlock> ignoredBlocks;

	private List<WeightedEnum<Rotation>> rots;
	private List<WeightedEnum<Mirror>> mirrors;

	private INumberProvider integrity = new ConstantProvider(2f); // 1++

	public WorldGenStructure(List<WeightedNBTTag> templates, List<WeightedBlock> ignoredBlocks, boolean ignoreEntities) {

		if (templates.size() > 1) {
			this.templates = templates;
			template = null;
		} else {
			this.templates = null;
			template = new Template();
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

	public WorldGenStructure setIntegrity(INumberProvider itg) {

		integrity = itg;
		return this;
	}

	public WorldGenStructure setDetails(List<WeightedEnum<Rotation>> rot, List<WeightedEnum<Mirror>> mir) {

		switch (rot.size()) {
			case 1:
				placementSettings.setRotation(rot.get(0).value);
			case 0:
				rot = null;
			default:
				rots = rot;
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
	public boolean generate(World world, Random random, BlockPos pos) {

		Template template = this.template;
		if (templates != null) {
			template = new Template();
			template.read(WeightedRandom.getRandomItem(random, templates).getCompoundTag());
		}

		PlacementSettings settings = this.placementSettings.copy();

		// WeightedRandomLong to supply `setSeed` instead of the worldgen random?
		settings.setRandom(random);

		if (rots != null) {
			settings.setRotation(WeightedRandom.getRandomItem(random, rots).value);
		}
		if (mirrors != null) {
			settings.setMirror(WeightedRandom.getRandomItem(random, mirrors).value);
		}

		if (ignoredBlocks != null) {
			settings.setReplacedBlock(WeightedRandom.getRandomItem(random, ignoredBlocks).block);
		}

		BlockPos start = template.getZeroPositionWithTransform(pos, settings.getMirror(), settings.getRotation());

		settings.setIntegrity(integrity.floatValue(world, random, new DataHolder(pos)));

		template.addBlocksToWorld(world, start, settings, 20);

		return true; // we probably did something. templates don't actually feed back information like that
	}

}
