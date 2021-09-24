package botaunomy.config;



import botaunomy.ModInfo;
import botaunomy.proxy.CommonProxy;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;


public class ConfigGui implements IModGuiFactory 
{
	@Override
	public void initialize(Minecraft minecraftInstance) {
		
		
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
	    return new myConfigGui(parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {

		return null;
	}
	
	public class myConfigGui extends GuiConfig 
	{
	    public myConfigGui(GuiScreen parent) 
	    {
	        super(parent,
	        		new ConfigElement(CommonProxy.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
	                ModInfo.modid, 
	                false, 
	                false, 
	                "Botaunomy Config");
	        
	        titleLine2 = CommonProxy.config.getConfigFile().getAbsolutePath();
	    }
	    
	    @Override
	    public void initGui()
	    {
	        // You can add buttons and initialize fields here
	        super.initGui();
	    }

	    
	    @Override
	    public void drawScreen(int mouseX, int mouseY, float partialTicks)
	    {
	        // You can do things like create animations, draw additional elements, etc. here
	        super.drawScreen(mouseX, mouseY, partialTicks);
	    }

	    @Override
	    protected void actionPerformed(GuiButton button)
	    {
	        // You can process any additional buttons you may have added here
	    	if (button.id==2000)
			if (CommonProxy.config.hasChanged()) {
					CommonProxy.config.save();
					Config.initGeneralConfig(CommonProxy.config);
			} 
	    	
	        super.actionPerformed(button);
	    }

	}
}

