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

import jp.nyatla.nyartoolkit.nyidmarker.data.*;

import java.awt.event.*;
import java.awt.*;
import java.util.Date;

import javax.media.Buffer;
import javax.media.opengl.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.j2d.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.utils.*;
import jp.nyatla.nyartoolkit.processor.*;

/*


*/


public class SingleARMarker implements GLEventListener, JmfCaptureListener
{
	class TextPanel
	{
		private TextRenderer _tr;
		public TextPanel(int i_size)
		{
			this._tr=new TextRenderer(new Font("SansSerif", Font.BOLD, 36));

		}
		public void draw(String i_str,float i_scale)
		{
			this._tr.begin3DRendering();
		    this._tr.setColor(1.0f, 0.2f, 0.2f, 0.8f);
		    this._tr.draw3D(i_str, 0f,0f,0f,i_scale);
			this._tr.end3DRendering();
			return;
		}
	}	
	/**
	 * １個のRawBit-Idマーカを認識するロジッククラス。
	 * detectMarker関数の呼び出しに同期して、transmatとcurrent_idパラメタを更新します。
	 * 
	 *
	 */
	class MarkerProcessor extends SingleARMarkerProcesser
	{	
		private Object _sync_object=new Object();
		public NyARTransMatResult transmat=null;
		public int current_code=-1;

		public MarkerProcessor(NyARParam i_cparam,int i_raster_format) throws NyARException
		{
			//アプリケーションフレームワークの初期化
			super();
			initInstance(i_cparam,i_raster_format);
			return;
		}
		protected void onEnterHandler(int i_code)
		{
			synchronized(this._sync_object){
				current_code=i_code;
			}
		}
		protected void onLeaveHandler()
		{
			synchronized(this._sync_object){
				current_code=-1;
				this.transmat=null;
			}
			return;			
		}

		protected void onUpdateHandler(NyARSquare i_square, NyARTransMatResult result)
		{
			synchronized(this._sync_object){
				this.transmat=result;
			}			
		}
	}
	
	private final String CARCODE_FILE1 = "../../Data/patt.hiro";
	
	
	private Animator _animator;
	private GLNyARRaster_RGB _cap_image;
	private JmfCaptureDevice _capture;

	private GL _gl;
	private NyARGLUtil _glnya;
	private TextPanel _panel;


	//NyARToolkit関係
	private NyARParam _ar_param;

	private double[] _camera_projection=new double[16];
	
	private Object _sync_object=new Object();
	private MarkerProcessor _processor;
	private NyARCode[] _code_table=new NyARCode[1];

	public SingleARMarker(NyARParam i_cparam) throws NyARException
	{
		JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
		this._ar_param=i_cparam;

		//キャプチャリソースの準備
		this._capture=devlist.getDevice(0);
		if(!this._capture.setCaptureFormat(SCREEN_X, SCREEN_Y,30.0f)){
			throw new NyARException();
		}
		this._capture.setOnCapture(this);
		this._cap_image = new GLNyARRaster_RGB(i_cparam,this._capture.getCaptureFormat());	

		this._code_table[0]=new NyARCode(16,16);
		this._code_table[0].loadARPattFromFile(CARCODE_FILE1);
		//プロセッサの準備
		this._processor=new MarkerProcessor(i_cparam,this._cap_image.getBufferReader().getBufferType());
		this._processor.setARCodeTable(_code_table,16,80.0);
		
		//OpenGLフレームの準備（OpenGLリソースの初期化、カメラの撮影開始は、initコールバック関数内で実行）
		Frame frame = new Frame("Java simpleLite with NyARToolkit");
		GLCanvas canvas = new GLCanvas();
		frame.add(canvas);
		canvas.addGLEventListener(this);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		
		//ウインドウサイズの調整
		frame.setVisible(true);
		Insets ins = frame.getInsets();
		frame.setSize(SCREEN_X + ins.left + ins.right, SCREEN_Y + ins.top + ins.bottom);
		canvas.setBounds(ins.left, ins.top, SCREEN_X, SCREEN_Y);
		return;
	}
	public void init(GLAutoDrawable drawable)
	{
		this._panel = new TextPanel(100);


		this._gl = drawable.getGL();
		this._gl.glEnable(GL.GL_DEPTH_TEST);

		this._gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//NyARToolkitの準備
		try {
			this._glnya = new NyARGLUtil(this._gl);
			//カメラパラメータの計算
			this._glnya.toCameraFrustumRH(this._ar_param,this._camera_projection);
			//キャプチャ開始
			this._capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this._animator = new Animator(drawable);
		this._animator.start();
		return;
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
	private double[] __display_wk=new double[16];
	
	
	public void display(GLAutoDrawable drawable)
	{
		NyARTransMatResult transmat_result = this._processor.transmat;
		if (!_cap_image.hasData()) {
			return;
		}
		// 背景を書く
		this._gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		this._glnya.drawBackGround(this._cap_image, 1.0);			
		if(this._processor.current_code<0 || transmat_result==null){
			
		}else{
			try{
				synchronized(this._sync_object){
					// Projection transformation.
					this._gl.glMatrixMode(GL.GL_PROJECTION);
					this._gl.glLoadMatrixd(_camera_projection, 0);
					this._gl.glMatrixMode(GL.GL_MODELVIEW);
					// Viewing transformation.
					this._gl.glLoadIdentity();
					// 変換行列をOpenGL形式に変換
					this._glnya.toCameraViewRH(transmat_result, __display_wk);
					this._gl.glLoadMatrixd(__display_wk, 0);
					// All other lighting and geometry goes here.
					this._gl.glPushMatrix();
					this._gl.glDisable(GL.GL_LIGHTING);

					
					//マーカのXZ平面をマーカの左上、表示開始位置を10cm上空へ。
					//くるーんくるん
					Date d = new Date();
					float r=(d.getTime()/50)%360;
					this._gl.glRotatef(r,0f,0f,1.0f);
					this._gl.glTranslatef(-1.0f,0f,1.0f);
					this._gl.glRotatef(90,1.0f,0f,0f);
					this._panel.draw("MarkerId:"+this._processor.current_code,0.01f);
					this._gl.glPopMatrix();
				}
				Thread.sleep(1);// タスク実行権限を一旦渡す
			}catch(Exception e){
				e.printStackTrace();
			}
		}		
		return;

	}
	/**
	 * カメラデバイスからのコールバック
	 */
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (this._sync_object) {
				this._cap_image.setBuffer(i_buffer);
				//フレームワークに画像を転送
				this._processor.detectMarker(this._cap_image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}


	private final static int SCREEN_X = 640;
	private final static int SCREEN_Y = 480;
	private final static String PARAM_FILE = "../../Data/camera_para.dat";
	//エントリポイント
	public static void main(String[] args)
	{
		try{
			NyARParam cparam= new NyARParam();
			cparam.loadARParamFromFile(PARAM_FILE);
			cparam.changeScreenSize(SCREEN_X, SCREEN_Y);		
			new SingleARMarker(cparam);
		}catch(Exception e){
			e.printStackTrace();
		}
		return;
	}
}

