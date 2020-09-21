package cofh.cofhworld.init;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.util.ChunkCoord;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public class ChunkGenerationHandler {

	private static final ChunkGenerationHandler INSTANCE = new ChunkGenerationHandler();

	final private static HashMap<Dimension, ArrayDeque<RetroChunkCoord>> chunksToGen = new HashMap<>();
	final private static HashMap<Dimension, ArrayDeque<ChunkCoord>> chunksToPreGen = new HashMap<>();

	public static void addRetrogenChunk(Dimension dimension, RetroChunkCoord chunk) {

		synchronized (chunksToGen) {
			if (chunksToGen.isEmpty() && chunksToPreGen.isEmpty()) {
				MinecraftForge.EVENT_BUS.register(INSTANCE);
			}
			ArrayDeque<RetroChunkCoord> chunks = chunksToGen.get(dimension);

			if (chunks == null) {
				chunks = new ArrayDeque<>(128);
				chunks.addLast(chunk);
				chunksToGen.put(dimension, chunks);
			} else {
				chunks.addLast(chunk);
			}
		}
	}

	public static void addPregenChunk(Dimension dimension, ChunkCoord chunk) {

		synchronized (chunksToPreGen) {
			if (chunksToGen.isEmpty() && chunksToPreGen.isEmpty()) {
				MinecraftForge.EVENT_BUS.register(INSTANCE);
			}
			ArrayDeque<ChunkCoord> chunks = chunksToPreGen.get(dimension);

			if (chunks == null) {
				chunks = new ArrayDeque<>(128);
				chunks.addLast(chunk);
				chunksToPreGen.put(dimension, chunks);
			} else {
				chunks.addLast(chunk);
			}
		}
	}

	private static byte pregenC, retroC;

	@SubscribeEvent
	public void tickEnd(ServerTickEvent event) {

		if (event.side != LogicalSide.SERVER) {
			return;
		}
		if (chunksToGen.isEmpty() && chunksToPreGen.isEmpty()) {
			MinecraftForge.EVENT_BUS.unregister(INSTANCE);
			return;
		}

		if (event.phase == Phase.START) {
			final HashSet<Dimension> toRemove = new HashSet<>();
			synchronized (chunksToGen) {
				chunksToGen.forEach((Dimension dim, ArrayDeque<RetroChunkCoord> chunks) -> {
					World world = dim.getWorld();

					if (chunks != null && chunks.size() > 0) {
						RetroChunkCoord r = chunks.pollFirst();
						ChunkCoord c = r.coord;
						if (retroC++ == 0 || chunks.size() < 3) {
							CoFHWorld.log.info("Retro-Generating " + c.toString() + ".");
						} else {
							CoFHWorld.log.debug("Retro-Generating " + c.toString() + ".");
						}
						retroC &= 63;
						long worldSeed = world.getSeed();
						Random rand = new Random(worldSeed);
						long xSeed = rand.nextLong() >> 2 + 1L;
						long zSeed = rand.nextLong() >> 2 + 1L;
						rand.setSeed(xSeed * c.chunkX + zSeed * c.chunkZ ^ worldSeed);
						WorldHandler.INSTANCE.generateWorld(rand, r, world, false);
					} else {
						toRemove.add(dim);
					}
				});
				for (Dimension dim : toRemove)
					chunksToGen.remove(dim);
			}
		} else {
			final HashSet<Dimension> toRemove = new HashSet<>();
			synchronized (chunksToPreGen) {
				chunksToPreGen.forEach((Dimension dim, ArrayDeque<ChunkCoord> chunks) -> {
					World world = dim.getWorld();

					if (chunks != null && chunks.size() > 0) {
						ChunkCoord c = chunks.pollFirst();
						if (pregenC++ == 0 || chunks.size() < 5) {
							CoFHWorld.log.info("Pre-Generating " + c.toString() + ".");
						} else {
							CoFHWorld.log.debug("Pre-Generating " + c.toString() + ".");
						}
						pregenC &= 63;
						long worldSeed = world.getSeed();
						Random rand = new Random(worldSeed);
						long xSeed = rand.nextLong() >> 2 + 1L;
						long zSeed = rand.nextLong() >> 2 + 1L;
						rand.setSeed(xSeed * c.chunkX + zSeed * c.chunkZ ^ worldSeed);
						WorldHandler.INSTANCE.generateWorld(rand, c.chunkX, c.chunkZ, world, true);
					} else {
						toRemove.add(dim);
					}
				});
				for (Dimension dim : toRemove)
					chunksToPreGen.remove(dim);
			}
		}
	}

	public static class RetroChunkCoord {

		private static final Set<String> emptySet = Collections.EMPTY_SET;
		public final ChunkCoord coord;
		public final Set<String> generatedFeatures;

		public RetroChunkCoord(ChunkCoord pos, ListNBT features) {

			coord = pos;
			if (features == null) {
				generatedFeatures = emptySet;
			} else {
				int i = 0, e = features.size();
				generatedFeatures = new HashSet<>(e);
				for (; i < e; ++i) {
					generatedFeatures.add(features.getString(i));
				}
			}
		}

		@Override
		public boolean equals(Object o) {

			if (o instanceof RetroChunkCoord) {
				return ((RetroChunkCoord) o).coord.equals(coord);
			} else if (o instanceof ChunkCoord) {
				return o.equals(coord);
			}
			return false;
		}

		@Override
		public int hashCode() {

			return coord.hashCode();
		}
	}

}
