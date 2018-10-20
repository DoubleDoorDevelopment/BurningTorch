package net.doubledoordev.burningtorch;

import net.doubledoordev.burningtorch.blocks.BlockBurningTorch;
import net.doubledoordev.burningtorch.items.ItemCharredTorchRemains;
import net.doubledoordev.burningtorch.tileentities.TorchTE;
import net.doubledoordev.burningtorch.util.EventHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(
        modid = BurningTorch.MOD_ID,
        name = BurningTorch.MOD_NAME,
        version = BurningTorch.VERSION
)
public class BurningTorch
{
    @SidedProxy(clientSide = "net.doubledoordev.burningtorch.proxy.ClientProxy", serverSide = "net.doubledoordev.burningtorch.BurningTorch")
    public static BurningTorch proxy;

    public static final String MOD_ID = "burningtorch";
    public static final String MOD_NAME = "Burning Torch";
    public static final String VERSION = "1.0.5";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static BurningTorch INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
        GameRegistry.registerTileEntity(TorchTE.class, MOD_ID +":torchte");
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks
    {
        @GameRegistry.ObjectHolder("burningtorch")
        public static final Block burningtorch = null;
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items
    {
        @GameRegistry.ObjectHolder("charredtorchremains")
        public static ItemCharredTorchRemains itemCharredTorchRemains;
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler
    {
        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event)
        {
            // Needs to be CIRCUITS for water to break.
           event.getRegistry().register(new BlockBurningTorch(Material.CIRCUITS));
        }

        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event)
        {
           event.getRegistry().register(new ItemBlock(Blocks.burningtorch).setRegistryName(Blocks.burningtorch.getRegistryName()));
           event.getRegistry().register(new ItemCharredTorchRemains());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event)
    {
        ModelLoader.setCustomStateMapper(Blocks.burningtorch, new StateMap.Builder().build());
    }
}
