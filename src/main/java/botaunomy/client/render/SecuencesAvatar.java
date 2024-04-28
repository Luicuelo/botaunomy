package botaunomy.client.render;

import java.util.HashMap;

public class SecuencesAvatar {

	//only can be one secuence active
	
	private HashMap<Integer,Secuence> secuences=new HashMap<Integer,Secuence>();
	private String SecuenceActiveSecuenceName="";
	
	public SecuencesAvatar() {
		init();
	}
	
	public  void init() {
		Secuence riseArm=addSecuence("RiseArm", 50);		
		riseArm.addRange("Arm", 0, -3.1416F*(3F/4F));
		riseArm.addRange("toolRotate", -70F+90F, -160F+90F);
		riseArm.addRange("toolOffsetY", 2.1F , 0.9F-.35F);
		riseArm.addRange("toolCorrection", 0F , (float)Math.PI+0.5F);
		Secuence casterArm=addSecuence("CasterArm", 50);		
		casterArm.addRange("Arm", 0, -(float)Math.PI*(3F/4F));
		casterArm.addRange("toolRotate", 75F,-50F); //En grados, negativos hacia arriba
		casterArm.addRange("toolOffsetY", 2.7F ,1.0F);//mas grande , mas abajo 1.1
		casterArm.addRange("toolCorrection",0.05F , -0.6F);//mas pequeño mas a la derecha
		casterArm.addRange("angle",0F ,(float) Math.PI);
	
		//casterArm.addRange("toolRotate", 73F,-50F); //En grados, negativos hacia arriba
		//casterArm.addRange("toolOffsetY", 2.6F ,1.0F);//mas grande , mas abajo 1.1
		//casterArm.addRange("toolCorrection",-0.1F , -0.6F);//mas pequeño mas a la derecha
	
		
		Secuence downArm=addSecuence("DownArm", 50);
		downArm.addRange("Arm",  -3.1416F*(3F/4F),0);
		
		SwinSecuence swingArm=new SwinSecuence ("swingArm", 25,riseArm );
		SwinSecuence swingCaster=new SwinSecuence ("swingCaster", 100,casterArm );
		addSecuence(swingArm);
		addSecuence(swingCaster);
		addSecuence(casterArm);
			
	}

	private   Secuence addSecuence(String s,float pduration) {
		Secuence secuence=new Secuence (s, pduration);
		return addSecuence(secuence);
	}
	
	private   Secuence addSecuence(Secuence secuence) {
		secuences.put(secuence.name.hashCode(), secuence);
		return secuence;
	}
	
	
	
	public boolean isElemenActiveSecuence(String elementName) {		
		if (!SecuenceActiveSecuenceName.equals("")) {
			Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());	
			return(secuence.isElementInSequence(elementName));
		}else return false;
	}
	
	public void ActivateSecuence(String secuenceName) {		
		if (SecuenceActiveSecuenceName.equals(secuenceName) )return; //only reset if distinct
		SecuenceActiveSecuenceName=secuenceName;
		Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());
		secuence.reset(); 		
	}
	
	public void update(float pticks) {
		if (!SecuenceActiveSecuenceName.equals("")) {;
			Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());
			if(secuence!=null)  {              
				secuence.update(pticks);				
			}
			//else FMLLog.log("Botaunomy", Level.INFO, "Secuence not found: "+SecuenceActiveSecuenceName);
		}
	}
	
	public float getValue(String elementName) {		
		if (!SecuenceActiveSecuenceName.equals("")) {
			Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());
			return(secuence.getValue(elementName));
		}else return 0F;
	}
	
	public boolean elementExists(String elementName) {
		if (!SecuenceActiveSecuenceName.equals("")) {
			Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());
			return(secuence.rangeExists(elementName));
		}else return false;
	}
	
	public float getEndValue(String SecuenceName,String elementName) {				
		Secuence secuence=secuences.get(SecuenceName.hashCode());
		return(secuence.getEndValue(elementName));

	}
	
	public float getInitValue(String SecuenceName,String elementName) {				
		Secuence secuence=secuences.get(SecuenceName.hashCode());
		return(secuence.getInitValue(elementName));

	}
	
	
	public boolean isActive() {
		if (SecuenceActiveSecuenceName.equals(""))return false;		
		Secuence secuence=secuences.get(SecuenceActiveSecuenceName.hashCode());	
		return !secuence.isFinished;
	}
}
