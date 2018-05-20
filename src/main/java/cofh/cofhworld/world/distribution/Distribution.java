package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.biome.BiomeInfo;
import cofh.cofhworld.data.biome.BiomeInfoSet;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IFeatureGenerator;
import gnu.trove.set.hash.THashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;
import java.util.Set;

public abstract class Distribution implements IFeatureGenerator, IConfigurableFeatureGenerator {

	public final String name;

	protected GenRestriction biomeRestriction = GenRestriction.NONE;
	protected GenRestriction dimensionRestriction = GenRestriction.NONE;

	public final boolean regen;

	protected boolean withVillage = true;

	protected int rarity;

	protected final BiomeInfoSet biomes = new BiomeInfoSet(1);
	protected final Set<Integer> dimensions = new THashSet<>();

	public Distribution(String name, boolean regen) {

		this.name = name;
		this.regen = regen;
	}

	public Distribution setWithVillage(boolean inVillage) {

		this.withVillage = inVillage;
		return this;
	}

	public Distribution setRarity(int rarity) {

		this.rarity = rarity;
		return this;
	}

	public Distribution addBiome(BiomeInfo biome) {

		biomes.add(biome);
		return this;
	}

	public Distribution addBiomes(BiomeInfoSet biomes) {

		this.biomes.addAll(biomes);
		return this;
	}

	public Distribution addDimension(int dimID) {

		dimensions.add(dimID);
		return this;
	}

	public Distribution setBiomeRestriction(GenRestriction restriction) {

		this.biomeRestriction = restriction;
		return this;
	}

	public Distribution setDimensionRestriction(GenRestriction restriction) {

		this.dimensionRestriction = restriction;
		return this;
	}

	protected boolean canGenerateInBiome(World world, int x, int z, Random rand) {

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
	public boolean generateFeature(Random random, int chunkX, int chunkZ, World world, boolean hasVillage, boolean newGen) {

		if (!newGen && !regen) {
			return false;
		}
		if (hasVillage && !withVillage) {
			return false;
		}
		if (dimensionRestriction != GenRestriction.NONE && dimensionRestriction == GenRestriction.BLACKLIST == dimensions.contains(world.provider.getDimension())) {
			return false;
		}
		if (rarity > 1 && random.nextInt(rarity) != 0) {
			return false;
		}
		return generateFeature(random, chunkX * 16 + 8, chunkZ * 16 + 8, world);
	}

	public abstract boolean generateFeature(Random random, int blockX, int blockZ, World world);

}
