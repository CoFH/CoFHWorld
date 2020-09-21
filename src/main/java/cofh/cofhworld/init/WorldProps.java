package cofh.cofhworld.init;

import cofh.cofhworld.parser.distribution.*;
import cofh.cofhworld.parser.generator.*;
import cofh.cofhworld.util.Utils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cofh.cofhworld.CoFHWorld.log;

public class WorldProps {

	private WorldProps() {

	}

	public static void preInit() {

		config();
		ModLoadingContext.get().registerConfig(Type.COMMON, config);
		FMLJavaModLoadingContext.get().getModEventBus().register(WorldProps.class);

		initFeatures();
	}

	private static ForgeConfigSpec config;

	private static BooleanValue replaceStandardGenerationValue;
	private static BooleanValue enableRetroactiveGenerationValue;

	private static BooleanValue enableFlatBedrockValue;
	private static BooleanValue enableRetroactiveFlatBedrockValue;
	private static BooleanValue forceFullRegenerationValue;

	private static IntValue numBedrockLayersValue;

	/* HELPERS */
	private static void config() {

		final ForgeConfigSpec.Builder config = new ForgeConfigSpec.Builder();

		{
			config.push("World");

			disableFeatureGeneration = config
					.comment("If TRUE, CoFH World will not generate features at all." +
							" This option is intended for use when you want another mod to handle ore generation but do not want to blank out the various .json files yourself." +
							" Flat Bedrock may still be used.")
					.define("DisableAllGeneration", false);

			replaceStandardGenerationValue = config
					.comment("If TRUE, standard Minecraft ore generation will be REPLACED." +
							" Configure in the '" + FILE_GEN_STANDARD_INTERNAL + "' file; standard Minecraft defaults have been provided." +
							" If you rename the '" + FILE_GEN_STANDARD_INTERNAL + "' file, this option WILL NOT WORK.")
					.define("ReplaceStandardGeneration", replaceStandardGeneration);

			enableRetroactiveGenerationValue = config
					.comment("If TRUE, world generation handled by CoFH World will be retroactively applied to existing chunks." +
							" This option will NOT apply previously existing generation to chunks that have been loaded before this option was enabled.")
					.define("RetroactiveGeneration", enableRetroactiveGeneration);

			forceFullRegenerationValue = config
					.comment("If TRUE, world generation handled by CoFH World will be applied to all existing chunks during retroactive generation." +
							" This option CIRCUMVENTS the logic CoFH World uses to avoid re-generating things that have already been applied to a chunk." +
							" ALL chunks loaded while this option is enabled will be re-generated EVERY time they are loaded.")
					.worldRestart()
					.define("ForceFullRegeneration", forceFullRegeneration);

			{
				config.push("Trees");

				chanceTreeGrowth = config
						.comment("This adjusts the % chance that a tree will grow as normal when it is meant to." +
								" Reducing this value will mean that trees take longer to grow, on average.")
						.defineInRange("SaplingGrowthChance", 100, 1, 100);

				config.pop();
			}

			{
				config.push("Bedrock");

				enableFlatBedrockValue = config
						.comment("If TRUE, the bedrock layer will be flattened.")
						.define("Flat", enableFlatBedrock);

				enableRetroactiveFlatBedrockValue = config
						.comment("If TRUE, Flat Bedrock will retroactively be applied to existing chunks, if retroactive generation is enabled.")
						.define("FlatRetroactive", enableRetroactiveFlatBedrock);

				numBedrockLayersValue = config
						.comment("This adjusts the number of layers of Flat Bedrock, if enabled.")
						.defineInRange("FlatBedrockLayers", numBedrockLayers, 1, maxBedrockLayers);

				config.pop();
			}

			config.pop();
		}

		WorldProps.config = config.build();
	}

	@SubscribeEvent
	public static void configLoading(final ModConfig.Loading event) {

		refreshConfig();
	}

	@SubscribeEvent
	public static void configReloading(final ModConfig.Reloading event) {

		refreshConfig();
	}

