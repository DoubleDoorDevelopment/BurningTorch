package net.doubledoordev.burningtorch.blocks.blockentities;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.RegistryObject;

import net.doubledoordev.burningtorch.blocks.SimpleBurningBlock;
import net.doubledoordev.burningtorch.util.Util;

public class BurningLightBlockEntity extends BlockEntity
{
    int rainTimer;
    int decayTimer;
    int tickCounter;

    public static void tick(Level level, BlockPos pos, BlockState state, BurningLightBlockEntity burningLightBlockEntity)
    {
        if (level != null && !level.isClientSide())
        {
            Block block = state.getBlock();

            if (state.getValue(BlockStateProperties.LIT) && block instanceof SimpleBurningBlock)
            {
                ((SimpleBurningBlock) block).doLifeCycleTick(level, pos, state, burningLightBlockEntity);
            }
        }
    }

    public BurningLightBlockEntity(RegistryObject<BlockEntityType<BurningLightBlockEntity>> burningBlockEntityType, BlockPos pos, BlockState state)
    {
        super(burningBlockEntityType.get(), pos, state);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        decayTimer = compound.getInt("decayTimer");
    }

    @ParametersAreNonnullByDefault
    @Override
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("decayTimer", decayTimer);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }

    public void handleRain(ForgeConfigSpec.BooleanValue rainExtinguishes, ForgeConfigSpec.IntValue rainUpdateRate, Level level, BlockPos pos, BlockState state)
    {
        if (rainExtinguishes.get())
        {
            if (level.isRainingAt(pos))
                rainTimer++;

            // Timer is measuring in ticks! There are 20 ticks in a second!!!!
            if (rainTimer > rainUpdateRate.get())
            {
                if (level.isRaining() && level.canSeeSky(worldPosition))
                {
                    level.setBlockAndUpdate(pos, state
                            .setValue(BlockStateProperties.LIT, false));
                    rainTimer = 0;
                }
            }
        }
    }

    public void decayBlock(ForgeConfigSpec.IntValue decayRate, Level level, BlockPos pos, BlockState state)
    {
        if (state.getValue(Util.DECAY) > 0)
        {
            decayTimer++;

            // Timer is measuring in ticks! There are 20 ticks in a second!!!!
            if (decayTimer > decayRate.get())
            {
                if (state.getValue(Util.DECAY) > 0)
                {
                    level.setBlockAndUpdate(pos, state.setValue(Util.DECAY, state.getValue(Util.DECAY) - 1));
                    decayTimer = 0;
                }
            }
        }
        else if (state.getValue(Util.DECAY) == 0)
        {
            level.removeBlockEntity(pos);

            BlockState replacementBlock = ((SimpleBurningBlock) state.getBlock()).getExpiredBlockStateReplacement();

            if (replacementBlock.hasProperty(BlockStateProperties.FACING))
                level.setBlockAndUpdate(pos, replacementBlock.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING)));
            else if (replacementBlock.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                level.setBlockAndUpdate(pos, replacementBlock.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.FACING)));
            else
                level.setBlockAndUpdate(pos, replacementBlock);
        }
    }

    @SuppressWarnings("deprecation")
    public void startFires(ForgeConfigSpec.IntValue percentForFire, ForgeConfigSpec.IntValue delayBetweenFire, Level level, BlockPos pos, BlockState state)
    {
        int fireStartPercent = percentForFire.get();

        // Only try to start fires if fire tick is on and the chance to start a fire is greater than 0.
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK) && fireStartPercent > 0)
        {
            // Then make sure the cool down has elapsed.
            if (tickCounter > delayBetweenFire.get())
            {
                Random random = level.random;
                BlockPos firePos = pos;

                // Make sure the area is loaded.
                if (!level.isAreaLoaded(firePos, 2) && state.getValue(BlockStateProperties.LIT))
                    return;

                // Random chance for the fire to be spawned.
                int randomInt = random.nextInt(101);

                // Check our chance to start a fire.
                if (randomInt > fireStartPercent)
                {
                    // Find a spot around the block in a 3 wide by 3 long 2 tall cube centered on the torch. 3 - 1 is required for the negative direction.
                    firePos = firePos.offset(random.nextInt(3) - 1, random.nextInt(1), random.nextInt(3) - 1);

                    if (level.isInWorldBounds(firePos))
                    {
                        // Check the space around us for a burnable block.
                        if (Util.isSurroundingBlockFlammable(level, firePos))
                        {
                            BlockState fireState = BaseFireBlock.getState(level, pos);
                            if (fireState.canSurvive(level, firePos))
                            {
                                // set it on fire.
                                level.setBlock(firePos, fireState, 11);
                            }
                        }
                    }
                    tickCounter = 0;
                }
            }
            else tickCounter++;
        }
    }
}
