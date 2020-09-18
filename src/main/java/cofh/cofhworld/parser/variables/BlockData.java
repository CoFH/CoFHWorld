package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedNBTTag;
import com.google.common.base.Optional;
import com.typesafe.config.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cofh.cofhworld.CoFHWorld.log;

public class BlockData {

	public static boolean parseBlockList(ConfigValue blockEntry, List<WeightedBlock> list, boolean wildcard) {

		if (blockEntry == null) {
			return false;
		}

		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedBlock entry = parseBlockEntry(blockList.get(i), wildcard);
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else if (blockEntry.valueType() == ConfigValueType.NULL) {
			return true;
		} else {
			WeightedBlock entry = parseBlockEntry(blockEntry, wildcard);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	@Nullable
	public static WeightedBlock parseBlockEntry(ConfigValue blockEntry, boolean wildcard) {

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
				List<WeightedNBTTag> dataTag = null;
				if (blockObject.hasPath("data-tag")) {
					dataTag = new ArrayList<>();
					if (!NBTData.parseNBTList(blockObject.getValue("data-tag"), dataTag)) {
						dataTag = null;
					}
				}
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
							if (state == null)
								return null;
						}
					}
					return new WeightedBlock(state, dataTag, weight);
				} else {
					ConfigValue data = null;
					if (blockObject.hasPath("data")) {
						data = blockObject.getValue("data");
					} else if (blockObject.hasPath("metadata")) {
						data = blockObject.getValue("metadata");
					}
					if (data != null) {
						log.warn("Using `metadata` (at line: {}) for blocks is deprecated, and will be removed in the future. Use `properties` instead.", data.origin().lineNumber());
						if (data.valueType() != ConfigValueType.NUMBER) {
							data = null; // silently consume the error. logic is deprecated anyway.
						}
					}
					int metadata = data != null ? MathHelper.clamp(((Number) data.unwrapped()).intValue(), min, 15) : min;
					return new WeightedBlock(block, metadata, dataTag, weight);
				}
			case STRING:
				block = parseBlock((String) blockEntry.unwrapped());
				if (block == null) {
					log.error("Invalid block name on line {}!", blockEntry.origin().lineNumber());
					return null;
				}
				return new WeightedBlock(block, min);
			default:
				log.error("Invalid type for block entry on line {}!", blockEntry.origin().lineNumber());
				return null;
		}
	}

	@Nullable
	private static Block parseBlock(String blockName) {

		ResourceLocation loc = new ResourceLocation(blockName);
		if (ForgeRegistries.BLOCKS.containsKey(loc)) {
			return ForgeRegistries.BLOCKS.getValue(loc);
		}
		return null;
	}

	@Nullable
	private static <T extends Comparable<T>> IBlockState setValue(IBlockState state, final IProperty<T> prop, String val) {

		Optional<T> value = prop.parseValue(val);
		if (!value.isPresent()) {
			String[] valid = prop.getAllowedValues().stream().map(prop::getName).distinct().toArray(String[]::new);
			// TODO: implement some edit distance algorithm and make suggestion for best match with minimum similarity
			log.error("Unknown value `{}` for property '{}'; allowed values are: \n{}", val, prop.getName(), Arrays.toString(valid));
			return null;
		} else {
			return state.withProperty(prop, value.get());
		}
	}

}
