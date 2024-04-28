/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy.registry;

import botaunomy.ModInfo;
import botaunomy.block.tile.TileElvenAvatar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.client.core.handler.ModelHandler;

/**
 * This was mostly copied from Botania, but I imagine it will get heavily modified and customized once I find my
 * voice in this modding business.
 *
 * @author "oneandonlyflexo"
 */
public abstract class BlockBase extends Block  {
	
	public ResourceLocation resourceLocation;
	
	public BlockBase(Material par2Material, String name) {
		super(par2Material);
		if(ModRegistry.tab != null) {
			setCreativeTab(ModRegistry.tab);
		}
		resourceLocation= new ResourceLocation(ModInfo.modid, name);
		ModRegistry.instance.addBlockForRegistry(this, true);
	}

	
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		if(Item.getItemFromBlock(this) != Items.AIR)
			ModelHandler.registerBlockToState(this, 0, getDefaultState());
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	public void customRegisterModels() {
		ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(BotaniaStateProps.CARDINALS).build());
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(this), 0, TileElvenAvatar.class);
		ModelResourceLocation mrl = new ModelResourceLocation(UtilResourceLocation.getItemBlockName(resourceLocation), "inventory");
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, mrl);
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
		super.eventReceived(state, world, pos, id, param);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity != null ? tileentity.receiveClientEvent(id, param) : false;
	}
		

}
