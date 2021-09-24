package botaunomy.block.tile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;

import botaunomy.Botaunomy;
import botaunomy.ItemStackType;
import botaunomy.config.Config;
import botaunomy.network.MessagePlayer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.mana.IManaItem;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

@Mod.EventBusSubscriber
public class ElvenFakePlayerHandler  {

	 private WeakReference<FakePlayer> refMyFakePlayer =new WeakReference<> (null); //Used to make the job, but not added to world.
	 EntityPlayer myElvenEntityPlayer=null; //Used to activate mob spawns, added to ws.playerEntities, server world and remote
	 private ArrayList<ItemStackType.Types> typePlayerToolCache;
	 TileElvenAvatar avatarParent;

	 
	public ElvenFakePlayerHandler (World ws,  BlockPos pos,TileElvenAvatar avatar ) {
		MinecraftForge.EVENT_BUS.register(this);
		avatarParent=avatar;
		if (ws==null)return;
		if (!ws.isRemote) {
			initServer( ws,  pos, avatar );		
		}

	}
	
	@SubscribeEvent
	public void onCriticalHit(CriticalHitEvent evt) {		
	  if (!evt.getEntityPlayer().world.isRemote) {
		  if (evt.getEntityPlayer().equals(refMyFakePlayer.get())){
	    	ItemStack itemStack=evt.getEntityPlayer().getHeldItemMainhand() ;
	    	//if (itemStack.getItem() instanceof ItemSword) {		
	    		Multimap<String, AttributeModifier> attr=itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
	    		Collection<AttributeModifier> coldmg=attr.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
	    		float dmg=0;
	    		for(AttributeModifier i : coldmg) {dmg= (float) i.getAmount();}
	    		//System.out.println("DMG: "+dmg); 
		    	evt.setResult( Event.Result.ALLOW );
		    	evt.setDamageModifier(dmg*1.2F);
	    	//}
	    }
	  }
	}		

	public WeakReference<FakePlayer>  getRefAndRetryInit (World ws,  BlockPos pos,TileElvenAvatar avatar )  {
		if(ws.isRemote) return null;
		if (refMyFakePlayer==null) return null;
		if (refMyFakePlayer.get()==null) initServer(ws, pos, avatar);				
		return refMyFakePlayer;
	}
	
	private void initPlayerPos(EntityPlayer player,BlockPos pos,TileElvenAvatar avatar ) {
		
		BlockPos spawnPos= new BlockPos(pos.getX(),pos.getY(),pos.getZ());		
		player.setSpawnPoint(spawnPos, false);
		player.posX=pos.getX()+0.5F;
		player.posY=pos.getY()-0.5F;
		player.posZ=pos.getZ()+0.5F;		

	}
	
	public void initClient(World w, BlockPos pos,TileElvenAvatar avatar)  {
		if (!w.isRemote) return;
		if (Config.disableFakePlayerAddedToWorld)return;
		
		UUID nameUuid=avatar.getUUID();
		if (nameUuid==null) return;
		if (myElvenEntityPlayer==null) {
			EntityPlayer f=findPlayer(w, nameUuid);
				if (f==null) {
				int navatar=TileElvenAvatar.nAvatarClient;
				TileElvenAvatar.nAvatarClient++;
				GameProfile gameProfile=new GameProfile(nameUuid, "ElvenAvt"+navatar+"_"+nameUuid.toString().substring(0,5));//ModInfo.modid
				myElvenEntityPlayer=new ElvenEntityPlayer(w, gameProfile);			
				((IsetSpectator)myElvenEntityPlayer).setSpectator(avatar.playerIsSpectator);
				initPlayerPos(myElvenEntityPlayer,pos,avatar);			
				w.playerEntities.add(myElvenEntityPlayer);
				//w.spawnEntity(myElvenEntityPlayer);
			}else myElvenEntityPlayer=f;
		}

	}
	
	public void setSpectator (boolean value) {
		if (myElvenEntityPlayer!=null)
			((IsetSpectator)myElvenEntityPlayer).setSpectator(value);
	}
	
	public boolean isSpectator() {
		if (myElvenEntityPlayer!=null)
			return ((IsetSpectator)myElvenEntityPlayer).getSpectator();
		return true;
	}
	
