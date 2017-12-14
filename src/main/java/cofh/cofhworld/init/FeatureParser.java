package cofh.cofhworld.init;

import cofh.cofhworld.biome.BiomeInfo;
import cofh.cofhworld.biome.BiomeInfoRarity;
import cofh.cofhworld.biome.BiomeInfoSet;
import cofh.cofhworld.feature.*;
import cofh.cofhworld.util.*;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import cofh.cofhworld.util.numbers.SkellamRandomProvider;
import cofh.cofhworld.util.numbers.UniformRandomProvider;
import cofh.cofhworld.world.generator.MultiGen;
import com.typesafe.config.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DungeonHooks.DungeonMob;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static cofh.cofhworld.CoFHWorld.log;

public class FeatureParser {

	private static HashMap<String, IDistributionParser> distributionParsers = new HashMap<>();
	private static HashMap<String, IGeneratorParser> generatorParsers = new HashMap<>();

	private FeatureParser() {

	}

	public static boolean registerDistribution(String name, IDistributionParser handler) {

		// TODO: provide this function through IFeatureHandler?
		if (!distributionParsers.containsKey(name)) {
			distributionParsers.put(name, handler);
			return true;
		}
		log.error("Attempted to register duplicate distribution '{}'!", name);
		return false;
	}

	public static boolean registerGenerator(String generator, IGeneratorParser handler) {

		// TODO: provide this function through IFeatureHandler?
		if (!generatorParsers.containsKey(generator)) {
			generatorParsers.put(generator, handler);
			return true;
		}
		log.error("Attempted to register duplicate generator '{}'!", generator);
		return false;
	}

	public static void addFiles(ArrayList<File> list, File folder) {

		final AtomicInteger dirs = new AtomicInteger(0);
		File[] fList = folder.listFiles((file, name) -> {

			if (name == null) {
				return false;
			} else if (new File(file, name).isDirectory()) {
				dirs.incrementAndGet();
				return true;
			}
			return name.toLowerCase(Locale.US).endsWith(".json");
		});

		Object o = folder == WorldProps.worldGenDir ? folder : WorldProps.worldGenPath.relativize(Paths.get(folder.getPath()));
		if (fList == null || fList.length <= 0) {
			log.debug("There are no World Generation files present in {}.", o);
			return;
		}
		int d = dirs.get();
		log.info("Found {} World Generation files and {} folders present in {}.", (fList.length - d), d, o);
		list.addAll(Arrays.asList(fList));
	}

	public static void parseGenerationFiles() {

		ArrayList<File> worldGenList = new ArrayList<>(5);
		{
			int i = 0;
			if (WorldProps.disableStandardGeneration) {
				worldGenList.add(WorldProps.standardGenFile); // prioritize this over all other files
				++i;
			}
			addFiles(worldGenList, WorldProps.worldGenDir);
			for (int e = worldGenList.size(); i < e; ++i) {
				File genFile = worldGenList.get(i);
				if (genFile.equals(WorldProps.standardGenFile)) {
					worldGenList.remove(i);
					break;
				}
			}
		}

		for (int i = 0; i < worldGenList.size(); ++i) {
			File genFile = worldGenList.get(i);

			if (genFile.isDirectory()) {
				worldGenList.remove(i--);
				addFiles(worldGenList, genFile);
			}
		}

		for (int i = 0, e = worldGenList.size(); i < e; ++i) {
			File genFile = worldGenList.get(i);
			String file = WorldProps.worldGenPath.relativize(Paths.get(genFile.getPath())).toString();

			Config genList;
			try {
				genList = ConfigFactory.parseFile(genFile, Includer.options).resolve(Includer.resolveOptions);
			} catch (Throwable t) {
				log.error("Critical error reading from a world generation file: \"{}\" > Please be sure the file is correct!", genFile, t);
				continue;
			}

			if (genList.hasPath("dependencies") && !processDependencies(genList.getValue("dependencies"))) {
				log.info("Unmet dependencies to load {}", file);
				continue;
			}

			if (genList.hasPath("populate")) {
				log.info("Reading world generation info from: {}:", file);
				Config genData = genList.getConfig("populate");
				for (Entry<String, ConfigValue> genEntry : genData.root().entrySet()) {
					String key = genEntry.getKey();
					try {
						if (genEntry.getValue().valueType() != ConfigValueType.OBJECT) {
							log.error("Error parsing generation entry: '{}' > This must be an object and is not.", key);
						} else {
							switch (parseFeature(key, genData.getConfig(key))) {
								case SUCCESS:
									log.debug("Generation entry successfully parsed: '{}'", key);
									break;
								case FAIL:
									log.error("Error parsing generation entry: '{}' > Please check the parameters.", key);
									break;
								case PASS:
									log.error("Error parsing generation entry: '{}' > It is a duplicate.", key);
							}
						}
					} catch (ConfigException ex) {
						String line = "";
						if (ex.origin() != null) {
							line = String.format(" on line %s", ex.origin().lineNumber());
						}
						log.error("Error parsing entry '{}'{}: {}", key, line, ex.getMessage());
						continue;
					} catch (Throwable t) {
						log.fatal("There was a severe error parsing '{}'!", key, t);
					}
				}
				log.info("Finished reading {}", file);
			} else {

			}
		}
	}

