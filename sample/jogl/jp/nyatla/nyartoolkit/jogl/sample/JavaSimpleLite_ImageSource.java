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
package jp.nyatla.nyartoolkit.jogl.sample;

import java.awt.event.*;
import java.awt.*;

import javax.media.opengl.*;

import com.sun.opengl.util.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.jogl.utils.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import jp.nyatla.nyartoolkit.utils.j2se.*;

/**
 * カメラ入力の代わりに、静止画を入力元に用いるSimpleLiteです。
 */
public class JavaSimpleLite_ImageSource implements GLEventListener
{
	private Animator _animator;

	private NyARBufferedImageRaster _src_image;

	private GL _gl;

	private NyARSingleDetectMarker _nya;

	private NyARParam _ar_param;

	private Object _sync_object=new Object();
	private double[] _camera_projection = new double[16];


	public JavaSimpleLite_ImageSource(NyARParam i_param, NyARCode i_ar_code,BufferedImage i_image) throws NyARException,IOException
	{
		this._ar_param = i_param;

		Frame frame = new Frame("NyARToolkit["+this.getClass().getName()+"]");
		//BufferedImageをラップする。
		this._src_image = new NyARBufferedImageRaster(i_image);
		
		
		// NyARToolkitの準備
		this._ar_param.changeScreenSize(i_image.getWidth(),i_image.getHeight());
		this._nya = new NyARSingleDetectMarker(this._ar_param, i_ar_code, 80.0,this._src_image.getBufferType());
		this._nya.setContinueMode(false);// ここをtrueにすると、transMatContinueモード（History計算）になります。
		
		// 3Dを描画するコンポーネント
		GLCanvas canvas = new GLCanvas();
		frame.add(canvas);
		canvas.addGLEventListener(this);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		frame.setVisible(true);
		Insets ins = frame.getInsets();
		frame.setSize(i_image.getWidth() + ins.left + ins.right, i_image.getHeight() + ins.top + ins.bottom);
		canvas.setBounds(ins.left, ins.top, i_image.getWidth(), i_image.getHeight());
	}

	public void init(GLAutoDrawable drawable)
	{
		this._gl = drawable.getGL();
		this._gl.glEnable(GL.GL_DEPTH_TEST);
		this._gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		// NyARToolkitの準備
		try {
			// キャプチャ開始
		} catch (Exception e) {
			e.printStackTrace();
		}
		// カメラパラメータの計算
		NyARGLUtil.toCameraFrustumRH(this._ar_param,1,10,10000,this._camera_projection);
		this._animator = new Animator(drawable);
		this._animator.start();
		return;
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		_gl.glViewport(0, 0, width, height);

		// 視体積の設定
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		// 見る位置
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
	}

	private boolean _is_marker_exist=false;
	private NyARTransMatResult __display_transmat_result = new NyARTransMatResult();

	private double[] __display_wk = new double[16];

	public void display(GLAutoDrawable drawable)
	{
		NyARTransMatResult transmat_result = __display_transmat_result;
		// 背景を書く
		this._gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		try{
			this._is_marker_exist=this._nya.detectMarkerLite(this._src_image);
			NyARGLDrawUtil.drawBackGround(this._gl,this._src_image, 1.0);
			synchronized(this._sync_object){
				// マーカーがあれば、立方体を描画
				if (this._is_marker_exist){
					System.out.println(this._nya.getConfidence());
					// マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
					// Projection transformation.
					this._gl.glMatrixMode(GL.GL_PROJECTION);
					this._gl.glLoadMatrixd(_camera_projection, 0);
					this._gl.glMatrixMode(GL.GL_MODELVIEW);
					// Viewing transformation.
					this._gl.glLoadIdentity();
					// 変換行列を取得
					this._nya.getTransmationMatrix(transmat_result);
					// 変換行列をOpenGL形式に変換
					NyARGLUtil.toCameraViewRH(transmat_result,1.0f, __display_wk);
					this._gl.glLoadMatrixd(__display_wk, 0);
					//立方体を描画
					this._gl.glPushMatrix(); // Save world coordinate system.
					this._gl.glTranslatef(0.0f, 0.1f,20); // Place base of cube on marker surface.
					this._gl.glDisable(GL.GL_LIGHTING); // Just use colours.
					NyARGLDrawUtil.drawColorCube(this._gl,40);
					this._gl.glPopMatrix(); // Restore world coordinate system.
				}
			}
			Thread.sleep(1);// タスク実行権限を一旦渡す
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	private final static String CARCODE_FILE = "../../Data/patt.hiro";

	private final static String PARAM_FILE = "../../Data/camera_para.dat";
	private final static String SAMPLE_FILES = "../../Data/staticimage_sample.png";

	public static void main(String[] args)
	{
		try {
			NyARParam param = new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			NyARCode code = new NyARCode(16, 16);
			code.loadARPattFromFile(CARCODE_FILE);
			BufferedImage src_image = ImageIO.read(new File(SAMPLE_FILES));

			new JavaSimpleLite_ImageSource(param, code,src_image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
