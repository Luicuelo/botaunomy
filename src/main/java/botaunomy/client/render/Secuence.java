package botaunomy.client.render;

import java.util.HashMap;

public class Secuence  {

	
	protected  HashMap<Integer,Range> ranges; 
	protected float duration;
	protected String name;
	
	protected float timeElapsed;
	protected boolean isFinished;
	protected float percent;	

	
	public Secuence(String pname, float pduration) {		
		ranges= new HashMap<Integer ,Range>(); 			
		name=pname;
		duration=pduration;	
		reset();		
	}	
	
	
	public void reset() {
		timeElapsed=0;
		percent=0;
		isFinished=false;
	}
	
	public void update (float pticks) {		
		if (percent<1){
			timeElapsed+=pticks;
			percent=timeElapsed/duration;
		}else {
			percent=1;
			isFinished=true;			
		}
	}	

	public boolean getisFinished() {
		return(isFinished);
	}	
	
	public  String getName() {
		return(name);
	}	
	
	public void addRange(String elementeName,float pinit, float pend)
	{
		Range arc=new Range(elementeName,pinit,pend);
		this.ranges.put(elementeName.hashCode(),arc);		
	}
	
	public boolean isElementInSequence(String elementName) {
		return this.ranges.containsKey(elementName.hashCode());
	}
	
	public float  getValue(String rangeName) {
		Range range=this.ranges.get(rangeName.hashCode());
		if (range!=null)
			return(range.getValue(percent));
		else {
				//FMLLog.log("Botaunomy", Level.INFO, "Value not found: "+this.getName()+" range "+rangeName);
				return 0;
			 }
	}
	
	public boolean rangeExists(String rangeName) {
		return this.ranges.containsKey(rangeName.hashCode());
	}
	
	public float  getEndValue(String rangeName) {
		Range range=this.ranges.get(rangeName.hashCode());		
		return(range.getEndValue());
		//			return(range.getValueSwing());
	}
	
	public float  getInitValue(String rangeName) {
		Range range=this.ranges.get(rangeName.hashCode());		
		return(range.getInitValue());
		//			return(range.getValueSwing());
	}
	
	protected class Range{

		private String name;
		private float init;
		private float end;
		public Range (String pname,float pinit, float pend) {
			name=pname;
			init=pinit;
			end=pend;
		}

		public String getName() {
			return name;
		}
		
		public float getValue(float percent) {			
			return  (init+(end-init)*percent) ;
		}
		
		public float getEndValue() {
			return end;
		}
		
		public float getInitValue() {
			return init;
		}
		
		public float getValueSwing(float percent) {
			
			if (percent<.5)
				return  end-((end-init)*percent*2);
			else
				return  (init+(end-init)*(percent-.5F)*2);
		
			
			
			
		}
		
	}
}

