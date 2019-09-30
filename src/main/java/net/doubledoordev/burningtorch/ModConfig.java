package net.doubledoordev.burningtorch;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.doubledoordev.burningtorch.BurningTorch.MOD_ID;

@Config(modid = MOD_ID)
@Config.LangKey("burningtorch.config.title")
public class ModConfig
{
    @Config.LangKey("burningtorch.config.rainupdaterate")
    @Config.Comment("How quickly a torch will react to a storm in ticks, Lower values are faster. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    public static int rainUpdateRate = 40;

    @Config.LangKey("burningtorch.config.burnrate")
    @Config.Comment("How many ticks between decay levels. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour. Use 0 to disable.")
    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    public static int decayRate = 18000;

    @Config.LangKey("burningtorch.config.itemstorelighttorches")
    @Config.Comment("What items can relight a torch.")
    public static String[] relightingItems = new String[1];

    static
    {
        relightingItems[0] = "minecraft:flint_and_steel";
    }

    @Config.LangKey("burningtorch.config.itemstoextinguish")
    @Config.Comment("What items can extinguish a torch.")
    public static String[] extinguishingingItems = new String[1];

    static
    {
        extinguishingingItems[0] = "minecraft:bucket";
    }

    @Config.LangKey("burningtorch.config.itemstoextend")
    @Config.Comment("What items can add more time to a torch. Number is levels added.")
    public static final Map<String, Integer> extendingingItems = new HashMap<>();

    static
    {
        extendingingItems.put("minecraft:coal", 5);
        extendingingItems.put("minecraft:planks", 1);
    }

    @Config.LangKey("burningtorch.config.rainextinguish")
    @Config.Comment("Should rain extinguish torches?")
    public static boolean shouldRainExtinguish = true;

    @Config.LangKey("burningtorch.config.lightatlevel5")
    @Config.Comment("How much light does new torches give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevel5 = 14;

    @Config.LangKey("burningtorch.config.lightatlevel4")
    @Config.Comment("How much light does an almost new torches give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevel4 = 14;

    @Config.LangKey("burningtorch.config.lightatlevel3")
    @Config.Comment("How much light does a half used torches give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevel3 = 14;

    @Config.LangKey("burningtorch.config.lightatlevel2")
    @Config.Comment("How much light does an almost burnt out torches give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevel2 = 14;

    @Config.LangKey("burningtorch.config.lightatlevel1")
    @Config.Comment("How much light does a burning out torches give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevel1 = 14;

    @Config.LangKey("burningtorch.config.lightunlit")
    @Config.Comment("How much light does an unlit torch give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevelUnlitTorch = 0;

    @Config.LangKey("burningtorch.config.startingdecaylevel")
    @Config.Comment("What decay level do the pumpkin start at?")
    @Config.RangeInt(min = 1, max = 5)
    public static int torchStartingDecayLevel = 5;

    @Config.LangKey("burningtorch.config.pumpkin.lightatlevel5")
    @Config.Comment("How much light does new pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevel5 = 14;

    @Config.LangKey("burningtorch.config.pumpkin.lightatlevel4")
    @Config.Comment("How much light does an almost new pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevel4 = 14;

    @Config.LangKey("burningtorch.config.pumpkin.lightatlevel3")
    @Config.Comment("How much light does a half used pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevel3 = 14;

    @Config.LangKey("burningtorch.config.pumpkin.lightatlevel2")
    @Config.Comment("How much light does an almost burnt out pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevel2 = 14;

    @Config.LangKey("burningtorch.config.pumpkin.lightatlevel1")
    @Config.Comment("How much light does a burning out pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevel1 = 14;

    @Config.LangKey("burningtorch.config.pumpkin.lightunlit")
    @Config.Comment("How much light does an unlit pumpkin give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int pumpkinLightLevelUnlit = 0;

    @Config.LangKey("burningtorch.config.pumpkin.startingdecaylevel")
    @Config.Comment("What decay level do the pumpkins start at?")
    @Config.RangeInt(min = 1, max = 5)
    public static int pumkinStartingDecayLevel = 5;

    @Config.LangKey("burningtorch.config.dropeditemswhenbroke")
    @Config.Comment("What is dropped when a torch is broke? Set quantity to 9 for nugget based math.")
    public static final Map<String, Integer> drops = new HashMap<>();

    static
    {
        drops.put("burningtorch:charredtorchremains", 9);
    }

    @Config.LangKey("burningtorch.config.dropeditemswhenvanillabroke")
    @Config.Comment("What is dropped when a vanilla torch is broke?")
    public static final Map<String, Integer> vanillaDrops = new HashMap<>();

    static
    {
        vanillaDrops.put("burningtorch:charredtorchremains", 3);
    }

    @Config.LangKey("burningtorch.config.removevanillatorchrecipe")
    @Config.Comment("Should the vanilla torch recipe be removed?")
    public static boolean removeVanillaTorchRecipe = true;

    @Config.LangKey("burningtorch.config.pumpkin.removevanilladrops")
    @Config.Comment("Should the vanilla Jack O Lantern recipe be removed?")
    public static boolean removeVanillaJackOLantenRecipe = true;

    @Config.LangKey("burningtorch.config.removevanillatorchdrops")
    @Config.Comment("Should vanilla torches drops be replaced?")
    public static boolean replaceVanillaTorchDrops = true;

    @Config.LangKey("burningtorch.config.pumpkin.removevanilladrops")
    @Config.Comment("Should vanilla torches drops be replaced?")
    public static boolean replaceVanillaPumpkinDrops = true;

    @Config.LangKey("burningtorch.config.vanillatorchitemdropchance")
    @Config.Comment("The drop chance for any items we add to vanilla torches.")
    @Config.RangeDouble(min = 0, max = 1)
    public static Double dropChance = 1D;

    @Config.LangKey("burningtorch.config.place.unlit")
    @Config.Comment("Toggle whither torches are lit when placed or unlit. true=lit false=unlit")
    public static boolean placeLitTorches = true;

    @Config.LangKey("burningtorch.config.place.unlit.pumpkin")
    @Config.Comment("Toggle whither pumpkins are lit when placed or unlit. true=lit false=unlit")
    public static boolean placeLitPumpkins = true;

    @Config.LangKey("burningtorch.config.torchstartsfire")
    @Config.Comment("Toggle whither torches start fires when lit. true=fire false=no fire")
    public static boolean torchesStartFireWhenLit = true;

    @Config.LangKey("burningtorch.config.torchburnsentities")
    @Config.Comment("Toggle whither torches burn entities when lit. true=burn false=no burn")
    public static boolean torchesBurnEntities = true;

    @Mod.EventBusSubscriber
    public static class SyncConfig
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(MOD_ID))
            {
                ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
