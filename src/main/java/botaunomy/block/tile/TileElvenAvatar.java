/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 * 
 * 
 * FakePlayer code from Thaumic-Tinkerer
 * https://github.com/Thaumic-Tinkerer/ThaumicTinkerer
 * Katrina Knight
 * KatrinaAS
 ******************************************************************************/

//TODO
//server fails


package botaunomy.block.tile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;
import com.google.common.base.Predicates;
import botaunomy.ItemStackType;
import botaunomy.ItemStackType.Types;
import botaunomy.client.render.SecuencesAvatar;
import botaunomy.config.Config;
import botaunomy.model.ModelAvatar;
import botaunomy.network.MessageEnabled;
import botaunomy.network.MessageMana;
import botaunomy.network.MessageMoveArm;
import botaunomy.network.MessageSpectator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IAvatarTile;
import vazkii.botania.api.item.IAvatarWieldable;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.tile.TileSimpleInventory;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.entity.EntitySpark;
import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.common.item.ModItems;

public class TileElvenAvatar extends TileSimpleInventory implements IAvatarTile , ITickable ,IElvenAvatarItemHadlerChangedListener,IManaPool,ISparkAttachable {

	private static final int CRAFT_EFFECT_EVENT = 0;
	private static final int CHARGE_EFFECT_EVENT = 1;

	
	public  SecuencesAvatar secuencesAvatar=new SecuencesAvatar();
	private float[][] anglePoints= new float[ModelAvatar.NARC][ModelAvatar.NPOINTS];

	
	public static final int POINTS_SEQUENCE_DURATION = 125;	
	public static final int MAX_MANA = 100000;// 1/5 ManaTablet
	private static final int AVATAR_TICK=20;
	private static final int TABLET_BURST = 5000;
	public static final double MANA_MIN_DIVISION=32d; //division to set 1 to the comparator meter.
	public static final double MIN_MANA=MAX_MANA/(MANA_MIN_DIVISION*15d);
	
	protected static final String TAG_ENABLED = "enabled";
	protected static final String TAG_TICKS_ELAPSED = "ticksElapsed";
	protected static final String TAG_MANA = "mana";
	protected static final String TAG_WAND = "wamd";
	protected static final String TAG_UUID = "uuid";
	protected static final String TAG_SPECT = "spectator";

	protected boolean enabled=true;
	private int ticksElapsed; //do not enter if starts disabled
	private int ticksElapsedDisabled=0;
	private int manaAvatar;
	
	public static int nAvatarServer;
	public static int nAvatarClient;
	private TitleElvenAvatar_FakePlayerHelper fakePlayerHelper;
	private boolean wandManaToTablet=false;
	private UUID playerUUID=null;
	public boolean playerIsSpectator=false;
	
	private boolean haveSpark=false;
	
	public TileElvenAvatar() {	
		super();
		fakePlayerHelper= new TitleElvenAvatar_FakePlayerHelper(this);
	}
	
	public UUID getUUID() {
		return playerUUID;
	}
	
	public void setUUID(UUID uuid) {
		playerUUID=uuid;
	}
	
	public void setClientUUID(UUID uuid) {
		 if (!world.isRemote)return;
		 if (this.playerUUID==null) {
			 this.playerUUID=uuid;
			 this.fakePlayerHelper.elvenFakePlayer.initClient(world, pos, this);
		 }
		 this.playerUUID=uuid;
	}

	public void onBreak() {
		this.fakePlayerHelper.elvenFakePlayer.removePlayer(world, this);
	}
	
	private TileEntityChest findChest() {
		TileEntity chest=null; 
		for(EnumFacing dir : EnumFacing.VALUES) {// EnumFacing.HORIZONTALS
			chest =  world.getTileEntity(pos.offset(dir));
			if (chest!=null && chest instanceof TileEntityChest) {
				return (TileEntityChest)chest;
			}
		}
		return null;
	}
	
