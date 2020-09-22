package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.block.*;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import com.typesafe.config.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static cofh.cofhworld.CoFHWorld.log;

public class BlockData {

	public static boolean parseBlockList(ConfigValue blockEntry, List<WeightedBlock> list) {

		if (blockEntry == null) {
			return false;
		}

		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedBlock entry = parseBlockEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else if (blockEntry.valueType() == ConfigValueType.NULL) {
			return true;
		} else {
			WeightedBlock entry = parseBlockEntry(blockEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	@Nullable
	public static WeightedBlock parseBlockEntry(ConfigValue blockEntry) {

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
					StateContainer<Block, BlockState> blockstatecontainer = block.getStateContainer();
					BlockState state = block.getDefaultState();
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
					if (state == null) {
						return null;
					}
					return new WeightedBlock(weight, state, dataTag);
				} else {
					return new WeightedBlock(weight, block, dataTag);
				}
			case STRING:
				block = parseBlock((String) blockEntry.unwrapped());
				if (block == null) {
					log.error("Invalid block name on line {}!", blockEntry.origin().lineNumber());
					return null;
				}
				return new WeightedBlock(block);
			default:
				log.error("Invalid type for block entry on line {}!", blockEntry.origin().lineNumber());
				return null;
		}
	}

	public static boolean parseMaterialList(ConfigValue blockEntry, List<Material> list) {

		if (blockEntry == null) {
			return false;
		}

		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				Material entry = parseMaterialEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else if (blockEntry.valueType() == ConfigValueType.NULL) {
			return true;
		} else {
			Material entry = parseMaterialEntry(blockEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	@Nullable
	public static Material parseMaterialEntry(ConfigValue blockEntry) {

		switch (blockEntry.valueType()) {
			case NULL:
				log.warn("Null Block entry on line {}!", blockEntry.origin().lineNumber());
				return null;
			case OBJECT:
				Config blockObject = ((ConfigObject) blockEntry).toConfig();
				Material blockMaterial = null;

				boolean inclsuive = !blockObject.hasPath("inclusive") || blockObject.getBoolean("inclusive");

				Block block = null;
				if (blockObject.hasPath("name")) {
					block = parseBlock(blockObject.getString("name"));
					if (block == null) {
						log.error("Invalid block name on line {}!", blockObject.getValue("name").origin().lineNumber());
						return null;
					}
					blockMaterial = new BlockMaterial(block, inclsuive);
				} else if (blockObject.hasPath("tag")) { // exclusive with name
					List<WeightedString> tags = new LinkedList<>();
					if (StringData.parseStringList(blockObject.getValue("tag"), tags)) {
						blockMaterial = TagMaterial.of(tags.stream().map((str) -> new ResourceLocation(str.value)).collect(Collectors.toList()), inclsuive);
					} else {
						return null;
					}
				}
				if (blockObject.hasPath("properties")) {
					if (block == null) {
						Object2ObjectArrayMap<String, String> properties = new Object2ObjectArrayMap<>();
						for (Map.Entry<String, ConfigValue> propEntry : blockObject.getObject("properties").entrySet()) {

							if (propEntry.getValue().valueType() != ConfigValueType.STRING && propEntry.getValue().valueType() != ConfigValueType.NULL) {
								log.error("Property '{}' is not a string. All block properties must be strings.", propEntry.getKey());
							} else {
								properties.put(propEntry.getKey(), (String) propEntry.getValue().unwrapped());
							}
						}
						Material t = new PropertyMaterial(properties, inclsuive);
						blockMaterial = blockMaterial != null ? inclsuive ? blockMaterial.and(t) : blockMaterial.or(t) : t;
					} else {
						StateContainer<Block, BlockState> blockstatecontainer = block.getStateContainer();
						Object2ObjectArrayMap<IProperty<?>, Object> properties = new Object2ObjectArrayMap<>();
						for (Map.Entry<String, ConfigValue> propEntry : blockObject.getObject("properties").entrySet()) {

							IProperty<?> prop = blockstatecontainer.getProperty(propEntry.getKey());
							if (prop == null) {
								log.warn("Block '{}' does not have property '{}'.", block.getRegistryName(), propEntry.getKey());
							}
							if (propEntry.getValue().valueType() != ConfigValueType.STRING && propEntry.getValue().valueType() != ConfigValueType.NULL) {
								log.error("Property '{}' is not a string. All block properties must be strings.", propEntry.getKey());
								prop = null;
							}

							if (prop != null) {
								if (propEntry.getValue().valueType() == ConfigValueType.NULL) {
									if (properties != null)
										properties.put(prop, null);
								} else {
									Optional<?> value = parseValue(prop, (String) propEntry.getValue().unwrapped());
									if (properties != null && value.isPresent()) {
										properties.put(prop, value);
									} else {
										properties = null;
									}
								}
							}
						}
						if (properties == null) {
							return null;
						}
						blockMaterial = new BlockPropertyMaterial(block, properties,inclsuive);
					}
				}
				if (blockMaterial == null) {
					log.error("Block entry must have property `tag`, `name`, or `properties`!");
				}
				return blockMaterial;
			case STRING:
				String blockName = (String) blockEntry.unwrapped();
				if (blockName.charAt(0) == '#') {
					return TagMaterial.of(Collections.singleton(new ResourceLocation(blockName.substring(1))), true);
				} else {
					block = parseBlock(blockName);
					if (block == null) {
						log.error("Invalid block name on line {}!", blockEntry.origin().lineNumber());
						return null;
					}
					return new BlockMaterial(block, true);
				}
			default:
				log.error("Invalid type for block entry on line {}!", blockEntry.origin().lineNumber());
				return null;
		}
	}

	@Nullable
	private static Block parseBlock(String blockName) {

		ResourceLocation loc = new ResourceLocation(blockName);
		if (Registry.BLOCK.containsKey(loc)) {
			return Registry.BLOCK.getValue(loc).get();
		}
		return null;
	}

	private static <T extends Comparable<T>> Optional<T> parseValue(final IProperty<T> prop, String val) {

		Optional<T> value = prop.parseValue(val);
		if (!value.isPresent()) {
			String[] valid = prop.getAllowedValues().stream().map(prop::getName).distinct().toArray(String[]::new);
			// TODO: implement some edit distance algorithm and make suggestion for best match with minimum similarity
			log.error("Unknown value `{}` for property '{}'; allowed values are: \n{}", val, prop.getName(), Arrays.toString(valid));
		}
		return value;
	}

	@Nullable
	private static <T extends Comparable<T>> BlockState setValue(BlockState state, final IProperty<T> prop, String val) {

		return parseValue(prop, val).map(t -> state == null ? state : state.with(prop, t)).orElse(null);
	}

}
