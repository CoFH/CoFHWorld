package cofh.cofhworld.command;

import cofh.cofhworld.command.BlockPredicateListArgument.PredicateList;
import cofh.cofhworld.command.BlockPredicateListArgument.PredicateList.PLTuple;
import cofh.cofhworld.util.Tuple;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPredicateArgument;
import net.minecraft.command.arguments.BlockPredicateArgument.IResult;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BlockPredicateListArgument implements com.mojang.brigadier.arguments.ArgumentType<PredicateList> {

	private static final char MOD_ID = '@', TAG_ID = '#', SPECIAL_ID = '%', INVERT_ID = '!', WILDCARD_ID = '*';

	private static final Collection<String> EXAMPLES = Arrays.asList(
			"stone", "minecraft:stone", INVERT_ID + "stone", "stone[foo=prop]", INVERT_ID + "stone[foo=prop]", "stone{baz=nbt}",
			TAG_ID + "stone", INVERT_ID + "" + TAG_ID + "stone[foo=prop]{baz=nbt}",
			WILDCARD_ID + "", WILDCARD_ID + "[foo=prop]", INVERT_ID + "" + WILDCARD_ID + "[foo=prop]{baz=nbt}",
			MOD_ID + "minecraft", INVERT_ID + "" + MOD_ID + "minecraft",
			SPECIAL_ID + "stone", INVERT_ID + "" + SPECIAL_ID + "stone"
	);

	private static final SimpleCommandExceptionType MULTIPLE_INVERSION = new SimpleCommandExceptionType(
			new TranslationTextComponent("cofhworld.command.filter.inversion.invalid")
	);
	private static final SimpleCommandExceptionType NO_ID = new SimpleCommandExceptionType(
			new TranslationTextComponent("cofhworld.command.filter.id.absent")
	);
	private static final DynamicCommandExceptionType BAD_CASE = new DynamicCommandExceptionType(v ->
			new TranslationTextComponent("cofhworld.command.filter.special.invalid", v)
	);

	public static interface PredicateList {

		Tuple<List<IResult>, List<IResult>> getRaw();

		default PLTuple getPredicates(ITagCollectionSupplier s) throws CommandSyntaxException {

			List<Predicate<CachedBlockInfo>> whiteFilters = new ArrayList<>(getRaw().getA().size()), blackFilters = new ArrayList<>(getRaw().getB().size());
			for (IResult v : getRaw().getA())
				whiteFilters.add(v.create(s));
			for (IResult v : getRaw().getB())
				blackFilters.add(v.create(s));
			return new PLTuple(whiteFilters, blackFilters);
		}

		final public static Tuple EMPTY = new Tuple(Collections.emptyList(), Collections.emptyList());

		public static <T, V> Tuple<T, V> emptyList() {

			return EMPTY;
		}

		final static class PLTuple extends Tuple<List<Predicate<CachedBlockInfo>>, List<Predicate<CachedBlockInfo>>> {

			public PLTuple(List<Predicate<CachedBlockInfo>> whitePredicates, List<Predicate<CachedBlockInfo>> blackPredicates) {

				super(whitePredicates, blackPredicates);
			}
		}
	}

	private final BlockPredicateArgument predicate = BlockPredicateArgument.blockPredicate();

	public BlockPredicateListArgument() {

	}

	public static BlockPredicateListArgument blockPredicateList() {

		return new BlockPredicateListArgument();
	}

	public static Predicate<CachedBlockInfo> getBlockPredicateList(CommandContext<CommandSource> p_199825_0_, String p_199825_1_) throws CommandSyntaxException {

		final PLTuple filterStream = p_199825_0_.getArgument(p_199825_1_, PredicateList.class).getPredicates(
				p_199825_0_.getSource().getServer().func_244266_aF()
		);
		if (filterStream.getA().size() == 0) {
			switch (filterStream.getB().size()) {
				case 1:
					return filterStream.getB().get(0).negate();
				case 2:
					return filterStream.getB().get(0).or(filterStream.getB().get(1)).negate();
				default:
					return v -> filterStream.getB().stream().noneMatch(f -> f.test(v));
			}
		} else if (filterStream.getB().size() == 0) {
			switch (filterStream.getA().size()) {
				case 1:
					return filterStream.getA().get(0);
				case 2:
					return filterStream.getA().get(0).or(filterStream.getA().get(1));
				default:
					return v -> filterStream.getA().stream().anyMatch(f -> f.test(v));
			}
		} else if (filterStream.getA().size() == 1 && filterStream.getB().size() == 1) {
			return filterStream.getA().get(0).and(filterStream.getB().get(0).negate());
		} else {
			return v -> filterStream.getB().stream().noneMatch(f -> f.test(v)) && filterStream.getA().stream().anyMatch(f -> f.test(v));
		}
	}

	public PredicateList parse(StringReader reader) throws CommandSyntaxException {

		if (!reader.canRead())
			return PredicateList::emptyList;

		ArrayList<BlockPredicateArgument.IResult> white = new ArrayList<>(), black = new ArrayList<>(), scratch;
		int start = reader.getCursor();

		while (reader.getRemainingLength() > 0) {
			char next = reader.peek();
			if (StringReader.isAllowedNumber(next) || isBooleanNext(reader, next)) {
				if (reader.getCursor() != start)
					reader.setCursor(reader.getCursor() - 1);
				break;
			}

			boolean invert = next == INVERT_ID;
			scratch = white;
			if (invert) {
				scratch = black;
				reader.skip();
				if (!reader.canRead() || Character.isWhitespace(reader.peek()))
					throw NO_ID.createWithContext(reader);
				next = reader.peek();
				if (next == '!')
					throw MULTIPLE_INVERSION.createWithContext(reader);
			}

			if (next == SPECIAL_ID) {
				reader.skip();
				Predicate<CachedBlockInfo> val = getSpecialCase(reader);
				scratch.add(p -> val);
			} else if (next == MOD_ID) {
				reader.skip();
				if (!reader.canRead() || Character.isWhitespace(reader.peek()))
					throw NO_ID.createWithContext(reader);
				Predicate<CachedBlockInfo> val = new BlockModPredicate(reader.readString());
				scratch.add(p -> val);
			} else if (next == WILDCARD_ID) {
				reader.skip();
				Map<String, String> props = null;
				CompoundNBT nbt = null;
				if (reader.canRead() && reader.peek() == '[') {
					BlockStateParser blockstateparser = (new BlockStateParser(reader, true));
					blockstateparser.readStringProperties();
					props = blockstateparser.getStringProperties();
				}
				if (reader.canRead() && reader.peek() == '{') {
					nbt = (new JsonToNBT(reader)).readStruct();
				}
				if (nbt != null | props != null) {
					PropertiesPredicate val = new PropertiesPredicate(props, nbt);
					scratch.add(p -> val);
				} else {
					scratch.add(p -> v -> true);
				}
			} else {
				BlockPredicateArgument.IResult val = predicate.parse(reader);
				scratch.add(val);
			}
			reader.skipWhitespace();
		}

		Tuple<List<IResult>, List<IResult>> ret = new Tuple<>(white, black);
		return () -> ret;
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

		StringReader reader = new StringReader(builder.getInput());

		int lastSpace = builder.getInput().lastIndexOf(" ") + 1;
		if (lastSpace < builder.getStart())
			lastSpace = builder.getStart();
		reader.setCursor(lastSpace);

		if (!reader.canRead() || StringReader.isAllowedNumber(reader.peek()) || isBooleanNext(reader, reader.peek()))
			return Suggestions.empty();

		if (reader.peek() == INVERT_ID) {
			reader.skip();
			if (reader.canRead() && reader.peek() == INVERT_ID)
				return Suggestions.empty();
		}

		if (reader.canRead() && reader.peek() == SPECIAL_ID) {
			reader.skip();
			return ISuggestionProvider.suggest(Sets.newHashSet(
					"fire", "replaceable", "plants-nether", "plants-ocean", "plants", "tree", "fluid", "rock", "sand", "ground", "air"
			).stream(), builder.createOffset(reader.getCursor()));
		} else if (reader.canRead() && reader.peek() == MOD_ID) {
			reader.skip();
			return ISuggestionProvider.suggest(ModList.get().applyForEachModContainer(ModContainer::getModId), builder.createOffset(reader.getCursor()));
		} else if (reader.canRead() && reader.peek() == WILDCARD_ID) {
			System.currentTimeMillis();
			return Suggestions.empty();
		} else {
			BlockStateParser stateParser = new BlockStateParser(reader, true);

			try {
				stateParser.parse(true);
			} catch (CommandSyntaxException p) {
				// ignored
			}

			return stateParser.getSuggestions(builder, BlockTags.getCollection());
		}
	}

	private final static Collection<Material> GROUND_MATERIAL = Sets.newHashSet(
			Material.EARTH, Material.ORGANIC, Material.CLAY, Material.SAND, Material.SNOW, Material.ICE, Material.PACKED_ICE, Material.SNOW_BLOCK
	), PLANTS = Sets.newHashSet(
			Material.PLANTS, Material.TALL_PLANTS, Material.CACTUS, Material.LEAVES, Material.WOOD, Material.BAMBOO, Material.BAMBOO_SAPLING, Material.GOURD,
			Material.ORGANIC
	), OCEAN_PLANTS = Sets.newHashSet(
			Material.OCEAN_PLANT, Material.SEA_GRASS, Material.CORAL, Material.SPONGE
	), NETHER_PLANTS = Sets.newHashSet(
			Material.NETHER_PLANTS, Material.NETHER_WOOD
	);

	private static Predicate<CachedBlockInfo> getSpecialCase(StringReader reader) throws CommandSyntaxException {

		final int cursor = reader.getCursor();
		final String key = reader.readString().toLowerCase(Locale.ROOT);
		switch (key) {
			case "air":
				return b -> b.getBlockState().isAir(b.getWorld(), b.getPos());
			case "ground":
				return b -> GROUND_MATERIAL.contains(b.getBlockState().getMaterial());
			case "sand":
				return b -> b.getBlockState().getMaterial() == Material.SAND;
			case "stone":
			case "stony":
			case "rock":
				return b -> b.getBlockState().getMaterial() == Material.ROCK;
			case "fluid":
				return b -> !b.getBlockState().getFluidState().isEmpty();
			case "tree":
				return b -> b.getBlockState().isIn(BlockTags.LEAVES) || b.getBlockState().isIn(BlockTags.LOGS);
			case "plant":
			case "plants":
				return b -> PLANTS.contains(b.getBlockState().getMaterial());
			case "plant-ocean":
			case "plants-ocean":
				return b -> OCEAN_PLANTS.contains(b.getBlockState().getMaterial());
			case "plant-nether":
			case "plants-nether":
				return b -> NETHER_PLANTS.contains(b.getBlockState().getMaterial());
			case "repl":
			case "replace":
			case "replaceable":
			case "replacement":
				return b -> b.getBlockState().getMaterial().isReplaceable();
			case "fire":
				return b -> b.getBlockState().getMaterial() == Material.FIRE ||
						b.getBlockState().getMaterial() == Material.LAVA ||
						b.getBlockState().isBurning(b.getWorld(), b.getPos());
			default:
				reader.setCursor(cursor);
				throw BAD_CASE.createWithContext(reader, key);
		}
	}

	private static final char[] trueChars = "rue".toCharArray(), falseChars = "alse".toCharArray();

	private static boolean isBooleanNext(StringReader reader, char next) {

		int offset = 1;
		char str = '\0';
		if (reader.canRead(3) && (next == 't' || next == 'f' || (StringReader.isQuotedStringStart(str = next) && ((next = reader.peek(offset++)) == 't' || next == 'f')))) {
			int minLength = next == 't' ? 2 : 3, i = 0;
			char[] targetString = next == 't' ? trueChars : falseChars;
			for (next = reader.peek(offset); i < targetString.length && next == targetString[i]; next = reader.peek(++offset)) {
				++i;
				if (!reader.canRead(offset + 2)) // +2 and not +1 for note below
					// StringReader post-increments when doing read(), thus canRead() returns true on <= length() and then peek() crashes on length(). What an awful design.
					return i >= minLength;
			}
			return i == targetString.length && reader.peek(offset - 1) == targetString[i - 1] && (str == next || Character.isWhitespace(next));
		}
		return false;
	}

	public Collection<String> getExamples() {

		return EXAMPLES;
	}

	static class BlockModPredicate implements Predicate<CachedBlockInfo> {

		private final String mod;

		public BlockModPredicate(String modid) {

			this.mod = String.valueOf(modid).toLowerCase(Locale.ROOT);
		}

		public boolean test(CachedBlockInfo block) {

			ResourceLocation blockid = block.getBlockState().getBlock().getRegistryName();
			return blockid != null && mod.equals(blockid.getNamespace());
		}
	}

	static class PropertiesPredicate implements Predicate<CachedBlockInfo> {

		@Nullable
		private final CompoundNBT nbt;
		@Nullable
		private final Map<String, String> properties;

		private PropertiesPredicate(@Nullable Map<String, String> propertiesIn, @Nullable CompoundNBT nbtIn) {

			properties = propertiesIn;
			nbt = nbtIn;
		}

		public boolean test(CachedBlockInfo block) {

			BlockState blockstate = block.getBlockState();
			if (properties != null) {
				for (Entry<String, String> entry : properties.entrySet()) {
					Property<?> property = blockstate.getBlock().getStateContainer().getProperty(entry.getKey());
					if (property == null) {
						return false;
					}

					Comparable<?> comparable = property.parseValue(entry.getValue()).orElse(null);
					if (comparable == null) {
						return false;
					}

					if (blockstate.get(property) != comparable) {
						return false;
					}
				}
			}

			if (nbt == null) {
				return true;
			} else {
				TileEntity tileentity = block.getTileEntity();
				return tileentity != null && NBTUtil.areNBTEquals(nbt, tileentity.write(new CompoundNBT()), true);
			}
		}
	}
}
