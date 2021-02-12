package cofh.cofhworld.command;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BlockFilters {

    private enum FilterTypes {
        ALL,
        BLOCK_ID,
        MOD_ID,
        TAG
    }

    private static class BlockFilter {

        public String filter;
        public boolean inverted = false;
        public FilterTypes blockFilterType;

        public BlockFilter(String input) {
            // This is a safeguard against stupidity.
            // In the event that an empty string (or an inverted filter containing a single '!')
            // then we will apply a filter that will never match anything.
            // Should anyone do this with replace blocks then it will ensure no blocks are replaced.
            if (input.length() == 0 || (input.length() == 1 && input.charAt(0) == '!')) {
                filter = "*";
                inverted = true;
                blockFilterType = FilterTypes.ALL;
                return;
            }

            int pos = 0;
            if (input.charAt(pos) == '!') {
                inverted = true;
                ++pos;
            }

            switch (input.charAt(pos)) {
                case '*':
                    blockFilterType = FilterTypes.ALL;
                    break;
                case '@':
                    blockFilterType = FilterTypes.MOD_ID;
                    ++pos;
                    break;
                case '#':
                    blockFilterType = FilterTypes.TAG;
                    ++pos;
                    break;
                default:
                    blockFilterType = FilterTypes.BLOCK_ID;
                    break;
            }

            filter = input.substring(pos);

            // If the filter type is based on the block ID and if no "mod ID" is specified
            // then we will assume that the filter is targeting a vanilla Minecraft block.
            if (blockFilterType == FilterTypes.BLOCK_ID && !filter.contains(":")) {
                filter = "minecraft:" + filter;
            }
        }
    }

    private final ArrayList<BlockFilter> includeFilters = new ArrayList<>();
    private final ArrayList<BlockFilter> excludeFilters = new ArrayList<>();

    public BlockFilters(String filterRaw) {

        for (String s : filterRaw.toLowerCase().split(",")) {
            BlockFilter bf = new BlockFilter(s);
            if (bf.inverted) {
                excludeFilters.add(bf);
            } else {
                includeFilters.add(bf);
            }
        }
    }

    public boolean isFilterMatch(BlockState blockState) {

        // Fast path return if we have already encountered this block type.
        Block testBlock = blockState.getBlock();

        // First we check to see if the block matches any of the include filters.
        boolean match = false;
        for (BlockFilter f : includeFilters) {
            switch (f.blockFilterType) {
                case ALL:
                    match = true;
                    break;
                case MOD_ID:
                    String idFilter = f.filter + ":";
                    match = blockState.toString().toLowerCase().contains(idFilter);
                    break;
                case TAG:
                    ResourceLocation tag = ResourceLocation.tryCreate(f.filter);
                    if (tag != null) {
                        match = testBlock.getTags().contains(tag);
                    }
                    break;
                case BLOCK_ID:
                    Block filterBlock = null;
                    ResourceLocation rl = ResourceLocation.tryCreate(f.filter);
                    if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
                        filterBlock = ForgeRegistries.BLOCKS.getValue(rl);
                    }

                    if (filterBlock == null) {
                        break;
                    }

                    BlockMatcher bm = BlockMatcher.forBlock(filterBlock);
                    match = bm.test(blockState);
                    break;
            }

            if (match) {
                break;
            }
        }

        // Next we check to ensure that the block does -not- match any of the exclude filters.
        for (BlockFilter f : excludeFilters) {
            switch (f.blockFilterType) {
                case ALL:
                    match = false;
                    break;
                case MOD_ID:
                    String idFilter = f.filter + ":";
                    match &= !blockState.toString().toLowerCase().contains(idFilter);
                    break;
                case TAG:
                    ResourceLocation tag = ResourceLocation.tryCreate(f.filter);
                    if (tag != null) {
                        match &= !testBlock.getTags().contains(tag);
                    }
                    break;
                case BLOCK_ID:
                    Block filterBlock = null;
                    ResourceLocation rl = ResourceLocation.tryCreate(f.filter);
                    if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
                        filterBlock = ForgeRegistries.BLOCKS.getValue(rl);
                    }

                    if (filterBlock == null) {
                        break;
                    }

                    BlockMatcher bm = BlockMatcher.forBlock(filterBlock);
                    match &= !bm.test(blockState);
                    break;
            }
        }

        return match;
    }
}
