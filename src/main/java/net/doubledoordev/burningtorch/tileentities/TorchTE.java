package net.doubledoordev.burningtorch.tileentities;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.BlockHolder;
import net.doubledoordev.burningtorch.blocks.BurningTorchBlock;
import net.doubledoordev.burningtorch.util.UtilMethods;

public class TorchTE extends TileEntity implements ITickableTileEntity
{
    int decayLevel = BurningTorchConfig.GENERAL.torchStartingDecayLevel.get();
    int rainTimer;
    int decayTimer;
    int tickCounter;

    private TorchTE(TileEntityType<?> p_i49963_1_)
    {
        super(p_i49963_1_);
    }

    public TorchTE()
    {
        this(TEHolder.torchte);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound)
    {
        super.load(state, compound);
        decayLevel = compound.getInt("decaylevel");
        decayTimer = compound.getInt("decayTimer");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound)
    {
        compound.putInt("decaylevel", decayLevel);
        compound.putInt("decayTimer", decayTimer);
        return super.save(compound);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        this.level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(BurningTorchBlock.DECAY, decayLevel));
    }

    @Override
    public void tick()
    {
        World world = this.level;

        if (world.getBlockState(worldPosition).getBlock() == BlockHolder.burningtorch.getBlock())
        {
            if (decayLevel > 0 && world.getBlockState(worldPosition).getValue(BurningTorchBlock.LIT))
            {
                rainTimer++;
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (rainTimer > BurningTorchConfig.GENERAL.rainUpdateRate.get() && BurningTorchConfig.GENERAL.shouldRainExtinguish.get())
                {
                    if (world.isRaining() && world.canSeeSky(worldPosition))
                    {
                        world.setBlockAndUpdate(worldPosition, world.getBlockState(worldPosition)
                                .setValue(BurningTorchBlock.LIT, false));
                        updateBlock();
                        rainTimer = 0;
                    }
                }

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (decayTimer > BurningTorchConfig.GENERAL.decayRate.get())
                {
                    if (decayLevel > 0)
                    {
                        this.decayLevel = decayLevel - 1;
                        world.setBlockAndUpdate(worldPosition, world.getBlockState(worldPosition)
                                .setValue(BurningTorchBlock.DECAY, decayLevel));
                        updateBlock();
                        decayTimer = 0;
                    }
                }
            }
            else if (decayLevel == 0)
            {
                world.removeBlockEntity(worldPosition);
                world.removeBlock(worldPosition, false);
                decayTimer = 0;
            }
        }
        else
        {
            world.removeBlockEntity(worldPosition);
        }

        //Fire handling from here.

        Random random = new Random();
        BlockPos firePos = worldPosition;

        // Check the game rules for fire ticks.
        if (world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK) && world.getBlockState(worldPosition).getBlock() == BlockHolder.burningtorch.getBlock())
        {
            // Make sure the area is loaded.
            if (!world.isAreaLoaded(firePos, 2) && world.getBlockState(worldPosition).getValue(BurningTorchBlock.LIT) && BurningTorchConfig.GENERAL.torchesStartFireWhenLit.get())
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

                BlockState fireState = AbstractFireBlock.getState(world, worldPosition);

                if (firePos.getY() >= 0 && firePos.getY() < world.getHeight())
                {
                    // Check the space around us for a burnable block.
                    if (UtilMethods.isSurroundingBlockFlammable(world, firePos))
                    {
                        if (fireState.canSurvive(world, firePos))
                        {
                            // set it on fire.
                            world.setBlock(firePos, fireState, 11);
                        }
                    }
                }
                tickCounter = 0;
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        updateBlock();
    }

    private void updateBlock()
    {
        setChanged();
    }

}

