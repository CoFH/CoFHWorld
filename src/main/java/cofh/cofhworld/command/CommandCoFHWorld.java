package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.world.IFeatureGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SUpdateLightPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

public class CommandCoFHWorld {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {

		dispatcher.register(
				LiteralArgumentBuilder.<CommandSource>literal("cofhworld")
						.then(SubCommandVersion.register())
						.then(SubCommandList.register())
						.then(SubCommandReload.register())
						.then(SubCommandCountBlocks.register())
						.then(SubCommandReplaceBlocks.register())
		);
	}

	private static class SubCommandVersion {

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("version")
					.executes(SubCommandVersion::execute);
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			context.getSource().sendFeedback(new StringTextComponent(
					String.valueOf(ModList.get().getModFileById(CoFHWorld.MOD_ID).getFile().getSubstitutionMap().get().getOrDefault("jarVersion", "DEV"))
			), true);
			return 1;
		}
	}

	// Command to reload all feature definitions
	private static class SubCommandReload {

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("reload")
					.requires(source -> source.hasPermissionLevel(4))
					.executes(SubCommandReload::execute);
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			String key;
			int rtn = 0;
			if (WorldHandler.reloadConfig()) {
				key = "cofhworld.reload.successful";
				rtn = 1;
			} else {
				key = "cofhworld.reload.failed";
			}

			context.getSource().sendFeedback(new TranslationTextComponent(key), true);
			return rtn;
		}
	}

	private static class BlockFilters {

		private enum FilterTypes {
			ALL,
			BLOCK_ID,
			MOD_ID,
			TAG
		}

		private static class BlockFilter {

			public String Filter;
			public boolean Inverted = false;
			public FilterTypes BlockFilterType;

			public BlockFilter(String input) {
				int pos = 0;
				if (input.charAt(pos) == '!') {
					Inverted = true;
					++pos;
				}

				switch (input.charAt(pos)) {
					case '*':
						BlockFilterType = FilterTypes.ALL;
						break;
					case '@':
						BlockFilterType = FilterTypes.MOD_ID;
						++pos;
						break;
					case '#':
						BlockFilterType = FilterTypes.TAG;
						++pos;
						break;
					default:
						BlockFilterType = FilterTypes.BLOCK_ID;
						break;
				}

				Filter = input.substring(pos);
			}
		}

		private final ArrayList<BlockFilter> includeFilters = new ArrayList<>();
		private final ArrayList<BlockFilter> excludeFilters = new ArrayList<>();

		public BlockFilters(String filterRaw) {

			for (String s : filterRaw.toLowerCase().split(",")) {
				BlockFilter bf = new BlockFilter(s);
				if (bf.Inverted) {
					excludeFilters.add(bf);
				} else {
					includeFilters.add(bf);
				}
			}
		}

		public boolean isFilterMatch(BlockState testBlockState) {

			// Fast path return if we have already encountered this block type.
			Block testBlock = testBlockState.getBlock();

			// First we check to see if the block matches any of the include filters.
			boolean match = false;
			for (BlockFilter f : includeFilters) {
				switch (f.BlockFilterType) {
					case ALL:
						match = true;
						break;
					case MOD_ID:
						// TODO: is there a cleaner way of doing this?
						String idFilter = f.Filter + ":";
						match = testBlockState.toString().toLowerCase().contains(idFilter);
						break;
					case TAG:
						ResourceLocation tag = ResourceLocation.tryCreate(f.Filter);
						if (tag != null) {
							match = testBlock.getTags().contains(tag);
						}
						break;
					case BLOCK_ID:
						Block filterBlock = null;
						ResourceLocation rl = ResourceLocation.tryCreate(f.Filter);
						if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
							filterBlock = ForgeRegistries.BLOCKS.getValue(rl);
						}

						if (filterBlock == null) {
							break;
						}

						BlockMatcher bm = BlockMatcher.forBlock(filterBlock);
						match = bm.test(testBlockState);
						break;
				}

				if (match) {
					break;
				}
			}

			// Next we check to ensure that the block does -not- match any of the exclude filters.
			for (BlockFilter f : excludeFilters) {
				switch (f.BlockFilterType) {
					case ALL:
						match = false;
						break;
					case MOD_ID:
						// TODO: is there a cleaner way of doing this?
						String idFilter = f.Filter + ":";
						match &= !testBlockState.toString().toLowerCase().contains(idFilter);
						break;
					case TAG:
						ResourceLocation tag = ResourceLocation.tryCreate(f.Filter);
						if (tag != null) {
							match &= !testBlock.getTags().contains(tag);
						}
						break;
					case BLOCK_ID:
						Block filterBlock = null;
						ResourceLocation rl = ResourceLocation.tryCreate(f.Filter);
						if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
							filterBlock = ForgeRegistries.BLOCKS.getValue(rl);
						}

						if (filterBlock == null) {
							break;
						}

						BlockMatcher bm = BlockMatcher.forBlock(filterBlock);
						match &= !bm.test(testBlockState);
						break;
				}
			}

			return match;
		}
	}

	private static class SubCommandCountBlocks {

		public static int permissionLevel = 3;

		private interface PlayerFunction {

			ServerPlayerEntity apply(CommandContext<CommandSource> t) throws CommandSyntaxException;
		}

		public static ArgumentBuilder<CommandSource, ?> register() {
			return Commands.literal("countblocks")
				.requires(source -> source.hasPermissionLevel(permissionLevel))
				// All default parameters.
				.then(gatherArguments(context -> context.getSource().asPlayer()))
				// Player centred, with radius applied to all directions.
				.then(Commands.argument("p", EntityArgument.player())
					.then(gatherArguments(context -> EntityArgument.getPlayer(context, "p"))));
		}

		public static ArgumentBuilder<CommandSource, ?> gatherArguments(PlayerFunction playerFunc) {
			return
				// Radius to be applied to x, y and z.
				Commands.argument("r1", IntegerArgumentType.integer())
					.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*"))
					// r1 applied to x and z, r2 applied to y.
					.then(Commands.argument("r2", IntegerArgumentType.integer())
						.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*"))
						// r1 applied to x, r2 applied to y and r3 applied to z.
						.then(Commands.argument("r3", IntegerArgumentType.integer())
							.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*"))
							// Radius defined for all sized, plus with type filter.
							.then(Commands.argument("filter", StringArgumentType.string())
								.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter")))
							)));

		}

		public static int executeWithPlayer(CommandContext<CommandSource> context, ServerPlayerEntity player, int xRadius, int yRadius, int zRadius, String filter) {
			BlockPos p = player.getPosition();

			int sX = p.getX() - xRadius;
			int sY = p.getY() - yRadius;
			int sZ = p.getZ() - zRadius;
			int eX = p.getX() + xRadius;
			int eY = p.getY() + yRadius;
			int eZ = p.getZ() + zRadius;

			return execute(context, sX, sY, sZ, eX, eY, eZ, filter);
		}

		public static int execute(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {

			String key;
			int rtn = countBlocks(context, sX, sY, sZ, eX, eY, eZ, filters);
			if (rtn == 1) {
				key = "cofhworld.countblocks.successful";
			} else {
				key = "cofhworld.countblocks.failed";
			}

			context.getSource().sendFeedback(new TranslationTextComponent(key), true);
			return rtn;
		}

		private static int countBlocks(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {
			Entity entity = context.getSource().getEntity();
			if (entity == null) {
				return 0;
			}

			World world = entity.getEntityWorld();
			if (world.isRemote) {
				return 0;
			}

			int maxY = world.getHeight();

			// Clamp y values to the world world height limits.
			// Do not use 256 here as it is variable in 1.17+
			if (sY < 0) {
				sY = 0;
			} else if (sY > maxY) {
				sY = maxY;
			}

			if (eY < 0) {
				eY = 0;
			} else if (sY > maxY) {
				eY = maxY;
			}

			String dbg = ""
					+ "start: (" + sX + "," + sY + "," + sZ + ") "
					+ "end: (" + eX + "," + eY + "," + eZ + ")";
			CoFHWorld.log.debug(dbg);

			BlockFilters blockFilters = new BlockFilters(filters);
			HashMap<BlockState, Integer> blockCounts = new HashMap<>();

			long totalBlocks = 0;

			for (int x = sX; x <= eX; x++) {
				for (int z = sZ; z <= eZ; z++) {
					for (int y = sY; y <= eY; y++) {
						BlockPos pos = new BlockPos(x, y, z);
						BlockState bState = world.getBlockState(pos);

						// Skip any air blocks that might be present.
						if (bState == Blocks.AIR.getDefaultState() ||
								bState == Blocks.CAVE_AIR.getDefaultState() ||
								bState == Blocks.VOID_AIR.getDefaultState()) {
							continue;
						}

						BlockState defaultState = bState.getBlock().getDefaultState();
						if (blockFilters.isFilterMatch(defaultState)) {
							int ofTypeCount = 1;
							if (blockCounts.containsKey(defaultState)) {
								ofTypeCount = blockCounts.get(defaultState) + 1;
							}

							blockCounts.put(defaultState, ofTypeCount);
						}

						++totalBlocks;
					}
				}
			}

			long totalMatchingBlocks = 0;
			for (Map.Entry<BlockState, Integer> entry : blockCounts.entrySet()) {
				Block b = entry.getKey().getBlock();
				int count = entry.getValue();

				CoFHWorld.log.debug(b.toString() + " " + count);

				totalMatchingBlocks += count;
			}

			CoFHWorld.log.debug("Total blocks scanned: " + totalBlocks);
			CoFHWorld.log.debug("Total blocks matched: " + totalMatchingBlocks);

			return 1;
		}
	}

	private static class SubCommandReplaceBlocks {

		public static int permissionLevel = 3;

		private interface PlayerFunction {

			ServerPlayerEntity apply(CommandContext<CommandSource> t) throws CommandSyntaxException;
		}

		public static ArgumentBuilder<CommandSource, ?> register() {
			return Commands.literal("replaceblocks")
					.requires(source -> source.hasPermissionLevel(permissionLevel))
					// All default parameters.
					.then(gatherArguments(context -> context.getSource().asPlayer()))
					// Player centred, with radius applied to all directions.
					.then(Commands.argument("p", EntityArgument.player())
							.then(gatherArguments(context -> EntityArgument.getPlayer(context, "p"))));
		}

		public static ArgumentBuilder<CommandSource, ?> gatherArguments(PlayerFunction playerFunc) {
			BlockStateInput air = new BlockStateInput(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
			return
				// r1 to be applied to x, y and z.
				Commands.argument("r1", IntegerArgumentType.integer())
					.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*", air))
					// r1 applied to x and z, r2 applied to y.
					.then(Commands.argument("r2", IntegerArgumentType.integer())
						.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*", air))
						// r1 applied to x, r2 applied to y and r3 applied to z.
						.then(Commands.argument("r3", IntegerArgumentType.integer())
							.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*", air))
							// Radius defined for all sized, plus with type filter.
							.then(Commands.argument("filter", StringArgumentType.string())
								.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"), air))
								// Radius defined for all sized, plus type filter, plus replacement block ID.
								.then(Commands.argument("block", BlockStateArgument.blockState())
									.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"), BlockStateArgument.getBlockState(context, "block"))
								)))));

		}

		public static int executeWithPlayer(CommandContext<CommandSource> context, ServerPlayerEntity player, int xRadius, int yRadius, int zRadius, String filter, BlockStateInput replacement) {
			BlockPos p = player.getPosition();

			int sX = p.getX() - xRadius;
			int sY = p.getY() - yRadius;
			int sZ = p.getZ() - zRadius;
			int eX = p.getX() + xRadius;
			int eY = p.getY() + yRadius;
			int eZ = p.getZ() + zRadius;

			return execute(context, sX, sY, sZ, eX, eY, eZ, filter, replacement);
		}

		public static int execute(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockStateInput replacement) {

			String key;
			int rtn = replaceBlocks(context, sX, sY, sZ, eX, eY, eZ, filters, replacement.getState());
			if (rtn == 1) {
				key = "cofhworld.replaceblocks.successful";
			} else {
				key = "cofhworld.replaceblocks.failed";
			}

			context.getSource().sendFeedback(new TranslationTextComponent(key), true);
			return rtn;
		}

		private static int replaceBlocks(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockState replacement) {
			Entity entity = context.getSource().getEntity();
			if (entity == null) {
				return 0;
			}

			World world = entity.getEntityWorld();
			if (world.isRemote) {
				return 0;
			}

			int maxY = world.getHeight();

			// Clamp y values to the world world height limits.
			// Do not use 256 here as it is variable in 1.17+
			if (sY < 0) {
				sY = 0;
			} else if (sY > maxY) {
				sY = maxY;
			}

			if (eY < 0) {
				eY = 0;
			} else if (sY > maxY) {
				eY = maxY;
			}

			String dbg = ""
					+ "start: (" + sX  + "," + sY + "," + sZ + ") "
					+ "end: (" + eX + "," + eY + "," + eZ + ")";
			CoFHWorld.log.debug(dbg);

			BlockFilters blockFilters = new BlockFilters(filters);
			HashSet<ChunkPos> updatedChunks = new HashSet<>();

			long totalBlocks = 0;
			long totalBlocksReplaced = 0;

			for (int x = sX; x <= eX; x++) {
				for (int z = sZ; z <= eZ; z++) {
					Chunk chunk = world.getChunkAt(new BlockPos(x, 0, z));
					for (int y = sY; y <= eY; y++) {
						BlockPos pos = new BlockPos(x, y, z);
						BlockState bState = world.getBlockState(pos);

						BlockState defaultState = bState.getBlock().getDefaultState();
						if (blockFilters.isFilterMatch(defaultState)) {
							if (chunk.setBlockState(pos, replacement, false) != null) {
								// We will need to notify the client(s) that the chunks have been updated later.
								ChunkPos cp = chunk.getPos();
								updatedChunks.add(cp);

								++totalBlocksReplaced;
							}
						}

						++totalBlocks;
					}
				}
			}

			// TODO: no idea how to do this in 1.16.
			// Notify the client(s) that the chunks have been updated.
			if (world instanceof ServerWorld) {
				ServerWorld sw = (ServerWorld)world;
				ChunkManager cm = sw.getChunkProvider().chunkManager;
				for (ChunkPos cp : updatedChunks) {
					Chunk c = sw.getChunk(cp.x, cp.z);
					cm.getTrackingPlayers(cp, false).forEach((player) -> sendChunkData(sw, player, c));
				}
			}

			CoFHWorld.log.debug("Total blocks scanned: " + totalBlocks);
			CoFHWorld.log.debug("Total blocks replaced: " + totalBlocksReplaced);

			return 1;
		}

		private static void sendChunkData(ServerWorld world, ServerPlayerEntity player, Chunk chunkIn) {
			IPacket<?>[] ipacket = new IPacket[2];
			ipacket[0] = new SChunkDataPacket(chunkIn, 65535);
			ipacket[1] = new SUpdateLightPacket(chunkIn.getPos(), world.getLightManager(), true);

			player.sendChunkLoad(chunkIn.getPos(), ipacket[0], ipacket[1]);
		}
	}

	// Command to list all feature definitions
	public static class SubCommandList {

		final private static int PAGE_SIZE = 8;

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("list")
					.requires(source -> source.hasPermissionLevel(1))
					.executes(SubCommandList::execute)
					.then(Commands.argument("page", IntegerArgumentType.integer(1, Math.max(1, (WorldHandler.getFeatures().size() - 1) / PAGE_SIZE) + 1))
							.executes(SubCommandList::executeWithPage));
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			return execute(context, 0);
		}

		public static int executeWithPage(CommandContext<CommandSource> context) throws CommandException {

			return execute(context, IntegerArgumentType.getInteger(context, "page"));
		}

		public static int execute(CommandContext<CommandSource> context, int page) throws CommandException {

			List<IFeatureGenerator> generators = WorldHandler.getFeatures();
			int maxPages = (generators.size() - 1) / PAGE_SIZE;

			TextComponent component = new TranslationTextComponent("cofhworld.list", page + 1, maxPages + 1);
			component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.GOLD));
			context.getSource().sendFeedback(component, true);

			if (generators.size() == 0) {
				component = new StringTextComponent("! EMPTY");
				component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.DARK_RED)); // TODO: PROBABLY BROKEN
				context.getSource().sendFeedback(component, true);
				return 1;
			}

			StringBuilder b = new StringBuilder();
			int maxIndex = Math.min((page + 1) * PAGE_SIZE, generators.size());
			for (int i = page * PAGE_SIZE; i < maxIndex; ++i) {
				b.append("* ").append(generators.get(i).getFeatureName()).append('\n');
			}
			b.deleteCharAt(b.length() - 1);

			component = new StringTextComponent(b.toString());
			component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.DARK_BLUE));
			context.getSource().sendFeedback(component, true);
			return 1;
		}
	}

}
