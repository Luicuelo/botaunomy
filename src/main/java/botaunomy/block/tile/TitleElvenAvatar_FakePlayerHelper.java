package botaunomy.block.tile;

import java.lang.ref.WeakReference;
import java.util.List;
import botaunomy.ItemStackType;
import botaunomy.block.ElvenAvatarBlock;
import botaunomy.config.Config;
import botaunomy.network.MessageInventoryEmpty;
import botaunomy.network.MessageMana;
import botaunomy.network.MessageMoveArm;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

public class TitleElvenAvatar_FakePlayerHelper {

	TileElvenAvatar avatar;

	protected ElvenFakePlayerHandler elvenFakePlayer;

	//private static final int USE_MANA_COST = 200;
	//private static final int ROD_MANA_COST = 200;
	//private static final int BREAK_MANA_COST=200;

	private static final int MANA_PER_TOOLDAMAGE=30;
	private BreakingData breakingData=new BreakingData();
	private boolean isRodClick=false;
	private boolean blockRighClick=false;
	private boolean toolUse=false;
	private List<Entity> entitiesList=null; //try to use with all detected
	int entitieIndex=0;
	private EmitResdstoneTimer emitResdstoneTimer=new EmitResdstoneTimer();
	
	
	public TitleElvenAvatar_FakePlayerHelper(TileElvenAvatar pavatar) {
		avatar=pavatar;
		elvenFakePlayer=new ElvenFakePlayerHandler(avatar.getWorld(), avatar.getPos(),pavatar);
	}
	
	private WeakReference<FakePlayer>  getRefAndRetryInit() {
		return  elvenFakePlayer.getRefAndRetryInit(getWorld(),  getPos(), avatar);
	}
	
	private class BreakingData{
		private boolean _isBreaking=false;
		private BlockPos posBlockToBreak;	
		private float curBlockDamageMP;
		
		public BreakingData() {
			
		}
		public boolean isBreaking() {
			return _isBreaking;
		}
		public void BeginBreak(BlockPos blockPos) {
			posBlockToBreak=blockPos;
			_isBreaking=true;
		}
		public void stopBreak() {
			posBlockToBreak=null;
			_isBreaking=false;
			curBlockDamageMP=0;
		}
		public BlockPos getPosBlockToBreak() {
			if (_isBreaking) return posBlockToBreak;
			return null;
		}
		
		public IBlockState 	getStateBlockToBreak() {
			return getWorld().getBlockState(posBlockToBreak);
		}
		
		public Block getBlockToBreak() {
			return getWorld().getBlockState(posBlockToBreak).getBlock();
		}
		
		public void addBlockDamage(float damage) {
			curBlockDamageMP+=damage;
		}
		
		public void addBlockDamage(EntityPlayer player) {
		       IBlockState iblockstate = getWorld().getBlockState(posBlockToBreak);
		       addBlockDamage( iblockstate.getPlayerRelativeBlockHardness(player, getWorld(), posBlockToBreak));
		}
		
		private int readBlockDamage() {
			return (int)(curBlockDamageMP * 10.0 - 1);
		}	
		public boolean blockIsFullDamage() {
			return (curBlockDamageMP >= 1.0F);
		}
		
		public boolean blockIsAir() {
			return getWorld().getBlockState(posBlockToBreak).getMaterial() == Material.AIR;
		}
		
		public void sendBlockBreakProgress100(EntityPlayer player) {			
			getWorld().sendBlockBreakProgress(player.getEntityId(), posBlockToBreak, -1);
		}
		
		public void sendBlockBreakProgress(EntityPlayer player) {
			getWorld().sendBlockBreakProgress(player.getEntityId(), posBlockToBreak, readBlockDamage());
		}
		
		public void readPacketNBT(NBTTagCompound par1nbtTagCompound) {			
			_isBreaking = par1nbtTagCompound.getBoolean("isBreaking");			
			if (!_isBreaking) {
				posBlockToBreak=null;
				curBlockDamageMP=0F;
			}
			else {				
				posBlockToBreak= new BlockPos(par1nbtTagCompound.getInteger("posBlockToBreakX"), par1nbtTagCompound.getInteger("posBlockToBreakY"), par1nbtTagCompound.getInteger("posBlockToBreakZ")); 				
			}
			
			
	 	}

