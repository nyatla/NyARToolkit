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
package jp.nyatla.nyartoolkit.jogl.sample.nft.sketch;


import java.awt.Dimension;
import java.io.FileInputStream;

import com.github.sarxos.webcam.Webcam;
import com.jogamp.opengl.GL;

import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.jogl2.sketch.GlSketch;
import jp.nyatla.nyartoolkit.jogl2.utils.*;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.nftsystem.NyARNftSystemConfig;
/**
 * NFTマーカを使うSimpleLiteです。
 */
public class NftSimpleLite extends GlSketch
{
	String nftdataset="../../Data/nft/infinitycat";
	String cparam="../../Data/testcase/camera_para5.dat";
	private Webcam camera;
	private NyARGlNftSystem nyar;
	private NyARGlRender render;
	private NyARSensor sensor;
	public void setup(GL gl)throws Exception
	{
		this.size(640,480);
		NyARNftSystemConfig config = new NyARNftSystemConfig(new FileInputStream(cparam),640,480);
		this.camera=Webcam.getDefault();
		this.camera.setViewSize(new Dimension(640,480));
		this.nyar=new NyARGlNftSystem(config);   //create MarkerSystem
		this.render=new NyARGlRender(this.nyar);
		this.sensor=new NyARSensor(config.getScreenSize());
		//
		this.id=this.nyar.addNftTarget(nftdataset,160);
		gl.glEnable(GL.GL_DEPTH_TEST);
		this.camera.open();
	}
	private int id;
	public void draw(GL gl)throws Exception
	{
		synchronized(this.camera){
			try {
				this.sensor.update(new NyARBufferedImageRaster(this.camera.getImage()));				
				this.render.drawBackground(gl,this.sensor.getSourceImage());
				this.render.loadARProjectionMatrix(gl);
				this.nyar.update(this.sensor);
				if(this.nyar.isExistTarget(this.id)){
					this.nyar.loadTransformMatrix(gl,this.id);
					this.render.colorCube(gl,40,80,60,20);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	public static void main(String[] args)
	{
		new NftSimpleLite().run();
		return;
	}

}
