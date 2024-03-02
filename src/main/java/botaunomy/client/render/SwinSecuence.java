package botaunomy.client.render;

import java.util.HashMap;

import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.FMLLog;

public class SwinSecuence extends Secuence {

	public SwinSecuence(String pname, float pduration) {
		super(pname, pduration);
	}

	@SuppressWarnings("unchecked")
	public SwinSecuence(String pname, float pduration, Secuence secuenceBase) {
		super(pname, pduration);				
		this.ranges=(HashMap<Integer, Range>) secuenceBase.ranges.clone();	
	}

	@Override
	public float getValue(String rangeName) {
		Range range=this.ranges.get(rangeName.hashCode());		
		if (range!=null)
			return(range.getValueSwing(percent));
		else {
			FMLLog.log("Botaunomy", Level.INFO, "Value not found: "+this.getName()+" range "+rangeName);
			return 0;		
		}
	}
	
	@Override
	public void update (float pticks) {		
		if (percent<1){
			timeElapsed+=pticks;
			percent=timeElapsed/duration;
		}else {
			reset();
		}
	}	
}
