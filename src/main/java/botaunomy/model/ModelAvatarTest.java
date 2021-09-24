package botaunomy.model;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import botaunomy.ModResources;
import botaunomy.block.tile.TileElvenAvatar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
//defines a Java class form Model Json
public  class ModelAvatarTest 
{
private static MyModelBase myModelBase= new MyModelBase();	
private static HashMap<String,ModelRenderer> modelRendererList= loadModelAvatarTest ();

private static class MyModelBase extends ModelBase{	
	public MyModelBase() {}
}
private static class Point{
	public float x;
	public float y;
	public float z;	
	public Point() {}
	public Point(float px , float py , float pz) {
		this();
		x=px;
		y=py;
		z=pz;
	}	
	public Point (JsonArray array) {
		x=array.get(0).getAsFloat();
		y=array.get(1).getAsFloat();
		z=array.get(2).getAsFloat();
	}
}
	
public  ModelAvatarTest () {}

private static JsonArray obtainJsonArray(JsonObject o, String path) {
	String[] nodes=path.split("\\.");
	JsonObject t=o;
	for (int i=0;i<nodes.length-1;i++) {
		t=(JsonObject)(t.get(nodes[i]));
	}
	return t.get(nodes[nodes.length-1]).getAsJsonArray();
}

private static HashMap<String,ModelRenderer>  loadModelAvatarTest () {	 

	modelRendererList=new  HashMap<String,ModelRenderer>() ;

	Gson gson = new Gson();
	JsonObject json = null ;
	 try {
			ResourceLocation model_json = new ResourceLocation(ModResources.MODEL_JSON_ELVEN_AVATAR);
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(model_json).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			JsonElement je = gson.fromJson(reader, JsonElement.class);
			json = je.getAsJsonObject();
			
			myModelBase.textureWidth= json.get("texture_size").getAsJsonArray().get(0).getAsInt();
			myModelBase.textureHeight= json.get("texture_size").getAsJsonArray().get(1).getAsInt();
			
			for (JsonElement elemento:json.get("elements").getAsJsonArray()){
				
				JsonObject $elementoJson=((JsonObject)elemento);
				
				String name=$elementoJson.get("name").getAsString();
				
				Point from= new Point($elementoJson.get("from").getAsJsonArray());
				Point to=new Point($elementoJson.get("to").getAsJsonArray());
				Point rotationTemp=new Point(obtainJsonArray($elementoJson,"rotation.origin"));
				Point rotation = new Point( -(rotationTemp.x-8),-(rotationTemp.y-24),rotationTemp.z-8);
				Point size = new Point (to.x-from.x, to.y-from.y, to.z-from.z);
				Point position = new Point ((rotationTemp.x-from.x)-size.x,(rotationTemp.y-from.y)-size.y,(rotationTemp.z-from.z)-size.z);
				
				boolean $textureMirrorx=false;
				int $txi=(int) Math.floor(obtainJsonArray($elementoJson,"faces.east.uv").get(0).getAsFloat()*2);
				int $txi2=(int) Math.floor(obtainJsonArray($elementoJson,"faces.west.uv").get(2).getAsFloat()*2);
				int $tyi=(int) Math.floor(obtainJsonArray($elementoJson,"faces.down.uv").get(1).getAsFloat()*2);
				int $tyi2=(int) Math.floor(obtainJsonArray($elementoJson,"faces.up.uv").get(3).getAsFloat()*2);
								
				if($txi >$txi2){
				    $txi=$txi2;
				    $textureMirrorx=true;
			    }
				if($tyi>$tyi2) $tyi=$tyi2;
				
				ModelRenderer t=new ModelRenderer(myModelBase);
				modelRendererList.put(name, t);

				t.setRotationPoint(rotation.x,rotation.y, rotation.z);
			    t.cubeList.add(new ModelBox(t,$txi,$tyi,position.x,position.y, position.z, (int)size.x, (int)size.y , (int)size.z, 0.0F,$textureMirrorx));
			    
				//de izquierda a derecha , desde -7 hasta 8 16 puntos, el 0 esta en el centro y hacia la izquierda disminuye
				//de cerca a lejos, , desde -7 hasta 8, el 0 esta en el centro y hacia cerca disminuye
				//de arriba abajo, desde 8 hasta 24, hacia arriba disminuye , el centro es el 24.
			}
						
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}

	 return modelRendererList;
}

public static void render(TileElvenAvatar avatar,float elapsed, boolean riseArm) { 		
	        float scale = 0.06666667F;	    	    	        
	        ModelRenderer rightarm=modelRendererList.get("rightarm");
	        rightarm.rotateAngleX=0;
	        if (avatar!=null) { 
	        	 if (riseArm) 
	        		 rightarm.rotateAngleX=avatar.secuencesAvatar.getEndValue("RiseArm","Arm");
	        	 else 
	        		if (avatar.secuencesAvatar.isElemenActiveSecuence("Arm"))
	        			rightarm.rotateAngleX=avatar.secuencesAvatar.getValue("Arm");
	        	
	        }	        
	        for (String key:modelRendererList.keySet()) {	        
	         modelRendererList.get(key).render(scale);
	        }
	}	
}