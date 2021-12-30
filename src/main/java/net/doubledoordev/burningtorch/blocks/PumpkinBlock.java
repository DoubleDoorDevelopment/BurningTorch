package net.doubledoordev.burningtorch.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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

import net.doubledoordev.burningtorch.BurningTorch;
import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.blockentities.BurningLightBlockEntity;
import net.doubledoordev.burningtorch.util.Util;

public class PumpkinBlock extends BaseEntityBlock implements SimpleWaterloggedBlock
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
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
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

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hitResult)
    {
        BurningLightBlockEntity burningLightBlockEntity = (BurningLightBlockEntity) level.getBlockEntity(pos);

        if (burningLightBlockEntity == null)
            return InteractionResult.FAIL;

        Tag<Item> extinguishTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "extinguish_items"));
        Tag<Item> relightTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "relight_items"));
        Tag<Item> cuttingTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(BurningTorch.MODID, "cutting_items"));

        Item mainHandItem = player.getMainHandItem().getItem();
        Item offHandItem = player.getOffhandItem().getItem();

        if (extinguishTag.contains(mainHandItem) || extinguishTag.contains(offHandItem))
        {
            level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(LIT, false));
            return InteractionResult.SUCCESS;
        }

        if (relightTag.contains(mainHandItem) || relightTag.contains(offHandItem))
        {
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.3F, 0.8F);
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(LIT, true));
            return InteractionResult.SUCCESS;
        }

        if (cuttingTag.contains(mainHandItem) || cuttingTag.contains(offHandItem))
        {
            if (state.getValue(DECAY) > 1)
            {
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.2F, 0.8F);
                burningLightBlockEntity.setDecayLevel(state.getValue(DECAY) - 1);
                return InteractionResult.SUCCESS;
            }
            else
                player.displayClientMessage(new TranslatableComponent("burningtorch.interact.shears.low"), true);
        }

        for (String itemValue : BurningTorchConfig.GENERAL.extendingItems.get())
        {
            String[] splitTagFromValue = itemValue.split(",");

            Tag<Item> fuelTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(splitTagFromValue[0]));
            int fuelValue = Integer.parseInt(splitTagFromValue[1]);

            if (fuelTag.contains(mainHandItem) || fuelTag.contains(offHandItem) && burningLightBlockEntity.getDecayLevel() < 5)
            {
                if (level.getBlockState(pos).getValue(DECAY) + fuelValue > 5)
                {
                    level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.3F, 0.8F);
                    burningLightBlockEntity.setDecayLevel(5);
                    if (!player.isCreative())
                    {
                        player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                    }
                    return InteractionResult.SUCCESS;
                }
                else
                {
                    burningLightBlockEntity.setDecayLevel(level.getBlockState(pos).getValue(DECAY) + fuelValue);
                    if (!player.isCreative())
                    {
                        player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Nonnull
    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext)
    {
        if (BurningTorchConfig.GENERAL.placeLitPumpkins.get())
            return defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection().getOpposite()).setValue(LIT, true);
        else
            return defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIT, DECAY, WATERLOGGED, FACING);
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BurningLightBlockEntity(pos, state);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockRegistry.BURNING_LIGHT_BLOCK_ENTITY.get(), BurningLightBlockEntity::tick);
    }
}
