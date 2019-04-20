package net.doubledoordev.burningtorch.util;

import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import net.doubledoordev.burningtorch.ModConfig;

public class EventHandlers
{
    @SubscribeEvent
    public void blockDestroy(BlockEvent.HarvestDropsEvent event)
    {
        //TODO: Add jackolanterns to this.
        if (event.getState().getBlock() == Blocks.TORCH && ModConfig.replaceVanillaTorchDrops || event.getState().getBlock() == Blocks.LIT_PUMPKIN && ModConfig.replaceVanillaPumpkinDrops)
        {
            event.getDrops().clear();
            for (Map.Entry<String, Integer> entry : ModConfig.vanillaDrops.entrySet())
            {
                event.getDrops().add(new ItemStack(Item.getByNameOrId(entry.getKey()), entry.getValue()));
            }
            event.setDropChance(ModConfig.dropChance.floatValue());
        }
    }

    @SubscribeEvent
    public void removeRecipe(RegistryEvent.Register<IRecipe> event)
    {
        ResourceLocation torch = new ResourceLocation("minecraft:torch");
        ResourceLocation pumpkin = new ResourceLocation("minecraft:lit_pumpkin");
        IForgeRegistryModifiable modRegistry = (IForgeRegistryModifiable) event.getRegistry();
        if (ModConfig.removeVanillaTorchRecipe)
        {
            modRegistry.remove(torch);
        }
        if (ModConfig.removeVanillaJackOLantenRecipe)
        {
            modRegistry.remove(pumpkin);
        }
    }

}
