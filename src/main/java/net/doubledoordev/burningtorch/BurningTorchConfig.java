package net.doubledoordev.burningtorch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;


public class BurningTorchConfig
{
    public static final BurningTorchConfig.General GENERAL;
    static final ForgeConfigSpec spec;

    static
    {
        final Pair<BurningTorchConfig.General, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(BurningTorchConfig.General::new);
        spec = specPair.getRight();
        GENERAL = specPair.getLeft();
    }

    public static class General
    {

        public static List<? extends String> extendingItemsList()
        {
            return new ArrayList<>(Arrays.asList("burningtorch:fuel_value_5,5", "burningtorch:fuel_value_1,1"));
        }

        // General stuff
        public IntValue rainUpdateRate;
        public IntValue decayRate;
        public IntValue percentToStartFire;
        public IntValue delayBetweenFire;
        public ConfigValue<List<? extends String>> extendingItems;
        public BooleanValue shouldRainExtinguish;
        // Torch stuff
        public BooleanValue torchesBurnEntities;
        public BooleanValue torchesStartFireWhenLit;
        public BooleanValue placeLitTorches;
        public IntValue torchlightLevel5;
        public IntValue torchlightLevel4;
        public IntValue torchlightLevel3;
        public IntValue torchlightLevel2;
        public IntValue torchlightLevel1;
        public IntValue torchStartingDecayLevel;
        // Pumpkin stuff
        public BooleanValue placeLitPumpkins;
        public IntValue pumpkinlightLevel5;
        public IntValue pumpkinlightLevel4;
        public IntValue pumpkinlightLevel3;
        public IntValue pumpkinlightLevel2;
        public IntValue pumpkinlightLevel1;
        public IntValue pumpkinStartingDecayLevel;

        General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General configuration settings")
                    .push("General");

            rainUpdateRate = builder
                    .comment("How quickly an unprotected torch will react to a storm in ticks, Lower values are faster. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
                    .translation("burningtorch.config.rainupdaterate")
                    .defineInRange("rainUpdateRate", 40, 1, Integer.MAX_VALUE);

            decayRate = builder
                    .comment("How many ticks between decay levels. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
                    .translation("burningtorch.config.burnrate")
                    .defineInRange("decayRate", 18000, 1, Integer.MAX_VALUE);

            shouldRainExtinguish = builder
                    .comment("Should rain extinguish unprotected torches?")
                    .translation("burningtorch.config.rainextinguish")
                    .define("shouldRainExtinguish", true);

            extendingItems = builder
                    .comment("Item Tag lists that are valid fuel sources for torches to consume. Number is the value for every item in the list.")
                    .translation("burningtorch.config.itemstoextend")
                    .defineList("extendingItems", BurningTorchConfig.General.extendingItemsList(), p -> p instanceof String);

            builder.pop();

            builder.comment("Torch configuration settings")
                    .push("Torches");

            torchStartingDecayLevel = builder
                    .comment("What decay level do the torches start at?")
                    .translation("burningtorch.config.startingdecaylevel")
                    .defineInRange("torchStartingDecayLevel", 5, 1, 5);

            torchesBurnEntities = builder
                    .comment("Toggle if torches burn entities when lit. true=lit false=unlit")
                    .translation("burningtorch.config.torchburnsentities")
                    .define("torchesBurnEntities", true);

            torchesStartFireWhenLit = builder
                    .comment("Toggle if torches burn entities when lit. true=burn false=no burn")
                    .translation("burningtorch.config.torchstartsfire")
                    .define("torchesStartFireWhenLit", true);

            percentToStartFire = builder
                    .comment("% Chance to start a fire near a torch.")
                    .translation("burningtorch.config.percentToStartFire")
                    .defineInRange("percentToStartFire", 50, 1, 100);

            delayBetweenFire = builder
                    .comment("Delay between the chance for a torch to start a fire in ticks. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
                    .translation("burningtorch.config.delayBetweenFire")
                    .defineInRange("delayBetweenFire", 40, 1, Integer.MAX_VALUE);

            placeLitTorches = builder
                    .comment("Toggle whither torches are lit when placed or unlit. true=lit false=unlit")
                    .translation("burningtorch.config.place.unlit")
                    .define("placeLitTorches", true);

            torchlightLevel5 = builder
                    .comment("How much light does new torches give?")
                    .translation("burningtorch.config.lightatlevel5")
                    .defineInRange("lightatlevel5", 15, 0, 16);

            torchlightLevel4 = builder
                    .comment("How much light does an almost new torches give?")
                    .translation("burningtorch.config.lightatlevel4")
                    .defineInRange("lightatlevel4", 15, 0, 16);

            torchlightLevel3 = builder
                    .comment("How much light does a half used torches give?")
                    .translation("burningtorch.config.lightatlevel3")
                    .defineInRange("lightatlevel3", 15, 0, 16);

            torchlightLevel2 = builder
                    .comment("How much light does an almost burnt out torches give?")
                    .translation("burningtorch.config.lightatlevel2")
                    .defineInRange("lightatlevel2", 15, 0, 16);

            torchlightLevel1 = builder
                    .comment("How much light does a burning out torches give?")
                    .translation("burningtorch.config.lightatlevel1")
                    .defineInRange("lightatlevel1", 15, 0, 16);

            builder.pop();


            builder.comment("Pumpkin configuration settings")
                    .push("Pumpkin");

            pumpkinStartingDecayLevel = builder
                    .comment("What decay level do the torches start at?")
                    .translation("burningtorch.config.startingdecaylevel")
                    .defineInRange("pumpkinStartingDecayLevel", 5, 1, 5);

            placeLitPumpkins = builder
                    .comment("Toggle if pumpkins are lit when placed or unlit. true=lit false=unlit")
                    .translation("burningtorch.config.place.unlit.pumpkin")
                    .define("placeLitPumpkins", true);

            pumpkinlightLevel5 = builder
                    .comment("How much light does new torches give?")
                    .translation("burningtorch.config.lightatlevel5")
                    .defineInRange("lightatlevel5", 15, 0, 16);

            pumpkinlightLevel4 = builder
                    .comment("How much light does an almost new torches give?")
                    .translation("burningtorch.config.lightatlevel4")
                    .defineInRange("lightatlevel4", 15, 0, 16);

            pumpkinlightLevel3 = builder
                    .comment("How much light does a half used torches give?")
                    .translation("burningtorch.config.lightatlevel3")
                    .defineInRange("lightatlevel3", 15, 0, 16);

            pumpkinlightLevel2 = builder
                    .comment("How much light does an almost burnt out torches give?")
                    .translation("burningtorch.config.lightatlevel2")
                    .defineInRange("lightatlevel2", 15, 0, 16);

            pumpkinlightLevel1 = builder
                    .comment("How much light does a burning out torches give?")
                    .translation("burningtorch.config.lightatlevel1")
                    .defineInRange("lightatlevel1", 15, 0, 16);

            builder.pop();

        }
    }
}