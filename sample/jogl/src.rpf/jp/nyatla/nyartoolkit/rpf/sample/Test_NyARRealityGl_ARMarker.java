/* 
 * PROJECT: NyARToolkit JOGL sample program.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008-2011 nyatla
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
package jp.nyatla.nyartoolkit.rpf.sample;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.Buffer;
import javax.media.opengl.*;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.rpf.mklib.ARTKMarkerTable;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTargetList;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource_Jmf;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLDrawUtil;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.gl.NyARRealityGl;

import com.sun.opengl.util.Animator;

/**
 * NyARRealityシステムのサンプル。
 * ARToolkitスタイルのマーカの上に、立方体を表示します。
 * 取り扱うマーカの種類は２種類(Hiro,Kanji)です。
 * パターンの種類によっては、内側のパターンを誤認識する場合があります。
 * @author nyatla
 *
 */
public class Test_NyARRealityGl_ARMarker implements GLEventListener, JmfCaptureListener
{

	private final static int SCREEN_X = 640;
	private final static int SCREEN_Y = 480;

	private Animator _animator;
	private JmfCaptureDevice _capture;

	private GL _gl;

	private Object _sync_object=new Object();

	NyARRealityGl _reality;
	NyARRealitySource_Jmf _src;
	ARTKMarkerTable _mklib;

