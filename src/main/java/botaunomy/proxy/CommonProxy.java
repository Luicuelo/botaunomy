/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import botaunomy.ModBlocks;
import botaunomy.ModDimensions;
import botaunomy.ModItems;
import botaunomy.block.tile.ElvenAvatarBlock;
import botaunomy.config.Config;
import botaunomy.network.ModSimpleNetworkChannel;
import botaunomy.registry.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod.EventBusSubscriber
public abstract class CommonProxy {

	//	public static GrimoireShelfBlock blockGrimoireShelf;
	//	public static ItemBlock itemBlockGrimoireShelf;
	public static Configuration config;


	
	/**
	 * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
	 * @param event
	 */
	public void preInit(FMLPreInitializationEvent e) {
		File directory = e.getModConfigurationDirectory();
		config = new Configuration(new File(directory.getPath(), "botaunomy.cfg"));
		Config.readConfig();

		ModBlocks.init();
		ModItems.init();
		ModDimensions.init();
		
		ModSimpleNetworkChannel.registerMessages();

	}

	/**
	 * Do your mod setup. Build whatever data structures you care about. Register recipes,
	 * send FMLInterModComms messages to other mods.
	 * @param e
	 */
	public void init(FMLInitializationEvent e) {
		
	}

	/**
	 * Handle interaction with other mods, complete your setup based on this.
	 * @param e
	 */
	public void postInit(FMLPostInitializationEvent e) {
		Config.setThaumcraftLoaded(Loader.isModLoaded("thaumcraft"));
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		ModRegistry.instance.registerBlocks(event);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		ModRegistry.instance.registerItems(event);
		//ModBlocks.registerTileEntities();
	}
		
	@SuppressWarnings("deprecation")
	@SubscribeEvent
    public static  void onBlockPlaced(BlockEvent.PlaceEvent event) {
		if (event.getWorld().isRemote) return;
	    try {	
	    	ItemStack stack = event.getItemInHand(); 	        
	        Block block = event.getPlacedBlock().getBlock(); 
	        if (block!=null&&block instanceof ElvenAvatarBlock) {
	        	if(stack!=null)((ElvenAvatarBlock)block).onBlockPlaced(event.getWorld(),event.getPos(), stack);
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}  
	
   
}
