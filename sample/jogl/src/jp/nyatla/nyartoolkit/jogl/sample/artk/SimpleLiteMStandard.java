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
package jp.nyatla.nyartoolkit.jogl.sample.artk;

import java.awt.event.*;
import java.awt.*;
import java.io.FileInputStream;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;


import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.NyARFrustum;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl2.utils.*;
import jp.nyatla.nyartoolkit.markersystem.INyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;


/**
 * このプログラムは、JMFからの映像入力からマーカを検出し、そこに立方体を重ねます。
 * 新しいSimpleLiteのサンプルです。
 * スケッチシステム/レンダリングクラスを使わずに、OpenGLAPIをそのまま使用します。
 * 動作は、スケッチサンプル{@link SimpleLiteM}と同じです。
 * ARマーカには、patt.hiro/patt_kanjiを使用して下さい。
 */
public class SimpleLiteMStandard implements GLEventListener
{
	// NyARToolkit関係
	private NyARJmfCamera _camera;
	private NyARGlMarkerSystem _nyar;
	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private final static String ARCODE_FILE2 = "../../Data/patt.kanji";
	private int[] ids=new int[2];
	public SimpleLiteMStandard(INyARMarkerSystemConfig i_config) throws NyARRuntimeException
	{		
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		JmfCaptureDevice d = devlist.getDevice(0);
		d.setCaptureFormat(i_config.getScreenSize(),30.0f);
		this._camera=new NyARJmfCamera(d);//create sensor system
		this._nyar=new NyARGlMarkerSystem(i_config);   //create MarkerSystem
		this.ids[0]=this._nyar.addARMarker(ARCODE_FILE2,16,25,80);
		this.ids[1]=this._nyar.addARMarker(ARCODE_FILE,16,25,80);
		
		Frame frame= new Frame("NyARTK program");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		GLCanvas canvas = new GLCanvas();
		frame.add(canvas);
		canvas.addGLEventListener(this);
		NyARIntSize s=i_config.getNyARSingleCameraView().getARParam().getScreenSize();

		frame.setVisible(true);
		Insets ins = frame.getInsets();
		frame.setSize(s.w + ins.left + ins.right,s.h + ins.top + ins.bottom);		
		canvas.setBounds(ins.left, ins.top, s.w,s.h);


		this._camera.start();
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl=drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		double[] pmat=new double[16];
		NyARFrustum.FrustumParam f=this._nyar.getFrustum().getFrustumParam(new NyARFrustum.FrustumParam());	
		NyARGLUtil.toCameraFrustumRH(this._nyar.getARParam(),1,f.near,f.far,pmat);
		gl.glLoadMatrixd(pmat,0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Animator animator = new Animator(drawable);
		animator.start();
		return;
	}
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL gl=drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, width, height);
		return;
	}
	@Override
	public void display(GLAutoDrawable drawable)
	{
		GL2 gl=drawable.getGL().getGL2();
		synchronized(this._camera)
		{
			try {
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
				NyARGLDrawUtil.drawBackGround(gl,this._camera.getSourceImage(), 1.0);				
				this._nyar.update(this._camera);
				if(this._nyar.isExist(this.ids[0])){
					gl.glMatrixMode(GL2.GL_MODELVIEW);
					gl.glPushMatrix();
					gl.glLoadMatrixd(this._nyar.getGlTransformMatrix(this.ids[0]),0);
					NyARGLDrawUtil.drawColorCube(gl,40);
					gl.glPopMatrix();
				}
				if(this._nyar.isExist(this.ids[1])){
					gl.glMatrixMode(GL2.GL_MODELVIEW);
					gl.glPushMatrix();
					gl.glLoadMatrixd(this._nyar.getGlTransformMatrix(this.ids[1]),0);
					NyARGLDrawUtil.drawColorCube(gl,40);
					gl.glPopMatrix();
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	private final static String PARAM_FILE = "../../Data/camera_para.dat";
	private final static int SCREEN_X = 640;
	private final static int SCREEN_Y = 480;

	public static void main(String[] args)
	{
		try {
			NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(new FileInputStream(PARAM_FILE),SCREEN_X, SCREEN_Y);
			new SimpleLiteMStandard(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}



}
