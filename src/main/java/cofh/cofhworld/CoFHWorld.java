package cofh.cofhworld;

import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.init.WorldProps;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod (CoFHWorld.MOD_ID)
public class CoFHWorld {

	public static final String MOD_ID = "cofhworld";
	public static final String MOD_NAME = "CoFH World";

	public static final String VERSION = "2.0.0b";
	public static final String VERSION_MAX = "2.1.0";
	public static final String VERSION_GROUP = "required-after:" + MOD_ID + "@[" + VERSION + "," + VERSION_MAX + ");";
	public static final String UPDATE_URL = "https://raw.github.com/cofh/version/master/" + MOD_ID + "_update.json";

	public static final String DEPENDENCIES = "required-after:forge@[" + "32.2.36,32.3.0" + ");";

	public static Logger log = LogManager.getLogger(MOD_NAME);
	//public static Configuration config;

	public CoFHWorld() {

		super();

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::preInit);
		modEventBus.addListener(this::loadComplete);

		WorldHandler.register();
	}

	/* INIT */
	public void preInit(FMLCommonSetupEvent event) {

		//WorldProps.configDir = event.getModConfigurationDirectory();

		//config = new Configuration(new File(WorldProps.configDir, "/cofh/world/config.cfg"), VERSION, true);
		//config.load();

		WorldProps.preInit();

		WorldHandler.initialize();
	}

	public void loadComplete(FMLLoadCompleteEvent event) {

		WorldHandler.reloadConfig(false);
		//config.save();

		log.info(MOD_NAME + ": Load Complete.");
	}

	public void onServerStarting(FMLServerStartingEvent event) {

		//event.registerServerCommand(new CommandCoFHWorld());
	}

}
