package cofh.cofhworld;

import cofh.cofhworld.command.CommandCoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.init.WorldProps;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod (CoFHWorld.MOD_ID)
public class CoFHWorld {

	public static final String MOD_ID = "cofhworld";
	public static final String MOD_NAME = "CoFH World";

	public static Logger log = LogManager.getLogger(MOD_NAME);

	public CoFHWorld() {

		super();

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::preInit);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
		MinecraftForge.EVENT_BUS.addListener(this::onCommandRegistration);

		WorldHandler.register();
	}

	/* INIT */
	public void preInit(FMLCommonSetupEvent event) {

		//WorldProps.configDir = event.getModConfigurationDirectory();

		WorldProps.preInit();

		WorldHandler.initialize();
	}

	public void onServerStarting(FMLServerAboutToStartEvent event) {

		WorldHandler.reloadConfig(true);
	}

	public void onCommandRegistration(RegisterCommandsEvent event) {

		CommandCoFHWorld.register(event.getDispatcher());
	}

}
