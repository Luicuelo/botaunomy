/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/




package botaunomy.block.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import botaunomy.ItemStackType;
import botaunomy.ModInfo;
import botaunomy.item.RodItem;
import botaunomy.registry.BlockBase;
import botaunomy.registry.TileEntityRegisteredBlocked;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.common.core.helper.InventoryHelper;
import vazkii.botania.common.lexicon.LexiconData;

public class ElvenAvatarBlock extends BlockBase implements ILexiconable,TileEntityRegisteredBlocked,IWandHUD  {
	
	//private UUID placerUUID;

	public static final String NAME = "elven_avatar";

	private static final AxisAlignedBB X_AABB = new AxisAlignedBB(.3125, 0, .21875, 1-.3125, 17/16.0, 1-.21875);
	private static final AxisAlignedBB Z_AABB = new AxisAlignedBB(.21875, 0, .3125, 1-.21875, 17/16.0, 1-.3125);
	public static final PropertyBool POWERED = PropertyBool.create("powered");


	public ElvenAvatarBlock() {
		super(Material.IRON, NAME);
		
        setTickRandomly(false);
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
		
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		if(avatar != null)
			avatar.renderHUD(mc, res);

		
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos){
		
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		//int signal =(int) Math.floor(((double)avatar.getCurrentMana()/(double)TileElvenAvatar.MAX_MANA)*15d);
		int signal =(int) Math.ceil(Math.floor(((double)avatar.getCurrentMana()/(double)TileElvenAvatar.MAX_MANA)*(15d*TileElvenAvatar.MANA_MIN_DIVISION))/TileElvenAvatar.MANA_MIN_DIVISION);
		//returns 1 over 208.
		return signal;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		
		return (blockState.getValue(POWERED) ? 1 : 0);

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
			ArrayList<ItemStackType.Types>  stackOnRealPlayerType=ItemStackType.getTypeTool(stackOnRealPlayer);
			
			boolean rodWorkOnPlayer= (stackOnRealPlayer.getItem() instanceof RodItem) && ItemStackType.isStackType(stackOnRealPlayerType,ItemStackType.Types.ROD_WORK);		
			boolean wandOnPlayer=stackOnRealPlayer.getUnlocalizedName().equals("item.twigWand"); 
			boolean avatarEmpy=avatar.getInventory().getType0().get(0)==ItemStackType.Types.NONE;
			
			//boolean rodWillOnAvatar= (stackOnAvatar.getItem() instanceof RodItem) && ItemStackType.getTypeTool(stackOnAvatar)==ItemStackType.Types.ROD_WILL;
			
			if(
					(ItemStackType.isStackType(avatar.getInventory().getType1() , ItemStackType.Types.ROD_WORK))
					&&
					!(avatarEmpy&&!stackOnRealPlayer.isEmpty()) //if avatar right hand is empty only gives work rod if player is empty
			)			
			{//rod_work to player				
				ItemHandlerHelper.giveItemToPlayer(realPlayer, avatar.getInventory().take1());
				avatar.markDirty();	
			}else
				if(!avatarEmpy && !rodWorkOnPlayer) { //from avatar to player					
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
						dontGive|=(ItemStackType.isStackType(stackOnRealPlayerType , ItemStackType.Types.BLOCK)); //is a block, not tool
						dontGive|=(ItemStackType.isStackType(stackOnRealPlayerType , ItemStackType.Types.NONE));
						
						//No dar Dissolvable si esta lleno
						dontGive|=(
								ItemStackType.isStackType(stackOnRealPlayerType , ItemStackType.Types.MANA)&&								
									((stackOnRealPlayer.getItem() instanceof IManaDissolvable) &&
										!(avatar.wandManaToTablet) &&
										avatar.isFull()
									)
								);
									
						if (wandOnPlayer) avatar.onWanded(realPlayer, avatar.getInventory().get0());
						if (!dontGive)							
						{																								
							if (rodWorkOnPlayer) {
								boolean itemBreak=ItemStackType.isStackType( avatar.getInventory().getType0(),ItemStackType.Types.BREAK);
								boolean itemRodWill=ItemStackType.isStackType( avatar.getInventory().getType0(),ItemStackType.Types.ROD_WILL);																
								if (itemBreak||itemRodWill||avatarEmpy) {
									avatar.getInventory().set1(stackOnRealPlayer.splitStack(1));
									//rod_work to left hand , only if tools is break type, use type is always righclick, no need of this rod.		
									avatar.resetBreak();
									avatar.markDirty();
								}else return false;
							}
							else {
								boolean itemCaster=ItemStackType.isStackType(stackOnRealPlayerType,ItemStackType.Types.CASTER);
								if (itemCaster) avatar.setOwner(realPlayer);
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
	    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess iblockAccess, BlockPos pos, IBlockState state, int fortune) {

		World world=(World)iblockAccess;
		if (world.isRemote)return;    	  
		if (drops==null)return;
		drops.clear();
		
    	ItemStack drop =new ItemStack(this);
    	TileEntity tile = world.getTileEntity(pos);	    	    	
    	if (tile!=null && tile instanceof TileElvenAvatar) {
    		TileElvenAvatar tileAvatar = (TileElvenAvatar) tile;
    		int mana=tileAvatar.getCurrentMana();
    		if(mana>0) {    			
    			NBTTagCompound stackTags=new NBTTagCompound();
    			stackTags.setInteger("mana", mana);    	
    			drop.setTagCompound(stackTags);    		
    		}    		
    	}   
    	drops.add(drop);
	}
    

	
    @Override //tinker
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        // If it will harvest, delay deletion of the block until after getDrops
        if(willHarvest && !player.capabilities.isCreativeMode) return true;
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    
	@Override // tinker
	public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state,@Nullable final TileEntity te, final ItemStack stack) {
		super.harvestBlock(world, player, pos, state, te, stack);
		this.onBlockHarvested(world, pos, state, player);
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		if (avatar != null) {
			InventoryHelper.dropInventory(avatar, world, state, pos);
			avatar.onBreak();
			world.removeTileEntity(pos);
			world.setBlockToAir(pos);
		}
	}
	

	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(pos);
		if (avatar != null) {
			InventoryHelper.dropInventory(avatar, world, state, pos);
			avatar.onBreak();
		}
		super.breakBlock(world, pos, state);
	}
	

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {	
		/*
		if (placer instanceof EntityPlayer){
			placerUUID=EntityPlayer.getUUID(((EntityPlayer)placer).getGameProfile());
		*/
		world.setBlockState(pos, state.withProperty(BotaniaStateProps.CARDINALS, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, false));			
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
		return new TileElvenAvatar();
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

	

	public void onBlockPlaced(World world, BlockPos pos, ItemStack itemStackUsed) {
		if (world.isRemote) return;

		if (itemStackUsed.hasTagCompound()) {
			NBTTagCompound tag = itemStackUsed.getTagCompound();
			if (tag != null) {
				int mana=tag.getInteger("mana");
				if (mana >0) {
					TileEntity tile = world.getTileEntity(pos);
					if (tile != null && tile instanceof TileElvenAvatar) {
						TileElvenAvatar tileAvatar = (TileElvenAvatar) tile;
						tileAvatar.setCurrentMana(mana);
					}
				}
			}
		}
	}
	

}
