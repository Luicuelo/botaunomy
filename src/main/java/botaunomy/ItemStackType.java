package botaunomy;

import java.util.ArrayList;

import botaunomy.config.Config;
import botaunomy.item.RodItem;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.item.IAvatarWieldable;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.mana.IManaItem;

public class ItemStackType {

    
	public static enum Types {
		NONE,
    	BREAK,
    	SHEAR,
    	USE,
    	KILL,  
    	MANA,
    	JUSTRC,
    	ROD_WILL,
    	ROD_WORK,
    	ROD_AVATAR,
    	EYE,
    	BLOCK
      }
    
	public ItemStackType() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean isStackType(ArrayList<ItemStackType.Types>   typesArray,Types type) {		
		for (int i=0;i<typesArray.size();i++) {
			if(typesArray.get(i)==type) return true;
		}
		return false;
	}
	
	public static  ArrayList<ItemStackType.Types>  getTypeTool(ItemStack i) {

		String s=i.getUnlocalizedName().toLowerCase();
		ArrayList<Types> sal=new ArrayList<ItemStackType.Types> ();


		
		if(i.isEmpty()) sal.add(Types.NONE);
		
		if (i.getItem() instanceof RodItem && s.contains("rod_will")) sal.add(Types.ROD_WILL);
		if (i.getItem() instanceof RodItem && s.contains("rod_work")) sal.add(Types.ROD_WORK);
		 
		if (Config.onBlockToolsList!=null)
		for(int a = 0;a<Config.onBlockToolsList.length; a++) {
			if (s.toLowerCase().contains(Config.onBlockToolsList[a].toLowerCase())) {
				sal.add(Types.BREAK);
				break;//not add twice same group
			}
		}
		if (Config.entitiesShearsList!=null)
		for(int a = 0;a<Config.entitiesShearsList.length; a++) {
			if (s.toLowerCase().contains(Config.entitiesShearsList[a].toLowerCase())) {
				sal.add(Types.SHEAR);
				break;
			}
		}	
		if (Config.entitiesToolsList!=null)
		for(int a = 0;a<Config.entitiesToolsList.length; a++) {
			if (s.toLowerCase().contains(Config.entitiesToolsList[a].toLowerCase())) {
				sal.add(Types.USE);
				break;
			}
		}
		if (Config.entitiesAtacksList!=null)
		for(int a = 0;a<Config.entitiesAtacksList.length; a++) {
			if (s.toLowerCase().contains(Config.entitiesAtacksList[a].toLowerCase())) {
				sal.add(Types.KILL);
				break;
			}
		}
		if (Config.itemsJustRighClickList!=null)
		for(int a = 0;a<Config.itemsJustRighClickList.length; a++) {
			if (s.toLowerCase().contains(Config.itemsJustRighClickList[a].toLowerCase())) {
				sal.add(Types.JUSTRC);
				break;
			}
		}	
		if (Config.itemsContainManaList!=null)
		for(int a = 0;a<Config.itemsContainManaList.length; a++) {
				Item item=i.getItem();
			    boolean haveMana=item instanceof IManaItem ;
			    haveMana|=item instanceof IManaDissolvable ;			    
				if (haveMana  &&  s.toLowerCase().contains(Config.itemsContainManaList[a])) {
					sal.add(Types.MANA);
					break;				
				}
		}			

		if (s.toLowerCase().contains("eyeofender")||s.toLowerCase().contains("thirdeye"))
			sal.add(Types.EYE);
		
		if (i.getItem() instanceof IAvatarWieldable)
			sal.add(Types.ROD_AVATAR);
		
		if (sal.size()==0)
			if (Block.getBlockFromItem(i.getItem()) != Blocks.AIR) sal.add(Types.BLOCK);
		
		if (sal.size()==0) sal.add(Types.NONE);
		
		return sal;
	}

}
