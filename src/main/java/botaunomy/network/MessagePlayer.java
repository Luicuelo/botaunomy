package botaunomy.network;

import java.util.UUID;

import botaunomy.block.tile.ElvenAvatarBlock;
import botaunomy.block.tile.TileElvenAvatar;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessagePlayer implements IMessage  {
	
	
	public MessagePlayer() {
	
	}

	private  UUID playerUuid;
	private BlockPos blockPos;
	
    @Override
    public void toBytes(ByteBuf buf) {
    	buf.writeLong(blockPos.toLong());
    	buf.writeInt(playerUuid.toString().length());
    	buf.writeBytes(playerUuid.toString().getBytes());
    }

    @Override
    public void fromBytes(ByteBuf buf) {        
    	blockPos = BlockPos.fromLong(buf.readLong());
        int snLen = buf.readInt();        
        byte[] bytesPlayerUuid = new byte[snLen];
        buf.readBytes(bytesPlayerUuid);                
        String  stringUUID= new String(bytesPlayerUuid); 
        playerUuid=UUID.fromString(stringUUID);   
    }
    
	public MessagePlayer(BlockPos pblockpos,UUID pplayerUuid) {
		blockPos=pblockpos;
		playerUuid=pplayerUuid;
		ModSimpleNetworkChannel.INSTANCE.sendToAll(this);
	}
	public static class MessagePlayerHandler implements IMessageHandler<MessagePlayer, IMessage> {
	    
		public MessagePlayerHandler() {
			
		}
		
		@SideOnly(Side.CLIENT)
		@Override
	    public IMessage onMessage(MessagePlayer message, MessageContext ctx) {

			World world = Minecraft.getMinecraft().world;
			IBlockState ibs = world.getBlockState(message.blockPos);
			Block block = ibs.getBlock();
			if (block instanceof ElvenAvatarBlock)
			 if (world.isBlockLoaded(message.blockPos)) {
				 TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(message.blockPos);
				 avatar.setClientUUID(message.playerUuid);
			 }		
	        return null;
	    }
	}
	
}
