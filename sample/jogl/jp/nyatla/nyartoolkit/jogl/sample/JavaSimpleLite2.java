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

import javax.media.*;
import javax.media.opengl.*;

import com.sun.opengl.util.Animator;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.utils.*;
/**
 * simpleLiteの複数マーカー同時認識バージョン
 * "Hiro"のマーカーと"人"のマーカーの混在環境で、Hiroのマーカー全てに
 * 立方体を表示します。
 */
public class JavaSimpleLite2 implements GLEventListener, JmfCaptureListener
{
	private final String CARCODE_FILE1 = "../../Data/patt.hiro";

	private final String CARCODE_FILE2 = "../../Data/patt.kanji";

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private final static int SCREEN_X = 640;

	private final static int SCREEN_Y = 480;

	private Animator _animator;

	private JmfNyARRaster_RGB _cap_image;

	private JmfCaptureDevice _capture;

	private GL _gl;

	//NyARToolkit関係
	private NyARDetectMarker _nya;

	private NyARParam _ar_param;
	private double[] _camera_projection=new double[16];


	public JavaSimpleLite2()
	{
		Frame frame = new Frame("NyARToolkit["+this.getClass().getName()+"]");

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
		frame.setSize(SCREEN_X + ins.left + ins.right, SCREEN_Y + ins.top + ins.bottom);
		canvas.setBounds(ins.left, ins.top, SCREEN_X, SCREEN_Y);
	}

	public void init(GLAutoDrawable drawable)
	{
		_gl = drawable.getGL();
		this._gl.glEnable(GL.GL_DEPTH_TEST);
		_gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//NyARToolkitの準備
		try {
			//キャプチャの準備
			JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
			_capture=devlist.getDevice(0);
			_capture.setCaptureFormat(SCREEN_X, SCREEN_Y,15f);
			_capture.setOnCapture(this);			
			//NyARToolkitの準備
			_ar_param = new NyARParam();
			_ar_param.loadARParamFromFile(PARAM_FILE);
			_ar_param.changeScreenSize(SCREEN_X, SCREEN_Y);

			//ARコードを2個ロード
			double[] width = new double[] { 80.0, 80.0 };
			NyARCode[] ar_codes = new NyARCode[2];
			ar_codes[0] = new NyARCode(16, 16);
			ar_codes[0].loadARPattFromFile(CARCODE_FILE1);
			ar_codes[1] = new NyARCode(16, 16);
			ar_codes[1].loadARPattFromFile(CARCODE_FILE2);
			//JMFラスタオブジェクト
			this._cap_image = new JmfNyARRaster_RGB(this._capture.getCaptureFormat());

			this._nya = new NyARDetectMarker(this._ar_param, ar_codes, width, 2,this._cap_image.getBufferType());
			this._nya.setContinueMode(false);//ここをtrueにすると、transMatContinueモード（History計算）になります。
			//キャプチャ開始
			this._capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//カメラパラメータの計算
		NyARGLUtil.toCameraFrustumRH(_ar_param,1,10,10000,_camera_projection);
		
		this._animator = new Animator(drawable);
		this._animator.start();

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		_gl.glViewport(0, 0, width, height);

		//視体積の設定
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		//見る位置
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
	}
	private NyARTransMatResult __display_transmat_result=new NyARTransMatResult();
	private double[] __display_wk=new double[16];
	
	public void display(GLAutoDrawable drawable)
	{
		NyARTransMatResult transmat_result=__display_transmat_result;

		try {
			if (!_cap_image.hasBuffer()) {
				return;
			}
			_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.          
			//画像チェックしてマーカー探して、背景を書く
			int found_markers;
			synchronized (_cap_image) {
				found_markers = _nya.detectMarkerLite(_cap_image, 110);
				//背景を書く
				NyARGLDrawUtil.drawBackGround(this._gl,_cap_image, 1.0);
			}
			//あったら立方体を書く
			for (int i = 0; i < found_markers; i++) {
				//1番のマーカーでなければ表示しない。
				if (_nya.getARCodeIndex(i) != 0) {
					continue;
				}
				//マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
				// Projection transformation.
				_gl.glMatrixMode(GL.GL_PROJECTION);
				_gl.glLoadMatrixd(_camera_projection, 0);
				_gl.glMatrixMode(GL.GL_MODELVIEW);
				// Viewing transformation.
				_gl.glLoadIdentity();
				//変換行列を取得
				_nya.getTransmationMatrix(i,transmat_result);
				//変換行列をOpenGL形式に変換
				NyARGLUtil.toCameraViewRH(transmat_result,1,__display_wk);
				_gl.glLoadMatrixd(__display_wk, 0);

				//立方体を描画
				this._gl.glPushMatrix(); // Save world coordinate system.
				this._gl.glTranslatef(0.0f, 0.0f,20f); // Place base of cube on marker surface.
				this._gl.glDisable(GL.GL_LIGHTING); // Just use colours.
				NyARGLDrawUtil.drawColorCube(this._gl,40f);
				this._gl.glPopMatrix(); // Restore world coordinate system.
			}
			Thread.sleep(1);//タスク実行権限を一旦渡す            
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (_cap_image) {
				_cap_image.setBuffer(i_buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	public static void main(String[] args)
	{
		new JavaSimpleLite2();
	}
}
