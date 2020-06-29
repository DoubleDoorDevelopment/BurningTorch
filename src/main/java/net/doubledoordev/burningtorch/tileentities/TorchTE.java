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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.doubledoordev.burningtorch.BurningTorchConfig;
import net.doubledoordev.burningtorch.blocks.BlockHolder;
import net.doubledoordev.burningtorch.blocks.BurningTorchBlock;
import net.doubledoordev.burningtorch.util.UtilMethods;

import static net.minecraft.world.GameRules.DO_FIRE_TICK;

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
    public void func_230337_a_(BlockState state, CompoundNBT compound)
    {
        super.func_230337_a_(state, compound);
        decayLevel = compound.getInt("decaylevel");
        decayTimer = compound.getInt("decayTimer");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.putInt("decaylevel", decayLevel);
        compound.putInt("decayTimer", decayTimer);
        return super.write(compound);
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    public int getDecayLevel()
    {
        return this.decayLevel;
    }

    public void setDecayLevel(int decayLevel)
    {
        this.decayLevel = decayLevel;
        this.world.setBlockState(pos, world.getBlockState(pos).with(BurningTorchBlock.DECAY, decayLevel));
    }

    @Override
    public void tick()
    {
        World world = this.world;

        if (world.getBlockState(pos).getBlock() == BlockHolder.burningtorch.getBlock())
        {
            if (decayLevel > 0 && world.getBlockState(pos).get(BurningTorchBlock.LIT))
            {
                rainTimer++;
                decayTimer++;

                // Timer is measuring in ticks! There are 20 ticks in a second!!!!
                if (rainTimer > BurningTorchConfig.GENERAL.rainUpdateRate.get() && BurningTorchConfig.GENERAL.shouldRainExtinguish.get())
                {
                    if (world.isRaining() && world.canBlockSeeSky(pos))
                    {
                        world.setBlockState(pos, world.getBlockState(pos)
                                .with(BurningTorchBlock.LIT, false));
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
                        world.setBlockState(pos, world.getBlockState(pos)
                                .with(BurningTorchBlock.DECAY, decayLevel));
                        updateBlock();
                        decayTimer = 0;
                    }
                }
            }
            else if (decayLevel == 0)
            {
                world.removeTileEntity(pos);
                world.removeBlock(pos, false);
                decayTimer = 0;
            }
        }
        else
        {
            world.removeTileEntity(pos);
        }

        //Fire handling from here.

        Random random = new Random();
        BlockPos firePos = pos;

        // Check the game rules for fire ticks.
        if (world.getGameRules().getBoolean(DO_FIRE_TICK) && world.getBlockState(pos).getBlock() == BlockHolder.burningtorch.getBlock())
        {
            // Make sure the area is loaded.
            if (!world.isAreaLoaded(firePos, 2) && world.getBlockState(pos).get(BurningTorchBlock.LIT) && BurningTorchConfig.GENERAL.torchesStartFireWhenLit.get())
                return;

            // Random int
            int randomInt = random.nextInt(101);
            // Count ticks.
            tickCounter++;

            // if the random int is greater than the config.
            if (randomInt < BurningTorchConfig.GENERAL.percentToStartFire.get() && tickCounter == BurningTorchConfig.GENERAL.delayBetweenFire.get())
            {
                IWorld iWorld = world.getWorld();
                // find a random spot.
                firePos = firePos.add(random.nextInt(3) - 1, random.nextInt(1), random.nextInt(3) - 1);

                BlockState fireState = AbstractFireBlock.func_235326_a_(world, pos);

                if (firePos.getY() >= 0 && firePos.getY() < world.getHeight())
                {
                    // Check the space around us for a burnable block.
                    if (UtilMethods.isSurroundingBlockFlammable(world, firePos))
                    {
                        if (fireState.isValidPosition(iWorld, firePos))
                        {
                            // set it on fire.
                            world.setBlockState(firePos, fireState, 11);
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
        deserializeNBT(pkt.getNbtCompound());
        updateBlock();
    }

    private void updateBlock()
    {
        //this.world.markForRerender(pos);
        markDirty();
    }

}

