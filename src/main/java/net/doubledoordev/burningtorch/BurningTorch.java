package net.doubledoordev.burningtorch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {

    }

//    @SubscribeEvent
//    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
//    {
//        blockRegistryEvent.getRegistry().registerAll(
//                new BurningTorchBlock(Block.Properties.create(Material.MISCELLANEOUS).lightValue(14).doesNotBlockMovement().hardnessAndResistance(0).sound(SoundType.WOOD)).setRegistryName("burningtorch")
//                //new BlockBurningPumpkin(Block.Properties.create(Material.GOURD, MaterialColor.ADOBE).hardnessAndResistance(1.0F).sound(SoundType.WOOD).lightValue(14))
//        );
//    }
//
//    @SubscribeEvent
//    public static void onRegisterItem(final RegistryEvent.Register<Item> event)
//    {
//        event.getRegistry().registerAll(
//                new BlockItem(BlockHolder.burningtorch, (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName("burningtorch"),
//                new ItemCharredTorchRemains(new Item.Properties().group(ItemGroup.MATERIALS)).setRegistryName("charredtorchremains")
//        );
//    }
//
//    @SubscribeEvent
//    public static void onRegisterTileEntityType(final RegistryEvent.Register<TileEntityType<?>> event)
//    {
//        event.getRegistry().registerAll(
//            TileEntityType.Builder.create(TorchTE::new, BlockHolder.burningtorch).build(null).setRegistryName("torchTE")
//        );
//    }
//    @SidedProxy(clientSide = "net.doubledoordev.burningtorch.proxy.ClientProxy", serverSide = "net.doubledoordev.burningtorch.BurningTorch")
//    public static BurningTorch proxy;
//
//    public static final String MOD_ID = "burningtorch";
//    public static final String MOD_NAME = "Burning Torch";
//    public static final String VERSION = "1.1.0";
//
//    /**
//     * This is the instance of your mod as created by Forge. It will never be null.
//     */
//    @Mod.Instance(MOD_ID)
//    public static BurningTorch INSTANCE;
//
//    /**
//     * This is the first initialization event. Register tile entities here.
//     * The registry events below will have fired prior to entry to this method.
//     */
//    @Mod.EventHandler
//    public void preinit(FMLPreInitializationEvent event)
//    {
//        GameRegistry.registerTileEntity(TorchTE.class, MOD_ID +":torchte");
//        GameRegistry.registerTileEntity(PumpkinTorchTE.class, MOD_ID + ":pumpkinte");
//        MinecraftForge.EVENT_BUS.register(new EventHandlers());
//    }
//
//    /**
//     * Forge will automatically look up and bind blocks to the fields in this class
//     * based on their registry name.
//     */
//    @GameRegistry.ObjectHolder(MOD_ID)
//    public static class Blocks
//    {
//        @GameRegistry.ObjectHolder("burningtorch")
//        public static final Block burningtorch = null;
//
//        @GameRegistry.ObjectHolder("burningpumpkin")
//        public static final Block burningpumpkin = null;
//    }
//
//    /**
//     * Forge will automatically look up and bind items to the fields in this class
//     * based on their registry name.
//     */
//    @GameRegistry.ObjectHolder(MOD_ID)
//    public static class Items
//    {
//        @GameRegistry.ObjectHolder("charredtorchremains")
//        public static ItemCharredTorchRemains itemCharredTorchRemains;
//    }
//
//    /**
//     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
//     */
//    @Mod.EventBusSubscriber
//    public static class ObjectRegistryHandler
//    {
//        /**
//         * Listen for the register event for creating custom blocks
//         */
//        @SubscribeEvent
//        public static void addBlocks(RegistryEvent.Register<Block> event)
//        {
//            // Needs to be CIRCUITS for water to break.
//           event.getRegistry().register(new BlockBurningTorch(Material.CIRCUITS));
//            event.getRegistry().register(new BlockBurningPumpkin(Material.WOOD));
//        }
//
//        /**
//         * Listen for the register event for creating custom items
//         */
//        @SubscribeEvent
//        public static void addItems(RegistryEvent.Register<Item> event)
//        {
//           event.getRegistry().register(new ItemBlock(Blocks.burningtorch).setRegistryName(Blocks.burningtorch.getRegistryName()));
//            event.getRegistry().register(new ItemBlock(Blocks.burningpumpkin).setRegistryName(Blocks.burningpumpkin.getRegistryName()));
//           event.getRegistry().register(new ItemCharredTorchRemains());
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public static void registerRenders(ModelRegistryEvent event)
//    {
//        ModelLoader.setCustomStateMapper(Blocks.burningtorch, new StateMap.Builder().build());
//        ModelLoader.setCustomStateMapper(Blocks.burningpumpkin, new StateMap.Builder().build());
//    }
}
