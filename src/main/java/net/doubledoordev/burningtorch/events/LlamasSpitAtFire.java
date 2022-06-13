package net.doubledoordev.burningtorch.events;

import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.aigoals.LlamaAttackBlockGoal;

public class LlamasSpitAtFire
{
    @SubscribeEvent
    public static void entityAI(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof Llama llama)
        {
            llama.goalSelector.addGoal(0, new LlamaAttackBlockGoal(llama, 5, BurningTorchConfig.GENERAL.llamaSpitHorizontalSearchRange.get(), BurningTorchConfig.GENERAL.llamaSpitVerticalSearchRange.get()));
        }
    }

    @SubscribeEvent
    public static void spitCollide(ProjectileImpactEvent event)
    {
        if (BurningTorchConfig.GENERAL.llamaSpitExtinguishesFirst.get() &&
                !event.getEntity().getLevel().isClientSide() && event.getProjectile() instanceof LlamaSpit)
        {
            if (event.getRayTraceResult().getType() == HitResult.Type.BLOCK)
            {
                BlockState hitState = event.getEntity().getLevel().getBlockState(event.getProjectile().getOnPos());

                if (hitState.hasProperty(BlockStateProperties.LIT))
                {
                    event.getEntity().getLevel().setBlockAndUpdate(event.getProjectile().getOnPos(), hitState.setValue(BlockStateProperties.LIT, false));
                }
            }
        }
    }
}