	private boolean insertInChest (ItemStack[] refItemStackIn, TileEntityChest chest) {

		 ItemStack itemStack=null;
		 ItemStack itemStackIn=refItemStackIn[0];
		 
		 for (int i = 0; i < chest.getSizeInventory(); ++i) {		     
			 itemStack = chest.getStackInSlot(i);
			 if (itemStack!=null && itemStack!=ItemStack.EMPTY) {
				 int quantity=itemStack.getCount();
				 int maxQuantity=itemStack.getMaxStackSize();
			 
				 if (itemStack.getItem().equals(itemStackIn.getItem())) {
					 if (quantity<chest.getInventoryStackLimit()&& quantity<maxQuantity) {
						 itemStack.setCount(quantity+1);
						 if (itemStackIn.getCount()-1<=0) refItemStackIn[0]=ItemStack.EMPTY;
						 else {
							 itemStackIn.shrink(1);
							 refItemStackIn[0]=itemStackIn;
						 }
						 chest.setInventorySlotContents(i, itemStack);
						 return true;
					 }					 
				 }	 
			 }
		 }
		 
		 for (int i = 0; i < chest.getSizeInventory(); ++i) {	
			 itemStack = chest.getStackInSlot(i);
			 if (itemStack==null || itemStack==ItemStack.EMPTY) {
				 itemStack=itemStackIn.copy();
				 itemStack.setCount(1);
				 chest.setInventorySlotContents(i, itemStack);
				 if (itemStackIn.getCount()-1<=0) refItemStackIn[0]=ItemStack.EMPTY;
				 else{
					 itemStackIn.shrink(1);
					 refItemStackIn[0]=itemStackIn;
				 }
				 return true;
			 }
		 }
		 
		 return false;
	}
	
	private  void  dropOrInsert(ElvenAvatarItemHadler inventory) {
		boolean insertado=false;
		TileEntityChest chest= this.findChest();	
		ItemStack itemStack=inventory.get0();
		ItemStack[] refItemStack=new ItemStack[1];
		refItemStack[0]=itemStack;
		if (chest!=null) insertado=this.insertInChest(refItemStack, chest);
		if (insertado) 
			inventory.set0(refItemStack[0]);
		else
			this.fakePlayerHelper.dropItem(inventory.take0());
	}
	
	@Override
	public void onItemStackHandlerChanged(ElvenAvatarItemHadler inventory,int slot) {
				
		if (getWorld().isRemote) return;				
		if (slot==0) {
			if (!ItemStackType.isStackType(getInventory().cacheType0,ItemStackType.Types.EYE))
				inventoryToFakePlayer(); //from player to avatar
			if (inventory.haveItem()) {
				//secuencesAvatar.ActivateSecuence("RiseArm");
				if(!this.secuencesAvatar.isActive())
					new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
				
				boolean isValid= inventory.isItemValid0();
				
				if (!isValid) {
					dropOrInsert(inventory);
				}
				
			}else {
								
				if (!resetBreak()) {
					new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);
				}
			}	
		}
		
