package botaunomy.network;

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

public class MessageSpectator implements IMessage  {
	
	
	public MessageSpectator() {
	
	}
	
	private BlockPos blockPos;
	private boolean Spectator;
	
    @Override
    public void toBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        buf.writeLong(blockPos.toLong());
        buf.writeBoolean(Spectator);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        blockPos = BlockPos.fromLong(buf.readLong());
        Spectator= buf.readBoolean();
    }
    
	public MessageSpectator(BlockPos pblockpos,boolean pSpectator) {
		blockPos=pblockpos;
		Spectator=pSpectator;
		ModSimpleNetworkChannel.INSTANCE.sendToAll(this);
	}
	public static class MessageSpectatorHandler implements IMessageHandler<MessageSpectator, IMessage> {
	    
		public MessageSpectatorHandler() {
			
		}
		
		@SideOnly(Side.CLIENT)
		@Override
	    public IMessage onMessage(MessageSpectator message, MessageContext ctx) {

			World world = Minecraft.getMinecraft().world;
			IBlockState ibs = world.getBlockState(message.blockPos);
			Block block = ibs.getBlock();
			if (block instanceof ElvenAvatarBlock)
			 if (world.isBlockLoaded(message.blockPos)) {
				 TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(message.blockPos);				 
				 avatar.setPlayerSpectator(message.Spectator);
			 }		
	        return null;
	    }
	}
	
}
