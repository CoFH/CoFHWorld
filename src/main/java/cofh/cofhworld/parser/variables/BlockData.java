package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;
import java.util.Map;

import static cofh.cofhworld.CoFHWorld.log;

public class BlockData {

	public static boolean parseBlockList(ConfigValue blockEntry, List<WeightedRandomBlock> list, boolean wildcard) {

		if (blockEntry == null) {
			return false;
		}

		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedRandomBlock entry = parseBlockEntry(blockList.get(i), wildcard);
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else if (blockEntry.valueType() == ConfigValueType.NULL) {
			return true;
		} else {
			WeightedRandomBlock entry = parseBlockEntry(blockEntry, wildcard);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static WeightedRandomBlock parseBlockEntry(ConfigValue blockEntry, boolean wildcard) {

		final int min = wildcard ? 0 : -1;
		Block block;
		switch (blockEntry.valueType()) {
			case NULL:
				log.warn("Null Block entry on line {}!", blockEntry.origin().lineNumber());
				return null;
			case OBJECT:
				Config blockObject = ((ConfigObject) blockEntry).toConfig();
				if (!blockObject.hasPath("name")) {
					log.error("Block entry needs a name!");
					return null;
				}
				String blockName;
				block = parseBlock(blockName = blockObject.getString("name"));
				if (block == null) {
					log.error("Invalid block name on line {}!", blockObject.getValue("name").origin().lineNumber());
					return null;
				}
				int weight = blockObject.hasPath("weight") ? MathHelper.clamp(blockObject.getInt("weight"), 1, 1000000) : 100;
				if (blockObject.hasPath("properties")) {
					BlockStateContainer blockstatecontainer = block.getBlockState();
					IBlockState state = block.getDefaultState();
					for (Map.Entry<String, ConfigValue> propEntry : blockObject.getObject("properties").entrySet()) {

						IProperty<?> prop = blockstatecontainer.getProperty(propEntry.getKey());
						if (prop == null) {
							log.warn("Block '{}' does not have property '{}'.", blockName, propEntry.getKey());
						}
						if (propEntry.getValue().valueType() != ConfigValueType.STRING) {
							log.error("Property '{}' is not a string. All block properties must be strings.", propEntry.getKey());
							prop = null;
						}

						if (prop != null) {
							state = setValue(state, prop, (String) propEntry.getValue().unwrapped());
						}
					}
					return new WeightedRandomBlock(state, weight);
				} else {
					ConfigValue data = null;
					if (blockObject.hasPath("data")) {
						data = blockObject.getValue("data");
					} else if (blockObject.hasPath("metadata")) {
						data = blockObject.getValue("metadata");
					}
					if (data != null) {
						log.warn("Using `metadata` (at line: {}) for blocks is deprecated, and will be removed in the future. Use `properties` instead.",
								data.origin().lineNumber());
						if (data.valueType() != ConfigValueType.NUMBER) {
							data = null; // silently consume the error. logic is deprecated anyway.
						}
					}
					int metadata = data != null ? MathHelper.clamp(((Number) data.unwrapped()).intValue(), min, 15) : min;
					return new WeightedRandomBlock(block, metadata, weight);
				}
			case STRING:
				block = parseBlock((String) blockEntry.unwrapped());
				if (block == null) {
					log.error("Invalid block name on line {}!", blockEntry.origin().lineNumber());
					return null;
				}
				return new WeightedRandomBlock(block, min);
			default:
				log.error("Invalid type for block entry on line!", blockEntry.origin().lineNumber());
				return null;
		}
	}

	private static Block parseBlock(String blockName) {

		ResourceLocation loc = new ResourceLocation(blockName);
		if (ForgeRegistries.BLOCKS.containsKey(loc)) {
			return ForgeRegistries.BLOCKS.getValue(loc);
		}
		return null;
	}

	private static <T extends Comparable<T>> IBlockState setValue(IBlockState state, IProperty<T> prop, String val) {

		// FIXME: parseValue returns an Optional, must handle
		return state.withProperty(prop, prop.parseValue(val).get());
	}

}
