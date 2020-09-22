package cofh.cofhworld.init;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.ChunkGenerationHandler.RetroChunkCoord;
import cofh.cofhworld.util.ChunkCoord;
import cofh.cofhworld.util.LinkedHashList;
import cofh.cofhworld.world.IFeatureGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus.Type;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

public class WorldHandler {

	public static final WorldHandler INSTANCE = new WorldHandler();

	private static List<IFeatureGenerator> features = new ArrayList<>();
	private static Set<String> featureNames = new HashSet<>();
	private static LinkedHashList<ChunkReference> populatingChunks = new LinkedHashList<>();

	private static List<Runnable> reloadCallbacks = new LinkedList<>();

	private static long genHash = 0;

	public static void initialize() {

	}

	public static void register() {

		//GameRegistry.registerWorldGenerator((random, chunkX, chunkZ, world, chunkGenerator, chunkProvider) -> populatingChunks.remove(new ChunkReference(world.provider.getDimension(), chunkX, chunkZ)), Integer.MAX_VALUE);

		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	private WorldHandler() {

	}

	public static void registerReloadCallback(Runnable callback) {

		reloadCallbacks.add(callback);
	}

	public static boolean reloadConfig() {

		return reloadConfig(true);
	}

	// FIXME: move all this non-world-handling logic somewhere else.
	public static boolean reloadConfig(boolean clear) {

		if (WorldProps.disableFeatureGeneration.get()) {
			CoFHWorld.log.warn(" Feature Generation has been disabled via the Config file. This option should only be set if you have explicitly configured another mod to handle ore generation.");
			return false;
		}
		if (clear) {
			// Reset all features so that config will reload properly
			features.clear();
			featureNames.clear();
		}

		// Parse all the generation files into features
		try {
			FeatureParser.processGenerationFiles();
			reloadCallbacks.forEach(Runnable::run);
			return true;
		} catch (Throwable t) {
			CoFHWorld.log.catching(t);
			return false;
		}
	}

	public static boolean registerFeature(IFeatureGenerator feature) {

		String featureName = feature.getFeatureName();
		if (featureName == null) {
			CoFHWorld.log.error("A feature attempted to register without providing a valid name... ignoring.");
			return false;
		}
		if (featureNames.contains(featureName)) {
			CoFHWorld.log.debug("Feature " + featureName + " was attempting to register a second time... ignoring.");
			return false;
		}
		featureNames.add(featureName);
		features.add(feature);
		genHash += featureName.hashCode();
		return true;
	}

	public static boolean removeFeature(IFeatureGenerator feature) {

		String featureName = feature.getFeatureName();
		if (featureName == null) {
			return false;
		}
		if (featureNames.contains(featureName)) {
			featureNames.remove(featureName);
			features.remove(feature);
			genHash -= featureName.hashCode();
		}
		return true;
	}

	@Nullable
	public static IFeatureGenerator findFeature(String name) {

		for (IFeatureGenerator feature : features) {
			if (feature.getFeatureName().equals(name)) {
				return feature;
			}
		}
		return null;
	}

	public static List<IFeatureGenerator> getFeatures() {

		return features;
	}

	/* EVENT HANDLING */
//	@SubscribeEvent
//	public void handlePopulateChunkEvent(PopulateChunkEvent.Pre event) {
//
//		populatingChunks.add(new ChunkReference(event.getWorld().provider.getDimension(), event.getChunkX(), event.getChunkZ()));
//	}
//
//	@SubscribeEvent
//	public void populateChunkEvent(PopulateChunkEvent.Post event) {
//
//		ChunkReference pos = populatingChunks.get(new ChunkReference(event.getWorld().provider.getDimension(), event.getChunkX(), event.getChunkZ()));
//
//		if (pos != null) {
//			pos.hasVillage = event.isHasVillageGenerated();
//		}
//	}

	private int GENERATING = 0;
	@SubscribeEvent
	public void handleChunkLoadEvent(ChunkDataEvent.Load event) {

		if (GENERATING != 0) {
			return; // erm... no.
		}
		if (event.getWorld() == null) {
			// ???
			CoFHWorld.log.debug(() -> "Null World for chunk! It's somewhere, somewhen, somehow, but we can't retrogen in it. At: " +
					(event.getChunk() == null ? "NULL" /* ??? ??? ??? */ : event.getChunk().getPos().toString()));
			return;
		}
		Dimension dim = event.getWorld().getDimension();
		CompoundNBT tag = (CompoundNBT) event.getData().get(CoFHWorld.MOD_ID);

		if (tag != null && tag.getBoolean("Populating")) {
			ChunkReference chunk = new ChunkReference(dim, event.getChunk().getPos().x, event.getChunk().getPos().z);
			populatingChunks.add(chunk);
			return;
		}

		ListNBT list = null;
		ChunkCoord cCoord = new ChunkCoord(event.getChunk());

		boolean regen = false;

		if (tag != null) {
			boolean genFeatures = false;
			boolean bedrock = WorldProps.enableRetroactiveFlatBedrock & WorldProps.enableFlatBedrock && !tag.contains("Bedrock");
			if (WorldProps.enableRetroactiveGeneration) {
				genFeatures = tag.getLong("Hash") != genHash;
				if (tag.contains("List", NBT.TAG_LIST)) {
					list = tag.getList("List", NBT.TAG_STRING);
					genFeatures |= list.size() != features.size();
				}
			}
			if (bedrock) {
				CoFHWorld.log.debug("Queuing RetroGen for flattening bedrock for the chunk at " + cCoord.toString() + ".");
				regen = true;
			}
			if (genFeatures) {
				CoFHWorld.log.debug("Queuing RetroGen for features for the chunk at " + cCoord.toString() + ".");
				regen = true;
			}
		} else {
			regen = WorldProps.enableRetroactiveFlatBedrock & WorldProps.enableFlatBedrock | WorldProps.enableRetroactiveGeneration;
		}
		if (regen) {
			ChunkGenerationHandler.addRetrogenChunk(dim, new RetroChunkCoord(cCoord, list));
		}
	}

	@SubscribeEvent
	public void handleChunkSaveEvent(ChunkDataEvent.Save event) {

		CompoundNBT genTag = event.getData().getCompound(CoFHWorld.MOD_ID);

		if (event.getChunk().getStatus().getType() == Type.PROTOCHUNK) {
			genTag.putBoolean("Populating", true);
			return;
		}
		if (WorldProps.enableFlatBedrock) {
			genTag.putBoolean("Bedrock", true);
		}
		ListNBT featureList = new ListNBT();
		for (int i = 0; i < features.size(); i++) {
			featureList.add(StringNBT.valueOf(features.get(i).getFeatureName()));
		}
		genTag.put("List", featureList);
		genTag.putLong("Hash", genHash);

		event.getData().put(CoFHWorld.MOD_ID, genTag);
	}

	@SubscribeEvent
	public void handleSaplingGrowTreeEvent(SaplingGrowTreeEvent event) {

		if (WorldProps.chanceTreeGrowth.get() >= 100) {
			return;
		}
		if (event.getWorld().getRandom().nextInt(100) >= WorldProps.chanceTreeGrowth.get()) {
			event.setResult(Result.DENY);
		}
	}

	public static void generate(WorldGenRegion region) {

		INSTANCE.generateWorld(region.getMainChunkX(), region.getMainChunkZ(), region, true);
	}

	/* HELPER FUNCTIONS */
	public void generateWorld(int chunkX, int chunkZ, IWorld world, boolean newGen) {

		replaceBedrock(chunkX, chunkZ, world, newGen);

		if (!newGen & !WorldProps.enableRetroactiveGeneration) {
			return;
		}
		SharedSeedRandom random = new SharedSeedRandom();
		long decorationSeed = random.setDecorationSeed(world.getSeed(), chunkX * 16, chunkZ * 16);
		try {
			++GENERATING;
			for (IFeatureGenerator feature : features) {
				//FallingBlock.fallInstantly = true;
				feature.generateFeature(random, chunkX, chunkZ, world, newGen);
			}
		} finally {
			--GENERATING;
		}
		//FallingBlock.fallInstantly = false;
	}

	public void generateWorld(RetroChunkCoord chunk, World world, boolean newGen) {

		int chunkX = chunk.coord.chunkX, chunkZ = chunk.coord.chunkZ;
		if ((newGen | WorldProps.enableRetroactiveGeneration) & WorldProps.forceFullRegeneration) {
			generateWorld(chunkX, chunkZ, world, true);
			return;
		}

		replaceBedrock(chunkX, chunkZ, world, newGen | WorldProps.forceFullRegeneration);

		if (!newGen & !WorldProps.enableRetroactiveGeneration) {
			return;
		}
		SharedSeedRandom random = new SharedSeedRandom();
		long decorationSeed = random.setDecorationSeed(world.getSeed(), chunkX * 16, chunkZ * 16);
		Set<String> genned = chunk.generatedFeatures;
		try {
			++GENERATING;
			for (IFeatureGenerator feature : features) {
				if (genned.contains(feature.getFeatureName())) {
					continue;
				}
				//FallingBlock.fallInstantly = true;
				feature.generateFeature(random, chunkX, chunkZ, world, newGen | WorldProps.forceFullRegeneration);
			}
		} finally {
			--GENERATING;
		}
		//FallingBlock.fallInstantly = false;
		if (!newGen) {
			world.getChunk(chunkX, chunkZ).markDirty();
		}
	}

	public void replaceBedrock(int chunkX, int chunkZ, IWorld world, boolean newGen) {

		if (!WorldProps.enableFlatBedrock | !newGen & !WorldProps.enableRetroactiveFlatBedrock) {
			return;
		}
		final int offsetX = chunkX * 16 + 8;
		final int offsetZ = chunkZ * 16 + 8;
		final int maxBedrockLayers = WorldProps.maxBedrockLayers + 1, numBedrockLayers = WorldProps.numBedrockLayers;

		BlockState filler = Blocks.STONE.getDefaultState();
		int floor = 0, roof = 0; // arbitrary defaults, just in case the below fails (if your modded dimension's server-side ChunkProvider isn't a subclass; too bad?)
		{
			AbstractChunkProvider chunkProvider = world.getChunkProvider();
			if (chunkProvider instanceof ServerChunkProvider) {
				ServerChunkProvider serverChunkProvider = (ServerChunkProvider) chunkProvider;
				GenerationSettings settings = serverChunkProvider.getChunkGenerator().getSettings();
				// interestingly, WorldGenRegion actually stores the ChunkProvider settings, but there is no getter.
				// looks to me like a method is being erased by the obfuscator because it's not called, making this entire code block required.
				filler = settings.getDefaultBlock();
				floor = settings.getBedrockFloorHeight();
				roof = settings.getBedrockRoofHeight();
			}
		}
		final BlockMatcher bedrockMatcher = BlockMatcher.forBlock(Blocks.BEDROCK);
		final BlockState bedrock = Blocks.BEDROCK.getDefaultState();
		final BlockPos.Mutable pos = new BlockPos.Mutable();

		if (floor < 256) {
			for (int blockX = 0; blockX < 16; blockX++) {
				for (int blockZ = 0; blockZ < 16; blockZ++) {
					for (int blockY = maxBedrockLayers; blockY --> 0; ) {
						BlockState state = world.getBlockState(pos.setPos(offsetX + blockX, floor + blockY, offsetZ + blockZ));
						if (state.isReplaceableOreGen(world, pos.setPos(offsetX + blockX, floor + blockY, offsetZ + blockZ), bedrockMatcher)) {
							if (blockY >= numBedrockLayers) {
								world.setBlockState(pos.setPos(offsetX + blockX, floor + blockY, offsetZ + blockZ), filler, 2 | 16);
							}
						} else if (blockY < numBedrockLayers) {
							world.setBlockState(pos.setPos(offsetX + blockX, floor + blockY, offsetZ + blockZ), bedrock, 2 | 16);
						}
					}
				}
			}
		}

		if (roof > 0) {
			for (int blockX = 0; blockX < 16; blockX++) {
				for (int blockZ = 0; blockZ < 16; blockZ++) {
					for (int blockY = maxBedrockLayers; blockY --> 0; ) {
						BlockState state = world.getBlockState(pos.setPos(offsetX + blockX, roof - blockY, offsetZ + blockZ));
						if (state.isReplaceableOreGen(world, pos.setPos(offsetX + blockX, roof - blockY, offsetZ + blockZ), bedrockMatcher)) {
							if (blockY >= numBedrockLayers) {
								world.setBlockState(pos.setPos(offsetX + blockX, roof - blockY, offsetZ + blockZ), filler, 2 | 16);
							}
						} else if (blockY < numBedrockLayers) {
							world.setBlockState(pos.setPos(offsetX + blockX, roof - blockY, offsetZ + blockZ), bedrock, 2 | 16);
						}
					}
				}
			}
		}
	}

	/* CHUNK REFERENCE CLASS */
	private static class ChunkReference {

		public final Dimension dimension;
		public final int xPos;
		public final int zPos;

		public ChunkReference(Dimension dim, int x, int z) {

			dimension = dim;
			xPos = x;
			zPos = z;
		}

		@Override
		public int hashCode() {

			return xPos * 43 + zPos * 3 + dimension.getType().getId();
		}

		@Override
		public boolean equals(Object o) {

			if (o == null || o.getClass() != getClass()) {
				if (o instanceof Chunk) {
					Chunk other = (Chunk) o;
					return xPos == other.getPos().x && zPos == other.getPos().z && dimension == other.getWorld().getDimension();
				}
				return false;
			}
			ChunkReference other = (ChunkReference) o;
			return other.dimension == dimension && other.xPos == xPos && other.zPos == zPos;
		}

	}

}