		public void writePacketNBT(NBTTagCompound par1nbtTagCompound) {
			par1nbtTagCompound.setBoolean("isBreaking", _isBreaking);
			if (posBlockToBreak!=null) {
				par1nbtTagCompound.setInteger("posBlockToBreakX", posBlockToBreak.getX());
				par1nbtTagCompound.setInteger("posBlockToBreakY", posBlockToBreak.getY());
				par1nbtTagCompound.setInteger("posBlockToBreakZ", posBlockToBreak.getZ());
			}
		}	
		
	}
	
	public void emitRedstone() {
		if (emitResdstoneTimer!=null)
			emitResdstoneTimer.emitRedstone();
	}
	
	private class EmitResdstoneTimer{
		
		private int ticksElapsed;
		private static final int PULSE_TIME=10;
		public  boolean isEnabled=false;
		
		public EmitResdstoneTimer(){}
				
		public void emitRedstone() {
			//BlockPos targetPos = getPos().offset(right());		
			//getWorld().getRedstonePower(targetPos, null)
			
			isEnabled=true;
			ticksElapsed=0;
			setState(isEnabled); 
		}
				
		public void checkStopEmitRedstone() {
			if (!isEnabled) return;
			ticksElapsed++;
			if (ticksElapsed>PULSE_TIME) {
				isEnabled=false;
				ticksElapsed=0;
				setState(isEnabled);
			}						
		}
		
		private void setState(boolean value) {			
			IBlockState state = getWorld().getBlockState(getPos());
			getWorld().setBlockState(getPos(), state.withProperty(ElvenAvatarBlock.POWERED, value));
			for (EnumFacing facing : EnumFacing.VALUES) {
				getWorld().notifyNeighborsOfStateChange(getPos().offset(facing), state.getBlock(), true);
			}
		}

	}		
	
	private void checkManaIsEmpty() {
		if (avatar.getCurrentMana()>=0 )return;			
		if (avatar.getInventory().haveItem())
			new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
		else
			new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);
								
