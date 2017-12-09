package cofh.cofhworld.init;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.feature.IFeatureGenerator;
import cofh.cofhworld.init.WorldTickHandler.RetroChunkCoord;
import cofh.cofhworld.util.ChunkCoord;
import cofh.cofhworld.util.LinkedHashList;
import gnu.trove.set.hash.THashSet;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

public class WorldHandler implements IWorldGenerator {

	public static final WorldHandler INSTANCE = new WorldHandler();

	private static List<IFeatureGenerator> features = new ArrayList<>();
	private static Set<String> featureNames = new THashSet<>();
	private static Set<EventType> standardGenEvents = new THashSet<>();
	private static LinkedHashList<ChunkReference> populatingChunks = new LinkedHashList<>();

	private static long genHash = 0;

	static {
		standardGenEvents.add(EventType.ANDESITE);
		standardGenEvents.add(EventType.COAL);
		standardGenEvents.add(EventType.DIAMOND);
		standardGenEvents.add(EventType.DIORITE);
		standardGenEvents.add(EventType.DIRT);
		standardGenEvents.add(EventType.EMERALD);
		standardGenEvents.add(EventType.GOLD);
		standardGenEvents.add(EventType.GRANITE);
		standardGenEvents.add(EventType.GRAVEL);
		standardGenEvents.add(EventType.IRON);
		standardGenEvents.add(EventType.LAPIS);
		standardGenEvents.add(EventType.REDSTONE);
		standardGenEvents.add(EventType.QUARTZ);

		standardGenEvents.add(EventType.SILVERFISH);
	}

	//TODO FIXME, Where is the propper place for this, Only used by FeatureParser
	private static Map<String, ModContainer> apis;

	public static Map<String, ModContainer> getLoadedAPIs() {

		if (apis == null) {
			apis = new HashMap<>();
			for (ModContainer m : ModAPIManager.INSTANCE.getAPIList()) {
				apis.put(m.getModId(), m);
			}
		}
		return apis;
	}

	public static void register() {

		GameRegistry.registerWorldGenerator(INSTANCE, 0);
		GameRegistry.registerWorldGenerator((random, chunkX, chunkZ, world, chunkGenerator, chunkProvider) -> populatingChunks.remove(new ChunkReference(world.provider.getDimension(), chunkX, chunkZ)), Integer.MAX_VALUE);

		MinecraftForge.EVENT_BUS.register(INSTANCE);
		MinecraftForge.ORE_GEN_BUS.register(INSTANCE);
	}

	public static void initialize() {

		if (WorldProps.enableFlatBedrock & WorldProps.enableRetroactiveFlatBedrock | WorldProps.enableRetroactiveGeneration) {
			// TODO: remove this condition when pregen works? (see handler for alternate)
			MinecraftForge.EVENT_BUS.register(WorldTickHandler.INSTANCE);
		}
	}

	private WorldHandler() {

	}

