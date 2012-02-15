package jp.nyatla.nyartoolkit.jogl.sample.sketch;

import java.awt.event.MouseEvent;

import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sketch.GlSketch;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGlMarkerSystem;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGlRender;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;



/**
 * JMFからの映像入力からマーカ2種を検出し、そこに立方体を重ねます。
 * ARマーカには、patt.hiro/patt.kanjiを使用して下さい。
 */
public class SimpleLite extends GlSketch
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
		
		this.id=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.start();
	}
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private int id;
	
	public void draw(GL gl)
	{
		synchronized(this.camera){
			try {
				this.render.drawBackground(gl, this.camera.getSourceImage());
				this.render.loadARProjectionMatrix(gl);
				this.nyar.update(this.camera);
				if(this.nyar.isExistMarker(this.id)){
					this.render.loadMarkerMatrix(gl,this.id);
					this.render.colorCube(gl,40,0,0,20);
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
			new SimpleLite();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}


}
