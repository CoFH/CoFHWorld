package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.world.generator.WorldGenStructure;
import com.google.common.collect.Lists;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuilderStructure implements IBuilder<WorldGenStructure> {

	// @formatter:off
	protected static ArrayList<WeightedEnum<Rotation>> ALL_ROTATION = Lists.newArrayList(
			new WeightedEnum<>(Rotation.NONE, 1),
			new WeightedEnum<>(Rotation.CLOCKWISE_90, 1),
			new WeightedEnum<>(Rotation.CLOCKWISE_180, 1),
			new WeightedEnum<>(Rotation.COUNTERCLOCKWISE_90, 1)
	);
	// @formatter:on
	protected static ArrayList<WeightedEnum<Mirror>> NO_MIRROR = new ArrayList<>();

	private List<WeightedNBTTag> templates;

	private List<Material> ignoredBlocks = Collections.emptyList();
	private boolean ignoreEntities = false;

	private List<WeightedEnum<Rotation>> rotations = ALL_ROTATION;
	private List<WeightedEnum<Mirror>> mirrors = NO_MIRROR;

	private INumberProvider integrity = null;

	public void setTemplates(List<WeightedNBTTag> templates) {

		this.templates = templates;
	}

	public void setIgnoredBlocks(List<Material> ignoredBlocks) {

		this.ignoredBlocks = ignoredBlocks;
	}

	public void setIgnoreEntities(boolean ignoreEntities) {

		this.ignoreEntities = ignoreEntities;
	}

	public void setRotations(List<WeightedEnum<Rotation>> rotations) {

		this.rotations = rotations;
	}

	public void setMirrors(List<WeightedEnum<Mirror>> mirrors) {

		this.mirrors = mirrors;
	}

	public void setIntegrity(INumberProvider integrity) {

		this.integrity = integrity;
	}

	@Nonnull
	@Override
	public WorldGenStructure build() {

		return new WorldGenStructure(templates, ignoredBlocks, ignoreEntities, rotations, mirrors, integrity);
	}
}
