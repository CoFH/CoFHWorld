package cofh.cofhworld.init;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.decoration.parser.*;
import cofh.cofhworld.feature.parser.*;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.world.generator.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cofh.cofhworld.CoFHWorld.log;

public class WorldProps {

	private WorldProps() {

	}

	public static void preInit() {

		configCommon();

		init();
	}

	/* HELPERS */
	private static void configCommon() {

		String category;
		String comment;

		category = "World";

		comment = "If TRUE, standard Minecraft ore generation will be REPLACED. Configure in the 00_minecraft.json file; standard Minecraft defaults have been provided. If you rename the 00_minecraft.json file, this option WILL NOT WORK.";
		disableStandardGeneration = CoFHWorld.config.getBoolean("ReplaceStandardGeneration", category, disableStandardGeneration, comment);

		comment = "If TRUE, world generation handled by CoFH World will be retroactively applied to existing chunks.";
		enableRetroactiveGeneration = CoFHWorld.config.getBoolean("RetroactiveGeneration", category, enableRetroactiveGeneration, comment);

		comment = "This adjusts the % chance that a tree will grow as normal when it is meant to. Reducing this value will mean that trees take longer to grow, on average.";
		chanceTreeGrowth = CoFHWorld.config.getInt("TreeGrowthChance", category, chanceTreeGrowth, 1, 100, comment);

		category = "World.Bedrock";

		comment = "If TRUE, the bedrock layer will be flattened.";
		enableFlatBedrock = CoFHWorld.config.getBoolean("EnableFlatBedrock", category, enableFlatBedrock, comment);

		comment = "This adjusts the number of layers of Flat Bedrock, if enabled.";
		numBedrockLayers = CoFHWorld.config.getInt("NumBedrockLayers", category, 2, 1, maxBedrockLayers, comment);

		comment = "If TRUE, Flat Bedrock will retroactively be applied to existing chunks, if retroactive generation is enabled.";
		enableRetroactiveFlatBedrock = CoFHWorld.config.getBoolean("EnableRetroactiveFlatBedrock", category, enableRetroactiveFlatBedrock, comment);
	}

	private static void init() {

		log.info("Registering default Feature Templates...");
		FeatureParser.registerTemplate("gaussian", new GaussianParser());
		FeatureParser.registerTemplate("uniform", new UniformParser());
		FeatureParser.registerTemplate("surface", new SurfaceParser());
		FeatureParser.registerTemplate("fractal", new FractalParser());
		FeatureParser.registerTemplate("decoration", new DecorationParser());
		FeatureParser.registerTemplate("underwater", new UnderfluidParser(true));
		FeatureParser.registerTemplate("underfluid", new UnderfluidParser(false));
		FeatureParser.registerTemplate("cave", new CaveParser());

		log.info("Registering default World Generators...");
		FeatureParser.registerGenerator(null, new WorldGenMinableCluster.Parser());
		FeatureParser.registerGenerator("", new WorldGenMinableCluster.Parser());
		FeatureParser.registerGenerator("cluster", new WorldGenMinableCluster.Parser());
		FeatureParser.registerGenerator("sparse-cluster", new WorldGenSparseMinableCluster.Parser());
		FeatureParser.registerGenerator("large-vein", new LargeVeinParser());
		FeatureParser.registerGenerator("decoration", new DecorationParser());
		FeatureParser.registerGenerator("lake", new WorldGenAdvLakes.Parser());
		FeatureParser.registerGenerator("plate", new PlateParser());
		FeatureParser.registerGenerator("geode", new WorldGenGeode.Parser());
		FeatureParser.registerGenerator("spike", new SpikeParser());
		FeatureParser.registerGenerator("boulder", new WorldGenBoulder.Parser());
		FeatureParser.registerGenerator("dungeon", new DungeonParser());
		FeatureParser.registerGenerator("stalagmite", new StalagmiteParser(false));
		FeatureParser.registerGenerator("stalactite", new StalagmiteParser(true));
		FeatureParser.registerGenerator("small-tree", new SmallTreeParser());

		log.info("Verifying or creating base world generation directory...");

		worldGenDir = new File(configDir, "/cofh/world/");
		worldGenPath = Paths.get(configDir.getPath());

		if (!worldGenDir.exists()) {
			try {
				if (!worldGenDir.mkdir()) {
					throw new Error("Could not create directory (unspecified error).");
				} else {
					log.info("Created world generation directory.");
				}
			} catch (Throwable t) {
				log.fatal("Could not create world generation directory:", t);
				return;
			}
		}
		standardGenFile = new File(worldGenDir, FILE_GEN_STANDARD_INTERNAL);

		try {
			if (standardGenFile.createNewFile()) {
				Utils.copyFileUsingStream(PATH_GEN_STANDARD_INTERNAL, standardGenFile);
				log.info("Created standard generation json.");
			} else if (!standardGenFile.exists()) {
				throw new Error("Unable to create standard generation json (unspecified error).");
			}
		} catch (Throwable t) {
			disableStandardGeneration = false;
			log.error("Could not create standard generation json.", t);
		}
		log.info("Complete.");
	}

	/* INSTANCE CONSTANTS */
	public static File configDir;

	/* WORLD */
	public static Path worldGenPath;
	public static File worldGenDir;
	public static File standardGenFile;

	public static final String FILE_GEN_STANDARD_INTERNAL = "00_minecraft.json";
	public static final String PATH_GEN_STANDARD_INTERNAL = "assets/cofhworld/world/" + FILE_GEN_STANDARD_INTERNAL;

	public static boolean disableStandardGeneration = false;
	public static boolean enableRetroactiveGeneration = false;

	public static boolean enableFlatBedrock = false;
	public static boolean enableRetroactiveFlatBedrock = false;
	public static boolean forceFullRegeneration = false;

	public static int chanceTreeGrowth = 100;
	public static int numBedrockLayers = 1;
	public static int maxBedrockLayers = 8;

}