		if (!getWorld().isRemote) {
			markDirty();
			IBlockState state=getWorld().getBlockState(pos);
			getWorld().notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	public boolean isAvatarTick() {
		return((ticksElapsed%AVATAR_TICK==1));
	}
	
	
	private boolean isHalfAvatarTick() {
		return((ticksElapsed%AVATAR_TICK==AVATAR_TICK/2));
	}
	
	public boolean haveItem() {
		return getInventory().haveItem();
	}

	public  void inventoryToFakePlayer() {
		fakePlayerHelper.inventoryToFakePlayer();
	}
	
	public boolean resetBreak() {
		return fakePlayerHelper.resetBreak();
	}
	
	public void updateRotatePoints(ModelRenderer[][] points,float[] RNDs,float elapsed) {
		
		for (int b=0;b<ModelAvatar.NARC;b++)
		for (int a=0; a<ModelAvatar.NPOINTS;a++) {			
			int points_duration = 1000- ((1000-POINTS_SEQUENCE_DURATION)*(manaAvatar/MAX_MANA));						
			anglePoints[b][a]+=((-3.1416F*2F/points_duration)*elapsed)*RNDs[a];
			if(anglePoints[b][a]>(-3.1416F*2F)) anglePoints[b][a]-=(-3.1416F*2F);			
			points[b][a].rotateAngleY=anglePoints[b][a]+(((-3.1416F*2.F)/ModelAvatar.NARC)*b);
			points[b][a].rotateAngleZ=points[b][a].rotateAngleY;
			points[b][a].rotateAngleX=points[b][a].rotateAngleY;			
		}		
	}
	
	
	public void setPlayerSpectator(boolean isSpectator) {
		this.playerIsSpectator=isSpectator;
		if (this.fakePlayerHelper.elvenFakePlayer.isSpectator()!=isSpectator) {
			this.fakePlayerHelper.elvenFakePlayer.setSpectator(isSpectator);		
			if (world!=null && !world.isRemote) new MessageSpectator(pos, isSpectator);
		}
	}
	
	@Override
	public  void  update() {
		
		//everytick server and client side
		ArrayList<ItemStackType.Types>  type0=getInventory().getType0();
		ArrayList<ItemStackType.Types>  type1=getInventory().getType1();
		
		if (ItemStackType.isStackType( type0,ItemStackType.Types.ROD_AVATAR)) {
			((IAvatarWieldable)(getInventory().get0().getItem())).onAvatarUpdate(this, getInventory().get0());			
		}

		
		if (getWorld().isRemote) return;
		//-----------------------------------------------------------------------------------
		
		if(isAvatarTick()) {							
			int manaCost=AVATAR_TICK*Config.mobSpawnerCostPertick;					
			boolean haveMana=(manaAvatar>=manaCost);			
			boolean haveEye=ItemStackType.isStackType(getInventory().cacheType0,ItemStackType.Types.EYE);
			boolean haveToBeSpectator=!haveEye||!haveMana||!enabled;
			if (!haveToBeSpectator) {				
				manaAvatar-=manaCost;
				markDirty();
				new MessageMana(getPos(),manaAvatar);
			}
			setPlayerSpectator(haveToBeSpectator);
	
		}
				
		boolean enabledBeforeRedstone=enabled;
		enabled = true;
		for(EnumFacing dir : EnumFacing.VALUES) {// EnumFacing.HORIZONTALS
			int redstoneSide = world.getRedstonePower(pos.offset(dir), dir);
			if(redstoneSide >= 14) {
				enabled = false;				
				break;
			}
		}
	
		if (enabledBeforeRedstone!=enabled) {
			new MessageEnabled (getPos(),enabled);
			if (enabled) this.ticksElapsed=0;
			if (secuencesAvatar.isActive())									
				if (getInventory().haveItem()) {						
					new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
				}else {
					new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);			
					resetBreak();
				}	
		}
		
		if(isAvatarTick()||(enabledBeforeRedstone!=enabled)) { //redstone signal forces change
			if(getInventory().haveItem()) {
				if (ItemStackType.isStackType( type0,ItemStackType.Types.ROD_WILL)) {										
						boolean rightClick=!ItemStackType.isStackType( type1,ItemStackType.Types.ROD_WORK);
						this.fakePlayerHelper.rodClick(this,rightClick);
				}else 
					if (!chargeMana(type0))	//its a tool					
					{
						if (ItemStackType.isStackType( type0,ItemStackType.Types.BREAK))
							if(ItemStackType.isStackType( type1,ItemStackType.Types.ROD_WORK)) 
								fakePlayerHelper.rightClickBlockWhithItem();//right click a block
							else								
								fakePlayerHelper.beginBreak(); 						
						if (ItemStackType.isStackType( type0,ItemStackType.Types.USE)||ItemStackType.isStackType( type0,ItemStackType.Types.SHEAR)||ItemStackType.isStackType( type0,ItemStackType.Types.KILL)) fakePlayerHelper.beginUse();
						if (ItemStackType.isStackType( type0,ItemStackType.Types.JUSTRC)) fakePlayerHelper.justRightClick(this);														
					}
			}
						
		}
		else {
			if (ticksElapsedDisabled>AVATAR_TICK) {
				ticksElapsedDisabled=0;
				chargeMana(type0); //try to load mana even if disabled.
			}else  ticksElapsedDisabled++;
		}
		
		if(enabled) {
			ticksElapsed++;	//if not enabled time stop.
		};
		
		fakePlayerHelper.updateHelper();
	}
	
