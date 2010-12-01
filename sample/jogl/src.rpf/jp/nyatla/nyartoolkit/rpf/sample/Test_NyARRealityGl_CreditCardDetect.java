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
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.rpf.mklib.CardDetect;
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
 * このサンプルは、定型以外のマーカを認識する実験プログラムです。
 *
 * 未知の四角形の比率推定をして、立方体を表示します。
 * このプログラムでは、未知の四角形を正面から撮影したときに比率を推定して、それがクレジットカードの比率に近ければ、
 * クレジットカードと判定して、その上に立方体を表示します。
 * 
 * マーカーには適当なクレジットカードを使ってください。
 * 
 * エッジ検出の性能が十分でないため、カード検出には十分なコントラストが必要です。
 * （白色のカードの場合は黒色の背景、黒色のカードなら白色の背景など。）
 * 
 * @author nyatla
 */
public class Test_NyARRealityGl_CreditCardDetect implements GLEventListener, JmfCaptureListener
{
	long clock;
	private final static int SCREEN_X = 640;
	private final static int SCREEN_Y = 480;

	private Animator _animator;
	private JmfCaptureDevice _capture;

	private GL _gl;

	private Object _sync_object=new Object();

	NyARRealityGl _reality;
	NyARRealitySource_Jmf _src;
	CardDetect _mklib;

