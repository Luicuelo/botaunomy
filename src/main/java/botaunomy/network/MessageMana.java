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

public class MessageMana implements IMessage  {
	
	public MessageMana() {
	
	}
	
	private BlockPos blockPos;
	private int mana;
	
    @Override
    public void toBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        buf.writeLong(blockPos.toLong());
        buf.writeInt(mana);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Encoding the position as a long is more efficient
        blockPos = BlockPos.fromLong(buf.readLong());
        mana=buf.readInt();
    }
    
	public MessageMana(BlockPos pblockpos, int pmana) {
		blockPos=pblockpos;
		mana=pmana;		
		ModSimpleNetworkChannel.INSTANCE.sendToAll(this);
	}
	public static class MessageManaHandler implements IMessageHandler<MessageMana, IMessage> {
	    
		public MessageManaHandler() {
			
		}
		
		@SideOnly(Side.CLIENT)
		@Override
	    public IMessage onMessage(MessageMana message, MessageContext ctx) {

			World world = Minecraft.getMinecraft().world;
			IBlockState ibs = world.getBlockState(message.blockPos);
			Block block = ibs.getBlock();
			if (block instanceof ElvenAvatarBlock)
			 if (world.isBlockLoaded(message.blockPos)) {
				 TileElvenAvatar avatar = (TileElvenAvatar) world.getTileEntity(message.blockPos);
				 avatar.setmana(message.mana);
			 }		
	        return null;
	    }
	}
	
}
