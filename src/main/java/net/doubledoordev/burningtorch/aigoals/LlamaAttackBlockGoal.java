package net.doubledoordev.burningtorch.aigoals;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.util.TagKeys;
import net.doubledoordev.burningtorch.util.Util;

/**
 * It spit, it break.
 */
public class LlamaAttackBlockGoal extends MoveToBlockGoal
{
    int attackCoolDown = -1;
    int breakTime;
    int lastBreakProgress = -1;
    float targetBreakTime = -1;

    public LlamaAttackBlockGoal(PathfinderMob mob, double speed, int searchRange)
    {
        super(mob, speed, searchRange);
    }

    public LlamaAttackBlockGoal(PathfinderMob mob, double speed, int searchRange, int verticalSearchRange)
    {
        super(mob, speed, searchRange, verticalSearchRange);
    }

    public boolean canUse()
    {
        return findNearestBlock();
    }

    public boolean canContinueToUse()
    {
        return canUse() && isValidTarget(mob.level, blockPos) && breakTime < targetBreakTime;
    }

    @Override
    public void start()
    {
        findNearestBlock();
        targetBreakTime = (mob.level.getBlockState(blockPos).getDestroySpeed(mob.level, blockPos) * BurningTorchConfig.GENERAL.llamaDestroySpeedMultiplier.get().floatValue());
    }

    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    public void tick()
    {
        Level level = mob.getLevel();

        //Get the block pos axis for doing math as pos only has int resolution.
        int posX = blockPos.getX();
        int posY = blockPos.getY();
        int posZ = blockPos.getZ();

        // guard against empty shapes....
        if (level.getBlockState(blockPos).getShape(level, blockPos).isEmpty())
            return;
        Vec3 centerOfTarget = level.getBlockState(blockPos).getShape(level, blockPos).bounds().getCenter();

        // make a vector holding our new values that gives us the most accurate spot to hit the center of the block.
        // y needs to be flipped because otherwise you target too high.
        Vec3 targetVec = new Vec3(
                posX > 0 ? posX - centerOfTarget.x() : posX + centerOfTarget.x(),
                posY > 0 ? posY + centerOfTarget.y() : posY - centerOfTarget.y(),
                posZ > 0 ? posZ - centerOfTarget.z() : posZ + centerOfTarget.z());

        // make the mob look at the target.
        mob.getLookControl().setLookAt(targetVec);
        if (--attackCoolDown == 0)
        {
            Llama llama = (Llama) mob;
            LlamaSpit llamaspit = new LlamaSpit(level, llama);
            // make sure to use the corrected targeting points from the vector. The Y subtraction is to correct for mob head offset.
            double x = targetVec.x() - llama.getX();
            double y = targetVec.y() - 0.6 - llamaspit.getY();
            double z = targetVec.z() - llama.getZ();
            double distance = Math.sqrt(x * x + z * z) * (double) 0.2F;
            llamaspit.shoot(x, y + distance, z, 1.5F, 10.0F);
            if (!llama.isSilent())
            {
                level.playSound(null, llama.getX(), llama.getY(), llama.getZ(), SoundEvents.LLAMA_SPIT, llama.getSoundSource(), 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
            }
            level.addFreshEntity(llamaspit);

            // Handling of breaking blocks that do not hate LIT states. Could likely do something funky with checking for other properties
            // but that's annoying to config for. Plus devs shouldn't be rolling their own lit states...
            if (!level.getBlockState(blockPos).hasProperty(BlockStateProperties.LIT) || !BurningTorchConfig.GENERAL.llamaSpitExtinguishesFirst.get())
            {
                ++breakTime;
                if (breakTime >= targetBreakTime)
                {
                    level.destroyBlock(blockPos, true, llama);
                    level.levelEvent(2001, blockPos, Block.getId(level.getBlockState(blockPos)));
                }
                else
                {
                    // updates the block break progress. there's 9 steps of cracks, get the % of progress then multiply by 9 to get the correct number.
                    level.destroyBlockProgress(mob.getId(), blockPos, (int) ((breakTime / targetBreakTime) * 9));
                }
            }
        }
        // reset the attack cool down after an attack.
        else if (attackCoolDown < 0)
        {
            attackCoolDown = BurningTorchConfig.GENERAL.llamaSpitAtBlockCoolDown.get();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos)
    {
        // set the block pos from the super class for future use.
        blockPos = pos;
        // get the target state.
        BlockState state = level.getBlockState(pos);
        // as the target can be anywhere within an area, including behind other objects. We need to cast a ray to get what's in the way
        // to validate the block can be hit.
        if (state.is(TagKeys.LLAMA_SPIT_TARGET))
        {
            BlockHitResult firstBlockInSightLine = level.clip(new ClipContext(mob.getPosition(1.0f), Util.blockPosToVec3(blockPos),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mob));

            // check that the target is really the target we want by comparing pos hit vs target pos.
            if (firstBlockInSightLine.getBlockPos().equals(blockPos))
            {
                // if the block has the lit property use it.
                if (state.hasProperty(BlockStateProperties.LIT) && BurningTorchConfig.GENERAL.llamaSpitExtinguishesFirst.get())
                {
                    return state.getValue(BlockStateProperties.LIT);
                }
                // otherwise we need to destroy it, so we need to check if it's in the tag.
                else return !state.getShape(level, blockPos).isEmpty();
            }
        }
        return false;
    }

    public void stop()
    {
        // this clears the break progress cracks on the block.
        mob.level.destroyBlockProgress(this.mob.getId(), blockPos, -1);
        lastBreakProgress = 0;
        breakTime = 0;
    }
}
