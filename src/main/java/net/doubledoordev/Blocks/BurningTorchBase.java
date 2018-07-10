package net.doubledoordev.Blocks;

import net.doubledoordev.BurningTorch;
import net.doubledoordev.TileEntities.TorchTE;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;


public class BurningTorchBase extends Block
{
    static final PropertyDirection DIRECTION = PropertyDirection.create("direction");
    static final PropertyBool LIT = PropertyBool.create("lit");
    protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.4000000059604645D, 0.0D, 0.4000000059604645D, 0.6000000238418579D, 0.6000000238418579D, 0.6000000238418579D);
    protected static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.699999988079071D, 0.6499999761581421D, 0.800000011920929D, 1.0D);
    protected static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.0D, 0.6499999761581421D, 0.800000011920929D, 0.30000001192092896D);
    protected static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(0.699999988079071D, 0.20000000298023224D, 0.3499999940395355D, 1.0D, 0.800000011920929D, 0.6499999761581421D);
    protected static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(0.0D, 0.20000000298023224D, 0.3499999940395355D, 0.30000001192092896D, 0.800000011920929D, 0.6499999761581421D);


    public BurningTorchBase(Material materialIn)
    {
        super(materialIn);
        this.setDefaultState(this.getDefaultState().withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setUnlocalizedName("burningtorch");
        this.setRegistryName(BurningTorch.MOD_ID, "burningtorch");
    }

    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        switch (state.getValue(DIRECTION))
        {
            case EAST:
                return TORCH_EAST_AABB;
            case WEST:
                return TORCH_WEST_AABB;
            case SOUTH:
                return TORCH_SOUTH_AABB;
            case NORTH:
                return TORCH_NORTH_AABB;
            default:
                return STANDING_AABB;
        }
    }

    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    private int getCoalAgeIncrease(World worldin)
    {
        return MathHelper.getInt(worldin.rand, 1, 5);
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, LIT, DIRECTION);
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        if (this.canPlaceAt(worldIn, pos, facing))
        {
            return this.getDefaultState().withProperty(DIRECTION, facing);
        }
        else
        {
            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (this.canPlaceAt(worldIn, pos, enumfacing))
                {
                    return this.getDefaultState().withProperty(DIRECTION, enumfacing);
                }
            }

            return this.getDefaultState();
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.onNeighborChangeInternal(worldIn, pos, state);
    }

    protected boolean onNeighborChangeInternal(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!this.checkForDrop(worldIn, pos, state))
        {
            return true;
        }
        else
        {
            EnumFacing enumfacing = state.getValue(DIRECTION);
            EnumFacing.Axis enumfacing$axis = enumfacing.getAxis();
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            BlockPos blockpos = pos.offset(enumfacing1);
            boolean flag = false;

            if (enumfacing$axis.isHorizontal() && worldIn.getBlockState(blockpos).getBlockFaceShape(worldIn, blockpos, enumfacing) != BlockFaceShape.SOLID)
            {
                flag = true;
            }
            else if (enumfacing$axis.isVertical() && !this.canPlaceOn(worldIn, blockpos))
            {
                flag = true;
            }

            if (flag)
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        EnumFacing enumfacing = stateIn.getValue(DIRECTION);
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;

        if (enumfacing.getAxis().isHorizontal())
        {
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.27D * (double)enumfacing1.getFrontOffsetX(), d1 + 0.22D, d2 + 0.27D * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.27D * (double)enumfacing1.getFrontOffsetX(), d1 + 0.22D, d2 + 0.27D * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
        }
        else
        {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    protected boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state)
    {
        if (state.getBlock() == this && this.canPlaceAt(worldIn, pos, state.getValue(DIRECTION)))
        {
            return true;
        }
        else
        {
            if (worldIn.getBlockState(pos).getBlock() == this)
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }

            return false;
        }
    }

    private boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing)
    {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, blockpos, facing);

        if (facing.equals(EnumFacing.UP) && this.canPlaceOn(worldIn, blockpos))
        {
            return true;
        }
        else if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
        {
            return !isExceptBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID;
        }
        else
        {
            return false;
        }
    }

    private boolean canPlaceOn(World worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock().canPlaceTorchOnTop(state, worldIn, pos);
    }

    public void decay(World worldin, BlockPos pos, IBlockState state)
    {
        /*
        int level = this.getAge(state) + getCoalAgeIncrease(worldin);
        int max = this.getMaxAge();

        if (level > max)
        {
            level = max;
        }
        worldin.setBlockState(pos, this.withAge(level));
        */
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TorchTE();
    }

    private TorchTE getTE(World world, BlockPos pos) {
        return (TorchTE) world.getTileEntity(pos);
    }
    public int getMetaFromState(IBlockState state)
    {
        int d = 0;

        if (state.getValue(LIT))
        {
            switch (state.getValue(DIRECTION))
            {
                case NORTH:
                    d = d | 1;
                    break;
                case EAST:
                    d = d | 2;
                    break;
                case WEST:
                    d = d | 3;
                    break;
                case SOUTH:
                    d = d | 4;
                    break;
                case UP:
                    d = d | 5;
                    break;
                case DOWN:
                    d = d | 6;
                    break;
                default:
                    d = d | 5;
                    break;
            }
        }
        else
        switch (state.getValue(DIRECTION))
        {
            case NORTH:
                d = d | 7;
                break;
            case EAST:
                d = d | 8;
                break;
            case WEST:
                d = d | 9;
                break;
            case SOUTH:
                d = d | 10;
                break;
            case UP:
                d = d | 11;
                break;
            case DOWN:
                d = d | 12;
                break;
            default:
                d = d | 11;
                break;
        }

        return d;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState blockState = this.getDefaultState();

        switch (meta)
        {
            case 1:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.NORTH);
                break;
            case 2:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.EAST);
                break;
            case 3:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.WEST);
                break;
            case 4:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.SOUTH);
                break;
            case 5:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP);
                break;
            case 6:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.DOWN);
                break;
            case 7:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.NORTH);
                break;
            case 8:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.EAST);
                break;
            case 9:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.WEST);
                break;
            case 10:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.SOUTH);
                break;
            case 11:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.UP);
                break;
            case 12:
                blockState = blockState.withProperty(LIT, false).withProperty(DIRECTION, EnumFacing.DOWN);
                break;
            default:
                blockState = blockState.withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP);
                break;
        }

        return blockState;
    }

}

