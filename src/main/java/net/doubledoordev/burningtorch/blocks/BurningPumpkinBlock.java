package net.doubledoordev.burningtorch.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.tileentities.PumpkinTorchTE;
import net.doubledoordev.burningtorch.tileentities.TorchTE;

public class BurningPumpkinBlock extends Block implements IWaterLoggable
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);

    public BurningPumpkinBlock(Block.Properties properties)
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        if (state.getValue(LIT))
        {
            switch (state.getValue(DECAY))
            {
                case 5:
                    return BurningTorchConfig.GENERAL.pumpkinlightLevel5.get();
                case 4:
                    return BurningTorchConfig.GENERAL.pumpkinlightLevel4.get();
                case 3:
                    return BurningTorchConfig.GENERAL.pumpkinlightLevel3.get();
                case 2:
                    return BurningTorchConfig.GENERAL.pumpkinlightLevel2.get();
                case 1:
                    return BurningTorchConfig.GENERAL.pumpkinlightLevel1.get();
            }
        }
        return 0;
    }

    @Override
    public boolean placeLiquid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn)
    {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidStateIn.getType() == Fluids.WATER)
        {
            boolean lit = state.getValue(LIT);
            if (lit)
            {
                worldIn.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            worldIn.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(LIT, false), 3);
            worldIn.getLiquidTicks().scheduleTick(pos, fluidStateIn.getType(), fluidStateIn.getType().getTickDelay(worldIn));
            return true;
        }
        else
        {
            return false;
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
                    if (!player.isCreative())
                    {
                        player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                    }
                    return ActionResultType.SUCCESS;
                }
                else
                {
                    torchTE.setDecayLevel(worldIn.getBlockState(pos).getValue(DECAY) + fuelValue);
                    if (!player.isCreative())
                    {
                        player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        if (BurningTorchConfig.GENERAL.placeLitPumpkins.get())
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, true);
        else
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
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
