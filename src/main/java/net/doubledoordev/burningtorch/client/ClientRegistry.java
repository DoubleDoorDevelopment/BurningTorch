package net.doubledoordev.burningtorch.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.doubledoordev.burningtorch.blocks.BlockRegistry;

public class ClientRegistry
{
    public static void init()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientRegistry::doClientStuff);
    }

    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BURNING_TORCH.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.BURNING_PUMPKIN.get(), RenderType.solid());
    }
}
