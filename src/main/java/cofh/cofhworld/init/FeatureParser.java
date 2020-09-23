package cofh.cofhworld.init;

import cofh.cofhworld.parser.DistributionData;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.IGeneratorParser;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.*;
import com.typesafe.config.impl.CoFHOrderedParsableFile;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.MavenVersionAdapter;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.maven.artifact.versioning.VersionRange;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static cofh.cofhworld.CoFHWorld.log;

//import net.minecraftforge.fml.common.Loader;
//import net.minecraftforge.fml.common.LoaderState;
//import net.minecraftforge.fml.common.ModContainer;
//import net.minecraftforge.fml.common.versioning.ArtifactVersion;
//import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
//import net.minecraftforge.fml.common.versioning.VersionParser;

public class FeatureParser {

	private static HashMap<String, IDistributionParser> distributionHandlers = new HashMap<>();
	private static HashMap<String, IGeneratorParser> generatorHandlers = new HashMap<>();
	public static ArrayList<IFeatureGenerator> parsedFeatures = new ArrayList<>();

	private FeatureParser() {

	}

	public static IDistributionParser getDistribution(String distribution) {

		return distributionHandlers.get(distribution);
	}

	public static IGeneratorParser getGenerator(String generator) {

		return generatorHandlers.get(generator);
	}

	public static boolean registerTemplate(String template, IDistributionParser handler) {

		// TODO: provide this function through IFeatureHandler?
		if (!distributionHandlers.containsKey(template)) {
			distributionHandlers.put(template, handler);
			return true;
		}
		log.error("Attempted to register duplicate template '{}'!", template);
		return false;
	}

	public static boolean registerGenerator(String generator, IGeneratorParser handler) {

		// TODO: provide this function through IFeatureHandler?
		if (!generatorHandlers.containsKey(generator)) {
			generatorHandlers.put(generator, handler);
			return true;
		}
		log.error("Attempted to register duplicate generator '{}'!", generator);
		return false;
	}

	public static void addFiles(ArrayList<File> list, File folder) {

		final String logPath = WorldProps.worldGenPath.relativize(Paths.get(folder.getPath())).toString();
		log.trace("Scanning folder \"{}\": ", logPath);

		final AtomicInteger dirs = new AtomicInteger(0);
		File[] fList = folder.listFiles((file, name) -> {

			if (name == null) {
				return false;
			} else if (new File(file, name).isDirectory()) {
				dirs.incrementAndGet();
				return !"includes".equalsIgnoreCase(name);
			}
			return name.toLowerCase(Locale.US).endsWith(".json");
		});

		if (fList == null || fList.length <= 0) {
			log.debug("There are no World Generation files present in \"{}\".", logPath);
			return;
		}
		int d = dirs.get();
		log.trace("Found {} World Generation files and {} folders present in \"{}\".", (fList.length - d), d, logPath);
		list.addAll(Arrays.asList(fList));
	}