		if (breakingData.isBreaking()) {
			breakingData.stopBreak();
		}
	}
	
	

	
	public void inventoryToFakePlayer() {
		elvenFakePlayer.inventoryToFakePlayer(avatar);
	}
	
	public void fakePlayerToInventory() {
		elvenFakePlayer.fakePlayerToInventory(avatar);
	}
	
	public boolean isBusy() {
		boolean isBusy;
		isBusy=breakingData.isBreaking()||isRodClick||blockRighClick||toolUse;
		return (isBusy);
	}
	
	public boolean resetBreak() {       
	
		if (breakingData.isBreaking()) {
			if (avatar.getInventory().haveItem())
				new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
			else
				new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);
			breakingData.stopBreak();
			return true;
		}return false;
    }
	
	private World getWorld() {
		return avatar.getWorld(); 
	}
	
	private BlockPos getPos() {
		return avatar.getPos();
	}
	
	public void updateHelper() {		
		
		WeakReference<FakePlayer> player=getRefAndRetryInit();
		if (player!=null) player.get().onUpdate();		
		if (player!=null && breakingData.isBreaking ()) continueBreaking();				
		else this.resetBreak();
		
		emitResdstoneTimer.checkStopEmitRedstone();		
	}
    
	public void sneak(boolean isSneaking) {
		FakePlayer player=getRefAndRetryInit().get();
		if (player.isSneaking()!=isSneaking)
			player.setSneaking(isSneaking);
	}
	
	public void  beginBreak() {
		
		if (!(this.getWorld() instanceof WorldServer)) return;
		if (!avatar.isEnabled())return;	
		if (isBusy()) return;		
		WeakReference<FakePlayer> player = getRefAndRetryInit();
		if (player == null) return;
		if(elvenFakePlayer.stackMainHandType().get(0)==ItemStackType.Types.BLOCK)return;  //This ain't no block placer! , if holding a block dont use. 
		if(avatar.getCurrentMana() < Config.breakManaCost ) return;
		if (breakingData.isBreaking()) return; 
		if (getWorld().isAirBlock(getTargetPos())) return;
		
		breakingData.BeginBreak(getTargetPos());
		breakingData.sendBlockBreakProgress(player.get());

        avatar.recieveMana(- Config.breakManaCost);			
        checkManaIsEmpty();
        emitResdstoneTimer.emitRedstone();
		new MessageMana(getPos(),avatar.getCurrentMana());
		new MessageMoveArm (getPos(),MessageMoveArm.SWING_ARM);
    }  
	
    private void continueBreaking() {
        
       if ( elvenFakePlayer.stackMainHand().isEmpty()) {//tool has been broken or removed
    	   resetBreak();
    	   return;
       }
       
       FakePlayer player=getRefAndRetryInit().get();       
       breakingData.addBlockDamage(player);

	   if (breakingData.blockIsFullDamage()) {
		    breakingData.sendBlockBreakProgress100(player);	       
	        this.onPlayerDestroyBlock();                   
	        breakingData.stopBreak();
	        
	   }else          
		   breakingData.sendBlockBreakProgress(player);	    	
	   	
    }
	
    private void onPlayerDestroyBlock( ) {
    	
    	FakePlayer player=getRefAndRetryInit().get();
        
    	//IBlockState stateBlockToBreak = getWorld().getBlockState(posBlockToBreak);
        //Block blockToBreak = stateBlockToBreak.getBlock();

        //if ((blockToBreak instanceof BlockCommandBlock) && !player.canUseCommandBlock())  return;
        ItemStack stackMainHand = elvenFakePlayer.stackMainHand();
        Item itemStackMainHand=stackMainHand.getItem();
        
        if (!(this.getWorld() instanceof WorldServer)) return;
        if ( player==null || !breakingData.isBreaking()) return;
        if (breakingData.blockIsAir()) return;        
        if (stackMainHand.isEmpty()) return;
        boolean notCanBeHarvested= (stackMainHand.getItem().onBlockStartBreak(stackMainHand, breakingData.getPosBlockToBreak(), player));               
     
        
       	//blockBreakParticles
       	getWorld().playEvent(2001, breakingData.getPosBlockToBreak(), Block.getStateId(breakingData.getStateBlockToBreak()));
       	if (!notCanBeHarvested&&breakingData.getBlockToBreak().canHarvestBlock(getWorld(), breakingData.getPosBlockToBreak() , player)) {
       		breakingData.getBlockToBreak().harvestBlock(getWorld(), player, breakingData.getPosBlockToBreak(), breakingData.getStateBlockToBreak(), null, stackMainHand); //itemblock drop
       		int fortune=EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stackMainHand) ;
       		breakingData.getBlockToBreak().dropXpOnBlockBreak(getWorld(),  breakingData.getPosBlockToBreak(),breakingData.getBlockToBreak().getExpDrop(breakingData.getStateBlockToBreak(), getWorld(), breakingData.getPosBlockToBreak(), fortune));
       		
       		if(itemStackMainHand instanceof IManaUsingItem && ((IManaUsingItem) itemStackMainHand).usesMana(stackMainHand)) {
       			ToolCommons.damageItem(stackMainHand, 1, player, MANA_PER_TOOLDAMAGE );
       		}else 
       			stackMainHand.onBlockDestroyed(getWorld(), breakingData.getStateBlockToBreak(), breakingData.getPosBlockToBreak(), player);//set use
       	}
      
        boolean flag = breakingData.getBlockToBreak().removedByPlayer(breakingData.getStateBlockToBreak(), getWorld(), breakingData.getPosBlockToBreak(), player, false);
        if (flag) {                	
        	  breakingData.getBlockToBreak().onBlockDestroyedByPlayer(getWorld(), breakingData.getPosBlockToBreak(), breakingData.getStateBlockToBreak());                            
        }
                
          // if (player.experience>0)                
          //player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "XP->" +player.experience), false);	
            	            	
        if (stackMainHand.isEmpty()) {
              net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, stackMainHand, EnumHand.MAIN_HAND);                    
              avatar.getInventory().empty0();              
              //elvenFakePlayer.inventoryToFakePlayer(avatar);
              if(getWorld() instanceof WorldServer) {
    			new MessageInventoryEmpty(getPos());
    			new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);    			
    		  }
                    
        }else {        	 
               	avatar.getInventory().set0(stackMainHand.copy());               		
               	new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
         	   }          
    }
   
    
	private BlockPos getTargetPos() {
		try {
			BlockPos targetPos = getPos().offset(avatar.getAvatarFacing());			
			return targetPos;
		}
		catch (Exception e) {
			//TODO: Print error
			return null;
		}
		
	}

	public void rightClickBlockWhithItem() {
		
		
		if (!avatar.isEnabled())return;		
		if (isBusy()) return;		
		WeakReference<FakePlayer> avatarPlayer = getRefAndRetryInit();
		if (avatarPlayer == null) return;
		
		if(avatar.getCurrentMana() >= Config.rodManaCost) {
	
			boolean interactedWithBlock = false;
			BlockPos targetPos=getTargetPos();
			if (targetPos!=null) {
				blockRighClick=true;								
				interactedWithBlock = interactBlockWithItem(avatar, avatarPlayer, targetPos);
			
				if(interactedWithBlock) {
					avatar.recieveMana(-Config.rodManaCost);
					checkManaIsEmpty();
					emitResdstoneTimer.emitRedstone();
					if(avatar.getWorld() instanceof WorldServer) {
						this.fakePlayerToInventory();
						if (!elvenFakePlayer.stackMainHand().isEmpty())
							new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
						else {
								new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);
								this.inventoryToFakePlayer();
							 }
						new MessageMana(getPos(),avatar.getCurrentMana());
						avatar.getWorld().markChunkDirty(targetPos, avatar);
					}
				}			
				blockRighClick=false;
			}
		}	
	}
	
	
	public void beginUse( ) {

		if (!avatar.isEnabled()) return;
		if (elvenFakePlayer.stackMainHand().isEmpty()) return;
		if(avatar.getCurrentMana() < Config.useManaCost) return;
		
		
		if (!toolUse) entitiesList=this.detectEntity(this.getPos());		
		if (entitiesList==null || entitiesList.size()==0) return; 

		
		Entity currentEntity=null;
		boolean interactedWithEntities = false;		
		try {		
			currentEntity=entitiesList.get(entitieIndex);
		}catch (Exception e) {}
		
		if (currentEntity!=null) interactedWithEntities=useTool(currentEntity);
		toolUse|=interactedWithEntities;
		
		if( interactedWithEntities) {
			avatar.recieveMana(-(Config.useManaCost));		
			checkManaIsEmpty();
			emitResdstoneTimer.emitRedstone();
			if(this.getWorld() instanceof WorldServer) {
				new MessageMana(getPos(),avatar.getCurrentMana());
				new MessageMoveArm (getPos(),MessageMoveArm.SWING_ARM);
			}
		}	
		
		entitieIndex++;
		if (entitieIndex>=entitiesList.size()) {				
			new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);						
			entitieIndex=0;		
			entitiesList.clear();	
			toolUse=false;
		}
		
		if ( elvenFakePlayer.stackMainHand().isEmpty()) { //tool has not  broken or removed    	 
			toolUse=false;
            avatar.getInventory().empty0();              
            //elvenFakePlayer.inventoryToFakePlayer(avatar);
            if(getWorld() instanceof WorldServer) {
    			new MessageInventoryEmpty(getPos());
    			new MessageMoveArm (getPos(),MessageMoveArm.DOWN_ARM);    			
    		}
		}
	}
	
	public boolean useTool( Entity entity) {
		
		WeakReference<FakePlayer> avatarPlayer = getRefAndRetryInit();
		if (avatarPlayer == null) return false;	
		if (avatarPlayer.get() == null) return false;
		if (entity == null ||entity.isDead) return false;

		boolean result = false;		
		ItemStack tool=elvenFakePlayer.stackMainHand();
		if (ItemStackType.isStackType( elvenFakePlayer.stackMainHandType(),ItemStackType.Types.SHEAR)) { // shears return true when entity ishearable is false, we must check before
			if 	(entity instanceof net.minecraftforge.common.IShearable) {				
				BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
				boolean entityShareable=((net.minecraftforge.common.IShearable)entity).isShearable(tool, entity.world, pos);
				if (!entityShareable){
					return false;
				}
			}
		}
		
		if (ItemStackType.isStackType( elvenFakePlayer.stackMainHandType(),ItemStackType.Types.SHEAR)||ItemStackType.isStackType( elvenFakePlayer.stackMainHandType(),ItemStackType.Types.USE) ) {
			
			String previosName=elvenFakePlayer.stackMainHand().getUnlocalizedName();
			EnumActionResult interaction = avatarPlayer.get().interactOn(entity, EnumHand.MAIN_HAND);			

			result=(interaction==EnumActionResult.SUCCESS);
							//result =avatarPlayer.get().getHeldItemMainhand().interactWithEntity(avatarPlayer.get(), (EntityLivingBase) entity, EnumHand.MAIN_HAND);		
							//result =avatarPlayer.get().getHeldItemMainhand().getItem().itemInteractionForEntity(avatarPlayer.get().getHeldItemMainhand(),avatarPlayer.get(),(EntityLivingBase) entity, EnumHand.MAIN_HAND);
							//entity.applyPlayerInteraction(null, null, null)
			
			if (result) avatar.getWorld().markChunkDirty(entity.getPosition(), avatar);
			if (result&& !previosName.equals(elvenFakePlayer.stackMainHand().getUnlocalizedName())) //item have change
				this.fakePlayerToInventory();
			return result;
		}
		
		if (ItemStackType.isStackType( elvenFakePlayer.stackMainHandType(),ItemStackType.Types.KILL) && entity instanceof EntityLivingBase) {
			if (avatarPlayer.get().getCooledAttackStrength(0)==1) {
				avatarPlayer.get().attackTargetEntityWithCurrentItem(entity);				
				/*
				float damage=0;
				if (elvenFakePlayer.stackMainHand().getItem() instanceof ItemSword)
			        {
						damage = ((ItemSword) elvenFakePlayer.stackMainHand().getItem()).getAttackDamage();
			        }
								
				entity.attackEntityFrom(DamageSource.causePlayerDamage(avatarPlayer.get()), damage);
				*/
				return true;
			}else return false;
		}		
		
		return false;
		
	}
	
	/*
	private  float getAttackDamage(ItemStack mainHand, EntityLivingBase entity) {
		IAttributeInstance dmgAttr = new AttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		if (!mainHand.isEmpty()) {
			Collection<AttributeModifier> modifiers = mainHand
					.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
					.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			for (AttributeModifier modifier : modifiers) {
				dmgAttr.applyModifier(modifier);
			}
		}
		float damage = (float) dmgAttr.getAttributeValue();
		float enchantDamage = EnchantmentHelper.getModifierForCreature(mainHand, entity.getCreatureAttribute());
		return damage + enchantDamage;
	}*/
		
	//for test bounding
    /*	
	private void changeBlock(BlockPos bp) {
	    Block b =new Block(Material.IRON);	    
		getWorld().setBlockState(bp, b.getDefaultState());
	}*/
	
	
	private AxisAlignedBB boundingNorth(BlockPos avatarPos) {	
		//looking North Zero coordinates of a block top left of block 		
		BlockPos e1 = avatarPos.offset(EnumFacing.NORTH,2).offset(EnumFacing.WEST);
		BlockPos e2 = avatarPos.offset(EnumFacing.EAST,2).offset(EnumFacing.UP);		
		return new AxisAlignedBB(e1,e2);
	}
	
	private AxisAlignedBB boundingSouth(BlockPos avatarPos) {		
		BlockPos e1 = avatarPos.offset(EnumFacing.SOUTH,3).offset(EnumFacing.WEST);
		BlockPos e2 = avatarPos.offset(EnumFacing.SOUTH).offset(EnumFacing.EAST,2).offset(EnumFacing.UP);
		return new AxisAlignedBB(e1,e2);
	}
	private AxisAlignedBB boundingEast(BlockPos avatarPos) {
		BlockPos e1 = avatarPos.offset(EnumFacing.NORTH,1).offset(EnumFacing.EAST);
		BlockPos e2 = avatarPos.offset(EnumFacing.SOUTH,2).offset(EnumFacing.EAST,3).offset(EnumFacing.UP);
		return new AxisAlignedBB(e1,e2);
	}
	private AxisAlignedBB boundingWest(BlockPos avatarPos) {
		BlockPos e1 = avatarPos.offset(EnumFacing.NORTH,1).offset(EnumFacing.WEST,2);
		BlockPos e2 = avatarPos.offset(EnumFacing.SOUTH,2).offset(EnumFacing.UP);
				return new AxisAlignedBB(e1,e2);
	}
	
	
    private List<Entity> detectEntity(BlockPos avatarPos) {
    	
  	//3x2
    	AxisAlignedBB bounding= new AxisAlignedBB(avatarPos);;
    	
    	switch  (avatar.getAvatarFacing()){
 
	    	case NORTH:
	    		bounding=boundingNorth(avatarPos);break;
			case EAST:
				bounding=boundingEast(avatarPos);break;			
			case SOUTH:
				bounding=boundingSouth(avatarPos);break;			
			case WEST:
				bounding=boundingWest(avatarPos);break;    	
			case DOWN: break;
			case UP: break;
			
		}
        
        List<Entity> detectedEntities;
        //detectedEntities = getWorld().getEntitiesWithinAABB(Entity.class,bounding);
        detectedEntities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounding); //living
        detectedEntities.removeIf(entity -> entity instanceof EntityPlayer);
        
        
        /*
        for (int i =0;i<detectedEntities.size();i++) {
        	Entity ent =detectedEntities.get(i);
        	if (ent instanceof net.minecraft.entity.passive.EntitySheep) {        		
        		((net.minecraft.entity.passive.EntitySheep)ent).setGlowing(true);
        	}
        }*/
        
                
        return detectedEntities;        
		//List<EntityMinecart> carts = world.getEntitiesWithinAABB(EntityMinecart.class, entityRange);		
    }
	

	
	public void readPacketNBT(NBTTagCompound par1nbtTagCompound) {				
		this.breakingData.readPacketNBT(par1nbtTagCompound);
 	}

	public void writePacketNBT(NBTTagCompound par1nbtTagCompound) {
		this.breakingData.writePacketNBT(par1nbtTagCompound);
	}	

	
	public void rodClick(TileElvenAvatar avatar, boolean rithClick) {
		if (!avatar.isEnabled()) {
			return;
		}

		if (isBusy()) return;
		
		if(avatar.getCurrentMana() >= Config.rodManaCost) {
	
			boolean interactedWithBlock = false;
			WeakReference<FakePlayer> avatarPlayer = getRefAndRetryInit();
			if (avatarPlayer == null) {
				return;
			}			
			BlockPos targetPos=getTargetPos();
			if (targetPos!=null) {
				isRodClick=true;
				interactedWithBlock = interactBlock(avatar, avatarPlayer, targetPos,rithClick);
				//interactedWithBlock=(avatarPlayer.get().interactionManager.processRightClick(avatarPlayer.get(), getWorld(), avatarPlayer.get().getHeldItemMainhand(), EnumHand.MAIN_HAND)== EnumActionResult.SUCCESS);
				 
				if(interactedWithBlock) {
					avatar.recieveMana(-Config.rodManaCost);
					checkManaIsEmpty();
					emitResdstoneTimer.emitRedstone();
					if(avatar.getWorld() instanceof WorldServer) {
						new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
						new MessageMana(getPos(),avatar.getCurrentMana());
					}
				}
				isRodClick=false;
			}
		}
	}
	
	
	public void justRightClick(TileElvenAvatar avatar) {
		if (!avatar.isEnabled()) return;
		if (isBusy()) return;				
		if(avatar.getCurrentMana() < Config.rodManaCost) return;
	
		
		
		boolean interactedWithBlock = false;
		WeakReference<FakePlayer> avatarPlayer = getRefAndRetryInit();
		if (avatarPlayer == null) {
			return;
		}			
		
					
		ActionResult<ItemStack> result =avatarPlayer.get().getHeldItemMainhand().getItem().onItemRightClick(getWorld(), avatarPlayer.get(), EnumHand.MAIN_HAND);
		interactedWithBlock=(result.getType()==EnumActionResult.SUCCESS);
		
		if(interactedWithBlock) {
				avatar.recieveMana(-Config.rodManaCost);
				this.fakePlayerToInventory();
				checkManaIsEmpty();
				emitResdstoneTimer.emitRedstone();
				if(avatar.getWorld() instanceof WorldServer) {
					new MessageMoveArm (getPos(),MessageMoveArm.RISE_ARM);
					new MessageMana(getPos(),avatar.getCurrentMana());
				}
		}		
	}

	
	private boolean interactBlock( 
			TileElvenAvatar tileElvenAvatar,
			WeakReference<FakePlayer> fakePlayer,
			BlockPos targetPos, boolean rightClick)
	{
		
		 if(elvenFakePlayer.stackMainHandType().get(0)==ItemStackType.Types.BLOCK){
			//This ain't no block placer!
			return false;
		}
		
		
		IBlockState iblockstate = getWorld().getBlockState(targetPos);
		boolean blockIsAir = iblockstate.getMaterial() == Material.AIR;
		if(!blockIsAir) {
			//ItemStack.EMPTY
			World world = tileElvenAvatar.getWorld();
			EnumActionResult r;	
			if(rightClick)
				r = fakePlayer.get().interactionManager.processRightClickBlock(fakePlayer.get(), world, ItemStack.EMPTY, EnumHand.MAIN_HAND, targetPos, EnumFacing.UP, .5F, .5F, .5F);
			else
			{
				fakePlayer.get().interactionManager.onBlockClicked(targetPos,  EnumFacing.UP);
				r = EnumActionResult.SUCCESS;
			}
				
				//.processL(fakePlayer.get(), world, elvenFakePlayer.stackMainHand(), EnumHand.MAIN_HAND, targetPos, EnumFacing.UP, .5F, .5F, .5F);
			
			
			if (r == EnumActionResult.SUCCESS||r == EnumActionResult.PASS) {
				if(avatar.getWorld() instanceof WorldServer) {
					new MessageMoveArm (getPos(),MessageMoveArm.SWING_ARM);
				}
				return true; //Yay!
			}

		}
		return false;
	}
	
	private boolean interactBlockWithItem( 
			TileElvenAvatar tileElvenAvatar,
			WeakReference<FakePlayer> fakePlayer,
			BlockPos targetPos)
	{
			
		if(elvenFakePlayer.stackMainHandType().get(0)==ItemStackType.Types.BLOCK)  {
			//This ain't no block placer!
			return false;
		}
		IBlockState iblockstate = getWorld().getBlockState(targetPos);
		boolean blockIsAir = iblockstate.getMaterial() == Material.AIR;
		if(!blockIsAir) {
						
			World world = tileElvenAvatar.getWorld();
			EnumActionResult r;
			r = fakePlayer.get().interactionManager.processRightClickBlock(fakePlayer.get(), world, elvenFakePlayer.stackMainHand(), EnumHand.MAIN_HAND, targetPos, EnumFacing.UP, .5F, .5F, .5F);
			if (r == EnumActionResult.SUCCESS) {
				if(avatar.getWorld() instanceof WorldServer) {
					new MessageMoveArm (getPos(),MessageMoveArm.SWING_ARM);
				}
				return true; //Yay!
			}
		}
		return false;
	}
	
		
	public void dropItem(ItemStack stack) {

		if (getWorld().isRemote) return;
			
		BlockPos targetPos = getPos().offset(avatar.getAvatarFacing());			
		EntityItem entityItem = new EntityItem(getWorld(), targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, stack);
		getWorld().spawnEntity(entityItem);
		
	}
		
}


/*
 
//Detect entities
Vec3d base;
Vec3d target;
Vec3d look;
base = new Vec3d(Objects.requireNonNull(player.get()).posX, Objects.requireNonNull(player.get()).posY, Objects.requireNonNull(player.get()).posZ);
	 RayTraceResult toUse;
	 RayTraceResult traceEntity;
	 RayTraceResult trace;
	 look = Objects.requireNonNull(player.get()).getLookVec();
	 target = base.add(new Vec3d(look.x * 5, look.y * 5, look.z * 5));
	 traceEntity = FakePlayerUtils.traceEntities((FakePlayer) player.get(), base, target, world);
	 trace = world.rayTraceBlocks(base, target, false, false, true);
 toUse = trace == null ? traceEntity : trace;
//ItemStack itm = FakePlayerUtils.leftClickInDirection((FakePlayer) player.get(), this.world, this.pos, EnumFacing.UP, world.getBlockState(pos), toUse);
if(toUse.typeOfHit== RayTraceResult.Type.BLOCK) 
*/	