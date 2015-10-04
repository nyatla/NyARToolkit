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
package jp.nyatla.nyartoolkit.jogl.sample.sketch.webcamcapture;


import java.awt.Dimension;
import javax.media.opengl.*;

import com.github.sarxos.webcam.Webcam;
import jp.nyatla.nyartoolkit.jogl.sketch.GlSketch;
import jp.nyatla.nyartoolkit.jogl.utils.*;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;
/**
 * WebcamCaptureの映像を使うサンプルです。
 * ARマーカには、patt.hiroを使用して下さい。
 */
public class WebCamSample extends GlSketch
{
	private Webcam camera;
	private NyARGlMarkerSystem nyar;
	private NyARGlMarkerSystemRender render;
	private NyARSensor sensor;
	public void setup(GL gl)throws Exception
	{
		this.size(640,480);
		NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(640,480);
		this.camera=Webcam.getDefault();
		this.camera.setViewSize(new Dimension(640,480));
		this.nyar=new NyARGlMarkerSystem(config);   //create MarkerSystem
		this.render=new NyARGlMarkerSystemRender(this.nyar);
		this.sensor=new NyARSensor(config.getScreenSize());
		this.id=this.nyar.addARMarker(ARCODE_FILE,16,25,80);
//		this.id=this.nyar.addPsARPlayCard(1,80);
//		this.id=this.nyar.addNyIdMarker(0,80); //for NyIdmarker #0
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.open();
	}
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private int id;
	
	public void draw(GL gl)throws Exception
	{
		synchronized(this.camera){
			try {
				this.sensor.update(new NyARBufferedImageRaster(this.camera.getImage()));				
				this.render.drawBackground(gl, this.sensor.getSourceImage());
				this.render.loadARProjectionMatrix(gl);
				
				this.nyar.update(this.sensor);
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
		new WebCamSample().run();
		return;
	}
}