	private static void refreshConfig() {

		replaceStandardGeneration = replaceStandardGenerationValue.get();
		enableRetroactiveGeneration = enableRetroactiveGenerationValue.get();

		enableFlatBedrock = enableFlatBedrockValue.get();
		enableRetroactiveFlatBedrock = enableRetroactiveFlatBedrockValue.get();
		forceFullRegeneration = forceFullRegenerationValue.get();

		numBedrockLayers = numBedrockLayersValue.get();
	}

	private static void initFeatures() {

		log.info("Registering default Feature Templates...");
		FeatureParser.registerTemplate("gaussian", new DistParserGaussian());
		FeatureParser.registerTemplate("uniform", new DistParserUniform());
		FeatureParser.registerTemplate("surface", new DistParserSurface());
		FeatureParser.registerTemplate("fractal", new DistParserLargeVein());
		FeatureParser.registerTemplate("decoration", new DistParserDecoration());
		FeatureParser.registerTemplate("underwater", new DistParserUnderfluid(true));
		FeatureParser.registerTemplate("underfluid", new DistParserUnderfluid(false));
		FeatureParser.registerTemplate("cave", new DistParserCave());
		FeatureParser.registerTemplate("sequential", new DistParserSequential());
		FeatureParser.registerTemplate("custom", new DistParserCustom());
		FeatureParser.registerTemplate("replace", new DistParserReplace());

		log.info("Registering default World Generators...");
		FeatureParser.registerGenerator(null, new GenParserCluster(false));
		FeatureParser.registerGenerator("", new GenParserCluster(false));
		FeatureParser.registerGenerator("cluster", new GenParserCluster(false));
		FeatureParser.registerGenerator("sparse-cluster", new GenParserCluster(true));
		FeatureParser.registerGenerator("large-vein", new GenParserLargeVein());
		FeatureParser.registerGenerator("decoration", new GenParserDecoration());
		FeatureParser.registerGenerator("lake", new GenParserLake());
		FeatureParser.registerGenerator("plate", new GenParserPlate());
		FeatureParser.registerGenerator("geode", new GenParserGeode());
		FeatureParser.registerGenerator("spike", new GenParserSpike());
		FeatureParser.registerGenerator("boulder", new GenParserBoulder());
		FeatureParser.registerGenerator("dungeon", new GenParserDungeon());
		FeatureParser.registerGenerator("stalagmite", new GenParserStalagmite(false));
		FeatureParser.registerGenerator("stalactite", new GenParserStalagmite(true));
		FeatureParser.registerGenerator("small-tree", new GenParserSmallTree());
		FeatureParser.registerGenerator("spout", new GenParserSpout());
		// meta-generators
		FeatureParser.registerGenerator("structure", new GenParserStructure());
		FeatureParser.registerGenerator("sequential", new GenParserSequential());
		FeatureParser.registerGenerator("consecutive", new GenParserConsecutive());

		log.info("Verifying or creating base world generation directory...");

		configDir = new File("./config"); // TODO: figure out how to make forge cough this up

		worldGenDir = new File(configDir, "/cofh/world/");
		worldGenPath = Paths.get(configDir.getPath());
		try {
			cannonicalWorldGenDir = worldGenDir.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
			replaceStandardGeneration = false;
			log.error("Could not create standard generation json.", t);
		}
		log.info("Complete.");
	}

	/* INSTANCE CONSTANTS */
	public static File configDir;

	/* WORLD */
	public static Path worldGenPath;
	public static File worldGenDir;
	public static String cannonicalWorldGenDir;
	public static File standardGenFile;

	public static final String FILE_GEN_STANDARD_INTERNAL = "00_minecraft.json";
	public static final String PATH_GEN_STANDARD_INTERNAL = "assets/cofhworld/world/" + FILE_GEN_STANDARD_INTERNAL;

	public static BooleanValue disableFeatureGeneration;
	public static boolean replaceStandardGeneration = false;
	public static boolean enableRetroactiveGeneration = true; // TODO: false

	public static boolean enableFlatBedrock = false;
	public static boolean enableRetroactiveFlatBedrock = false;
	public static boolean forceFullRegeneration = true; // TODO: very false

	public static IntValue chanceTreeGrowth;
	public static int numBedrockLayers = 1;
	public static int maxBedrockLayers = 8;

}
