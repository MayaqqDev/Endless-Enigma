package dev.mayaqq.endless;

import dev.mayaqq.endless.config.EndlessConfig;
import dev.mayaqq.endless.networking.EndlessC2SPackets;
import dev.mayaqq.endless.registry.*;
import dev.mayaqq.endless.server.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Endless implements ModInitializer {
    public static final String MOD_ID = "endless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Path SERVER_DATA_PATH;
    public static EndlessConfig CONFIG = EndlessConfig.createAndLoad();
    public static MinecraftServer SERVER;
    @Override
    public void onInitialize() {
        LOGGER.info("The Void is endless.");

        ServerEvents.init();
        EndlessItems.register();
        EndlessBlocks.register();
        EndlessBlockEntities.register();
        EndlessC2SPackets.register();
        EndlessFeatures.register();
        EndlessFluids.register();
        EndlessScreenHandlerTypes.register();
        EndlessCutscenes.register();
        EndlessTags.register();
        EndlessCommands.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}