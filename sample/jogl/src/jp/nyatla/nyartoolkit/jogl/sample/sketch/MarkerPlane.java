package jp.nyatla.nyartoolkit.jogl.sample.sketch;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sample.NyARGlMarkerSystem;
import jp.nyatla.nyartoolkit.jogl.sample.NyARGlRender;




/**
 * マウスポインタの位置のマーカ平面を計算して、そこに立方体を表示します。
 * ARマーカには、patt.hiroを使用して下さい。
 */
public class MarkerPlane extends GlSketch
{
	private NyARJmfCamera camera;
	private NyARGlMarkerSystem nyar;
	private NyARGlRender render;	
	public void setup(GL gl)throws NyARException
	{
		this.size(640,480);
		NyARParam param=new NyARParam();
		param.loadDefaultParameter();
		param.changeScreenSize(640,480);
		this.camera=new NyARJmfCamera(param,30.0f);//create sensor system
		this.nyar=new NyARGlMarkerSystem(param);   //create MarkerSystem
		this.render=new NyARGlRender(this.nyar);
		this.ids[0]=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.start();
	}
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private int[] ids=new int[1];
	private Point mp=new Point();
	public void draw(GL gl)
	{
		synchronized(this.camera)
		{
			try {
				this.nyar.update(this.camera);
				this.render.drawBackground(gl,this.camera.getSourceImage());
				this.render.loadARProjectionMatrix(gl);
				if(this.nyar.isExistMarker(this.ids[0])){
					NyARDoublePoint3d p=new NyARDoublePoint3d();
					this.nyar.getMarkerPlanePos(this.ids[0],this.mp.x,this.mp.y,p);
					this.render.loadMarkerMatrix(gl,ids[0]);
					this.render.colorCube(gl,40,p.x,p.y,p.z+20);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void mouseMoved(MouseEvent e)
	{
		mp.setLocation(e.getPoint());
	}
	public static void main(String[] args)
	{
		try {
			new MarkerPlane();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
