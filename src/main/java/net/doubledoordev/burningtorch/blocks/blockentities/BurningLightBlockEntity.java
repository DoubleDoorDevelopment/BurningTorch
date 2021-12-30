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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.BlockRegistry;
import net.doubledoordev.burningtorch.blocks.TorchBlock;
import net.doubledoordev.burningtorch.util.Util;

public class BurningLightBlockEntity extends BlockEntity
{
    public static void tick(Level level, BlockPos pos, BlockState state, BurningLightBlockEntity burningLightBlockEntity)
    {
        if (level != null && !level.isClientSide())

            if (state.getValue(BlockStateProperties.LIT))
            {
                if (state.getBlock() != BlockRegistry.BURNING_PUMPKIN.get())
                {
                    burningLightBlockEntity.handleRain(level, pos, state);
                    burningLightBlockEntity.startFires(level, pos, state);
                }
                burningLightBlockEntity.decayBlock(level, pos, state);
            }
    }

    int decayLevel;
    int rainTimer;
    int decayTimer;
    int tickCounter;

    public BurningLightBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockRegistry.BURNING_LIGHT_BLOCK_ENTITY.get(), pos, state);

        if (getBlockState().getBlock() == BlockRegistry.BURNING_TORCH.get())
            decayLevel = BurningTorchConfig.GENERAL.torchStartingDecayLevel.get();
        else
            decayLevel = BurningTorchConfig.GENERAL.pumpkinStartingDecayLevel.get();
    }

    @ParametersAreNonnullByDefault
    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        decayLevel = compound.getInt("decaylevel");
        decayTimer = compound.getInt("decayTimer");
    }

    @ParametersAreNonnullByDefault
    @Override
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("decaylevel", decayLevel);
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
        return this.save(new CompoundTag());
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        if (level != null)
            this.level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(TorchBlock.DECAY, decayLevel));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }

    private void handleRain(Level level, BlockPos pos, BlockState state)
    {
        // Timer is measuring in ticks! There are 20 ticks in a second!!!!
        if (rainTimer > BurningTorchConfig.GENERAL.rainUpdateRate.get() && BurningTorchConfig.GENERAL.shouldRainExtinguish.get())
        {
            if (level.isRaining() && level.canSeeSky(worldPosition))
            {
                level.setBlockAndUpdate(pos, state
                        .setValue(BlockStateProperties.LIT, false));
                rainTimer = 0;
            }
        }
    }

    private void decayBlock(Level level, BlockPos pos, BlockState state)
    {
        if (decayLevel > 0)
        {
            rainTimer++;
            decayTimer++;

            // Timer is measuring in ticks! There are 20 ticks in a second!!!!
            if (decayTimer > BurningTorchConfig.GENERAL.decayRate.get())
            {
                if (decayLevel > 0)
                {
                    decayLevel = decayLevel - 1;
                    level.setBlockAndUpdate(pos, state
                            .setValue(BlockStateProperties.LIT, false));
                    decayTimer = 0;
                }
            }
        }
        else if (decayLevel == 0)
        {
            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);
            decayTimer = 0;
        }
        else
        {
            level.removeBlockEntity(pos);
        }
    }

    private void startFires(Level level, BlockPos pos, BlockState state)
    {
        Random random = new Random();
        BlockPos firePos = pos;

        // Check the game rules for fire ticks.
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK))
        {
            // Make sure the area is loaded.
            if (!level.isAreaLoaded(firePos, 2) && state.getValue(BlockStateProperties.LIT) && BurningTorchConfig.GENERAL.torchesStartFireWhenLit.get())
                return;

            // Random int
            int randomInt = random.nextInt(101);
            // Count ticks.
            tickCounter++;

            // if the random int is greater than the config.
            if (randomInt < BurningTorchConfig.GENERAL.percentToStartFire.get() && tickCounter == BurningTorchConfig.GENERAL.delayBetweenFire.get())
            {
                // find a random spot.
                firePos = firePos.offset(random.nextInt(3) - 1, random.nextInt(1), random.nextInt(3) - 1);

                BlockState fireState = BaseFireBlock.getState(level, pos);

                if (firePos.getY() >= 0 && firePos.getY() < level.getHeight())
                {
                    // Check the space around us for a burnable block.
                    if (Util.isSurroundingBlockFlammable(level, firePos))
                    {
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
    }
}
