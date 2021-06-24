package net.doubledoordev.burningtorch.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.doubledoordev.burningtorch.blocks.BlockHolder;
import net.doubledoordev.burningtorch.blocks.BurningPumpkinBlock;
import net.doubledoordev.burningtorch.blocks.BurningTorchBlock;
import net.doubledoordev.burningtorch.items.ItemCharredTorchRemains;
import net.doubledoordev.burningtorch.tileentities.PumpkinTorchTE;
import net.doubledoordev.burningtorch.tileentities.TorchTE;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlers
{
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
    {
        blockRegistryEvent.getRegistry().registerAll(
                new BurningTorchBlock(AbstractBlock.Properties.of(Material.DECORATION)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.WOOD)
                        .randomTicks())
                        .setRegistryName("burningtorch"),

                new BurningPumpkinBlock(AbstractBlock.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE)
                        .strength(1.0F)
                        .sound(SoundType.WOOD))
                        .setRegistryName("burningpumpkin")
        );
    }

    @SubscribeEvent
    public static void onRegisterItem(final RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(BlockHolder.burningtorch, (new Item.Properties()).tab(ItemGroup.TAB_DECORATIONS)).setRegistryName("burningtorch"),
                new BlockItem(BlockHolder.burningpumpkin, (new Item.Properties()).tab(ItemGroup.TAB_DECORATIONS)).setRegistryName("burningpumpkin"),
                new ItemCharredTorchRemains(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)).setRegistryName("charredtorchremains")
        );
    }

    @SubscribeEvent
    public static void onRegisterTileEntityType(final RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.of(TorchTE::new, BlockHolder.burningtorch).build(null).setRegistryName("torchte"),
                TileEntityType.Builder.of(PumpkinTorchTE::new, BlockHolder.burningpumpkin).build(null).setRegistryName("pumpkintorchte")
        );
    }

    @SubscribeEvent
    public static void clientRendering(FMLClientSetupEvent event)
    {
        DeferredWorkQueue.runLater(() -> RenderTypeLookup.setRenderLayer(BlockHolder.burningtorch, RenderType.cutoutMipped()));
    }
}
