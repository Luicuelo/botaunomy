/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import botaunomy.command.HandCommand;
import botaunomy.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


/**
 * Botaunomy! The oddly spelled, slightly quirky addon to Botania. This mod is my first foray into modding since waaay
 * back in the beta 1.5-1.7 days.  It's a wild ride and if you're reading this I hope you enjoy my code and don't
 * think too harshly of me.  I have an 18 month old atm and am sacrificing a bit of my already hectic sleep schedule
 * to work on this.
 *
 * @author "oneandonlyflexo"
 */

@Mod(modid = ModInfo.modid,
name = ModInfo.name,
version = ModInfo.version,
useMetadata = true,
dependencies = "required-after:botania",
acceptedMinecraftVersions = "[1.12,1.12.2]",
acceptableRemoteVersions = "[0.3.5]",
guiFactory = ModInfo.modid+".config.ConfigGui")

public class Botaunomy
{

	private boolean isNight;
	private boolean lastState = false;
	
	@Mod.Instance
	public static Botaunomy instance;

	//Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="botaunomy.proxy.ClientProxy", serverSide="botaunomy.proxy.ServerProxy")
	public static CommonProxy proxy;

	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		proxy.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
		MinecraftForge.EVENT_BUS.register(this);
	}	
	
    @Mod.EventHandler
    public void init(FMLServerStartingEvent event)
    {
      //logger.info("initalise FMLServerStartingEvent :" + NAME);
      event.registerServerCommand(new HandCommand());
    }
    
    @SubscribeEvent
    public void worldTickEvent(WorldTickEvent event) {
       if (!event.world.isRemote)
       {
    	   
    	   // This is called every tick, do something every 20 ticks
    	   if (event.world.getWorldTime() % 20L != 10 || event.phase != TickEvent.Phase.END)return;
    	   boolean haveBeenChange=false;		
           
           if (event.world.getWorldTime()>=13000)        	   
           {
               if (event.world.playerEntities.size() > 0)
               {
            	   isNight=true;
            	   if (isNight!=lastState) haveBeenChange=true;
            	   lastState=isNight;
            	   if (!haveBeenChange)return;
            	   
                   String message = "Its night:";
                   WorldServer ws=(WorldServer) event.world;
                   List<EntityPlayer> list= ws.playerEntities;
                   List<EntityPlayer> listReal= new ArrayList<EntityPlayer>();
                   
                   int total=list.size();
                   int asleep=0;
                   for(EntityPlayer p:list) {
                	   if (!(p instanceof FakePlayer)) listReal.add(p);                		                   	   
                	   if (!p.isPlayerFullyAsleep()) asleep++;
                   }
                   
                   message+=" Asleep; "+asleep+"/"+total;
                   TextComponentString text = new TextComponentString(message);
                   text.getStyle().setColor(TextFormatting.GREEN);
                   
                   for(EntityPlayer p:listReal) {                	  
                	   p.sendMessage(text);
                   }
               }               
           }else{ 
        	   isNight=false;
        	   if (isNight!=lastState) haveBeenChange=true;        	   
        	   lastState=isNight;
           }
       }
    }
}
