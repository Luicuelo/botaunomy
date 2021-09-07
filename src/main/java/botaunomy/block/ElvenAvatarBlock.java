/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/




package botaunomy.block;

import java.util.UUID;
import javax.annotation.Nonnull;
import botaunomy.item.RodItem;
import botaunomy.registry.BlockBase;
import botaunomy.registry.TileEntityRegisteredBlocked;
import botaunomy.ModInfo;
import botaunomy.block.tile.TileElvenAvatar;
import botaunomy.ItemStackType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.common.block.tile.TileSimpleInventory;
import vazkii.botania.common.core.helper.InventoryHelper;
import vazkii.botania.common.lexicon.LexiconData;

public class ElvenAvatarBlock extends BlockBase implements ILexiconable,TileEntityRegisteredBlocked,IWandHUD  {
	
	private UUID placerUUID;

	public static final String NAME = "elven_avatar";

	private static final AxisAlignedBB X_AABB = new AxisAlignedBB(.3125, 0, .21875, 1-.3125, 17/16.0, 1-.21875);
	private static final AxisAlignedBB Z_AABB = new AxisAlignedBB(.21875, 0, .3125, 1-.21875, 17/16.0, 1-.3125);
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public ElvenAvatarBlock() {
		super(Material.IRON, NAME);
		setHardness(2.0F);
		setSoundType(SoundType.METAL);
		setDefaultState(blockState.getBaseState().withProperty(BotaniaStateProps.CARDINALS, EnumFacing.NORTH).withProperty(POWERED, false));
		
		this.setUnlocalizedName(ModInfo.modid + "." + NAME);		
	}

	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
		// TODO Auto-generated method stub
		
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		if(avatar != null)
			avatar.renderHUD(mc, res);

		
	}

	/*
	private EnumFacing left(EnumFacing facing) {
		if(facing==EnumFacing.SOUTH) return EnumFacing.WEST;
		if(facing==EnumFacing.WEST) return EnumFacing.NORTH;
		if(facing==EnumFacing.NORTH) return EnumFacing.EAST;
		if(facing==EnumFacing.EAST) return EnumFacing.SOUTH;
		return EnumFacing.DOWN;
	}
   */	

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		
		//EnumFacing facing= blockState.getValue(BotaniaStateProps.CARDINALS);		
		//if (side==left(facing)) //only emits redstone left side
				return (blockState.getValue(POWERED) ? 1 : 0);
		//else return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return this.getWeakPower(blockState, blockAccess, pos, side);		
	}
	
	
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(state.getValue(BotaniaStateProps.CARDINALS).getAxis() == EnumFacing.Axis.X)
			return X_AABB;
		else return Z_AABB;
		
	}

	@Nonnull
	@Override
	public BlockStateContainer createBlockState() {
	
		//return new BlockStateContainer(this, BotaniaStateProps.CARDINALS);
		return new BlockStateContainer(this, new IProperty[] { BotaniaStateProps.CARDINALS,POWERED });
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		
		int meta =state.getValue(BotaniaStateProps.CARDINALS).getIndex();	
		meta <<= 1;						
		meta |= (state.getValue(POWERED) ? 1 : 0);    //One bit
    	return meta;    	    	
	}
	

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		int power = meta & 0b1;
		meta >>= 1;		
		if (meta < 2 || meta > 5) {
			meta = 2;
		}
		return getDefaultState().withProperty(BotaniaStateProps.CARDINALS, EnumFacing.getFront(meta)).withProperty(POWERED, power == 1);
	}

	
	/*
	@Override
	public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		avatar.onWanded(player, stack);			
		return true;
	}*/
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer realPlayer, EnumHand hand, EnumFacing s, float xs, float ys, float zs) {
		
			if (hand == EnumHand.OFF_HAND ) return false;
			if (world.isRemote) return false;
			
			TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
			ItemStack stackOnRealPlayer = realPlayer.getHeldItem(hand);	//getHeldItemMainhand()		
			ItemStackType.Types stackOnRealPlayerType=ItemStackType.getTypeTool(stackOnRealPlayer);
			
			boolean rodWorkOnPlayer= (stackOnRealPlayer.getItem() instanceof RodItem) && stackOnRealPlayerType==ItemStackType.Types.ROD_WORK;		
			boolean wandOnPlayer=stackOnRealPlayer.getUnlocalizedName().equals("item.twigWand"); 
			//boolean rodWillOnAvatar= (stackOnAvatar.getItem() instanceof RodItem) && ItemStackType.getTypeTool(stackOnAvatar)==ItemStackType.Types.ROD_WILL;
			
			if(avatar.getInventory().getType1()==ItemStackType.Types.ROD_WORK) {//rod_work to player				
				ItemHandlerHelper.giveItemToPlayer(realPlayer, avatar.getInventory().take1());
				avatar.markDirty();	
			}else
				if(avatar.getInventory().getType0()!=ItemStackType.Types.NONE && !rodWorkOnPlayer) { //from avatar to player					
					if (!wandOnPlayer) {
						ItemHandlerHelper.giveItemToPlayer(realPlayer, avatar.getInventory().take0());
						return true;
					} else {						
						avatar.onWanded(realPlayer, avatar.getInventory().get0());
					}
					
				} else //from player to avatar					
					if(!stackOnRealPlayer.isEmpty()) 				
					{  																
						boolean dontGive;						
						dontGive=wandOnPlayer; //dont let give botania twigwand or block
						dontGive|=(stackOnRealPlayerType == ItemStackType.Types.BLOCK); //is a block, not tool
						dontGive|=(stackOnRealPlayerType == ItemStackType.Types.NONE);
									
						if (wandOnPlayer) avatar.onWanded(realPlayer, avatar.getInventory().get0());
						if (!dontGive)							
						{																								
							if (rodWorkOnPlayer) {
								if (avatar.getInventory().getType0()==ItemStackType.Types.BREAK) {
									avatar.getInventory().set1(stackOnRealPlayer.splitStack(1));
									//rod_work to left hand , only if tools is break type, use type is always righclick, no need of this rod.		
									avatar.resetBreak();
									avatar.markDirty();
								}else return false;
							}
							else {								
								avatar.getInventory().set0(stackOnRealPlayer.splitStack(1));
								//now  event do this
								//avatar.secuencesAvatar.ActivateSecuence("RiseArm");		
								//avatar.inventoryToFakePlayer();
							}
							avatar.markDirty();
							return true;
						}
					}		
		
		return false;
	}
	
	//player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "->" +nameStackOnPlayer), false);	


	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileSimpleInventory inv = (TileSimpleInventory) world.getTileEntity(pos);
		InventoryHelper.dropInventory(inv, world, state, pos);
		super.breakBlock(world, pos, state);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {	
		if (placer instanceof EntityPlayer){
			placerUUID=EntityPlayer.getUUID(((EntityPlayer)placer).getGameProfile());
		}else {
			placerUUID = UUID.randomUUID();
		}	
		world.setBlockState(pos, state.withProperty(BotaniaStateProps.CARDINALS, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, false));
		//TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);		
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileElvenAvatar(placerUUID);
	}
	
	
	@Override
	public Class<? extends TileEntity> getTileEntityClass(){
		return TileElvenAvatar.class;
	}

	@Nonnull
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.avatar;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		customRegisterModels();
	}


}
