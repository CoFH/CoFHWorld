package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.block.*;
import cofh.cofhworld.util.LinkedHashList;
import cofh.cofhworld.util.Tuple;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import com.typesafe.config.*;
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

	public static List<WeightedBlock> parseBlockList(ConfigValue blockEntry) {

		List<WeightedBlock> list = new ArrayList<>();
		if (!parseBlockList(blockEntry, list))
			;

		return list;
	}


	public static boolean parseBlockList(ConfigValue blockEntry, List<WeightedBlock> list) {

		if (blockEntry == null) {
			return false;
		}

		boolean r = true;
		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedBlock entry = parseBlockEntry(blockList.get(i));
				if (entry == null) {
					r = false;
					continue;
				}
				list.add(entry);
			}
		} else {
			WeightedBlock entry = parseBlockEntry(blockEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return r;
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

	public static List<Material> parseMaterialList(ConfigValue materialEntry) {

		List<Material> list = new ArrayList<>();
		if (!parseMaterialList(materialEntry, list))
			;

		return list;
	}

	public static boolean parseMaterialList(ConfigValue blockEntry, List<Material> list) {

		if (blockEntry == null) {
			return false;
		}

		boolean r = true;
		if (blockEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) blockEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				Material entry = parseMaterialEntry(blockList.get(i));
				if (entry == null) {
					r = false; // parse all of them if we can
					continue;
				}
				if (entry != Material.ALWAYS) {
					list.add(entry); // empty arrays always pass
				}
			}
		} else {
			Material entry = parseMaterialEntry(blockEntry);
			if (entry == null) {
				return false;
			}
			if (entry != Material.ALWAYS) {
				list.add(entry); // empty arrays always pass
			}
		}
		return r;
	}

	@Nullable
	public static Material parseMaterialEntry(ConfigValue matEntry) {

		switch (matEntry.valueType()) {
			case BOOLEAN:
				// true would be "test always passes";
				// false would be "i don't want a test" since "test never passes" is functionally useless
			case NULL:
				return Material.ALWAYS;
			case OBJECT:
				Config matObject = ((ConfigObject) matEntry).toConfig();
				Material material = null;

				boolean inclusive = !matObject.hasPath("inclusive") || matObject.getBoolean("inclusive");

				Block block = null;
				if (matObject.hasPath("name")) {
					block = parseBlock(matObject.getString("name"));
					if (block == null) {
						log.error("Invalid block name on line {}!", matObject.getValue("name").origin().lineNumber());
						return null;
					}
					material = new BlockMaterial(block, inclusive);
				} else if (matObject.hasPath("tag")) { // exclusive with name
					List<WeightedString> tags = new LinkedList<>();
					if (StringData.parseStringList(matObject.getValue("tag"), tags)) {
						material = TagMaterial.of(tags.stream().map(str -> new ResourceLocation(str.value)).collect(Collectors.toList()), inclusive);
					} else {
						return null;
					}
				} else if (matObject.hasPath("fluid")) { // exclusive with name
					List<WeightedString> tags = new LinkedList<>();
					if (StringData.parseStringList(matObject.getValue("tag"), tags)) {
						material = new FluidMaterial(inclusive, tags.stream().map(str -> str.value).distinct().toArray(String[]::new));
					} else {
						return null;
					}
				}
				if (matObject.hasPath("material-type")) {
					List<WeightedString> tags = new LinkedList<>();
					if (StringData.parseStringList(matObject.getValue("material-type"), tags)) {
						Material t = new MaterialPropertyMaterial(inclusive, tags.stream().map(v -> v.value).toArray(String[]::new));
						material = material != null ? inclusive ? material.and(t) : material.or(t) : t;
					} else {
						return null;
					}
				}
				properties: if (matObject.hasPath("properties")) {
					Material propertyMaterial;
					if (block == null) {
						List<Tuple<String, String>> properties = new LinkedHashList<>();
						for (Map.Entry<String, ConfigValue> propEntry : matObject.getObject("properties").entrySet()) {

							if (propEntry.getValue().valueType() != ConfigValueType.STRING && propEntry.getValue().valueType() != ConfigValueType.NULL) {
								log.error("Property '{}' is not a string. All block properties must be strings.", propEntry.getKey());
								properties = null;
							} else if (properties != null) {
								properties.add(new Tuple<>(propEntry.getKey(), (String) propEntry.getValue().unwrapped()));
							}
						}
						if (properties == null) {
							return null;
						}
						if (properties.size() == 0) {
							log.debug("`properties` tag had no valid entries, ignoring.");
							break properties;
						}
						propertyMaterial = PropertyMaterial.of(properties, inclusive);
					} else {
						StateContainer<Block, BlockState> blockstatecontainer = block.getStateContainer();
						List<Tuple<IProperty<?>, ?>> properties = new LinkedHashList<>();
						for (Map.Entry<String, ConfigValue> propEntry : matObject.getObject("properties").entrySet()) {

							IProperty<?> prop = blockstatecontainer.getProperty(propEntry.getKey());
							if (prop == null) {
								log.warn("Block '{}' does not have property '{}'.", block.getRegistryName(), propEntry.getKey());
								properties = null;
							}
							if (propEntry.getValue().valueType() != ConfigValueType.STRING && propEntry.getValue().valueType() != ConfigValueType.NULL) {
								log.error("Property '{}' is not a string. All block properties must be strings.", propEntry.getKey());
								prop = null;
								properties = null;
							}

							if (prop != null) {
								if (propEntry.getValue().valueType() == ConfigValueType.NULL) {
									if (properties != null)
										properties.add(new Tuple<>(prop, null));
								} else {
									Optional<?> value = parseValue(prop, (String) propEntry.getValue().unwrapped());
									if (properties != null && value.isPresent()) {
										properties.add(new Tuple<>(prop, value.get()));
									} else {
										properties = null;
									}
								}
							}
						}
						if (properties == null) {
							return null;
						}
						if (properties.size() == 0) {
							log.debug("`properties` tag had no valid entries, ignoring.");
							break properties;
						}
						propertyMaterial = PropertyMaterial.of(block, properties, inclusive);
					}
					if (propertyMaterial != null) {
						material = material != null ? inclusive ? material.and(propertyMaterial) : material.or(propertyMaterial) : propertyMaterial;
					}
				}
				if (material == null) {
					log.error("Material entry must have property `tag`, `name`, `fluid`, `material-type`, or `properties`!");
				}
				return material;
			case STRING:
				String blockName = (String) matEntry.unwrapped();
				if (blockName.charAt(0) == '#') {
					return TagMaterial.of(Collections.singleton(new ResourceLocation(blockName.substring(1))), true);
				} else {
					block = parseBlock(blockName);
					if (block == null) {
						log.error("Invalid block name on line {}!", matEntry.origin().lineNumber());
						return null;
					}
					return new BlockMaterial(block, true);
				}
			default:
				log.error("Invalid type for material entry on line {}!", matEntry.origin().lineNumber());
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
