/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy.config;

import org.apache.logging.log4j.Level;

import botaunomy.Botaunomy;
import botaunomy.proxy.CommonProxy;
import net.minecraftforge.common.config.Configuration;

public class Config {

	static final String CATEGORY_GENERAL = "general";
	static final String CATEGORY_DIMENSION = "dimensions";

    // This values below you can access elsewhere in your mod:
    private static String toolsCanBeUsedOnBlock = "pickaxe;hoe;shovel;hatchet;shears";
    private static String toolsCanBeConsumedOnBlock = "dyePowder.white;pestle;petal";
    private static String toolsCanBeUsedWithEntities= "bucket;bowl";
    private static String toolsCanShearEntities= "shears";
    private static String toolsCanAtackEntities= "sword";
    private static String itemsContainMana= "manatablet;capacitor";
    private static String itemsJustRighClick="splash_potion";   
    
    public static String[] onBlockToolsList;
    public static String[] onBlockConsumedToolsList;
    public static String[] entitiesToolsList;
    public static String[] entitiesShearsList;
    public static String[] entitiesAtacksList;
    public static String[] itemsContainManaList;
    public static String[] itemsJustRighClickList;
    
    public static int useManaCost=160;
    public static int rodManaCost=160;
    public static int breakManaCost=160;
    public static int mobSpawnerCostPertick=2;
    
    public static boolean fakePlayersAreAsleep=true;
    public static boolean asleepMessage=false;
    public static boolean disableFakePlayerAddedToWorld=false;
    
    // Call this from CommonProxy.preInit(). It will create our config if it doesn't
    // exist yet and read the values if it does exist.
	public static void readConfig() {
		Configuration cfg = CommonProxy.config;
		try {

            cfg.load();
            initGeneralConfig(cfg);
            //initDimensionConfig(cfg);

		} catch (Exception e1) {
			Botaunomy.logger.log(Level.ERROR, "Problem loading config file!", e1);
		} finally {
			if (cfg.hasChanged()) {
				cfg.save();
			} 
		}
	}
 
	public static void splitStrings() {
        //is needed to check if had shear , by defaut returns always true.
        if (toolsCanBeUsedOnBlock.length()>0)
        	onBlockToolsList = toolsCanBeUsedOnBlock.split(";");              
        if (toolsCanBeConsumedOnBlock.length()>0)
        	onBlockConsumedToolsList = toolsCanBeConsumedOnBlock.split(";");              
        if (toolsCanBeUsedWithEntities.length()>0)
        	entitiesToolsList = toolsCanBeUsedWithEntities.split(";");
        if (toolsCanShearEntities.length()>0)
        	entitiesShearsList = toolsCanShearEntities.split(";");
        if (toolsCanAtackEntities.length()>0)
        	entitiesAtacksList = toolsCanAtackEntities.split(";");
        if (itemsContainMana.length()>0)
        	itemsContainManaList = itemsContainMana.split(";");
        if (itemsJustRighClick.length()>0)
        	itemsJustRighClickList = itemsJustRighClick.split(";");
		
	}
    public static void initGeneralConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        
        fakePlayersAreAsleep=cfg.getBoolean("FakePlayers_AreSleep", CATEGORY_GENERAL, fakePlayersAreAsleep, "if true fake player are asleep, false to use with vote system to sleep");
        asleepMessage=cfg.getBoolean("FakePlayers_asleepMessage", CATEGORY_GENERAL, asleepMessage, "if true print debug message every night");        
        disableFakePlayerAddedToWorld=cfg.getBoolean("FakePlayers_DisableAddedToWorld", CATEGORY_GENERAL, disableFakePlayerAddedToWorld, "if true fake player are no added to word, so cant activate spawners");
        
        toolsCanBeUsedOnBlock = cfg.getString("ToolsCanBeUsedOnBlock", CATEGORY_GENERAL, toolsCanBeUsedOnBlock, "Set name of tools , or part of name, than can be used on block, separated by ;");
        toolsCanBeConsumedOnBlock = cfg.getString("ToolsCanBeConsumedOnBlock", CATEGORY_GENERAL, toolsCanBeConsumedOnBlock, "Set name of tools , or part of name, than can be used on block, but is consumed, separated by ;");        
        toolsCanBeUsedWithEntities = cfg.getString("ToolsCanBeUsedWithEntities", CATEGORY_GENERAL, toolsCanBeUsedWithEntities, "Set name of tools , or part of name, than can be used, separated by ;");
        toolsCanShearEntities = cfg.getString("ToolsCanBeShearEntities", CATEGORY_GENERAL, toolsCanShearEntities, "Set name of tools , or part of name, than can be used to shear, separated by ;");
        toolsCanAtackEntities = cfg.getString("ToolsCanAtackEntities", CATEGORY_GENERAL, toolsCanAtackEntities, "Set name of tools , or part of name, than can be used to attack, separated by ;");
        itemsContainMana = cfg.getString("ItemContainMana", CATEGORY_GENERAL, itemsContainMana, "Set name of items , or part of name, than can contain mana, separated by ;");
        itemsJustRighClick = cfg.getString("itemsJustRighClick", CATEGORY_GENERAL, itemsJustRighClick, "Set name of items , or part of name, right click with no block or entity");
        
        useManaCost=cfg.getInt("Cost_UseManaCost", CATEGORY_GENERAL, useManaCost, 50, 1000, "Mana cost each time avatar uses a tool on entity");
        rodManaCost=cfg.getInt("Cost_RodManaCost", CATEGORY_GENERAL, rodManaCost, 50, 1000, "Mana cost each time avatar uses rod on a block, same for just use a item");
        breakManaCost=cfg.getInt("Cost_BreakManaCost", CATEGORY_GENERAL, breakManaCost, 50, 1000, "Mana cost each time avatar try to break a block");        
        mobSpawnerCostPertick=cfg.getInt("Cost_MobSpawnerCostPertick", CATEGORY_GENERAL, mobSpawnerCostPertick, 1, 10, "Mana cost per tick for ativate mob spawners");

        splitStrings();
        
    }

}