	public Test_NyARRealityGl_CreditCardDetect(NyARParam i_param) throws NyARException
	{
		clock=0;
		Frame frame = new Frame("NyARToolkit+RPF["+this.getClass().getName()+"]");
		
		// キャプチャの準備
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		this._capture = devlist.getDevice(0);
		if (!this._capture.setCaptureFormat(SCREEN_X, SCREEN_Y, 15.0f)) {
			throw new NyARException();
		}
		this._capture.setOnCapture(this);
		//Realityの構築
		i_param.changeScreenSize(SCREEN_X, SCREEN_Y);	
		//キャプチャ画像と互換性のあるRealitySourceを構築
//		this._src=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),i_param.getDistortionFactor(),2,100);
		this._src=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),null,2,100);
		//OpenGL互換のRealityを構築		
		this._reality=new NyARRealityGl(i_param.getPerspectiveProjectionMatrix(),i_param.getScreenSize(),10,10000,3,10);
		//マーカライブラリ(比率推定)の構築
		this._mklib= new CardDetect();

				
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
				//背景を描画
				this._reality.glDrawRealitySource(this._gl,this._src);
				// Projection transformation.
				this._gl.glMatrixMode(GL.GL_PROJECTION);
				this._reality.glLoadCameraFrustum(this._gl);
				//ターゲットリストを走査して、画面に内容を反映
				NyARRealityTargetList tl=this._reality.refTargetList();
				for(int i=tl.getLength()-1;i>=0;i--){
					NyARRealityTarget t=tl.getItem(i);
					CardDetect.UnknownRectInfo tag=((CardDetect.UnknownRectInfo)(t.tag));
					switch(t.getTargetType())
					{
					case NyARRealityTarget.RT_KNOWN:
						//立方体の描画
						this._gl.glMatrixMode(GL.GL_MODELVIEW);
						this._gl.glLoadIdentity();
						NyARDoubleMatrix44 m=t.refTransformMatrix();
						this._reality.glLoadModelViewMatrix(this._gl,m);
						//カード番号を消す。
						_gl.glColor3d(0.0,0.0, 0.0);
						_gl.glBegin(GL.GL_POLYGON);
						_gl.glVertex2d(-35,-20);
						_gl.glVertex2d(35,-20);
						_gl.glVertex2d(35,20);
						_gl.glVertex2d(-35,20);
						_gl.glEnd();
						
						_gl.glPushMatrix(); // Save world coordinate system.
						_gl.glTranslatef(0,0,20f); // Place base of cube on marker surface.
						_gl.glDisable(GL.GL_LIGHTING); // Just use colours.
						_gl.glLineWidth(1);
						NyARGLDrawUtil.drawColorCube(this._gl,40f);
						_gl.glPopMatrix(); // Restore world coordinate system.
						break;
					case NyARRealityTarget.RT_UNKNOWN:
						if(tag==null){
							break;
						}
						if(t.getGrabbRate()<20){
							break;
						}
						if(tag.last_status==CardDetect.MORE_FRONT_CENTER)
						{
							NyARDoublePoint2d[] p=t.refTargetVertex();
							//もっと真ん中から写せメッセージ
							double c=Math.sin((clock%45*4)*Math.PI/180);
							_gl.glColor3d(c,c, 0.0);
							_gl.glLineWidth(2);
							NyARGLDrawUtil.beginScreenCoordinateSystem(this._gl,SCREEN_X,SCREEN_Y,true);
							_gl.glBegin(GL.GL_LINE_LOOP);
							_gl.glVertex2d(p[0].x,p[0].y);
							_gl.glVertex2d(p[1].x,p[1].y);
							_gl.glVertex2d(p[2].x,p[2].y);
							_gl.glVertex2d(p[3].x,p[3].y);
							_gl.glEnd();
							NyARGLDrawUtil.endScreenCoordinateSystem(this._gl);
							NyARGLDrawUtil.beginScreenCoordinateSystem(this._gl,SCREEN_X,SCREEN_Y,false);
							NyARGLDrawUtil.setFontColor(t.getGrabbRate()<50?Color.RED:Color.BLUE);
							NyARIntPoint2d cp=new NyARIntPoint2d();
							t.getTargetCenter(cp);
							_gl.glTranslated(cp.x,SCREEN_Y-cp.y,1);
							NyARGLDrawUtil.drawText("Please view the card from the front. "+t.getGrabbRate(),1f);
							NyARGLDrawUtil.endScreenCoordinateSystem(this._gl);
							
						}else if(tag.last_status==CardDetect.ESTIMATE_NOW)
						{
							NyARDoublePoint2d[] p=t.refTargetVertex();
							//もっと真ん中から写せメッセージ
							double c=Math.sin((clock%45*8)*Math.PI/180);
							_gl.glColor3d(0,c, 0.0);
							_gl.glLineWidth(2);
							NyARGLDrawUtil.beginScreenCoordinateSystem(this._gl,SCREEN_X,SCREEN_Y,true);
							_gl.glBegin(GL.GL_LINE_LOOP);
							_gl.glVertex2d(p[0].x,p[0].y);
							_gl.glVertex2d(p[1].x,p[1].y);
							_gl.glVertex2d(p[2].x,p[2].y);
							_gl.glVertex2d(p[3].x,p[3].y);
							_gl.glEnd();
							NyARGLDrawUtil.endScreenCoordinateSystem(this._gl);
							NyARGLDrawUtil.beginScreenCoordinateSystem(this._gl,SCREEN_X,SCREEN_Y,false);
							NyARGLDrawUtil.setFontColor(t.getGrabbRate()<50?Color.RED:Color.BLUE);
							NyARIntPoint2d cp=new NyARIntPoint2d();
							t.getTargetCenter(cp);
							_gl.glTranslated(cp.x,SCREEN_Y-cp.y,1);
							NyARGLDrawUtil.drawText("Check card size...",1f);
							NyARGLDrawUtil.endScreenCoordinateSystem(this._gl);
							
						}
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
		//クロック進行
		clock++;
		try {
			synchronized (this._sync_object)
			{
				this._src.setImage(i_buffer);
				this._reality.progress(this._src);
				for(int i=this._reality.refTargetList().getLength()-1;i>=0;i--)
				{
					NyARRealityTarget t=this._reality.refTargetList().getItem(i);
					switch(t.getTargetType())
					{
					case NyARRealityTarget.RT_UNKNOWN:
						//tagに推定用オブジェクトが割り当てられていなければ割り当てる。
						if(t.tag==null){
							t.tag=new CardDetect.UnknownRectInfo();
						}
						CardDetect.UnknownRectInfo r=(CardDetect.UnknownRectInfo)t.tag;
						//推定
						this._mklib.detectCardDirection(t, r);
						switch(r.last_status){
						case CardDetect.ESTIMATE_COMPLETE:
							//レートチェック(17/11(1.54)くらいならクレジットカードサイズ。)
							if(1.35<r.rate && r.rate<1.75){
								//クレジットカードサイズをセット
								if(!this._reality.changeTargetToKnown(t,r.artk_direction,85,55)){
									//遷移の成功チェック
									break;//失敗
								}
							}else{
								//サイズ違う？
								this._reality.changeTargetToDead(t,50);
							}
							break;
						case CardDetect.ESTIMATE_NOW:
							break;
						case CardDetect.FAILED_ESTIMATE:
							this._reality.changeTargetToDead(t,15);
							break;
						}
					default:
						//他の種類は特にやることは無い。
					}
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

	public static void main(String[] args)
	{
		try {
			NyARParam param = new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			new Test_NyARRealityGl_CreditCardDetect(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