	public Test_NyARRealityGl_ARMarker(NyARParam i_param) throws NyARException
	{
		Frame frame = new Frame("NyARToolkit+RPF["+this.getClass().getName()+"]");
		
		// キャプチャの準備
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		this._capture = devlist.getDevice(0);
		if (!this._capture.setCaptureFormat(SCREEN_X, SCREEN_Y, 30.0f)) {
			throw new NyARException();
		}
		this._capture.setOnCapture(this);
		//Realityの構築
		i_param.changeScreenSize(SCREEN_X, SCREEN_Y);	
		//キャプチャ画像と互換性のあるRealitySourceを構築(樽型歪みの少ないカメラの時は、distortionFactorをnullにすること。)
//		this._src=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),i_param.getDistortionFactor(),2,100);
		this._src=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),null,2,100);
		//OpenGL互換のRealityを構築		
		this._reality=new NyARRealityGl(i_param.getPerspectiveProjectionMatrix(),i_param.getScreenSize(),10,10000,3,10);
		//マーカライブラリ(ARTKId)の構築
		this._mklib= new ARTKMarkerTable(10,16,16,25,25,4);
		//マーカテーブルの作成（２種類）
		this._mklib.addMarkerFromARPattFile(PATT_HIRO,0,"HIRO",80,80);
		this._mklib.addMarkerFromARPattFile(PATT_KANJI,1,"KANJI",80,80);
				
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
		this._gl = drawable.getGL();
		this._gl.glEnable(GL.GL_DEPTH_TEST);
		this._gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		NyARGLDrawUtil.setFontStyle("SansSerif",Font.BOLD,24);
		// NyARToolkitの準備
		try {
			// キャプチャ開始
			_capture.start();
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

		// 視体積の設定
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		// 見る位置
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable)
	{
		//RealitySourceにデータが処理する。
		if(!this._src.isReady())
		{
			return;
		}
		
		// 背景を書く
		this._gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		try{
			synchronized(this._sync_object){
				
				this._reality.glDrawRealitySource(this._gl,this._src);
				// Projection transformation.
				this._gl.glMatrixMode(GL.GL_PROJECTION);
				this._reality.glLoadCameraFrustum(this._gl);
				//ターゲットリストを走査して、画面に内容を反映
				NyARRealityTargetList tl=this._reality.refTargetList();
				for(int i=tl.getLength()-1;i>=0;i--){
					NyARRealityTarget t=tl.getItem(i);
					switch(t.getTargetType())
					{
					case NyARRealityTarget.RT_KNOWN:
						//立方体の描画
						this._gl.glMatrixMode(GL.GL_MODELVIEW);
						this._gl.glLoadIdentity();
						NyARDoubleMatrix44 m=t.refTransformMatrix();
						this._reality.glLoadModelViewMatrix(this._gl,m);
						_gl.glPushMatrix(); // Save world coordinate system.
						_gl.glTranslatef(0f,0,20f); // Place base of cube on marker surface.
						_gl.glDisable(GL.GL_LIGHTING); // Just use colours.
						NyARGLDrawUtil.drawColorCube(this._gl,40f);
						_gl.glPopMatrix(); // Restore world coordinate system.
						//マーカ情報の描画
						_gl.glPushMatrix(); // Save world coordinate system.
						this._reality.glLoadModelViewMatrix(this._gl,m);
						_gl.glTranslatef(0,0,90f); // Place base of cube on marker surface.
						_gl.glRotatef(90,1.0f,0.0f,0.0f); // Place base of cube on marker surface.
						NyARGLDrawUtil.setFontColor(t.getGrabbRate()<50?Color.RED:Color.BLUE);
						ARTKMarkerTable.GetBestMatchTargetResult d=((ARTKMarkerTable.GetBestMatchTargetResult)(t.tag));
						NyARGLDrawUtil.drawText("Name:"+d.name+" GRUB:"+t.grab_rate+"%",0.5f);
						_gl.glPopMatrix();
						
						break;
					case NyARRealityTarget.RT_UNKNOWN:
						NyARDoublePoint2d[] p=t.refTargetVertex();						
						NyARGLDrawUtil.beginScreenCoordinateSystem(this._gl,SCREEN_X,SCREEN_Y,true);
						_gl.glLineWidth(2);
						_gl.glColor3f(1.0f,0.0f,0.0f);
						_gl.glBegin(GL.GL_LINE_LOOP);
						_gl.glVertex2d(p[0].x,p[0].y);
						_gl.glVertex2d(p[1].x,p[1].y);
						_gl.glVertex2d(p[2].x,p[2].y);
						_gl.glVertex2d(p[3].x,p[3].y);
						_gl.glEnd();
						NyARGLDrawUtil.endScreenCoordinateSystem(this._gl);
						
						break;
					}
				}
			}
			Thread.sleep(1);// タスク実行権限を一旦渡す
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * カメラのキャプチャした画像を非同期に受け取る関数。
	 * 画像を受け取ると、同期を取ってRealityを1サイクル進めます。
	 */
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (this._sync_object)
			{
				this._src.setImage(i_buffer);
				this._reality.progress(this._src);
				//UnknownTargetを1個取得して、遷移を試す。
				NyARRealityTarget t=this._reality.selectSingleUnknownTarget();
				if(t==null){
					return;
				}
				//ターゲットに一致するデータを検索
				ARTKMarkerTable.GetBestMatchTargetResult r=new ARTKMarkerTable.GetBestMatchTargetResult();
				if(this._mklib.getBestMatchTarget(t,this._src,r)){
					if(r.confidence<0.6)
					{	//一致率が低すぎる。
						return;
					}
					//既に認識しているターゲットの内側のものでないか確認する？(この処理をすれば、二重認識は無くなる。)
					
					//一致度を確認して、80%以上ならKnownターゲットへ遷移
					if(!this._reality.changeTargetToKnown(t,r.artk_direction,r.marker_width)){
					//遷移の成功チェック
						return;//失敗
					}
					//遷移に成功したので、tagにResult情報をコピーしておく。（後で表示に使う）
					t.tag=r;
				}else{
					//一致しないので、このターゲットは捨てる。
					this._reality.changeTargetToDead(t,15);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}
	
	private final static String PARAM_FILE = "../../Data/camera_para.dat";
	private final static String PATT_HIRO = "../../Data/patt.hiro";
	private final static String PATT_KANJI = "../../Data/patt.kanji";

	public static void main(String[] args)
	{
		try {
			NyARParam param = new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			new Test_NyARRealityGl_ARMarker(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
