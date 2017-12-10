package cofh.cofhworld.feature;

import cofh.cofhworld.biome.BiomeInfoSet;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import gnu.trove.set.hash.THashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;
import java.util.Set;

import static cofh.cofhworld.CoFHWorld.log;

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

    private String name;

    private boolean enabled = true;

    private GenRestriction biomeRestriction = GenRestriction.NONE;
    private GenRestriction dimensionRestriction = GenRestriction.NONE;

    private boolean retrogen = false;

    private boolean withVillage = true;

    private int rarity;
    private INumberProvider chunkCount;

    private final BiomeInfoSet biomes = new BiomeInfoSet(1);
    private final Set<Integer> dimensions = new THashSet<>();


    private IGenerator generator;
    private IDistribution distribution;

    public Feature(String name, Config config) {
        this.name = name;
        loadFromConfig(config);
    }

    public void setDistribution(IDistribution d) {

        this.distribution = d;
    }

    public void setGenerator(IGenerator g) {

        this.generator = g;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public INumberProvider getChunkCount() {
        return chunkCount;
    }

    public final String getName() {

        return name;
    }

    public boolean generate(Random random, int chunkX, int chunkZ, World world, boolean hasVillage, boolean newGen) {

        if (!newGen && !retrogen) {
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

        return distribution.apply(this, random, chunkX * 16 + 8, chunkZ * 16 + 8, world);
    }

    public boolean canGenerateInBiome(World world, int x, int z, Random rand) {

        if (biomeRestriction != GenRestriction.NONE) {
            Biome biome = world.getBiome(new BlockPos(x, 0, z));
            return !(biomeRestriction == GenRestriction.BLACKLIST == biomes.contains(biome, rand));
        }
        return true;
    }

    public boolean applyGenerator(World world, Random rand, BlockPos p) {
        return this.generator.generate(this, world, rand, p);
    }

    private void loadFromConfig(Config config) {
        // If the feature is disabled, quick bail
        if (config.hasPath("enabled") && !config.getBoolean("enabled")) {
            this.enabled = false;
            return;
        }

        this.retrogen = config.hasPath("retrogen") && config.getBoolean("retrogen");

        // Identify the type of biome restriction; ensure it's an object
        if (config.hasPath("biome")) {
            ConfigValue data = config.getValue("biome");
            if (data.valueType() == ConfigValueType.OBJECT) {
                this.biomeRestriction = GenRestriction.get(config.getString("biome.restriction"));
                this.biomes.addAll(FeatureParser.parseBiomeRestrictions(config.getConfig("biome")));
            } else if (data.valueType() == ConfigValueType.STRING) {
                if (config.getString("biome").equals("all")) {
                    this.biomeRestriction = GenRestriction.NONE;
                } else {
                    log.error("Invalid biome restriction {} on feature {}", data, name);
                }
            } else {
                // Invalid biome restriction entry; default is already NONE, so just log a warning
                log.warn("Skipping biome restriction {} on feature {}; needs to be an object.", data, name);
            }
        }

        // Identify type of dimension restriction; it should be an object, list or number
        if (config.hasPath("dimension")) {
            String field = "dimension";
            ConfigValue data = config.getValue("dimension");
            switch (data.valueType()) {
                case OBJECT:
                    this.dimensionRestriction = GenRestriction.get(config.getString("dimension.restriction"));
                    field += ".value"; // 'ware the explicit fall through to next case
                case LIST:
                    // Force to whitelist if no restriction was specified
                    if (this.dimensionRestriction == GenRestriction.NONE) {
                        this.dimensionRestriction = GenRestriction.WHITELIST;
                    }

                    // Process all dimensions in the list
                    ConfigList restrictionList = config.getList(field);
                    for (int i = 0; i < restrictionList.size(); i++) {
                        ConfigValue val = restrictionList.get(i);
                        if (val.valueType() == ConfigValueType.NUMBER) {
                            dimensions.add(((Number) val.unwrapped()).intValue());
                        }
                    }
                    break;
                case NUMBER:
                    this.dimensionRestriction = GenRestriction.WHITELIST;
                    this.dimensions.add(((Number)data.unwrapped()).intValue());
                    break;
                default:
                    log.warn("Skipping dimension restriction {} on feature {}; needs to be an object, list or number.", data, name);
            }
        }

        // Get other feature specific values
        this.chunkCount = FeatureParser.parseNumberValue(config.getValue("cluster-count"), 0, Long.MAX_VALUE);

        if (config.hasPath("chunk-chance")) {
            this.rarity = MathHelper.clamp(config.getInt("chunk-chance"), 1, 1000000000);
        }

        if (config.hasPath("in-village")) {
            this.withVillage = config.getBoolean("in-village");
        }
    }
}
