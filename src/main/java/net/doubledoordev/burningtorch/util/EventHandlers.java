package net.doubledoordev.burningtorch.util;

import java.util.Map;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.ModConfig;

public class EventHandlers
{
    Random random = new Random();
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

    @SubscribeEvent
    public void entityHit(LivingAttackEvent event)
    {
        Entity entity = event.getEntity();
        Entity source = event.getSource().getTrueSource();
        Iterable<ItemStack> heldItems;

        if (source instanceof EntityLiving | source instanceof EntityPlayer)
        {
            heldItems = source.getHeldEquipment();

            for (ItemStack item : heldItems)
            {
                if (item != null && item.getItem() == ItemBlock.getItemFromBlock(BurningTorch.Blocks.burningtorch))
                {
                    if (ModConfig.fireAttackChance != 0 && ModConfig.fireAttackChance > random.nextInt(100))
                        entity.setFire(5);
                }
            }
        }
    }

}