	public static void processGenerationFiles() {

		log.info("Accumulating world generation files from: \"{}\"", WorldProps.worldGenDir.toString());
		ArrayList<File> worldGenList = new ArrayList<>(5);

		{
			/*
			 * Standard generation file handled specially, so we break out the first pass of adding files and folders
			 */
			int i = 0;
			if (WorldProps.replaceStandardGeneration) {
				log.info("Replacing standard generation with file \"{}\"", WorldProps.worldGenPath.relativize(Paths.get(WorldProps.standardGenFile.getPath())));
				worldGenList.add(WorldProps.standardGenFile);
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

		// Continue scanning nested folders, breadth first
		for (int i = 0; i < worldGenList.size(); ++i) {
			File genFile = worldGenList.get(i);

			if (genFile.isDirectory()) {
				worldGenList.remove(i--);
				addFiles(worldGenList, genFile);
			}
		}

		log.info("Found a total of {} world generation files.", worldGenList.size());

		ArrayList<ConfigContainer> processedGenList = new ArrayList<>(worldGenList.size());
		for (int i = 0, e = worldGenList.size(); i < e; ++i) {
			File genFile = worldGenList.get(i);
			String file = WorldProps.worldGenPath.relativize(Paths.get(genFile.getPath())).toString();

			Config genList;
			try {
				log.debug("Parsing world generation file: \"{}\"", file);
				genList = new CoFHOrderedParsableFile(genFile, Includer.options).parse().toConfig().resolve(Includer.resolveOptions);
			} catch (Throwable t) {
				log.fatal("Critical error reading from a world generation file: \"{}\" > Please be sure the file is correct!", genFile, t);
				continue;
			}

			if (genList.hasPath("dependencies") && !processDependencies(genList.getValue("dependencies"))) {
				log.debug("Unmet dependencies to load file \"{}\"", file);
				continue;
			}
			if (genList.hasPath("enabled")) {
				ConfigValue en = genList.getValue("enabled");
				if (en.valueType() == ConfigValueType.BOOLEAN) {
					if (!genList.getBoolean("enabled")) {
						log.debug("Generation file \"{}\" is being skipped because it is disabled.", file);
						continue;
					}
				} else {
					log.warn("Generation file \"{}\" is being skipped because the file's global `enabled` flag is not a boolean.", file);
					continue;
				}
			}

			try {
				long pri = genList.hasPath("priority") ? genList.getLong("priority") : 0;
				String namespace = genList.hasPath("namespace") ? genList.getString("namespace") + ":" : "";
				processedGenList.add(new ConfigContainer(genList, pri, namespace));
			} catch (Throwable t) {
				log.error("Error reading world generation file: \"{}\" > Please be sure the file is correct!", genFile, t);
				continue;
			}
			log.trace("World generation file \"{}\" ready to be processed", file);
		}

		processedGenList.sort((ConfigContainer l, ConfigContainer r) -> {

			long lv = l.priority, rv = r.priority;

			return Long.compare(rv, lv);
		});

		parseGenerationFiles(processedGenList);

		// clean up
		apis = null;
	}

	public static boolean processDependencies(ConfigValue value) {

		try {
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
		} catch (RuntimeException e) {
			log.catching(e);
		}
		return false;
	}

	private static Map<String, ModContainer> apis;

	public static Map<String, ModContainer> getLoadedAPIs() {

		if (apis == null) {
			apis = new HashMap<>();
			ModList.get().forEachModContainer((modID, mc) -> apis.put(modID, mc));
		}
		return apis;
	}

	public static boolean processDependency(ConfigValue value) {

		String id;
		ModContainer con;
		VersionRange range = null;
		boolean retComp = true;
		switch (value.valueType()) {
			case STRING:
				id = (String) value.unwrapped();
				if (id.contains("@")) {
					int idx = id.indexOf('@');
					id = id.substring(0, idx);
					range = MavenVersionAdapter.createFromVersionSpec(id.substring(idx + 1));
				}
				con = getLoadedAPIs().get(id);
				break;
			case OBJECT:
				Config data = ((ConfigObject) value).toConfig();
				id = data.getString("id");
				con = getLoadedAPIs().get(id);
				if (data.hasPath("version")) {
					range = MavenVersionAdapter.createFromVersionSpec(data.getString("version"));
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
			log.debug("Dependency '{}' is not loaded or disabled.", id);
			return false == retComp;
		}
		ModLoadingStage state = con.getCurrentState();
		if (state == ModLoadingStage.ERROR) {
			log.debug("Dependency '{}' is crashed.", id);
			return false == retComp;
		}
		if (range != null) {
			if (retComp != range.containsVersion(con.getModInfo().getVersion())) {
				log.debug("Dependency '{}' has an incompatible version {}.", id, con.getModInfo().getVersion());
				return false;
			} else {
				return true;
			}
		}
		return true == retComp;
	}

	public static void parseGenerationFiles(ArrayList<ConfigContainer> processedGenList) {

		for (int i = 0, e = processedGenList.size(); i < e; ++i) {
			ConfigContainer genList = processedGenList.get(i);
			String file = WorldProps.worldGenPath.relativize(Paths.get(genList.config.origin().filename())).toString();
			log.info("Reading world generation info from: \"{}\":", file);
			{
				parseGenerationTag(genList, "populate", FeatureParser::parsePopulateEntry);

				parseGenerationTag(genList, "structure", FeatureParser::parseStructureEntry);
			}
			log.debug("Finished reading \"{}\"", file);
		}

		processedGenList.size();
	}

	public static void parseGenerationTag(ConfigContainer genList, String tag, BiFunction<String, Config, ActionResultType> parseEntry) {

		if (genList.config.hasPath(tag)) {
			log.trace("Processing `{}` entries", tag);
			Config genData = genList.config.getConfig(tag);
			for (Entry<String, ConfigValue> genEntry : genData.root().entrySet()) {
				String key = genEntry.getKey();
				ConfigValue value = genEntry.getValue();
				try {
					if (value.valueType() != ConfigValueType.OBJECT) {
						log.error("Error parsing `{}` entry: '{}' > This must be an object and is not.", tag, key);
					} else {
						log.debug("Parsing `{}` entry '{}':", tag, key);
						switch (parseEntry.apply(genList.namespace + key, genData.getConfig(key))) {
							case SUCCESS:
								log.debug("Parsed `{}` entry successfully: '{}'", tag, key);
								break;
							case FAIL:
								log.error("Error parsing `{}` entry: '{}' > Please check the parameters.", tag, key);
								break;
							case PASS:
								log.error("Error parsing `{}` entry: '{}' > It is a duplicate.", tag, key);
						}
					}
				} catch (ConfigException ex) {
					String line = "";
					if (ex.origin() != null) {
						line = String.format(" on line %s", ex.origin().lineNumber());
					}
					log.error("Error parsing `{}` entry '{}'{}: {}", tag, key, line, ex.getMessage());
					continue;
				} catch (Throwable t) {
					log.fatal("There was a severe error parsing `{}` entry '{}' on line {}!", tag, key, value.origin().lineNumber(), t);
				}
			}
			log.trace("Finished processing `{}` entries", tag);
		} else {
			log.trace("File does not contain tag `{}`", tag);
		}
	}

	public static ActionResultType parsePopulateEntry(String featureName, Config genObject) {

		if (genObject.hasPath("enabled")) {
			if (!genObject.getBoolean("enabled")) {
				log.debug("\"{}\" is disabled.", featureName);
				return ActionResultType.SUCCESS;
			}
		}

		try {
			IFeatureGenerator feature = DistributionData.parseFeature(featureName, genObject);
			if (feature.getFeatureName() != null) {
				parsedFeatures.add(feature);
				return WorldHandler.registerFeature(feature) ? ActionResultType.SUCCESS : ActionResultType.PASS;
			} else {
				throw new IDistributionParser.InvalidDistributionException("Distribution doesn't have a name", genObject.origin());
			}
		} catch (IDistributionParser.InvalidDistributionException e) {
			log.error("Distribution '{}' failed to parse its entry on line {}!", featureName, e.origin().lineNumber());
			log.catching(Level.DEBUG, e);
		}

		return ActionResultType.FAIL;
	}

	public static ActionResultType parseStructureEntry(String featureName, Config genObject) {

		if (genObject.hasPath("enabled")) {
			if (!genObject.getBoolean("enabled")) {
				log.debug("\"{}\" is disabled.", featureName);
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.FAIL;
	}

	private static class ConfigContainer {

		public final Config config;
		public final long priority;
		public final String namespace;

		public ConfigContainer(Config config, long priority, String namespace) {

			this.config = config;
			this.priority = priority;
			this.namespace = namespace;
		}

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
		@Nullable
		public ConfigObject includeFile(ConfigIncludeContext context, File file) {

			try {
				if (!FilenameUtils.directoryContains(WorldProps.worldGenDir.getCanonicalPath(), file.getCanonicalPath())) {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
			return new CoFHOrderedParsableFile(file, context.parseOptions()).parse();
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
