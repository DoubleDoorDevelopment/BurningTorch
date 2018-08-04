package net.doubledoordev.burningtorch;

import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

@Config(modid = BurningTorch.MOD_ID)
@Config.LangKey("burningtorch.config.title")
public class ModConfig
{
    @Config.LangKey("burningtorch.config.rainupdaterate")
    @Config.Comment("How quickly a torch will react to a storm in ticks, Lower values are faster. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    public static int rainUpdateRate = 40;

    @Config.LangKey("burningtorch.config.burnrate")
    @Config.Comment("How many ticks between decay levels. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
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
    @Config.Comment("What items can relight a torch.")
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
    @Config.Comment("How much light does an unlit torche give?")
    @Config.RangeInt(min = 0, max = 16)
    public static int lightLevelUnlitTorch = 0;

    @Config.LangKey("burningtorch.config.startingdecaylevel")
    @Config.Comment("What decay level do the torches start at?")
    @Config.RangeInt(min = 1, max = 5)
    public static int startingDecayLevel = 5;

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

    @Config.LangKey("burningtorch.config.removevanillatorchdrops")
    @Config.Comment("Should vanilla torches drops be replaced?")
    public static boolean replaceVanillaTorchDrops = true;

    @Config.LangKey("burningtorch.config.vanillatorchitemdropchance")
    @Config.Comment("The drop chance for any items we add to vanilla torches.")
    @Config.RangeDouble(min = 0, max = 1)
    public static Double dropChance = 1D;

}