	public static boolean processDependencies(ConfigValue value) {

		if (value.valueType() == ConfigValueType.LIST) {
			ConfigList list = (ConfigList) value;
			boolean r = true;
			for (int i = 0, e = list.size(); i < e; ++i) {
				r &= processDependency(list.get(i));
			}
			return r;
		} else {
			return processDependency(value);
		}
	}

	public static boolean processDependency(ConfigValue value) {

		String id;
		ModContainer con;
		ArtifactVersion vers = null;
		boolean retComp = true;
		switch (value.valueType()) {
			case STRING:
				id = (String) value.unwrapped();
				if (id.contains("@")) {
					vers = VersionParser.parseVersionReference(id);
					id = vers.getLabel();
				}
				con = Loader.instance().getIndexedModList().get(id);
				break;
			case OBJECT:
				Config data = ((ConfigObject) value).toConfig();
				id = data.getString("id");
				con = Loader.instance().getIndexedModList().get(id);
				if (data.hasPath("version")) {
					vers = new DefaultArtifactVersion(id, data.getString("version"));
				}
				if (data.hasPath("exclude")) {
					retComp = !data.getBoolean("exclude");
				}
				break;
			default:
				log.fatal("Invalid dependency at line {}!", value.origin().lineNumber());
				return false;
		}
		if (con == null) {
			con = WorldHandler.getLoadedAPIs().get(id);
			if (con == null) {
				log.debug("Dependency '{}' is not loaded.", id);
				return false == retComp;
			}
		}
		LoaderState.ModState state = Loader.instance().getModState(con);
		if (state == LoaderState.ModState.DISABLED || state == LoaderState.ModState.ERRORED) {
			log.debug("Dependency '{}' is disabled or crashed.", id);
			return false == retComp;
		}
		if (vers != null) {
			if (retComp != vers.containsVersion(con.getProcessedVersion())) {
				log.debug("Dependency '{}' has an incompatible version.", id);
				return false;
			} else {
				return true;
			}
		}
		return true == retComp;
	}