	private boolean chargeMana(ArrayList<Types> type0) {
	
		if (ItemStackType.isStackType( type0,ItemStackType.Types.MANA))  {			
			ItemStack stackMana=getInventory().get0();
			Item itemmana=stackMana.getItem();
			EntityItem entityItemMana= new EntityItem(getWorld(),pos.getX(), pos.getY(), pos.getZ(), stackMana);
			
			if (itemmana instanceof IManaDissolvable) {
				((IManaDissolvable)itemmana).onDissolveTick((IManaPool)this,stackMana, entityItemMana);						
				stackMana=getInventory().get0();
				if (stackMana.isEmpty()) {
					getInventory().take0();
				}
				
			}else 
				if (stackMana.getItem() instanceof IManaItem){
					if (wandManaToTablet) avatarToTablet(stackMana);
					else tabletToAvatar(stackMana);
				}
			return true;
		}
		return false;
	}
	
    private void tabletToAvatar	( ItemStack stack) {
	   if(getWorld().isRemote) return;
	   IManaItem tablet=(IManaItem)stack.getItem();
	   int manaActualTablet=tablet.getMana(stack);
	   if (manaActualTablet<=0) return;	   
	   int burst=TABLET_BURST;
	   int espacioAvatar=MAX_MANA-manaAvatar;	   
	   if (espacioAvatar<=0) return;
	   
	   if (manaActualTablet<burst) burst=manaActualTablet;
	   if (burst>espacioAvatar) burst=espacioAvatar;
	   
	   tablet.addMana(stack, -burst);
	   manaAvatar+=burst;	   
	   markDirty();
	   new MessageMana(getPos(),manaAvatar);
	   fakePlayerHelper.emitRedstone();
	   world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.spreaderFire, SoundCategory.BLOCKS, 0.11F, 1F);
	   
   }
   
    private void avatarToTablet ( ItemStack stack) {
	   if(getWorld().isRemote) return;	   
	   if (manaAvatar<=0)return;	   
	   int burst=TABLET_BURST;
	   IManaItem tablet=(IManaItem)stack.getItem();
	   int manaActualTablet=tablet.getMana(stack);
	   int manaMaxTablet=tablet.getMaxMana(stack);
	   int espacioTablet=manaMaxTablet-manaActualTablet;	   	 
	   if (espacioTablet<=0) return;
	   
	   if (manaAvatar<burst) burst=manaAvatar;
	   if (burst>espacioTablet) burst=espacioTablet;		   
	
	   manaAvatar-=burst;
	   tablet.addMana(stack, burst);
	   markDirty();
	   new MessageMana(getPos(),manaAvatar);
	   fakePlayerHelper.emitRedstone();
	   world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.spreaderFire, SoundCategory.BLOCKS, 0.11F, 1F);
	 
   }



	@Override
	public EnumFacing getAvatarFacing() {
		return world.getBlockState(getPos()).getValue(BotaniaStateProps.CARDINALS);
	}
	
	@Override
	public BlockPos getPos() {
		return super.getPos();
	}

	@Override
	public int getBlockMetadata() {
		//return EnumFacing.NORTH.getIndex();
		return 0;
	}
   
	@Override
	public int getElapsedFunctionalTicks() {
		return ticksElapsed;
	}
	
	@Override
	public ElvenAvatarItemHadler getInventory() {
		return (ElvenAvatarItemHadler)getItemHandler();		
	}
	
	@Override
	public int getSizeInventory() {
		return 2; //two hands
	}
    
    @Override
    protected SimpleItemStackHandler createItemHandler() {	
    	return new ElvenAvatarItemHadler(this, true);        
    }

	@Override
	public boolean isOutputtingPower() {
		return wandManaToTablet;
	}

	@Override
	public EnumDyeColor getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColor(EnumDyeColor color) {
		// TODO Auto-generated method stub		
	}

	@Override
	public int getCurrentMana() {
		return manaAvatar;
	}

	@Override
	public boolean isFull() {
		return manaAvatar >= MAX_MANA;
	}

	@Override
	public void recieveMana(int mana) {
		this.manaAvatar = Math.max(0, Math.min(MAX_MANA, this.manaAvatar + mana));
		new MessageMana(getPos(),manaAvatar);
		markDirty();				
	}

	@Override
	public boolean canRecieveManaFromBursts() {
		return getInventory().haveItem() && !(ItemStackType.isStackType( getInventory().getType0(),ItemStackType.Types.MANA));
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean penabled) {
		 this.enabled=penabled;
	}
	
	@Override
	public void readPacketNBT(NBTTagCompound par1nbtTagCompound) {
		
		super.readPacketNBT(par1nbtTagCompound.getCompoundTag("inventory"));//read inventory
		enabled = par1nbtTagCompound.getBoolean(TAG_ENABLED);
		ticksElapsed = par1nbtTagCompound.getInteger(TAG_TICKS_ELAPSED);
		manaAvatar = par1nbtTagCompound.getInteger(TAG_MANA);		
		wandManaToTablet= par1nbtTagCompound.getBoolean(TAG_WAND);
		playerIsSpectator=par1nbtTagCompound.getBoolean(TAG_SPECT);
		try {
			this.playerUUID= UUID.fromString(par1nbtTagCompound.getString(TAG_UUID));
		}catch (Exception e) {}
        
		if (world!=null && world.isRemote && this.playerUUID!=null)      	
        	this.fakePlayerHelper.elvenFakePlayer.initClient( world,  pos, this );     
        else
        	setPlayerSpectator(playerIsSpectator);
		this.fakePlayerHelper.readPacketNBT(par1nbtTagCompound);
		
		
	}
	
	@Override
	public void writePacketNBT(NBTTagCompound par1nbtTagCompound) {
		par1nbtTagCompound.setTag("inventory", this.getInventory().serializeNBT());
		par1nbtTagCompound.setBoolean(TAG_ENABLED, enabled);
		par1nbtTagCompound.setInteger(TAG_TICKS_ELAPSED, ticksElapsed);
		par1nbtTagCompound.setInteger(TAG_MANA, manaAvatar);
		par1nbtTagCompound.setBoolean(TAG_WAND, wandManaToTablet);
		par1nbtTagCompound.setBoolean(TAG_SPECT, playerIsSpectator);
		
		if (this.playerUUID!=null)
			par1nbtTagCompound.setString(TAG_UUID, this.playerUUID.toString());
		this.fakePlayerHelper.writePacketNBT(par1nbtTagCompound);
	}



	public void setmana(int pmana) {
		//to set from server message
		manaAvatar=pmana;
	}
	
	public boolean haveMana(){
		return (manaAvatar>=MIN_MANA);
	}
	
	public void onWanded(EntityPlayer player, ItemStack wand) {
		if(player == null) return;
		if(world.isRemote) return;
			
		wandManaToTablet = !wandManaToTablet;
		world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.ding, SoundCategory.PLAYERS, 0.11F, 1F);
		//player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "->Wand Change "+String.valueOf(wandManaToTablet)), false);
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);		
	}


	public void renderHUD(Minecraft mc, ScaledResolution res)
	{
		{
			
			Block avatarBlock= getWorld().getBlockState(pos).getBlock();
			//ItemStack avatarStack = new ItemStack(avatarBlock);
			
			ItemStack avatarStack = new ItemStack(avatarBlock, 1, this.getBlockMetadata());
			String name = I18n.format("Elven Avatar");//+" "+String.valueOf(wandManaToTablet); 
			int color = 0x4444FF;
			
			int manaInt=manaAvatar;
			if (manaInt>MAX_MANA) manaInt=MAX_MANA;
			HUDHandler.drawSimpleManaHUD(color, manaInt, MAX_MANA, name, res);
			
			int x = res.getScaledWidth() / 2 - 11;
			int y = res.getScaledHeight() / 2 + 30;
		
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			mc.renderEngine.bindTexture(HUDHandler.manaBar);
			
			int u = wandManaToTablet ? 22 : 0;
			vazkii.botania.client.core.helper.RenderHelper.drawTexturedModalRect(x, y, 0, u, 38, 22, 15);
			GlStateManager.color(1F, 1F, 1F, 1F);
			
			
			ItemStack tablet = new ItemStack(ModItems.manaTablet);
			ItemManaTablet.setStackCreative(tablet);
			
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(tablet, x - 20, y);
			mc.getRenderItem().renderItemAndEffectIntoGUI(avatarStack, x + 26, y);			
			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
			
			GlStateManager.disableLighting();
			GlStateManager.disableBlend();
		}
	}

	public class ElvenAvatarItemHadler extends SimpleItemStackHandler{
		
		private ArrayList<ItemStackType.Types>  cacheType0;
		private ArrayList<ItemStackType.Types>  cacheType1;
		private ItemStack [] lastTryToInsert=new ItemStack [2] ;
		private List <IElvenAvatarItemHadlerChangedListener> listIInventoryChangedListener= new ArrayList<IElvenAvatarItemHadlerChangedListener> ();  
    	
		
		public <T extends TileSimpleInventory & IElvenAvatarItemHadlerChangedListener > ElvenAvatarItemHadler(T inv, boolean allowWrite) {
			super(inv, allowWrite);
			if (super.getStackInSlot(0)==null) super.setStackInSlot(0, ItemStack.EMPTY);
			if (super.getStackInSlot(1)==null) super.setStackInSlot(1, ItemStack.EMPTY);		

			reloadCache();
			addIInventoryChangedListener(inv);
		}
		
		private void reloadCache() {
			cacheType0= ItemStackType.getTypeTool(super.getStackInSlot(0));
			cacheType1= ItemStackType.getTypeTool(super.getStackInSlot(1));
		}
		
		 @Override
	    public void deserializeNBT(NBTTagCompound nbt) {
		 	super.deserializeNBT(nbt);
		 	reloadCache() ;			
		}
		
		public void addIInventoryChangedListener(IElvenAvatarItemHadlerChangedListener listener) {
			if (listener!=null)
				listIInventoryChangedListener.add(listener);
		}
		
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}
		
		@Override
		public void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			markDirty();
			if (slot==1) cacheType1= ItemStackType.getTypeTool(super.getStackInSlot(1));
			if (slot==0) {
					cacheType0=ItemStackType.getTypeTool(super.getStackInSlot(0));
					lastTryToInsert[slot]=null; 
			}

			for(int i=0;i<listIInventoryChangedListener.size();i++) {				
				listIInventoryChangedListener.get(i).onItemStackHandlerChanged(this,slot);
			}
						
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			
			if (!isHalfAvatarTick()) return stack; //waits update changes
			if (slot==0 &&  get0()!=ItemStack.EMPTY) return stack;
			if (slot==1 &&  get1()!=ItemStack.EMPTY) return stack;
			
			boolean valid;
			valid=isItemValid(slot,stack);			
			if (valid) {
				lastTryToInsert[slot]=null;
				return super.insertItem(slot, stack, simulate);
			}
			else {
				lastTryToInsert[slot]=stack.copy();			
				return stack;
			}
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {			
			//if (!isHalfAvatarTick()) return ItemStack.EMPTY; //waits update changes
			
			boolean allow=false;
			if (slot==1) allow=true;	
			
			if (slot==0 && ItemStackType.isStackType(getType0(),ItemStackType.Types.MANA)) {
				ItemStack stack=getInventory().get0();
				Item itemmana=stack.getItem();				
				if (itemmana instanceof IManaDissolvable) 
					allow=false; //spawned to world when used
				else {								
					IManaItem tablet=(IManaItem)stack.getItem();
					int manaActualTablet=tablet.getMana(stack);
					if (wandManaToTablet && manaActualTablet>=tablet.getMaxMana(stack))
						allow=true;					
					if (!wandManaToTablet && manaActualTablet==0)
						allow=true;	
				}
				//to transfer mana, only can extract if tablet is full or empty
			}				 			
			else allow=true;	
			
			
			if (allow)return super.extractItem(slot, amount, simulate);
				else return ItemStack.EMPTY;
			
		}
	
		@Override
		public ItemStack getStackInSlot(int slot) {
			return super.getStackInSlot(slot);
		}
				
		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			super.setStackInSlot(slot, stack);
		}
		
		public boolean isItemValid0() {
			return isItemValid(0, getStackInSlot(0));
		}
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			
			if (lastTryToInsert[slot]!=null && stack.isItemEqual(lastTryToInsert[slot])) return false;
			boolean valid_super=super.isItemValid(slot, stack);
			
			boolean valid=true;
			if (valid_super) {
				ArrayList<ItemStackType.Types>  type=ItemStackType.getTypeTool(stack);	
				
				if (slot==1) valid=(ItemStackType.isStackType( type,ItemStackType.Types.ROD_WORK) && ItemStackType.isStackType( cacheType0,ItemStackType.Types.BREAK));
				else {	
					
					if (type.get(0)==ItemStackType.Types.NONE) valid=false;
					if (valid && ItemStackType.isStackType( type,ItemStackType.Types.BLOCK)) valid=false;
					if (valid &&  ItemStackType.isStackType( type,ItemStackType.Types.ROD_WORK)) valid=false;
					if (ItemStackType.isStackType( type ,ItemStackType.Types.MANA)) {
						Item itemmana=stack.getItem();
						if (itemmana instanceof IManaDissolvable && wandManaToTablet==false && isFull()) valid=false; //if idissolvabe and is full , return false
					}					
				}				
			}
			return valid;
		}
			
		public void set0(ItemStack stack) {
			super.setStackInSlot(0, stack);
		}
		
		public void set1(ItemStack stack) {
			super.setStackInSlot(1, stack);
		}
		
		public void empty0() {
			setStackInSlot(0, ItemStack.EMPTY);
		}
		
		public void empty1() {
			setStackInSlot(1, ItemStack.EMPTY);
		}
		
		public ItemStack get0() {
			return getStackInSlot(0);
		}
		
		public ItemStack get1() {
			return getStackInSlot(1);
		}
				
		public ItemStack take0() {
			ItemStack t=getStackInSlot(0);
			setStackInSlot(0, ItemStack.EMPTY);
			return t;			
		}
		
		public ItemStack take1() {
			ItemStack t=getStackInSlot(1);
			setStackInSlot(1, ItemStack.EMPTY);
			return t;			
		}
		
		public ArrayList<ItemStackType.Types>  getType0() {
			return cacheType0;
		}
		
		public ArrayList<ItemStackType.Types>  getType1() {
			return cacheType1;
		}
		
		public boolean haveItem() {
			ItemStack stack=getStackInSlot(0);
			return(stack!=null && stack!=ItemStack.EMPTY);
		}
	
    }
	
	


	@Override
	public boolean canAttachSpark(ItemStack stack) {
	   return true;	    
	}

	@Override
	public void attachSpark(ISparkEntity entity) {
		haveSpark=true;
	}

	@Override
	public int getAvailableSpaceForMana() {
		return TileElvenAvatar.MAX_MANA-manaAvatar;
	}

	@Override
	public ISparkEntity getAttachedSpark() {
		AxisAlignedBB bounding=new AxisAlignedBB(pos.up(), pos.up().add(1, 1, 1));
		EntitySpark spark;
	    List<Entity> sparks;       
	    sparks = world.getEntitiesWithinAABB(Entity.class,bounding, Predicates.instanceOf(ISparkEntity.class));
	    if(sparks.size() == 1) {	    	
	    	 spark= (EntitySpark) sparks.get(0);	 	    	
	    	if (spark!=null && spark.isEntityAlive()) {
	    		return spark;
	    	}
	    }
	    
	    if(haveSpark) {
	    	markDirty();
		    haveSpark=false;
	    }
	    return null;		 	
	}

	@Override
	public boolean areIncomingTranfersDone() {
		return this.isFull();
	}
	
	@Override
	public boolean receiveClientEvent(int event, int param) {
		switch(event) {
			case CRAFT_EFFECT_EVENT: {
				if(world.isRemote) {
					for(int i = 0; i < 25; i++) {
						float red = (float) Math.random();
						float green = (float) Math.random();
						float blue = (float) Math.random();
						Botania.proxy.sparkleFX(pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, pos.getY() + 0.75, pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2,
									red, green, blue, (float) Math.random(), 10);
					}
				}

				return true;
			}
			case CHARGE_EFFECT_EVENT: {
				if(world.isRemote) {
					if(ConfigHandler.chargingAnimationEnabled) {
						boolean outputting = param == 1;
						Vector3 itemVec = Vector3.fromBlockPos(pos).add(0.5, 0.5 + Math.random() * 0.3, 0.5);
						Vector3 tileVec = Vector3.fromBlockPos(pos).add(0.2 + Math.random() * 0.6, 0, 0.2 + Math.random() * 0.6);
						Botania.proxy.lightningFX(outputting ? tileVec : itemVec,
								outputting ? itemVec : tileVec, 80, world.rand.nextLong(), 0x4400799c, 0x4400C6FF);
					}
				}
				return true;
			}
			default: return super.receiveClientEvent(event, param);
		}
	}
	
}
