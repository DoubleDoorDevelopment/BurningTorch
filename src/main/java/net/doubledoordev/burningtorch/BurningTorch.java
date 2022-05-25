package net.doubledoordev.burningtorch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import net.doubledoordev.burningtorch.blocks.BlockRegistry;
import net.doubledoordev.burningtorch.client.ClientRegistry;
import net.doubledoordev.burningtorch.items.ItemRegistry;

@Mod("burningtorch")
public class BurningTorch
{
    public static final String MOD_ID = "burningtorch";

    public static Logger LOGGER = LogManager.getLogger();

    public BurningTorch()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BurningTorchConfig.spec);

        // Register the doClientStuff method for modloading on the client only.
        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ClientRegistry.init();
        }

        MinecraftForge.EVENT_BUS.register(this);

        ItemRegistry.ITEMS_DEFERRED.register(modEventBus);
        BlockRegistry.BLOCK_DEFERRED.register(modEventBus);
        BlockRegistry.TILE_ENTITY_DEFERRED.register(modEventBus);
    }
}
