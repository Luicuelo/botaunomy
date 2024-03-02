package botaunomy.registry;

/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Nibbler, which is open source:
 * https://github.com/oneandonlyflexo/nibbler
 ******************************************************************************/

import java.util.ArrayList;




import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;


/**
 * This class is used for making automatically registering items easier.  All a mod has to do to make it so that
 * blocks/items get auto-registered is create a mod-local NibblerRegistry and then have its blocks/items implement
 * NibblerRegisteredObject.  The mod also has to make sure that when you would normally register blocks/items, the
 * mod-local NibblerRegistry has it's registration methods called.
 *
 * @author "oneandonlyflexo"
 */
@Mod.EventBusSubscriber
public class Registry {

	private ArrayList<BlockBase> blocks = new ArrayList<>();
	private ArrayList<BlockBase> blockItems = new ArrayList<>();
	private ArrayList<ItemBase> items = new ArrayList<>();

	public void addBlockForRegistry(BlockBase block, boolean addItem) {
		blocks.add(block);
		if(addItem) {
			blockItems.add(block);
		}
	}

	public void addItemForRegistry(ItemBase item) {
		items.add(item);
	}

	public void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();		
		
		for(int i = 0; i < blocks.size(); i++) {
			
			blocks.get(i).setRegistryName(blocks.get(i).resourceLocation);
			blocks.get(i).setUnlocalizedName(UtilResourceLocation.getUnlocalizedName(blocks.get(i).resourceLocation));			
			registry.register(blocks.get(i));
		}
	}

	public void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		
		for(int i = 0; i < items.size(); i++) {
			items.get(i).setRegistryName(items.get(i).resourceLocation);
			items.get(i).setUnlocalizedName(UtilResourceLocation.getUnlocalizedName(items.get(i).resourceLocation));
			registry.register(items.get(i));		
		}

		for(int i = 0; i < blockItems.size(); i++) {			
			ItemBlock itemBlock=new ItemBlock (blockItems.get(i));
			ResourceLocation rl=new ResourceLocation (UtilResourceLocation.getItemBlockName(blockItems.get(i).resourceLocation)) ;
					
					
			
			
			//NibbleResourceLocation rl=blockItems.get(i).nibbleResourceLocation;
			itemBlock.setRegistryName(rl);
			itemBlock.setUnlocalizedName(UtilResourceLocation.getUnlocalizedName(rl));					
			registry.register(itemBlock);
		}

		for(int i = 0; i < blocks.size(); i++) {
			Block block = blocks.get(i);
			if (block.hasTileEntity())
				if(block instanceof TileEntityRegisteredBlocked) {
					GameRegistry.registerTileEntity(((TileEntityRegisteredBlocked)block).getTileEntityClass(), block.getRegistryName().toString());					
				}
		}
	}

	public void registerModels(ModelRegistryEvent  event) {		
		for(int i = 0; i < blockItems.size(); i++) {
			blocks.get(i).registerModels();
		}

		for(int i = 0; i < items.size(); i++) {
			items.get(i).registerModels();
		}
	}
	

}