	public static boolean reloadConfig() {
		// Reset all features so that config will reload properly
		features.clear();
		featureNames.clear();

		// Parse all the generation files into features
		try {
			FeatureParser.parseGenerationFiles();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
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
	@SubscribeEvent
	public void handlePopulateChunkEvent(PopulateChunkEvent.Pre event) {

		populatingChunks.add(new ChunkReference(event.getWorld().provider.getDimension(), event.getChunkX(), event.getChunkZ()));
	}

	@SubscribeEvent
	public void populateChunkEvent(PopulateChunkEvent.Post event) {

		ChunkReference pos = populatingChunks.get(new ChunkReference(event.getWorld().provider.getDimension(), event.getChunkX(), event.getChunkZ()));

		if (pos != null) {
			pos.hasVillage = event.isHasVillageGenerated();
		}
	}

	@SubscribeEvent
	public void handleChunkLoadEvent(ChunkDataEvent.Load event) {

		int dim = event.getWorld().provider.getDimension();

		boolean regen = false;
		NBTTagCompound tag = (NBTTagCompound) event.getData().getTag(CoFHWorld.MOD_ID);

		if (tag != null && tag.getBoolean("Populating")) {
			ChunkReference chunk = new ChunkReference(dim, event.getChunk().x, event.getChunk().z);
			chunk.hasVillage = tag.getBoolean("HasVillage");
			populatingChunks.add(chunk);
			return;
		}

		NBTTagList list = null;
		ChunkCoord cCoord = new ChunkCoord(event.getChunk());

		if (tag != null) {
			boolean genFeatures = false;
			boolean bedrock = WorldProps.enableRetroactiveFlatBedrock & WorldProps.enableFlatBedrock && !tag.hasKey("Bedrock");
			if (WorldProps.enableRetroactiveGeneration) {
				genFeatures = tag.getLong("Hash") != genHash;
				if (tag.hasKey("List")) {
					list = tag.getTagList("List", Constants.NBT.TAG_STRING);
					genFeatures |= list.tagCount() != features.size();
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
			// TODO: this is threaded. should we synchronize?
			ArrayDeque<RetroChunkCoord> chunks = WorldTickHandler.chunksToGen.get(dim);

			if (chunks == null) {
				WorldTickHandler.chunksToGen.put(dim, new ArrayDeque<>(128));
				chunks = WorldTickHandler.chunksToGen.get(dim);
			}
			if (chunks != null) {
				chunks.addLast(new RetroChunkCoord(cCoord, list));
				WorldTickHandler.chunksToGen.put(dim, chunks);
			}
		}
	}

	@SubscribeEvent
	public void handleChunkSaveEvent(ChunkDataEvent.Save event) {

		NBTTagCompound genTag = event.getData().getCompoundTag(CoFHWorld.MOD_ID);

		ChunkReference chunk = populatingChunks.get(event.getChunk());
		if (chunk != null) {
			genTag.setBoolean("Populating", true);
			genTag.setBoolean("HasVillage", chunk.hasVillage);
			return;
		}
		if (WorldProps.enableFlatBedrock) {
			genTag.setBoolean("Bedrock", true);
		}
		NBTTagList featureList = new NBTTagList();
		for (int i = 0; i < features.size(); i++) {
			featureList.appendTag(new NBTTagString(features.get(i).getFeatureName()));
		}
		genTag.setTag("List", featureList);
		genTag.setLong("Hash", genHash);

		event.getData().setTag(CoFHWorld.MOD_ID, genTag);
	}

	@SubscribeEvent (priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void handleOreGenEvent(OreGenEvent.GenerateMinable event) {

		if (!WorldProps.disableStandardGeneration) {
			return;
		}
		if (standardGenEvents.contains(event.getType())) {
			event.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void handleSaplingGrowTreeEvent(SaplingGrowTreeEvent event) {

		if (WorldProps.chanceTreeGrowth >= 100) {
			return;
		}
		if (event.getWorld().rand.nextInt(100) >= WorldProps.chanceTreeGrowth) {
			event.setResult(Result.DENY);
		}
	}

	/* IWorldGenerator */
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

		generateWorld(random, chunkX, chunkZ, world, true);
	}

	/* HELPER FUNCTIONS */
	public void generateWorld(Random random, int chunkX, int chunkZ, World world, boolean newGen) {

		replaceBedrock(random, chunkX, chunkZ, world, newGen | WorldProps.forceFullRegeneration);

		if (!newGen & !WorldProps.enableRetroactiveGeneration) {
			return;
		}
		ChunkReference pos = new ChunkReference(world.provider.getDimension(), chunkX, chunkZ);
		pos = populatingChunks.get(pos);
		boolean hasVillage = pos == null ? false : pos.hasVillage;
		for (IFeatureGenerator feature : features) {
			BlockFalling.fallInstantly = true;
			feature.generateFeature(random, chunkX, chunkZ, world, hasVillage, newGen | WorldProps.forceFullRegeneration);
		}
		BlockFalling.fallInstantly = false;
		if (!newGen) {
			world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
		}
	}

	public void generateWorld(Random random, RetroChunkCoord chunk, World world, boolean newGen) {

		int chunkX = chunk.coord.chunkX, chunkZ = chunk.coord.chunkZ;
		if ((newGen | WorldProps.enableRetroactiveGeneration) & WorldProps.forceFullRegeneration) {
			generateWorld(random, chunkX, chunkZ, world, newGen);
			return;
		}

		replaceBedrock(random, chunkX, chunkZ, world, newGen | WorldProps.forceFullRegeneration);

		if (!newGen & !WorldProps.enableRetroactiveGeneration) {
			return;
		}
		THashSet<String> genned = chunk.generatedFeatures;
		ChunkReference pos = new ChunkReference(world.provider.getDimension(), chunkX, chunkZ);
		pos = populatingChunks.get(pos);
		boolean hasVillage = pos == null ? false : pos.hasVillage;
		for (IFeatureGenerator feature : features) {
			if (genned.contains(feature.getFeatureName())) {
				continue;
			}
			BlockFalling.fallInstantly = true;
			feature.generateFeature(random, chunkX, chunkZ, world, hasVillage, newGen | WorldProps.forceFullRegeneration);
		}
		BlockFalling.fallInstantly = false;
		if (!newGen) {
			world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
		}
	}

	public void replaceBedrock(Random random, int chunkX, int chunkZ, World world, boolean newGen) {

		if (!WorldProps.enableFlatBedrock | !newGen & !WorldProps.enableRetroactiveFlatBedrock) {
			return;
		}
		int offsetX = chunkX * 16 + 8;
		int offsetZ = chunkZ * 16 + 8;

		/* Determine if this is a void age; halt if so. */
		boolean isVoidAge = !world.getBlockState(new BlockPos(offsetX, 0, offsetZ)).getBlock().isAssociatedBlock(Blocks.BEDROCK);
		isVoidAge |= !world.getBlockState(new BlockPos(offsetX + 4, 0, offsetZ + 4)).getBlock().isAssociatedBlock(Blocks.BEDROCK);
		isVoidAge |= !world.getBlockState(new BlockPos(offsetX + 8, 0, offsetZ + 8)).getBlock().isAssociatedBlock(Blocks.BEDROCK);
		isVoidAge |= !world.getBlockState(new BlockPos(offsetX + 12, 0, offsetZ + 12)).getBlock().isAssociatedBlock(Blocks.BEDROCK);

		if (isVoidAge) {
			return;
		}
		// TODO: pull out the ExtendedStorageArray and edit that directly. faster.
		IBlockState filler = world.getBiome(new BlockPos(offsetX, 0, offsetZ)).fillerBlock;
		// NOTE: filler block is dirt by default, the actual filler block for the biome is part of a method body
		int meta = 0; // no meta field for filler
		switch (world.provider.getDimension()) {
			case -1:
			/* This is a hack because Mojang coded the Nether wrong. Are you surprised? */
				filler = Blocks.NETHERRACK.getDefaultState();
				break;
			case 0:
			/*
			 * Due to above note, overworld gets replaced with stone. other dimensions are on their own for helping us with the filler block
			 */
				filler = Blocks.STONE.getDefaultState();
				break;
			case 1:
			/* This is a hack because Mojang coded The End wrong. Are you surprised? */
				filler = Blocks.END_STONE.getDefaultState();
				break;

		}
		for (int blockX = 0; blockX < 16; blockX++) {
			for (int blockZ = 0; blockZ < 16; blockZ++) {
				for (int blockY = 5; blockY > WorldProps.numBedrockLayers - 1; blockY--) {
					BlockPos pos = new BlockPos(offsetX + blockX, blockY, offsetZ + blockZ);
					IBlockState state = world.getBlockState(pos);
					if (state.getBlock().isAssociatedBlock(Blocks.BEDROCK)) {
						world.setBlockState(pos, filler, 2);
					}
				}
				for (int blockY = WorldProps.numBedrockLayers - 1; blockY > 0; blockY--) {
					BlockPos pos = new BlockPos(offsetX + blockX, blockY, offsetZ + blockZ);
					IBlockState state = world.getBlockState(pos);
					if (!state.getBlock().isAssociatedBlock(Blocks.BEDROCK)) {
						world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), 2);
					}
				}
			}
		}
		/* Flatten bedrock on the top as well */
		int worldHeight = world.getActualHeight();

		if (world.getBlockState(new BlockPos(offsetX, worldHeight - 1, offsetZ)).getBlock().isAssociatedBlock(Blocks.BEDROCK)) {
			for (int blockX = 0; blockX < 16; blockX++) {
				for (int blockZ = 0; blockZ < 16; blockZ++) {
					for (int blockY = worldHeight - 2; blockY > worldHeight - 6; blockY--) {
						BlockPos pos = new BlockPos(offsetX + blockX, blockY, offsetZ + blockZ);
						IBlockState state = world.getBlockState(pos);
						if (state.getBlock().isAssociatedBlock(Blocks.BEDROCK)) {
							world.setBlockState(pos, filler, 2);
						}
					}
					for (int blockY = worldHeight - WorldProps.numBedrockLayers; blockY < worldHeight - 1; blockY++) {
						BlockPos pos = new BlockPos(offsetX + blockX, blockY, offsetZ + blockZ);
						IBlockState state = world.getBlockState(pos);
						if (!state.getBlock().isAssociatedBlock(Blocks.BEDROCK)) {
							world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), 2);
						}
					}
				}
			}
		}
	}

	/* CHUNK REFERENCE CLASS */
	private static class ChunkReference {

		public final int dimension;
		public final int xPos;
		public final int zPos;
		public boolean hasVillage;

		public ChunkReference(int dim, int x, int z) {

			dimension = dim;
			xPos = x;
			zPos = z;
		}

		@Override
		public int hashCode() {

			return xPos * 43 + zPos * 3 + dimension;
		}

		@Override
		public boolean equals(Object o) {

			if (o == null || o.getClass() != getClass()) {
				if (o instanceof Chunk) {
					Chunk other = (Chunk) o;
					return xPos == other.x && zPos == other.z && dimension == other.getWorld().provider.getDimension();
				}
				return false;
			}
			ChunkReference other = (ChunkReference) o;
			return other.dimension == dimension && other.xPos == xPos && other.zPos == zPos;
		}

	}

}
