package net.doubledoordev.burningtorch.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.tileentities.PumpkinTorchTE;

public class BurningPumpkinBlock extends Block implements IWaterLoggable
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);

    public BurningPumpkinBlock(Block.Properties properties)
    {
        super(properties);

        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(LIT, true)
                .with(DECAY, 5)
                .with(FACING, Direction.UP)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, IFluidState fluidStateIn)
    {
        if (!state.get(BlockStateProperties.WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER)
        {
            boolean lit = state.get(LIT);
            if (lit)
            {
                worldIn.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            worldIn.setBlockState(pos, state.with(WATERLOGGED, true).with(LIT, false), 3);
            worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            return true;
        }
        else
        {
            return false;
        }
    }

    // Changes the lighting level based off the LIT blockstate property.
    @Override
    public int getLightValue(BlockState state)
    {
        if (state.get(LIT))
        {
            return 15;
//            switch (state.get(DECAY))
//            {
//                case 5:
//                   return 15;
//                case 4:
//                    return 13;
//                case 3:
//                    return 10;
//                case 2:
//                    return 7;
//                case 1:
//                    return 4;
//            }
        }
        return 0;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        PumpkinTorchTE pumpkinTE = (PumpkinTorchTE) worldIn.getTileEntity(pos);

        for (String item : BurningTorchConfig.GENERAL.relightingItems.get())
        {
            if (player.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || player.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && !state.get(LIT))
            {
                worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.3F, 0.8F);
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).with(LIT, true));
                return ActionResultType.SUCCESS;
            }
        }

        for (String item : BurningTorchConfig.GENERAL.extinguishingingItems.get())
        {
            if (player.getHeldItemMainhand().getItem().getRegistryName().toString().equals(item) || player.getHeldItemOffhand().getItem().getRegistryName().toString().equals(item) && state.get(LIT))
            {
                worldIn.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.3F, 0.8F);
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).with(LIT, false));
                return ActionResultType.SUCCESS;
            }
        }

        for (String itemValue : BurningTorchConfig.GENERAL.extendingItems.get())
        {
            String[] splitItemValue = itemValue.split(",");
            if (player.getHeldItemMainhand().getItem().getRegistryName().toString().equals(splitItemValue[0]) && pumpkinTE.getDecayLevel() < 5)
            {
                if (worldIn.getBlockState(pos).get(DECAY) + Integer.valueOf(splitItemValue[1]) > 5)
                {
                    worldIn.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.3F, 0.8F);
                    pumpkinTE.setDecayLevel(5);
                    if (!player.isCreative())
                    {
                        player.getHeldItemMainhand().setCount(player.getHeldItemMainhand().getCount() - 1);
                    }
                    return ActionResultType.SUCCESS;
                }
                else
                {
                    pumpkinTE.setDecayLevel(worldIn.getBlockState(pos).get(DECAY) + Integer.valueOf(splitItemValue[1]));
                    if (!player.isCreative())
                    {
                        player.getHeldItemMainhand().setCount(player.getHeldItemMainhand().getCount() - 1);
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }

        if (player.getHeldItemMainhand().getItem() == Items.SHEARS)
        {
            if (state.get(DECAY) > 1)
            {
                worldIn.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 0.2F, 0.8F);
                pumpkinTE.setDecayLevel(state.get(DECAY) - 1);
                return ActionResultType.SUCCESS;
            }
            else
                player.sendStatusMessage(new TranslationTextComponent("burningtorch.interact.shears.low"), true);
        }
        return ActionResultType.FAIL;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {

        if (BurningTorchConfig.GENERAL.placeLitPumpkins.get())
            return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()).with(LIT, true);
        else
            return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()).with(LIT, false);
    }

    @Override
    public IFluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(LIT, DECAY, WATERLOGGED, FACING);
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
        return new PumpkinTorchTE();
    }
}