	public static EnumActionResult parseFeature(String name, Config genObject) {

		// Setup the base Feature object
		Feature feature = new Feature(name, genObject);
		if (!feature.isEnabled()) {
			return EnumActionResult.SUCCESS;
		}

		// Try to parse the distribution and world generators; if either fails, we'll bail
		IDistribution distribution = parseDistribution(name, genObject);
		if (distribution == null) {
			log.warn("Failed to instantiate distribution for feature {}.", name);
			return EnumActionResult.FAIL;
		}

		IGenerator generator = parseGenerator(name, genObject, distribution.defaultGenerator(), distribution.defaultMaterials());
		if (generator == null) {
			log.warn("Failed to instantiate generator for feature {}.", name);
			return EnumActionResult.FAIL;
		}

		// Hook the distribution and generator into our Feature
		feature.setDistribution(distribution);
		feature.setGenerator(generator);

		// Register the finalized feature
		return WorldHandler.registerFeature(feature) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	public static IDistribution parseDistribution(String featureName, Config genObject) {

		String distName = genObject.getString("distribution");
		IDistributionParser distParser = distributionParsers.get(distName);
		if (distParser != null) {
			return distParser.parse(featureName, genObject, log);
		} else {
			log.warn("Unable to find distribution {} for feature {}.", distName, featureName);
			return null;
		}
	}

	public static IGenerator parseGenerator(String featureName, Config genObject, String defaultGenerator, List<WeightedRandomBlock> defaultMaterial) {

		if (!genObject.hasPath("generator")) {
			return null;
		}

		boolean midAir = genObject.hasPath("mid-air") && genObject.getBoolean("mid-air");
		if (midAir) {
			log.info("Enabling mid-air generation on {}", featureName);
		}

		List<WeightedRandomBlock> matList = new ArrayList<WeightedRandomBlock>(defaultMaterial);
		if (!FeatureParser.parseResList(genObject.root().get("material"), matList, false)) {
			if (WorldProps.verboseLogging) {
				log.warn("Parsing 'material' setting for on feature {} failed; using defaults: {}", featureName, defaultMaterial);
			}
			matList = defaultMaterial;
		}

		ConfigValue genData = genObject.root().get("generator");
		ConfigValueType genDataType = genData.valueType();

		if (genDataType == ConfigValueType.OBJECT) {
			// Single generator def
			return parseGeneratorData(defaultGenerator, midAir, genObject.getConfig("generator"), matList);

		} else if (genDataType == ConfigValueType.LIST) {
			// We have a weighted array of generators; walk the list and wrap with a MultiGen
			List<? extends Config> list = genObject.getConfigList("generator");
			ArrayList<WeightedRandomWorldGenerator> gens = new ArrayList<>(list.size());
			for (Config genElement : list) {
				IGenerator gen = parseGeneratorData(defaultGenerator, midAir, genElement, matList);
				int weight = genElement.hasPath("weight") ? genElement.getInt("weight") : 100;
				gens.add(new WeightedRandomWorldGenerator(gen, weight));
			}
			return new MultiGen(gens);

		} else {
			log.error("Invalid generator data type for {}; must be an object or list.", featureName);
			return null;
		}
	}

	public static IGenerator parseGeneratorData(String defaultGenerator, boolean midAir, Config genObject, List<WeightedRandomBlock> matList) {

		String name = defaultGenerator;
		if (genObject.hasPath("type")) {
			name = genObject.getString("type");
			if (!generatorParsers.containsKey(name)) {
				log.warn("Unknown generator '{}' using '{}'", name, defaultGenerator);
				name = defaultGenerator;
			}
		}

		List<WeightedRandomBlock> resList = new ArrayList<>();
		if (!FeatureParser.parseResList(genObject.getValue("block"), resList, true)) {
			return null;
		}

		IGeneratorParser parser = generatorParsers.get(name);
		if (parser == null) {
			throw new IllegalStateException("Generator '" + name + "' is not registered!");
		}

		// If mid-air generation is enabled, make sure to add Air to the list of valid materials
		// TODO: Verify that this works as expected with decoration & surface
		if (midAir) {
			matList.add(new WeightedRandomBlock(Blocks.AIR, -1));
		}

		return parser.parseGenerator(name, genObject, log, resList, matList);
	}

	public static BiomeInfoSet parseBiomeRestrictions(Config genObject) {

		BiomeInfoSet set;
		ConfigValue data = genObject.getValue("value");
		if (data.valueType() == ConfigValueType.LIST) {
			ConfigList restrictionList = (ConfigList) data;
			set = new BiomeInfoSet(restrictionList.size());
			for (int i = 0, e = restrictionList.size(); i < e; i++) {
				BiomeInfo info = parseBiomeData(restrictionList.get(i));
				if (info != null) {
					set.add(info);
				}
			}
		} else {
			set = new BiomeInfoSet(1);
			BiomeInfo info = parseBiomeData(data);
			if (info != null) {
				set.add(info);
			}
		}
		return set;
	}

	public static BiomeInfo parseBiomeData(ConfigValue element) {

		BiomeInfo info = null;
		switch (element.valueType()) {
			case NULL:
				log.info("Null biome entry. Ignoring.");
				break;
			case OBJECT:
				Config obj = ((ConfigObject) element).toConfig();
				String type = obj.getString("type");
				boolean wl = !obj.hasPath("whitelist") || obj.getBoolean("whitelist");
				ConfigValue value = obj.root().get("entry");
				List<String> array = value.valueType() == ConfigValueType.LIST ? obj.getStringList("entry") : null;
				String entry = array != null ? null : (String) value.unwrapped();
				int rarity = obj.hasPath("rarity") ? obj.getInt("rarity") : -1;

				l:
				if (type.equalsIgnoreCase("name")) {
					if (array != null) {
						List<String> names = array;
						if (rarity > 0) {
							info = new BiomeInfoRarity(names, 4, true, rarity);
						} else {
							info = new BiomeInfo(names, 4, true);
						}
					} else {
						if (rarity > 0) {
							info = new BiomeInfoRarity(entry, rarity);
						} else {
							info = new BiomeInfo(entry);
						}
					}
				} else {
					Object data;
					int t;
					if (type.equalsIgnoreCase("dictionary")) {
						if (array != null) {
							ArrayList<Type> tags = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; k++) {
								tags.add(Type.getType(array.get(k)));
							}
							data = tags.toArray(new Type[tags.size()]);
							t = 6;
						} else {
							data = Type.getType(entry);
							t = 2;
						}
					} else if (type.equalsIgnoreCase("id")) {
						if (array != null) {
							ArrayList<ResourceLocation> ids = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; ++k) {
								ids.add(new ResourceLocation(array.get(k)));
							}
							data = ids;
							t = 8;
						} else {
							data = new ResourceLocation(entry);
							t = 7;
						}
					} else if (type.equalsIgnoreCase("temperature")) {
						if (array != null) {
							ArrayList<TempCategory> temps = new ArrayList<>(array.size());
							for (int k = 0, j = array.size(); k < j; k++) {
								temps.add(TempCategory.valueOf(array.get(k)));
							}
							data = EnumSet.copyOf(temps);
							t = 5;
						} else {
							data = TempCategory.valueOf(entry);
							t = 1;
						}
					} else {
						log.warn("Biome entry of unknown type");
						break l;
					}
					if (data != null) {
						if (rarity > 0) {
							info = new BiomeInfoRarity(data, t, wl, rarity);
						} else {
							info = new BiomeInfo(data, t, wl);
						}
					}
				}
				break;
			case STRING:
				info = new BiomeInfo((String) element.unwrapped());
				break;
			default:
				log.error("Unknown biome type in at line {}", element.origin().lineNumber());
		}
		return info;
	}

	public static Block parseBlockName(String blockRaw) {

		return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockRaw));
	}

	public static WeightedRandomBlock parseBlockEntry(ConfigValue genElement, boolean clamp) {

		final int min = clamp ? 0 : -1;
		Block block;
		switch (genElement.valueType()) {
			case NULL:
				log.warn("Null Block entry!");
				return null;
			case OBJECT:
				Config blockElement = ((ConfigObject) genElement).toConfig();
				if (!blockElement.hasPath("name")) {
					log.error("Block entry needs a name!");
					return null;
				}
				String blockName;
				block = parseBlockName(blockName = blockElement.getString("name"));
				if (block == Blocks.AIR && !blockName.equalsIgnoreCase("minecraft:air")) {
					log.error("Invalid block entry: {}", blockName);
					return null;
				}
				int weight = blockElement.hasPath("weight") ? MathHelper.clamp(blockElement.getInt("weight"), 1, 1000000) : 100;
				if (blockElement.hasPath("properties")) {
					BlockStateContainer blockstatecontainer = block.getBlockState();
					IBlockState state = block.getDefaultState();
					for (Entry<String, ConfigValue> propEntry : blockElement.getObject("properties").entrySet()) {

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
					int metadata = blockElement.hasPath("data") ? MathHelper.clamp(blockElement.getInt("data"), min, 15) : blockElement.hasPath("metadata") ? MathHelper.clamp(blockElement.getInt("metadata"), min, 15) : min;
					return new WeightedRandomBlock(block, metadata, weight);
				}
			case STRING:
				String name = (String) genElement.unwrapped();
				block = parseBlockName(name);
				if (block == Blocks.AIR && !name.equalsIgnoreCase("minecraft:air")) {
					log.error("Invalid block entry 2: {}", name);
					return null;
				}
				return new WeightedRandomBlock(block, min);
			default:
				return null;
		}
	}

	private static <T extends Comparable<T>> IBlockState setValue(IBlockState state, IProperty<T> prop, String val) {

		return state.withProperty(prop, prop.parseValue(val).get());
	}

	public static boolean parseResList(ConfigValue genElement, List<WeightedRandomBlock> resList, boolean clamp) {

		if (genElement == null) {
			return false;
		}

		if (genElement.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) genElement;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedRandomBlock entry = parseBlockEntry(blockList.get(i), clamp);
				if (entry == null) {
					return false;
				}
				resList.add(entry);
			}
		} else if (genElement.valueType() == ConfigValueType.NULL) {
			return true;
		} else {
			WeightedRandomBlock entry = parseBlockEntry(genElement, clamp);
			if (entry == null) {
				return false;
			}
			resList.add(entry);
		}
		return true;
	}

	public static WeightedRandomNBTTag parseEntityEntry(ConfigValue genElement) {

		switch (genElement.valueType()) {
			case NULL:
				log.warn("Null entity entry!");
				return null;
			case OBJECT:
				Config genObject = ((ConfigObject) genElement).toConfig();
				NBTTagCompound data;
				if (genObject.hasPath("spawner-tag")) {
					try {
						data = JsonToNBT.getTagFromJson(genObject.getString("spawner-tag"));
					} catch (NBTException e) {
						log.error("Invalid entity entry at line {}!", genElement.origin().lineNumber(), e);
						return null;
					}
				} else if (!genObject.hasPath("entity")) {
					log.error("Invalid entity entry at line {}!", genElement.origin().lineNumber());
					return null;
				} else {
					data = new NBTTagCompound();
					String type = genObject.getString("entity");
					data.setString("EntityId", type);
				}
				int weight = genObject.hasPath("weight") ? genObject.getInt("weight") : 100;
				return new WeightedRandomNBTTag(weight, data);
			case STRING:
				String type = (String) genElement.unwrapped();
				if (type == null) {
					log.error("Invalid entity entry!");
					return null;
				}
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("EntityId", type);
				return new WeightedRandomNBTTag(100, tag);
			default:
				log.warn("Invalid entity entry type at line {}", genElement.origin().lineNumber());
				return null;
		}
	}

	public static boolean parseEntityList(ConfigValue genElement, List<WeightedRandomNBTTag> list) {

		if (genElement.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) genElement;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedRandomNBTTag entry = parseEntityEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedRandomNBTTag entry = parseEntityEntry(genElement);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static DungeonMob parseWeightedStringEntry(ConfigValue genElement) {

		int weight = 100;
		String type = null;
		switch (genElement.valueType()) {
			case LIST:
				log.warn("Lists are not supported for string values at line {}.", genElement.origin().lineNumber());
				return null;
			case NULL:
				log.warn("Null string entry at line {}", genElement.origin().lineNumber());
				return null;
			case OBJECT:
				Config genObject = ((ConfigObject) genElement).toConfig();
				if (genObject.hasPath("type")) {
					type = genObject.getString("name");
				} else {
					log.warn("Value missing 'type' field at line {}", genElement.origin().lineNumber());
				}
				if (genObject.hasPath("weight")) {
					weight = genObject.getInt("weight");
				}
				break;
			case BOOLEAN:
			case NUMBER:
			case STRING:
				type = String.valueOf(genElement.unwrapped());
				break;
		}
		return new DungeonMob(weight, new ResourceLocation(type));
	}

	public static boolean parseWeightedStringList(ConfigValue genElement, List<DungeonMob> list) {

		if (genElement.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) genElement;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				DungeonMob entry = parseWeightedStringEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			DungeonMob entry = parseWeightedStringEntry(genElement);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static WeightedRandomItemStack parseWeightedRandomItem(ConfigValue genElement) {

		if (genElement.valueType() == ConfigValueType.NULL) {
			return null;
		}
		int metadata = 0, stackSize = 1, chance = 100;
		ItemStack stack;

		if (genElement.valueType() != ConfigValueType.OBJECT) {
			stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(String.valueOf(genElement.unwrapped()))), 1, metadata);
		} else {
			Config item = ((ConfigObject) genElement).toConfig();

			if (item.hasPath("metadata")) {
				metadata = item.getInt("metadata");
			}
			if (item.hasPath("count")) {
				stackSize = item.getInt("count");
			} else if (item.hasPath("stack-size")) {
				stackSize = item.getInt("stack-size");
			} else if (item.hasPath("amount")) {
				stackSize = item.getInt("amount");
			}
			if (stackSize <= 0) {
				stackSize = 1;
			}
			if (item.hasPath("weight")) {
				chance = item.getInt("weight");
			}
			if (item.hasPath("ore-name")) {
				String oreName = item.getString("ore-name");
				if (!Utils.oreNameExists(oreName)) {
					log.error("Invalid ore name for item at line {}!", genElement.origin().lineNumber());
					return null;
				}
				ItemStack oreStack = OreDictionary.getOres(oreName, false).get(0);
				stack = Utils.cloneStack(oreStack, stackSize);
			} else {
				if (!item.hasPath("name")) {
					log.error("Item entry missing valid name or ore name at line {}!", genElement.origin().lineNumber());
					return null;
				}
				stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.getString("name"))), stackSize, metadata);
			}
			if (item.hasPath("nbt")) {
				try {
					NBTTagCompound nbtbase = JsonToNBT.getTagFromJson(item.getString("nbt"));

					stack.setTagCompound(nbtbase);
				} catch (NBTException t) {
					log.error("Item has invalid NBT data.", t);
				}
			}
		}
		return new WeightedRandomItemStack(stack, chance);
	}

	public static boolean parseWeightedItemList(ConfigValue genElement, List<WeightedRandomItemStack> res) {

		if (genElement.valueType() != ConfigValueType.LIST) {
			WeightedRandomItemStack entry = parseWeightedRandomItem(genElement);
			if (entry == null) {
				return false;
			}
			res.add(entry);
		} else {
			ConfigList list = (ConfigList) genElement;

			for (int i = 0, e = list.size(); i < e; ++i) {
				WeightedRandomItemStack entry = parseWeightedRandomItem(list.get(i));
				if (entry == null) {
					return false;
				}
				res.add(entry);
			}
		}
		return true;
	}

	public static INumberProvider parseNumberValue(ConfigValue genElement) {

		return parseNumberValue(genElement, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public static INumberProvider parseNumberValue(ConfigValue genElement, long min, long max) {

		switch (genElement.valueType()) {
			case NUMBER:
				return new ConstantProvider(boundCheck((Number) genElement.unwrapped(), min, max));
			case OBJECT:
				ConfigObject genData = (ConfigObject) genElement;
				Config genProp = genData.toConfig();
				switch (genData.size()) {
					case 1:
						if (genData.containsKey("value")) {
							return new ConstantProvider(boundCheck(genProp.getNumber("value"), min, max));
						} else if (genData.containsKey("variance")) {
							return new SkellamRandomProvider(boundCheck(genProp.getNumber("variance"), min, max));
						}
						break;
					case 2:
						if (genData.containsKey("min") && genData.containsKey("max")) {
							return new UniformRandomProvider(boundCheck(genProp.getNumber("min"), min, max), boundCheck(genProp.getNumber("max"), min, max));
						}
						break;
					default:
						throw new Error(String.format("Too many properties on object at line %s", genElement.origin().lineNumber()));
					case 0:
						break;
				}
				throw new Error(String.format("Unknown properties on object at line %s", genElement.origin().lineNumber()));
			default:
				throw new Error(String.format("Unsupported data type at line %s", genElement.origin().lineNumber()));
		}
	}

	public static Number boundCheck(Number value, long min, long max) {

		if (value.longValue() >= min) {
			if (value.longValue() <= max) {
				return value;
			}
			return new Long(max);
		}
		return new Long(min);
	}

	/* INCLUDER CLASS */
	public static class Includer implements ConfigIncluder, ConfigIncluderClasspath, ConfigIncluderFile, ConfigIncluderURL {

		public static Includer includer = new Includer();
		public static ConfigParseOptions options = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF).setIncluder(includer);
		public static ConfigResolveOptions resolveOptions = ConfigResolveOptions.noSystem();

		@Override
		public ConfigIncluder withFallback(ConfigIncluder fallback) {

			return this;
		}

		@Override
		public ConfigObject include(ConfigIncludeContext context, String what) {

			return includeFile(context, new File(what));
		}

		@Override
		public ConfigObject includeFile(ConfigIncludeContext context, File file) {

			try {
				if (!FilenameUtils.directoryContains(WorldProps.worldGenDir.getCanonicalPath(), file.getCanonicalPath())) {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
			return ConfigFactory.parseFileAnySyntax(file, context.parseOptions()).root();
		}

		@Override
		public ConfigObject includeResources(ConfigIncludeContext context, String what) {

			throw new IllegalArgumentException("Cannot include resources!");
		}

		@Override
		public ConfigObject includeURL(ConfigIncludeContext context, URL what) {

			throw new IllegalArgumentException("Cannot include URLs!");
		}

	}

}
