package jp.nyatla.nyartoolkit.jogl.sample.sketch;

import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sample.*;

/**
 * マーカ平面から画像を取得するサンプルプログラムです。
 * 2種類のマーカ表面の画像を取得して表示するデモです。
 * ARマーカには、patt.hiro/patt.kanjiを使用して下さい。
 */
public class ImagePickup extends GlSketch
{
	private NyARJmfCamera camera;
	private NyARGlMarkerSystem nyar;
	private NyARGlRender render;
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private final static String ARCODE_FILE2 = "../../Data/patt.kanji";
	public void setup(GL gl)throws NyARException
	{
		this.size(640,480);
		NyARParam param=new NyARParam();
		param.loadDefaultParameter();
		param.changeScreenSize(640,480);
		this.camera=new NyARJmfCamera(param,30.0f);//create sensor system
		this.nyar=new NyARGlMarkerSystem(param);   //create MarkerSystem
		this.render=new NyARGlRender(this.nyar);
		//regist 2 markers
		this.ids[0]=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
		this.ids[1]=this.nyar.addARMarker(ARCODE_FILE2,16,25,80);
		this._raster=new NyARRgbRaster(64,64);
		gl.glEnable(GL.GL_DEPTH_TEST);
		//start camera
		this.camera.start();
	}

	private int[] ids=new int[2];
	//temporary
	private NyARRgbRaster _raster;
	public void draw(GL gl)
	{
		//lock async update.
		synchronized(this.camera)
		{
			try{
				this.nyar.update(this.camera);
				this.render.drawBackground(gl,this.camera.getSourceImage());
				gl.glPushMatrix();
				this.render.loadScreenProjectionMatrix(gl,640,480);
				this.render.setStrokeWeight(gl,1.0f);
				this.render.setColor(gl,255,255,0);
				for(int i=0;i<ids.length;i++){
					if(!this.nyar.isExistMarker(ids[i])){
						continue;
					}
					this.render.polygon(gl,this.nyar.getMarkerVertex2D(ids[i]));					
					this.nyar.getMarkerPlaneImage(ids[i],this.camera,-40,-40,80,80,this._raster);
					this.render.drawRaster(gl,i*64,0,this._raster);
				}
				gl.glPopMatrix();
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	public static void main(String[] args)
	{
		try {
			new ImagePickup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
