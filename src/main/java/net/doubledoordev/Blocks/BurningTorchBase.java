package net.doubledoordev.Blocks;

import net.doubledoordev.TileEntities.TorchTE;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

import static net.doubledoordev.BurningTorch.MOD_ID;


public class BurningTorchBase extends Block
{
    public static final PropertyDirection DIRECTION = PropertyDirection.create("direction");
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyInteger DECAY = PropertyInteger.create("decay",0, 5);
    //TODO: Clean up the bounding boxes more as some are a bit goofy.
    private static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(     0.4000000059604645D, 0.0D,                 0.4000000059604645D, 0.6000000238418579D,  0.8000000238418579D, 0.6000000238418579D);
    private static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(  0.3499999940395355D, 0.20000000298023224D, 0.599999988079071D,  0.599999761581421D,   1.000100011920929D,  1.0D);
    private static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(  0.3499999940395355D, 0.20000000298023224D, 0.0D,                0.6499999761581421D,  1.000100011920929D,  0.40000001192092896D);
    private static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(   0.599999988079071D,  0.20000000298023224D, 0.3499999940395355D, 1.0D,                 1.000100011920929D,  0.6499999761581421D);
    private static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(   0.0D,                0.20000000298023224D, 0.3499999940395355D, 0.40000001192092896D, 1.000100011920929D,  0.6499999761581421D);

    //TODO: Decay isn't saved in the TE.
    //TODO: Decay wall rendering is missing.
    //TODO: Configs options are missing.
    //TODO: Torches when destroyed need to drop special stuff.
    //TODO: Adding burnables to the torch to add torch life needs to be done.
    public BurningTorchBase(Material materialIn)
    {
        super(materialIn);
        this.setDefaultState(this.getDefaultState().withProperty(LIT, true).withProperty(DIRECTION, EnumFacing.UP).withProperty(DECAY, 5));
        this.setLightLevel(0.9375f);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setUnlocalizedName("burningtorch");
        this.setRegistryName(MOD_ID, "burningtorch");
    }

    // Handles the relighting of torches.
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (playerIn.getHeldItemMainhand().getItem() == Items.FLINT_AND_STEEL || playerIn.getHeldItemOffhand().getItem() == Items.FLINT_AND_STEEL && !state.getValue(LIT))
        {
            worldIn.setBlockState(pos, Block.getBlockFromName(MOD_ID + ":burningtorch").getDefaultState()
                    .withProperty(LIT, true)
                    .withProperty(DIRECTION, state.getValue(DIRECTION))
                    .withProperty(DECAY, state.getValue(DECAY)));
            return true;
        }
        return false;
    }

    // Changes the lighting level based off the LIT blockstate property.
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if (!state.getValue(LIT))
        {
            return 0;
        }
        return 14;
    }

    // Set the render layer, CUTOUT is required or you get stupid looking torches...
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    // Sets the collision box, as this is a torch we don't want people to get stuck on it NULL_AABB is used...
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    // Switch statement that handles the bounding boxes for each direction.
    @Override
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

    // Does stuff, Check the super...
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    // Does stuff, Check the super...
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    // Does stuff, Check the super...
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, DIRECTION, LIT, DECAY);
    }

    // Does stuff....
    @Override
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

    // Does stuff....
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.onNeighborChangeInternal(worldIn, pos, state);
    }

    // Does stuff....
    private boolean onNeighborChangeInternal(World worldIn, BlockPos pos, IBlockState state)
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

    // Handles the effects on the top of the torch.
    //TODO: Could use some perfecting on the wall torchs as its a weeeee bit off.
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        boolean lit = stateIn.getValue(LIT);
        EnumFacing enumfacing = stateIn.getValue(DIRECTION);
        int decay = stateIn.getValue(DECAY);

        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.9D;
        double d2 = (double)pos.getZ() + 0.5D;

        switch (decay)
        {
            case 5:
                d1 = (double)pos.getY() + 0.9D;
                break;
            case 4:
                d1 = (double)pos.getY() + 0.85D;
                break;
            case 3:
                d1 = (double)pos.getY() + 0.79D;
                break;
            case 2:
                d1 = (double)pos.getY() + 0.70D;
                break;
            case 1:
                d1 = (double)pos.getY() + 0.45D;
                break;
            case 0:
                d1 = (double)pos.getY() + 0.35D;
                break;

        }

        if (lit)
        {
            if (enumfacing.getAxis().isHorizontal())
            {
                EnumFacing enumfacing1 = enumfacing.getOpposite();
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.27D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.22D, d2 + 0.27D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.27D * (double) enumfacing1.getFrontOffsetX(), d1 + 0.22D, d2 + 0.27D * (double) enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
            }
            else
            {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    // Does stuff....
    private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state)
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

    // Does stuff....
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

    // Does stuff....
    private boolean canPlaceOn(World worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock().canPlaceTorchOnTop(state, worldIn, pos);
    }

    // Part of attaching the TE to the block.
    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TorchTE();
    }

    // Part of attaching the TE to the block.
    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    //TODO: This will be used to handle the rendering for the decay levels
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState blockState = state;

        if (state.getValue(LIT))
        {
            switch (state.getValue(DECAY))
            {
                case 5:
                switch (state.getValue(DIRECTION))
                {
                    case NORTH:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    case EAST:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    case WEST:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    case SOUTH:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    case UP:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    case DOWN:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                    default:
                        blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 5);
                        break;
                }
                break;

                case 4:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 4);
                            break;
                    }
                    break;

                case 3:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 3);
                            break;
                    }
                    break;

                case 2:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 2);
                            break;
                    }
                    break;

                case 1:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 1);
                            break;
                    }
                    break;

                case 0:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, true).withProperty(DECAY, 0);
                            break;
                    }
                    break;
            }
        }
        else
            switch (state.getValue(DECAY))
            {
                case 5:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 5);
                            break;
                    }
                    break;

                case 4:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 4);
                            break;
                    }
                    break;

                case 3:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 3);
                            break;
                    }
                    break;

                case 2:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 2);
                            break;
                    }
                    break;

                case 1:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 1);
                            break;
                    }
                    break;

                case 0:
                    switch (state.getValue(DIRECTION))
                    {
                        case NORTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        case EAST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.EAST).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        case WEST:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.WEST).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        case SOUTH:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.SOUTH).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        case UP:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.UP).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        case DOWN:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.DOWN).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                        default:
                            blockState = blockState.withProperty(DIRECTION, EnumFacing.NORTH).withProperty(LIT, false).withProperty(DECAY, 0);
                            break;
                    }
                    break;
            }

        return blockState;

    }

    // Self explanatory.
    @Override
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

    // Self explanatory.
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

