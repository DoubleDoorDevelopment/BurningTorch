package net.doubledoordev.burningtorch.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.blocks.BlockBurningPumpkin;
import net.doubledoordev.burningtorch.blocks.BlockBurningTorch;
import net.doubledoordev.burningtorch.items.ItemCharredTorchRemains;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends BurningTorch
{
    @GameRegistry.ObjectHolder("burningtorch:charredtorchremains")
    public static ItemCharredTorchRemains itemCharredTorchRemains;

    @GameRegistry.ObjectHolder("burningtorch:burningtorch")
    public static BlockBurningTorch burningtorch;

    @GameRegistry.ObjectHolder("burningtorch:burningpumpkin")
    public static BlockBurningPumpkin burningPumpkin;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        itemCharredTorchRemains.initModel();
        burningtorch.initModel();
        burningPumpkin.initModel();
    }

    @Override
    public void preinit(FMLPreInitializationEvent event)
    {
        super.preinit(event);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        initModels();
    }
}