	private void setFakeConnection(ElvenFakePlayer fakeplayer) {
		fakeplayer.connection = new NetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.SERVERBOUND), refMyFakePlayer.get()) {
			@SuppressWarnings("rawtypes")
			@Override
			public void sendPacket(Packet packetIn) {}
		};	
		
	}
	
	private EntityPlayer findPlayer(World w, UUID nameUuid) {
		List<EntityPlayer> entities= w.playerEntities;
		for (int i=0;i<entities.size();i++) {
			if (entities.get(i).getGameProfile().getId().equals(nameUuid)) {
				return entities.get(i);
			}
		}
		return null;
	}
	
	public void  removePlayer(World w, TileElvenAvatar avatar) {
		if (w==null) return;
		UUID nameUuid=avatar.getUUID();
		List<EntityPlayer> entities= w.playerEntities;
		for (int i=0;i<entities.size();i++) {
			if (entities.get(i).getGameProfile().getId().equals(nameUuid)) {
				entities.remove(i);
				break;
			}
		}		
	}
	
	private void initServer(World ws, BlockPos pos,TileElvenAvatar avatar)  { 
		try {
			
			if (!(ws instanceof WorldServer)) return;
			WorldServer wss=(WorldServer) ws;			
			UUID nameUuid=avatar.getUUID();
			if (nameUuid==null) { 
				nameUuid=UUID.randomUUID();
				avatar.setUUID(nameUuid);
			}		
			GameProfile gameProfile=new GameProfile(nameUuid, "FkElvenAvt"+"_"+nameUuid.toString().substring(0,5));//ModInfo.modid
			ElvenFakePlayer myFakePlayer=new ElvenFakePlayer(wss, gameProfile); //returns not espectator
			refMyFakePlayer = new WeakReference<>(myFakePlayer);	
			//fakePlayer = new WeakReference<FakePlayer>(FakePlayerFactory.get(ws, profile));
			//fakePlayer = new WeakReference<>(FakePlayerUtils.get(ws, new GameProfile(uname, ModInfo.modid)));
			
			setFakeConnection(myFakePlayer);
						
			initPlayerPos((ElvenFakePlayer)myFakePlayer,pos,avatar);	
			myFakePlayer.rotationYaw = getYaw(avatar.getAvatarFacing());						
			myFakePlayer.rotationPitch=0;
			
			
			if (myElvenEntityPlayer==null&& !Config.disableFakePlayerAddedToWorld) {
				EntityPlayer f=findPlayer(ws, nameUuid);
				if (f==null) {
					int navatar=TileElvenAvatar.nAvatarServer;
					TileElvenAvatar.nAvatarServer++;
					GameProfile gameProfileCopy=new GameProfile(nameUuid, "ElvenAvt"+navatar);//ModInfo.modid
					ElvenFakePlayer myElvenEntityPlayerCopy=new ElvenFakePlayer(wss, gameProfileCopy);						
					myElvenEntityPlayerCopy.copyFrom(myFakePlayer, false);
					myElvenEntityPlayerCopy.setSpectator(avatar.playerIsSpectator);
					initPlayerPos(myElvenEntityPlayerCopy,pos,avatar);			
					setFakeConnection(myElvenEntityPlayerCopy);
					myElvenEntityPlayerCopy.goToSleep(Config.fakePlayersAreAsleep);
					ws.playerEntities.add(myElvenEntityPlayerCopy); //added as player
					myElvenEntityPlayer=myElvenEntityPlayerCopy;
					//w.spawnEntity(myElvenEntityPlayer);
				}else myElvenEntityPlayer=f;
				
				new MessagePlayer(pos,nameUuid); //activate client side.
			}	
							
			
			
			this.inventoryToFakePlayer(avatar);			
			ItemStack fakeTablet=new ItemStack(new FakeTable());
			myFakePlayer.inventory.mainInventory.set(35, fakeTablet);

		}
		catch (Exception e) {
			Botaunomy.logger.error("Exception thrown trying to create fake player : " + e.getMessage());
			refMyFakePlayer= null;
		}
		
	}
	
	public static float getYaw(EnumFacing currentFacing) {
		switch (currentFacing) {
		case DOWN:
		case UP:
		case SOUTH:
		default:
			return 0;
		case WEST:
			return 90f;
		case NORTH:
			return 180f;
		case EAST:
			return 270f;
		}
	}
	
	public   ArrayList<ItemStackType.Types>  stackMainHandType(){
		return typePlayerToolCache;
	} 
	
	public ItemStack stackMainHand() {
		if(refMyFakePlayer.get()!=null)
			return refMyFakePlayer.get().getHeldItemMainhand();
		else 
			return ItemStack.EMPTY;
	}
	
	public void fakePlayerToInventory(TileElvenAvatar avatar ) {
		 FakePlayer player=refMyFakePlayer.get();
		 if (player==null) return;
		 ItemStack stack=player.getHeldItem(EnumHand.MAIN_HAND);
		 if (stack==null || stack== ItemStack.EMPTY) return;
		 
		 if (stack.isEmpty()) avatar.getInventory().empty0();
		 else 	avatar.getInventory().set0(stack);
		 
		 typePlayerToolCache=avatar.getInventory().getType0();	
		 
	}	
	
	public void inventoryToFakePlayer(TileElvenAvatar avatar ) {
		 FakePlayer player=refMyFakePlayer.get();
		 if (player!=null) {
			 
				ItemStack inventoryItem = avatar.getInventory().get0();
				
				if (inventoryItem!=null && !(inventoryItem.isEmpty())) {
					
					//player.inventory.mainInventory.set(player.inventory.currentItem, inventoryItem);
					//player.getAttributeMap().applyAttributeModifiers(inventoryItem.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
					player.setHeldItem(EnumHand.MAIN_HAND, inventoryItem);
					typePlayerToolCache=avatar.getInventory().getType0();

				}else
				{
					//player.inventory.mainInventory.clear();
					//player.inventory.mainInventory				
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
					typePlayerToolCache=new ArrayList<ItemStackType.Types>();
					typePlayerToolCache.add(ItemStackType.Types.NONE) ;
														
				}
		 }
	}	
	
	public interface IsetSpectator {

		public void setSpectator (boolean value) ;
		public boolean getSpectator();
	}
	
	public class ElvenEntityPlayer extends EntityPlayer implements IsetSpectator{
		private boolean spectatorValue=false; //equal to server.
		private boolean elvenSleeping;

		public ElvenEntityPlayer(World worldIn, GameProfile gameProfileIn) {
			super(worldIn, gameProfileIn);
			this.onGround = true;
			this.setSilent(true);	
			this.setSneaking(false);
			this.sleeping=false;	
			this.elvenSleeping=false;
		}
		
		@Override
		public boolean isPlayerFullyAsleep() {
			return this.elvenSleeping;			
		}
		
		@Override
	    public boolean isPlayerSleeping()
	    {
	        return this.elvenSleeping;
	    }
		
		@Override
		public void setSpectator (boolean value) {
			spectatorValue=value;
		}

		@Override
		public boolean getSpectator() {
			return spectatorValue;
		}
	
		@Override
		public boolean isSpectator() {
			return spectatorValue;
		}

		@Override
		public boolean isCreative() {
			return true;
		}

	}

	public class ElvenFakePlayer extends FakePlayer implements IsetSpectator{
		private boolean spectatorValue=false;
		private boolean elvenSleeping;
		public ElvenFakePlayer(WorldServer world, GameProfile name) {
			super(world, name);
			this.onGround = true;
			this.setSilent(true);	
			this.setSneaking(false);			
		}
		
		public void  goToSleep(boolean value) {
			this.elvenSleeping=value;
		}
		
		@Override
		public boolean isPlayerFullyAsleep() {
			return this.elvenSleeping;			
		}
		
		@Override
	    public boolean isPlayerSleeping()
	    {
	        return this.elvenSleeping;
	    }
				
		@Override
		public void setSpectator (boolean value) {
			spectatorValue=value;
		}
		
		@Override
		public boolean getSpectator() {
			return spectatorValue;
		}
		
		@Override
		public boolean isSpectator() {
			return spectatorValue;
		}
		
		@Override
	    public float getCooledAttackStrength(float adjustTicks)
	    {
			return super.getCooledAttackStrength(adjustTicks);
	    }
			
		@Override
	    public void onUpdate()
	    {
	     	super.onUpdate();
	     	++this.ticksSinceLastSwing;
	    }
				
	}

	private class FakeTable extends Item implements  IManaItem{

		@Override
		public int getMana(ItemStack stack) {
			if (avatarParent!=null)
				return avatarParent.getCurrentMana();
			return 0;
		}

		@Override
		public int getMaxMana(ItemStack stack) {
			return TileElvenAvatar.MAX_MANA;
		}

		@Override
		public void addMana(ItemStack stack, int mana) {
			if (avatarParent!=null)
				avatarParent.recieveMana(mana);			
		}

		@Override
		public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
			return false;
		}

		@Override
		public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
			return false;
		}

		@Override
		public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
			return false;
		}

		@Override
		public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
			return true;
		}

		@Override
		public boolean isNoExport(ItemStack stack) {
			return false;
		}
		
	}
	
	
	
}
