/* 
 * PROJECT: NyARToolkit JOGL sample program.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.dev;


import javax.media.opengl.*;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.sketch.GlSketch;
import jp.nyatla.nyartoolkit.jogl.utils.*;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
/**
 * This is simple pattern capture program.
 * should use right camera parameter for correctly capturing.
 * use ImageCaptureTest.pdf to print A4 paper.
 * The pdf has 4 2cm Id marker and 4cm capture area.
 * 4 Id markers are placed on each corner of 9cm square.
 */
public class ImageCaptureTest extends GlSketch
{
	private NyARJmfCamera camera;
	private NyARGlMarkerSystem nyar;
	private NyARGlMarkerSystemRender render;	
	public void setup(GL gl)throws Exception
	{
		this.size(640,480);
		NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(640,480);
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		JmfCaptureDevice d = devlist.getDevice(0);
		d.setCaptureFormat(config.getScreenSize(),30.0f);
		this.camera=new NyARJmfCamera(d);//create sensor system
		this.nyar=new NyARGlMarkerSystem(config);   //create MarkerSystem
		this.render=new NyARGlMarkerSystemRender(this.nyar);
		
		for(int i=0;i<4;i++){
			this.id[i]=this.nyar.addNyIdMarker(i,20);
		}
		//prepare image buffer
		this.bmf=new NyARBufferedImageRaster(64,64);		
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.start();
	}
	private int[] id=new int[4];
	public NyARBufferedImageRaster bmf;

	
	public void draw(GL gl)throws Exception
	{
		synchronized(this.camera){
			try {
				this.render.drawBackground(gl, this.camera.getSourceImage());
				this.nyar.update(this.camera);
				//check 4 markers
				for(int i=0;i<4;i++){
					if(!this.nyar.isExist(this.id[i])){
						return;
					}
				}
				//get corner points
				NyARDoublePoint2d p[]=NyARDoublePoint2d.createArray(4);
				for(int i=0;i<4;i++){
					this.nyar.getScreenPos(this.id[i],0,0,0,p[i]);
				}
				//make 2 cross lines
				NyARLinear l1=new NyARLinear();
				NyARLinear l2=new NyARLinear();
				l1.makeLinearWithNormalize(p[0],p[2]);
				l2.makeLinearWithNormalize(p[1],p[3]);
				NyARDoublePoint2d center=new NyARDoublePoint2d();
				l1.crossPos(l2, center);
				
				//move points to inner square.
				for(int i=0;i<4;i++)
				{
					p[i].x=(p[i].x-center.x)*(4.0/7)+center.x;
					p[i].y=(p[i].y-center.y)*(4.0/7)+center.y;
				}
				
				
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPushMatrix();
				gl.glOrtho(0,640,480, 0, -1, 1);
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPushMatrix();
				gl.glLoadIdentity();
				this.render.polygon(gl,p);
				

				//capture square image to bitmap
				this.camera.getPerspectiveImage(p[0].x,p[0].y,p[1].x,p[1].y,p[2].x,p[2].y,p[3].x,p[3].y,bmf);
				//show bitmap
				this.render.drawImage2d(gl,0,0,bmf);
				//bmf.getBufferedImage();//use the function to access bitmap.
				gl.glPopMatrix();
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPopMatrix();
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	public static void main(String[] args)
	{
		new ImageCaptureTest().run();
		return;
	}
}
