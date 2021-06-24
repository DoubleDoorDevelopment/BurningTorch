package net.doubledoordev.burningtorch.blocks;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.tileentities.TorchTE;

public class BurningTorchBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);

    private static final VoxelShape STANDING = Block.box(6, 0, 6, 10, 13, 10);

    private static final VoxelShape TORCH_NORTH = Block.box(10, 3, 10, 6, 16, 16);
    private static final VoxelShape TORCH_EAST = Block.box(0, 3, 10, 6, 16, 6);
    private static final VoxelShape TORCH_SOUTH = Block.box(6, 3, 0, 10, 16, 6);
    private static final VoxelShape TORCH_WEST = Block.box(16, 3, 6, 10, 16, 10);

    public BurningTorchBlock(Block.Properties properties)
    {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, true)
                .setValue(DECAY, 5)
                .setValue(FACING, Direction.UP)
        );
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        if (state.getValue(LIT))
        {
            switch (state.getValue(DECAY))
            {
                case 5:
                    return BurningTorchConfig.GENERAL.torchlightLevel5.get();
                case 4:
                    return BurningTorchConfig.GENERAL.torchlightLevel4.get();
                case 3:
                    return BurningTorchConfig.GENERAL.torchlightLevel3.get();
                case 2:
                    return BurningTorchConfig.GENERAL.torchlightLevel2.get();
                case 1:
                    return BurningTorchConfig.GENERAL.torchlightLevel1.get();
            }
        }
        return 0;
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos)
    {
        if (BurningTorchConfig.GENERAL.torchesBurnEntities.get())
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
     */
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        // Check the state, if we have an up deal with the break check for standing torches (stole from Torchblock)
        if (stateIn.getValue(FACING) == Direction.UP)
        {
            return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
        // Otherwise we do a side check and act accordingly. (stole from WallTorchBlock)
        else
        {
            return facing.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TorchTE torchTE = (TorchTE) worldIn.getBlockEntity(pos);

        ITag<Item> extinguishTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MOD_ID, "extinguish_items"));
        ITag<Item> relightTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MOD_ID, "relight_items"));
        ITag<Item> cuttingTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MOD_ID, "cutting_items"));

        Item mainHandItem = player.getMainHandItem().getItem();
        Item offHandItem = player.getOffhandItem().getItem();

        if (extinguishTag.contains(mainHandItem) || extinguishTag.contains(offHandItem))
        {
            worldIn.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.3F, 0.8F);
            worldIn.setBlockAndUpdate(pos, worldIn.getBlockState(pos).setValue(LIT, false));
            return ActionResultType.SUCCESS;
        }

        if (relightTag.contains(mainHandItem) || relightTag.contains(offHandItem))
        {
            worldIn.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.3F, 0.8F);
            worldIn.setBlockAndUpdate(pos, worldIn.getBlockState(pos).setValue(LIT, true));
            return ActionResultType.SUCCESS;
        }

        if (cuttingTag.contains(mainHandItem) || cuttingTag.contains(offHandItem))
        {
            if (state.getValue(DECAY) > 1)
            {
                worldIn.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundCategory.BLOCKS, 0.2F, 0.8F);
                torchTE.setDecayLevel(state.getValue(DECAY) - 1);
                return ActionResultType.SUCCESS;
            }
            else
                player.displayClientMessage(new TranslationTextComponent("burningtorch.interact.shears.low"), true);
        }

        for (String itemValue : BurningTorchConfig.GENERAL.extendingItems.get())
        {
            String[] splitTagFromValue = itemValue.split(",");

            ITag<Item> fuelTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(splitTagFromValue[0]));
            int fuelValue = Integer.parseInt(splitTagFromValue[1]);

            if (fuelTag.contains(mainHandItem) || fuelTag.contains(offHandItem) && torchTE.getDecayLevel() < 5)
            {
                if (worldIn.getBlockState(pos).getValue(DECAY) + fuelValue > 5)
                {
                    worldIn.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.3F, 0.8F);
                    torchTE.setDecayLevel(5);
                }
                else
                {
                    torchTE.setDecayLevel(worldIn.getBlockState(pos).getValue(DECAY) + fuelValue);
                }
                if (!player.isCreative())
                {
                    player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }

    // Stole from wall torches.
    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos)
    {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);

        return blockstate.isFaceSturdy(level, blockpos, direction);
    }

    // Switch statement that handles the bounding boxes for each direction.
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        switch (state.getValue(FACING))
        {
            case EAST:
                return TORCH_EAST;
            case WEST:
                return TORCH_WEST;
            case SOUTH:
                return TORCH_SOUTH;
            case NORTH:
                return TORCH_NORTH;
            default:
                return STANDING;
        }
    }

    // NOTE: Must be entity collide as projectile collide only works on SOLID blocks. (Lights unlit torches)
    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        // Stole from campfires and modified.
        if (!worldIn.isClientSide())
        {
            if (entityIn.isOnFire() && !state.getValue(LIT))
            {
                worldIn.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
            }
        }
    }

    // Handles the effects on the top of the torch.
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        TorchTE torchTE = (TorchTE) worldIn.getBlockEntity(pos);
        boolean lit = stateIn.getValue(LIT);
        Direction facing = stateIn.getValue(FACING);
        int decay = torchTE.getDecayLevel();
        double random = Math.random();

        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY() + 0.9D;
        double d2 = (double) pos.getZ() + 0.5D;

        // Fix d1 for height changes.
        switch (decay)
        {
            case 5:
                d1 = (double) pos.getY() + 0.9D;
                break;
            case 4:
                d1 = (double) pos.getY() + 0.85D;
                break;
            case 3:
                d1 = (double) pos.getY() + 0.75D;
                break;
            case 2:
                d1 = (double) pos.getY() + 0.62D;
                break;
            case 1:
                d1 = (double) pos.getY() + 0.44D;
                break;
            case 0:
                d1 = (double) pos.getY() + 0.35D;
                break;
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
                        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.2D * (double) facing.getOpposite().getStepX(), d1 + 0.20D, d2 + 0.2D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.2D * (double) facing.getOpposite().getStepX(), d1 + 0.20D, d2 + 0.2D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 4:
                        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.22D * (double) facing.getOpposite().getStepX(), d1 + 0.16D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.22D * (double) facing.getOpposite().getStepX(), d1 + 0.16D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 3:
                        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.24D * (double) facing.getOpposite().getStepX(), d1 + 0.15D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.24D * (double) facing.getOpposite().getStepX(), d1 + 0.15D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 2:
                        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.3D * (double) facing.getOpposite().getStepX(), d1 + 0.16D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.3D * (double) facing.getOpposite().getStepX(), d1 + 0.16D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 1:
                        if (random > 0.5)
                        {
                            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.35D * (double) facing.getOpposite().getStepX(), d1 + 0.28D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                            worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.35D * (double) facing.getOpposite().getStepX(), d1 + 0.28D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        }
                        else
                            worldIn.addParticle(ParticleTypes.LARGE_SMOKE, d0 + 0.35D * (double) facing.getOpposite().getStepX(), d1 + 0.28D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        break;
                    case 0:
                        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.1D * (double) facing.getOpposite().getStepX(), d1 + 0.09D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0 + 0.1D * (double) facing.getOpposite().getStepX(), d1 + 0.09D, d2 + 0.3D * (double) facing.getOpposite().getStepZ(), 0.0D, 0.0D, 0.0D);
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
                        worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                        worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                        break;
                    case 1:
                        if (random > 0.5)
                        {
                            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                            worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                        }
                        else
                            worldIn.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                        break;
                }
            }
        }
    }

    //Stole from wall torches, Modified to work with a single block.
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        // Set the blockstate to default.
        BlockState blockstate = this.defaultBlockState();
        // Gets world data?
        IWorldReader iworldreader = context.getLevel();
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
                if (blockstate.canSurvive(iworldreader, blockpos))
                {
                    if (BurningTorchConfig.GENERAL.placeLitTorches.get())
                        return blockstate.setValue(LIT, true);
                    else return blockstate;
                }
            }
            // If the direction we get back isn't horizontal we place the torch like normal with the default state.
            else if (BurningTorchConfig.GENERAL.placeLitTorches.get())
                return blockstate.setValue(LIT, true);
            else return blockstate;
        }
        return null;
    }

    // Builds all the states.
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(LIT, DECAY, FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TorchTE();
    }
}