package cofh.cofhworld.feature;

import cofh.cofhworld.biome.BiomeInfo;
import cofh.cofhworld.biome.BiomeInfoSet;
import gnu.trove.set.hash.THashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;
import java.util.Set;

public class Feature {

    public enum GenRestriction {
        NONE, BLACKLIST, WHITELIST;

        public static cofh.cofhworld.feature.Feature.GenRestriction get(String restriction) {

            if (restriction.equalsIgnoreCase("blacklist")) {
                return BLACKLIST;
            }
            if (restriction.equalsIgnoreCase("whitelist")) {
                return WHITELIST;
            }
            return NONE;
        }
    }

    private final String name;

    private final GenRestriction biomeRestriction;
    private final GenRestriction dimensionRestriction;

    private final boolean regen;

    private boolean withVillage = true;

    private int rarity;

    private final BiomeInfoSet biomes = new BiomeInfoSet(1);
    private final Set<Integer> dimensions = new THashSet<>();

    private IGenerator generator;
    private IDistribution distribution;

    /**
     * Shortcut to add a Feature with no biome or dimension restriction.
     */
    public Feature(String name, boolean regen) {

        this(name, GenRestriction.NONE, regen, GenRestriction.NONE);
    }

    /**
     * Shortcut to add a Feature with a dimension restriction but no biome restriction.
     */
    public Feature(String name, boolean regen, GenRestriction dimRes) {

        this(name, GenRestriction.NONE, regen, dimRes);
    }

    /**
     * Shortcut to add a Feature with a biome restriction but no dimension restriction.
     */
    public Feature(String name, GenRestriction biomeRes, boolean regen) {

        this(name, biomeRes, regen, GenRestriction.NONE);
    }

    public Feature(String name, GenRestriction biomeRes, boolean regen, GenRestriction dimRes) {

        this.name = name;
        this.biomeRestriction = biomeRes;
        this.dimensionRestriction = dimRes;
        this.regen = regen;
    }

    public void setRarity(int rarity) {

        this.rarity = rarity;
    }

    public void setDistribution(IDistribution d) {

        this.distribution = d;
    }

    public void setGenerator(IGenerator g) {

        this.generator = g;
    }

    public Feature addBiome(BiomeInfo biome) {

        biomes.add(biome);
        return this;
    }

    public Feature addBiomes(BiomeInfoSet biomes) {

        this.biomes.addAll(biomes);
        return this;
    }

    public Feature addDimension(int dimID) {

        dimensions.add(dimID);
        return this;
    }

    public final String getName() {

        return name;
    }

    public boolean generate(Random random, int chunkX, int chunkZ, World world, boolean hasVillage, boolean newGen) {

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

        return distribution.apply(this, random, chunkX, chunkZ, world, generator);
    }

    public boolean canGenerateInBiome(World world, int x, int z, Random rand) {

        if (biomeRestriction != GenRestriction.NONE) {
            Biome biome = world.getBiome(new BlockPos(x, 0, z));
            return !(biomeRestriction == GenRestriction.BLACKLIST == biomes.contains(biome, rand));
        }
        return true;
    }
}
