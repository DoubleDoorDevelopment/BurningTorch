package net.doubledoordev.burningtorch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import net.doubledoordev.burningtorch.util.EventHandlers;

@Mod("burningtorch")
public class BurningTorch
{
    public static final String MOD_ID = "burningtorch";

    public static Logger LOGGER = LogManager.getLogger();

    public BurningTorch()
    {
        MinecraftForge.EVENT_BUS.register(BurningTorchConfig.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BurningTorchConfig.spec);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }
}
