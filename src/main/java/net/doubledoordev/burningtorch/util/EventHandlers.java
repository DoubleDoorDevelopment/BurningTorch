package net.doubledoordev.burningtorch.util;

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
                new BurningTorchBlock(Block.Properties.create(Material.MISCELLANEOUS)
                        .doesNotBlockMovement()
                        .hardnessAndResistance(0)
                        .sound(SoundType.WOOD)
                        .tickRandomly())
                        .setRegistryName("burningtorch"),
                new BurningPumpkinBlock(Block.Properties.create(Material.GOURD, MaterialColor.ADOBE)
                        .hardnessAndResistance(1.0F)
                        .sound(SoundType.WOOD))
                        .setRegistryName("burningpumpkin")
        );
    }

    @SubscribeEvent
    public static void onRegisterItem(final RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(BlockHolder.burningtorch, (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName("burningtorch"),
                new BlockItem(BlockHolder.burningpumpkin, (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName("burningpumpkin"),
                new ItemCharredTorchRemains(new Item.Properties().group(ItemGroup.MATERIALS)).setRegistryName("charredtorchremains")
        );
    }

    @SubscribeEvent
    public static void onRegisterTileEntityType(final RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(TorchTE::new, BlockHolder.burningtorch).build(null).setRegistryName("torchte"),
                TileEntityType.Builder.create(PumpkinTorchTE::new, BlockHolder.burningpumpkin).build(null).setRegistryName("pumpkintorchte")
        );
    }

    @SubscribeEvent
    public static void clientRendering(FMLClientSetupEvent event)
    {
        DeferredWorkQueue.runLater(() -> RenderTypeLookup.setRenderLayer(BlockHolder.burningtorch, RenderType.getCutoutMipped()));
    }
}
