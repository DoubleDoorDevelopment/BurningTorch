package net.doubledoordev.burningtorch.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.items.ItemRegistry;

public class VanillaLootTableEdits
{
    @SubscribeEvent
    public static void modifyTorchDrops(LootTableLoadEvent event)
    {
        ResourceLocation jackOLanternTableLoc = new ResourceLocation("minecraft:blocks/pumpkin");
        ResourceLocation torchTableLoc = new ResourceLocation("minecraft:blocks/torch");

        //TODO: This doesn't work. Need to remove the old table, replace with charred table
        if (BurningTorchConfig.GENERAL.replaceVanillaDrops.get() &&
                (event.getTable().getLootTableId().equals(jackOLanternTableLoc) || event.getTable().getLootTableId().equals(torchTableLoc)))
        {
            event.getTable().removePool("torch");

            LootPool.Builder charredWoodPool = new LootPool.Builder();

            charredWoodPool.add(LootItem.lootTableItem(ItemRegistry.CHARRED_WOOD.get()).when(ExplosionCondition.survivesExplosion())
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))));

            event.getTable().addPool(charredWoodPool.build());
        }
    }
}
