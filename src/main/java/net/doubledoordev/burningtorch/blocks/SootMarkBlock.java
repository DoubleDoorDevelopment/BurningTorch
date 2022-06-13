package net.doubledoordev.burningtorch.blocks;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class SootMarkBlock extends DirectionalBlock
{
    private static final VoxelShape SOOT_DOWN = Block.box(0, 0, 0, 16, 0.1, 16);
    private static final VoxelShape SOOT_NORTH = Block.box(0, 0, 15.9, 16, 16, 16);
    private static final VoxelShape SOOT_SOUTH = Block.box(0, 0, 0, 16, 16, 0.1);
    private static final VoxelShape SOOT_EAST = Block.box(0, 0, 0, 0.1, 16, 16);
    private static final VoxelShape SOOT_WEST = Block.box(15.9, 0, 0, 16, 16, 16);

    public SootMarkBlock(Properties properties)
    {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
        );
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
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        return canSurvive(state, level, currentPos) ? state : Blocks.AIR.defaultBlockState();
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext placeContext)
    {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid)
    {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);

        return blockstate.isFaceSturdy(level, blockpos, direction);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext collisionContext)
    {
        return switch (state.getValue(FACING))
                {
                    case EAST -> SOOT_EAST;
                    case WEST -> SOOT_WEST;
                    case SOUTH -> SOOT_SOUTH;
                    case NORTH -> SOOT_NORTH;
                    default -> SOOT_DOWN;
                };
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext)
    {
        // Get a default block state.
        BlockState blockstate = this.defaultBlockState();
        // Get the level data.
        Level level = placeContext.getLevel();
        // Get the pos we are working with.
        BlockPos blockpos = placeContext.getClickedPos();
        // Return the direction the player was looking.
        Direction[] lookingDirection = placeContext.getNearestLookingDirections();

        // Loop over each direction
        for (Direction direction : lookingDirection)
        {
            // If that direction is a horizontal one.
            if (direction.getAxis().isHorizontal())
            {
                // Flip the direction 180 to set on the wall.
                Direction wallFacingDirection = direction.getOpposite();
                // Change the block state to match the new direction.
                blockstate = blockstate.setValue(FACING, wallFacingDirection);
                // If we have a valid spot to place the block, we place it.
                if (blockstate.canSurvive(level, blockpos))
                {
                    return blockstate;
                }
            }
            // Place it flat.
            else return blockstate;
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder)
    {
        stateBuilder.add(BlockStateProperties.FACING);
    }
}
