/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy.proxy;

import botaunomy.block.tile.TileElvenAvatar;
import botaunomy.client.render.RenderTileElvenAvatar;
import botaunomy.registry.ModRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		TileElvenAvatar.nAvatarClient=0;
		initRenderers();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);		
	}

	@SideOnly(Side.CLIENT)
	private void initRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileElvenAvatar.class, new RenderTileElvenAvatar());
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		ModRegistry.instance.registerModels(event);
	}


}
