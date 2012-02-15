package jp.nyatla.nyartoolkit.jogl.sample.sketch;

import java.awt.event.MouseEvent;

import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sketch.GlSketch;
import jp.nyatla.nyartoolkit.jogl.utils.*;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;



/**
 * JMFからの映像入力からマーカ2種を検出し、そこに立方体を重ねます。
 * ARマーカには、patt.hiro/patt.kanjiを使用して下さい。
 */
public class SimpleLiteM extends GlSketch
{
	private NyARJmfCamera camera;
	private NyARGlMarkerSystem nyar;
	private NyARGlRender render;	
	public void setup(GL gl)throws NyARException
	{
		this.size(640,480);
		NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(640,480);
		this.camera=new NyARJmfCamera(config,30.0f);//create sensor system
		this.nyar=new NyARGlMarkerSystem(config);   //create MarkerSystem
		this.render=new NyARGlRender(this.nyar);
		
//		this.ids[0]=this.nyar.addARMarker(ARCODE_FILE2,16,25,80);
		this.ids[0]=this.nyar.addNyIdMarker(50,80);
		this.ids[1]=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.start();
	}
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private int[] ids=new int[2];
	
	public void draw(GL gl)
	{
		synchronized(this.camera){
			try {
				this.render.drawBackground(gl, this.camera.getSourceImage());
				this.render.loadARProjectionMatrix(gl);
//				gl.glMatrixMode(GL.GL_PROJECTION);
//				gl.glLoadMatrixd(this.nyar.getGlProjectionMatrix(),0);				
				this.nyar.update(this.camera);
				if(this.nyar.isExistMarker(this.ids[0])){
//					gl.glMatrixMode(GL.GL_MODELVIEW);
//					gl.glPushMatrix();
//					gl.glLoadMatrixd(this.nyar.getGlMarkerMatrix(this.ids[0]),0);
					this.render.loadMarkerMatrix(gl,this.ids[0]);
					this.render.colorCube(gl,40,0,0,20);
//					NyARGLDrawUtil.drawColorCube(gl,40);
//					gl.glPopMatrix();
				}
				if(this.nyar.isExistMarker(this.ids[1])){
					gl.glMatrixMode(GL.GL_MODELVIEW);
					gl.glPushMatrix();
					gl.glLoadMatrixd(this.nyar.getGlMarkerMatrix(this.ids[1]),0);
					NyARGLDrawUtil.drawColorCube(gl,40);
					gl.glPopMatrix();
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	public static void main(String[] args)
	{
		try {
			new SimpleLiteM();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}


}
