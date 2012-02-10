package jp.nyatla.nyartoolkit.jogl.sample.sketch;

import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sample.NyARGlMarkerSystem;
import jp.nyatla.nyartoolkit.jogl.utils.*;



/**
 * JMFからの映像入力からマーカ2種を検出し、そこに立方体を重ねます。
 * ARマーカには、patt.hiro/patt.kanjiを使用して下さい。
 */
public class SimpleLite extends GlSketch
{
	private NyARJmfCamera camera;
	private NyARGlMarkerSystem nyar;
	public void setup(GL gl)throws NyARException
	{
		this.size(640,480);
		NyARParam param=new NyARParam();
		param.loadDefaultParameter();
		param.changeScreenSize(640,480);
		this.camera=new NyARJmfCamera(param,30.0f);//create sensor system
		this.nyar=new NyARGlMarkerSystem(param);   //create MarkerSystem
		this.ids[0]=this.nyar.addARMarker(ARCODE_FILE2,16,25,80);
		this.ids[1]=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadMatrixd(this.nyar.getGlProjectionMatrix(),0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.start();
	}
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private final static String ARCODE_FILE2 = "../../Data/patt.kanji";
	private int[] ids=new int[2];
	
	public void draw(GL gl)
	{
		synchronized(this.camera){
			try {
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
				NyARGLDrawUtil.drawBackGround(gl,this.camera.getSourceImage(), 1.0);				
				this.nyar.update(this.camera);
				if(this.nyar.isExistMarker(this.ids[0])){
					gl.glMatrixMode(GL.GL_MODELVIEW);
					gl.glPushMatrix();
					gl.glLoadMatrixd(this.nyar.getGlMarkerTransMat(this.ids[0]),0);
					NyARGLDrawUtil.drawColorCube(gl,40);
					gl.glPopMatrix();
				}
				if(this.nyar.isExistMarker(this.ids[1])){
					gl.glMatrixMode(GL.GL_MODELVIEW);
					gl.glPushMatrix();
					gl.glLoadMatrixd(this.nyar.getGlMarkerTransMat(this.ids[1]),0);
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
			new SimpleLite();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
