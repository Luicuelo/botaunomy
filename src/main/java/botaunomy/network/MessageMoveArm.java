package botaunomy.network;

import botaunomy.block.tile.ElvenAvatarBlock;
import botaunomy.block.tile.TileElvenAvatar;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageMoveArm implements IMessage  {
	
	public MessageMoveArm() {
	
	}
	
	public static final int SWING_ARM=1;
	public static final int RISE_ARM=2;
	public static final int DOWN_ARM=3;
	public static final int CASTER_ARM=4;
	public static final int SWINGC_ARM=5;  
	
	private BlockPos blockPos;
	private int nSecuencia;
	
    @Override
    public void toBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        buf.writeLong(blockPos.toLong());
        buf.writeInt(nSecuencia);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        blockPos = BlockPos.fromLong(buf.readLong());
        nSecuencia=buf.readInt();
    }
    
	public MessageMoveArm(BlockPos pblockpos, int pNSecuencia) {
		blockPos=pblockpos;
		nSecuencia=pNSecuencia;
		ModSimpleNetworkChannel.INSTANCE.sendToAll(this);
	}
	public static class MessageMoveArmHandler implements IMessageHandler<MessageMoveArm, IMessage> {
	    
		public MessageMoveArmHandler() {
			
		}
		
		@SideOnly(Side.CLIENT)
		@Override
	    public IMessage onMessage(MessageMoveArm message, MessageContext ctx) {
			
			World world = Minecraft.getMinecraft().world; 
			if(!world.isRemote) return null;//isRemote=true, client
			if (world.getBlockState(message.blockPos).getBlock() instanceof ElvenAvatarBlock)
			 if (world.isBlockLoaded(message.blockPos)) {
				 TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(message.blockPos);
				 int nSecuencia=message.nSecuencia;
				 if (nSecuencia==SWING_ARM)
					 avatar.secuencesAvatar.ActivateSecuence("swingArm");	
				 if (nSecuencia==RISE_ARM)
					 avatar.secuencesAvatar.ActivateSecuence("RiseArm");
				 if (nSecuencia==DOWN_ARM)
					 avatar.secuencesAvatar.ActivateSecuence("DownArm");
				 if (nSecuencia==CASTER_ARM)
					 avatar.secuencesAvatar.ActivateSecuence("CasterArm");
				 if (nSecuencia==SWINGC_ARM)
					 avatar.secuencesAvatar.ActivateSecuence("swingCaster");

 
			 }		
	        return null;
	    }
	}
	
}