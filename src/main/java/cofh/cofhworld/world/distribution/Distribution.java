package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.biome.BiomeInfoSet;
import cofh.cofhworld.util.LinkedHashList;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IFeatureGenerator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.util.Arrays;
import java.util.Random;

public abstract class Distribution implements IFeatureGenerator, IConfigurableFeatureGenerator {

	public final String name;

	protected GenRestriction biomeRestriction = GenRestriction.NONE;
	protected GenRestriction dimensionRestriction = GenRestriction.NONE;
	protected GenRestriction structureRestriction = GenRestriction.NONE;

	public final boolean regen;

	protected int rarity;

	protected final BiomeInfoSet biomes = new BiomeInfoSet(1);
	protected final IntSet dimensions = new IntOpenHashSet();
	protected final LinkedHashList<String> structures = new LinkedHashList<>(1);

	public Distribution(String name, boolean regen) {

		this.name = name;
		this.regen = regen;
	}

	public Distribution setRarity(int rarity) {

		this.rarity = rarity; // TODO: deterministic chunk placement via ICondition and NumberProvider
		return this;
	}

	public Distribution addStructures(String[] structures) {

		this.structures.addAll(Arrays.asList(structures));
		return this;
	}

	public Distribution setStructureRestriction(GenRestriction restriction) {

		this.structureRestriction = restriction;
		return this;
	}

	public Distribution addBiomes(BiomeInfoSet biomes) {

		this.biomes.addAll(biomes);
		return this;
	}

	public Distribution setBiomeRestriction(GenRestriction restriction) {

		this.biomeRestriction = restriction;
		return this;
	}

	public Distribution addDimension(int dimID) {

		dimensions.add(dimID);
		return this;
	}

	public Distribution setDimensionRestriction(GenRestriction restriction) {

		this.dimensionRestriction = restriction;
		return this;
	}

	protected boolean canGenerateInBiome(IWorld world, int x, int z, Random rand) {

		if (biomeRestriction != GenRestriction.NONE) {
			Biome biome = world.getBiome(new BlockPos(x, 0, z));
			return biomeRestriction == GenRestriction.WHITELIST == biomes.contains(biome, rand);
		}
		return true;
	}

	/* IFeatureGenerator */
	@Override
	public final String getFeatureName() {

		return name;
	}

	@Override
	public boolean generateFeature(Random random, int chunkX, int chunkZ, ISeedReader world, boolean newGen) {

		if (!newGen && !regen) {
			return false;
		}
		if (structureRestriction != GenRestriction.NONE) {
			for (String structure : structures)
				if (structureRestriction == GenRestriction.WHITELIST == world.getChunk(chunkX, chunkZ).getStructureReferences().getOrDefault(structure,
						// TODO BAD
						LongSets.EMPTY_SET).isEmpty())
					return false;
		}

		if (dimensionRestriction != GenRestriction.NONE) { // TODO: completely broken
			Registry<World> reg = LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).func_244267_aX().getRegistry(Registry.WORLD_KEY);
			if (dimensionRestriction == GenRestriction.BLACKLIST == dimensions.contains(reg.getId(world.getWorld())))
				return false;
		}
		if (rarity > 1 && random.nextInt(rarity) != 0) {
			return false;
		}
		return generateFeature(random, chunkX * 16 + 8, chunkZ * 16 + 8, world);
	}

	public abstract boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world);

}
