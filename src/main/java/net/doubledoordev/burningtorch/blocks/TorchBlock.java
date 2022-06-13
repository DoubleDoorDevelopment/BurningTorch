package net.doubledoordev.burningtorch.blocks;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningLightBlockEntity;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningTorchBlockEntity;
import net.doubledoordev.burningtorch.util.Util;

public class TorchBlock extends BaseEntityBlock implements SimpleBurningBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty DECAY = Util.DECAY;

    private static final VoxelShape STANDING = Block.box(6, 0, 6, 10, 13, 10);
    private static final VoxelShape TORCH_NORTH = Block.box(6, 3, 10, 10, 16, 16);
    private static final VoxelShape TORCH_EAST = Block.box(0, 3, 6, 6, 16, 10);
    private static final VoxelShape TORCH_SOUTH = Block.box(6, 3, 0, 10, 16, 6);
    private static final VoxelShape TORCH_WEST = Block.box(10, 3, 6, 16, 16, 10);

    public TorchBlock(Block.Properties properties)
    {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, true)
                .setValue(DECAY, 5)
                .setValue(FACING, Direction.UP)
        );
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        if (state.getValue(LIT))
        {
            switch (state.getValue(DECAY))
            {
                case 5:
                    return BurningTorchConfig.GENERAL.torchLightDecay5.get();
                case 4:
                    return BurningTorchConfig.GENERAL.torchLightDecay4.get();
                case 3:
                    return BurningTorchConfig.GENERAL.torchLightDecay3.get();
                case 2:
                    return BurningTorchConfig.GENERAL.torchLightDecay2.get();
                case 1:
                    return BurningTorchConfig.GENERAL.torchLightDecay1.get();
            }
        }
        return 0;
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter world, BlockPos pos)
    {
        if (BurningTorchConfig.GENERAL.torchBurnsEntities.get())
        {
            return world.getBlockState(pos).getValue(LIT);
        }
        return false;
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     **/
    @SuppressWarnings("deprecation")
    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        // Check the state, if we have an up deal with the break check for standing torches (stole from Torchblock)
        if (stateIn.getValue(FACING) == Direction.UP)
        {
            return facing == Direction.DOWN && !this.canSurvive(stateIn, level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
        }
        // Otherwise we do a side check and act accordingly. (stole from WallTorchBlock)
        else
        {
            return facing.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
        }
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hit)
    {
        if (Util.extinguishBurningSource(player, level, pos, interactionHand) ||
                Util.igniteBurningSource(player, level, pos, interactionHand) ||
                Util.trimBurningSource(player, level, pos, state, interactionHand) ||
                Util.refuelBurningSource(BurningTorchConfig.GENERAL.torchExtendingTags, player, level, pos, state, interactionHand))
            return InteractionResult.SUCCESS;

        return InteractionResult.FAIL;
    }

    // Stole from wall torches.
    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);

        return blockstate.isFaceSturdy(level, blockpos, direction);
    }

    // Switch statement that handles the bounding boxes for each direction.
    @SuppressWarnings("deprecation")
    @Override
    @ParametersAreNonnullByDefault
    @Nonnull
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext)
    {
        return switch (state.getValue(FACING))
                {
                    case EAST -> TORCH_EAST;
                    case WEST -> TORCH_WEST;
                    case SOUTH -> TORCH_SOUTH;
                    case NORTH -> TORCH_NORTH;
                    default -> STANDING;
                };
    }

    // NOTE: Must be entity collide as projectile collide only works on SOLID blocks. (Lights unlit torches)
    @SuppressWarnings("deprecation")
    @Override
    @ParametersAreNonnullByDefault
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        // Stole from campfires and modified. This harms the entity on the torch.
        if (BurningTorchConfig.GENERAL.torchBurnsEntities.get() && !entity.fireImmune() && state.getValue(LIT) &&
                entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity))
        {
            entity.hurt(DamageSource.IN_FIRE, 1);
        }

        // Torches don't stop entities, thus if they collide and are on fire, light torch.
        if (!state.getValue(LIT) && entity.getRemainingFireTicks() > 0)
            level.setBlockAndUpdate(pos, state.setValue(LIT, true));
    }

    // Handles the effects on the top of the torch.
    @ParametersAreNonnullByDefault
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random)
    {
        BurningLightBlockEntity burningLightBlockEntity = (BurningLightBlockEntity) level.getBlockEntity(pos);

        if (burningLightBlockEntity == null)
            return;

        boolean lit = state.getValue(LIT);
        Direction facing = state.getValue(FACING);
        int decay = state.getValue(Util.DECAY);

        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY() + 0.9D;
        double z = (double) pos.getZ() + 0.5D;

        // Fix d1 for height changes.
        switch (decay)
        {
            case 5 -> y = (double) pos.getY() + 0.9D;
            case 4 -> y = (double) pos.getY() + 0.85D;
            case 3 -> y = (double) pos.getY() + 0.75D;
            case 2 -> y = (double) pos.getY() + 0.62D;
            case 1 -> y = (double) pos.getY() + 0.44D;
            case 0 -> y = (double) pos.getY() + 0.35D;
        }

        if (lit)
        {
            // Checking if torch is placed on the side of something.
            if (facing.getAxis().isHorizontal())
            {
                // Side attached torches particle spawning.
                switch (decay)
                {
                    case 5:
                        level.addParticle(ParticleTypes.SMOKE, x + 0.2D * (double) facing.getOpposite().getStepX(), y + 0.20D, z + 0.2D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x + 0.2D * (double) facing.getOpposite().getStepX(), y + 0.20D, z + 0.2D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 4:
                        level.addParticle(ParticleTypes.SMOKE, x + 0.22D * (double) facing.getOpposite().getStepX(), y + 0.16D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x + 0.22D * (double) facing.getOpposite().getStepX(), y + 0.16D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 3:
                        level.addParticle(ParticleTypes.SMOKE, x + 0.24D * (double) facing.getOpposite().getStepX(), y + 0.15D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x + 0.24D * (double) facing.getOpposite().getStepX(), y + 0.15D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 2:
                        level.addParticle(ParticleTypes.SMOKE, x + 0.3D * (double) facing.getOpposite().getStepX(), y + 0.16D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x + 0.3D * (double) facing.getOpposite().getStepX(), y + 0.16D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 1:
                        if (random.nextFloat() > 0.5)
                        {
                            level.addParticle(ParticleTypes.SMOKE, x + 0.35D * (double) facing.getOpposite().getStepX(), y + 0.28D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                            level.addParticle(ParticleTypes.FLAME, x + 0.35D * (double) facing.getOpposite().getStepX(), y + 0.28D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        }
                        else if (BurningTorchConfig.GENERAL.torchBurnoutWarning.get())
                            level.addParticle(ParticleTypes.LARGE_SMOKE, x + 0.35D * (double) facing.getOpposite().getStepX(), y + 0.28D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 0:
                        level.addParticle(ParticleTypes.SMOKE, x + 0.1D * (double) facing.getOpposite().getStepX(), y + 0.09D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x + 0.1D * (double) facing.getOpposite().getStepX(), y + 0.09D, z + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                }

            }
            // Otherwise its a regular torch.
            else
            {
                // Standing torch particle spawning.
                switch (decay)
                {
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 0:
                        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
                        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
                        break;
                    case 1:
                        if (random.nextFloat() > 0.5)
                        {
                            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
                            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
                        }
                        else if (BurningTorchConfig.GENERAL.torchBurnoutWarning.get())
                            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
                        break;
                }
            }
        }
    }

    //Stole from wall torches, Modified to work with a single block.
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // Set the blockstate to default.
        BlockState blockstate = this.defaultBlockState().setValue(DECAY, BurningTorchConfig.GENERAL.torchStartingDecayLevel.get());
        // Gets world data?
        Level level = context.getLevel();
        // Get the pos we are working with.
        BlockPos blockpos = context.getClickedPos();
        // Return the direction the player was looking.
        Direction[] adirection = context.getNearestLookingDirections();

        // Loop over each direction
        for (Direction direction : adirection)
        {
            // If that direction is a horizontal one.
            if (direction.getAxis().isHorizontal())
            {
                // Flip the direction 180 to set on the wall.
                Direction direction1 = direction.getOpposite();
                // Change the blockstate to match the new direction.
                blockstate = blockstate.setValue(FACING, direction1);
                // If we have a valid spot to place the block, we place it.
                if (blockstate.canSurvive(level, blockpos))
                {
                    if (BurningTorchConfig.GENERAL.torchPlaceLit.get())
                        return blockstate.setValue(LIT, true);
                    else return blockstate;
                }
            }
            // If the direction we get back isn't horizontal we place the torch like normal with the default state.
            else if (BurningTorchConfig.GENERAL.torchPlaceLit.get())
                return blockstate.setValue(LIT, true);
            else return blockstate;
        }
        return null;
    }

    // Builds all the states.
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIT, DECAY, FACING);
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
        return new BurningTorchBlockEntity(pos, state);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockRegistry.BURNING_TORCH_LIGHT_BLOCK_ENTITY.get(), BurningLightBlockEntity::tick);
    }

    @Override
    public void doLifeCycleTick(Level level, BlockPos pos, BlockState state, BurningLightBlockEntity burningLightBlockEntity)
    {
        burningLightBlockEntity.handleRain(BurningTorchConfig.GENERAL.torchRainExtinguish, BurningTorchConfig.GENERAL.torchRainUpdateRate, level, pos, state);
        burningLightBlockEntity.startFires(BurningTorchConfig.GENERAL.torchPercentToStartFire, BurningTorchConfig.GENERAL.torchDelayBetweenFire, level, pos, state);
        burningLightBlockEntity.decayBlock(BurningTorchConfig.GENERAL.torchDecayRate, level, pos, state);
    }

    @Override
    public BlockState getExpiredBlockStateReplacement()
    {
        if (BurningTorchConfig.GENERAL.torchMakesSootMark.get())
            return SimpleBurningBlock.super.getExpiredBlockStateReplacement();
        else return Blocks.AIR.defaultBlockState();
    }
}