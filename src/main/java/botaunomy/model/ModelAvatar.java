// Made with Powershell

package botaunomy.model;
import org.lwjgl.opengl.GL11;

import botaunomy.block.tile.TileElvenAvatar;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelAvatar 
{
		public static final int NPOINTS=33;
		public static final int NARC=3;
	
		private static MyModelBase myModelBase= new MyModelBase();			
		private static ModelRenderer body;
		private static ModelRenderer rightarm;
		private static ModelRenderer leftarm;
		private static ModelRenderer rightleg;
		private static ModelRenderer leftleg;
		private static ModelRenderer head;
		private static ModelRenderer botton;
		private static ModelRenderer[][] points= new ModelRenderer[NARC][NPOINTS];
		private static float[] rndForPoints= new float[NPOINTS];
		@SuppressWarnings("unused")
		private static boolean loaded=loadModelAvatar();		
		private static class MyModelBase extends ModelBase{	
			public MyModelBase() {}
		}
	
	public  ModelAvatar() {
	}
	
	private static boolean loadModelAvatar() {	
		
		for (int a=0; a<NPOINTS;a++) {
			rndForPoints[a]=(float)Math.random();
		}

		myModelBase.textureWidth = 32;
		myModelBase.textureHeight = 32;	
	
		body= new ModelRenderer(myModelBase);
		body.setRotationPoint(0F,16F,0F);
		body.cubeList.add(new ModelBox(body, 0, 12, -3.0F, -2.0F, -2.0F, 6, 4, 4, 0.0F, false));
		
		rightarm= new ModelRenderer(myModelBase);
		rightarm.setRotationPoint(-4F,14F,0.5F);
		rightarm.cubeList.add(new ModelBox(rightarm, 0, 20, -1F,0F,-1.5F,2,6,3, 0.0F, false));
		
		leftarm= new ModelRenderer(myModelBase);
		leftarm.setRotationPoint(4F,14F,0.5F);
		leftarm.cubeList.add(new ModelBox(leftarm, 0, 20, -1F,0F,-1.5F,2,6,3, 0.0F, true));
	
		rightleg= new ModelRenderer(myModelBase);
		rightleg.setRotationPoint(-1.5F,19F,0.5F);
		rightleg.cubeList.add(new ModelBox(rightleg, 0, 20, -1.5F,-1F,-1.5F,3,6,3, 0.0F, false));
	
		leftleg= new ModelRenderer(myModelBase);
		leftleg.setRotationPoint(1.5F,19F,0.5F);
		leftleg.cubeList.add(new ModelBox(leftleg, 0, 20, -1.5F,-1F,-1.5F,3,6,3, 0.0F, true));

		head= new ModelRenderer(myModelBase);
		head.setRotationPoint(0F,14F,0F);
		head.cubeList.add(new ModelBox(head, 0, 0, -3F,-6F,-3F,6,6,6, 0.0F, false));
				
		botton= new ModelRenderer(myModelBase);
		botton.setRotationPoint(0F,19.5F,2.5F);	
		botton.cubeList.add(new ModelBox(botton, 4, 12, -2F,-1.5F,-0.5F,4,3,1, 0.0F, false));
        
		//de izquierda a derecha , desde -7 hasta 8 16 puntos, el 0 esta en el centro y hacia la izquierda disminuye
		//de cerca a lejos, , desde -7 hasta 8, el 0 esta en el centro y hacia cerca disminuye
		//de arriba abajo, desde 8 hasta 24, hacia arriba disminuye , el centro es el 24.
		//rotation
        //x=-($elementoJson.rotation.origin[0]-8)
        //y=-($elementoJson.rotation.origin[1]-24)
        //z=($elementoJson.rotation.origin[2]-8)		
		//Position
        //x=($elementoJson.rotation.origin[0]-$From.X)-($Size.X)
        //y=($elementoJson.rotation.origin[1]-$From.Y)-($Size.Y)
        //z=($elementoJson.rotation.origin[2]-$From.Z)-($Size.Z)

		float origenx=-14F;
		float origeny=-4; //-8 , but is scaled 2/3
		
		for (int b=0;b<NARC;b++)
		for (int a=0; a<NPOINTS;a++) {
			points[b][a]=new ModelRenderer(myModelBase);
			double angle=(3.1416F/2F)/16F*a;
			double cos=Math.cos(angle)*16;
			float desx=(float) -(origenx+cos);
			double sin=Math.sin(angle)*16;
			float desy=(float) (sin);

			double color=6-Math.floor((double)(rndForPoints[a]*7F));
			int textureOffsetX= (int)color*4;
			
			points[b][a].setRotationPoint(0F,origeny+24F,0F);						
			points[b][a].setTextureOffset(textureOffsetX, 29);			
			points[b][a].cubeList.add(new ModelBox(points[b][a], textureOffsetX, 29,origenx+desx-1F, -desy-1F ,-1F,1,1,1,0F, false));
		}			
		return true;
	}

	public static void render(TileElvenAvatar avatar,float elapsed, boolean riseArm, boolean renderPoints) {				
	        float scale = 0.06666667F;	        
	         rightarm.rotateAngleX=0;
	         if (avatar!=null) { 
	        	 if (riseArm) 
	        		 rightarm.rotateAngleX=avatar.secuencesAvatar.getEndValue("RiseArm","Arm");
	        	 else 
	        		if (avatar.secuencesAvatar.isElemenActiveSecuence("Arm"))
	        			rightarm.rotateAngleX=avatar.secuencesAvatar.getValue("Arm");
	        	
	         }

	         ModelAvatar.body.render(scale);
	         ModelAvatar.rightarm.render(scale);
	         ModelAvatar.leftarm.render(scale);
	         ModelAvatar.rightleg.render(scale);
	         ModelAvatar.leftleg.render(scale);
	         ModelAvatar.head.render(scale);
	         ModelAvatar.botton.render(scale);
			
			if (avatar!=null && avatar.isEnabled() && renderPoints ) {
				
				//float difscale=((float)(Math.random()*scale)/10F)-(scale/10F);
				avatar.updateRotatePoints(points,rndForPoints,elapsed);
				
				for (int b=0;b<NARC;b++)
				for (int a=0; a<NPOINTS;a++) {
					if (!avatar.haveMana()||!avatar.haveItem()||!avatar.isEnabled()) break;
					else if (Math.random()>.35)
						//points[b][a].render(scale*2F/3F);
						renderParticles(5,points[b][a],scale*2F/3F);
				}
			}			
	}
	
    private static  void renderParticles( float partialTicks, ModelRenderer point, float scale)   {

			//GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			//GlStateManager.alphaFunc(516, 0.003921569F);
			//GlStateManager.alphaFunc(516, 0.1F);     		    	
			//GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			//GlStateManager.depthMask(false);
			//GlStateManager.disableTexture2D();
			//GlStateManager.disableCull();
	
                 try
                        {
                            //particle.renderParticle(bufferbuilder, entityIn, partialTicks, f, f4, f1, f2, f3);
                        	point.render(scale);
                        }            
                        catch (Throwable throwable){}
	        
	 		//GlStateManager.enableCull();
            //GlStateManager.enableTexture2D();
	        //GlStateManager.depthMask(true);    	     
			//GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.disableAlpha();
	        GlStateManager.disableBlend();   
	        //GlStateManager.popMatrix();
    }

}