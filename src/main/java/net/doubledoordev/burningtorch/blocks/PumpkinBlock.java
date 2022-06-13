package net.doubledoordev.burningtorch.blocks;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningLightBlockEntity;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningPumpkinBlockEntity;
import net.doubledoordev.burningtorch.util.Util;
import org.jetbrains.annotations.NotNull;

public class PumpkinBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, SimpleBurningBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty DECAY = Util.DECAY;

    public PumpkinBlock(Block.Properties properties)
    {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, true)
                .setValue(DECAY, 5)
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
    {
        if (state.getValue(LIT))
        {
            switch (state.getValue(DECAY))
            {
                case 5:
                    return BurningTorchConfig.GENERAL.pumpkinLightDecay5.get();
                case 4:
                    return BurningTorchConfig.GENERAL.pumpkinLightDecay4.get();
                case 3:
                    return BurningTorchConfig.GENERAL.pumpkinLightDecay3.get();
                case 2:
                    return BurningTorchConfig.GENERAL.pumpkinLightDecay2.get();
                case 1:
                    return BurningTorchConfig.GENERAL.pumpkinLightDecay1.get();
            }
        }
        return 0;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState)
    {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER)
        {
            boolean lit = state.getValue(LIT);
            if (lit)
            {
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            level.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(LIT, false), 3);
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            return true;
        }
        else
        {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hitResult)
    {
        if (!level.isClientSide())
        {
            if (Util.extinguishBurningSource(player, level, pos, interactionHand) ||
                    Util.igniteBurningSource(player, level, pos, interactionHand) ||
                    Util.trimBurningSource(player, level, pos, state, interactionHand) ||
                    Util.refuelBurningSource(BurningTorchConfig.GENERAL.pumpkinExtendingTags, player, level, pos, state, interactionHand))
                return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext)
    {
        return Util.BUMP_INTO_BLOCK;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIT, DECAY, WATERLOGGED, FACING);
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        // Stole from campfires and modified. This harms the entity on the torch.
        if (BurningTorchConfig.GENERAL.pumpkinBurnsEntities.get() && !entity.fireImmune() && state.getValue(LIT) &&
                entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity))
        {
            entity.hurt(DamageSource.IN_FIRE, 1);
        }

        // Torches don't stop entities, thus if they collide and are on fire, light torch.
        if (!state.getValue(LIT) && entity.getRemainingFireTicks() > 0)
            level.setBlockAndUpdate(pos, state.setValue(LIT, true));
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BurningPumpkinBlockEntity(pos, state);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockRegistry.BURNING_PUMPKIN_LIGHT_BLOCK_ENTITY.get(), BurningLightBlockEntity::tick);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random)
    {
        if (BurningTorchConfig.GENERAL.pumpkinBurnoutWarning.get() && state.getValue(DECAY) == 1 && random.nextFloat() > 0.5)
        {
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY() + 1D;
            double z = (double) pos.getZ() + 0.5D;

            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext)
    {
        if (BurningTorchConfig.GENERAL.pumpkinPlaceLit.get())
            return defaultBlockState()
                    .setValue(FACING, placeContext.getHorizontalDirection().getOpposite())
                    .setValue(LIT, true)
                    .setValue(DECAY, BurningTorchConfig.GENERAL.pumpkinStartingDecayLevel.get());
        else
            return defaultBlockState()
                    .setValue(FACING, placeContext.getHorizontalDirection().getOpposite())
                    .setValue(LIT, false)
                    .setValue(DECAY, BurningTorchConfig.GENERAL.pumpkinStartingDecayLevel.get());
    }

    @Override
    public void doLifeCycleTick(Level level, BlockPos pos, BlockState state, BurningLightBlockEntity burningLightBlockEntity)
    {
        burningLightBlockEntity.handleRain(BurningTorchConfig.GENERAL.pumpkinRainExtinguish, BurningTorchConfig.GENERAL.pumpkinRainUpdateRate, level, pos, state);
        burningLightBlockEntity.startFires(BurningTorchConfig.GENERAL.pumpkinPercentToStartFire, BurningTorchConfig.GENERAL.pumpkinDelayBetweenFire, level, pos, state);
        burningLightBlockEntity.decayBlock(BurningTorchConfig.GENERAL.pumpkinDecayRate, level, pos, state);
    }

    @Override
    public BlockState getExpiredBlockStateReplacement()
    {
        if (BurningTorchConfig.GENERAL.pumpkinLeavesPumpkin.get())
            return Blocks.CARVED_PUMPKIN.defaultBlockState();
        else if (BurningTorchConfig.GENERAL.pumpkinMakesSootMark.get())
            return SimpleBurningBlock.super.getExpiredBlockStateReplacement();
        else return Blocks.AIR.defaultBlockState();
    }
}
