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

import java.awt.event.*;
import java.awt.*;
import javax.media.Buffer;
import javax.media.opengl.*;
import com.sun.opengl.util.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.utils.*;
/**
 * 2種類の最適化アルゴリズムを比較するテストプログラム
 *
 */

class Program implements JmfCaptureListener
{
	private OptimizeCompareTest _view1;
	private OptimizeCompareTest _view2;
	public Object _sync_object=new Object();	
	public NyARParam _ar_param;
	public NyARCode _ar_code;
	private final static int SCREEN_X = 320;
	private final static int SCREEN_Y = 240;
	private JmfCaptureDevice _capture;
	public GLNyARRaster_RGB _cap_image;
	public Program(NyARParam i_param, NyARCode i_ar_code) throws NyARException
	{
		// キャプチャの準備
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		this._capture = devlist.getDevice(0);
		if (!this._capture.setCaptureFormat(SCREEN_X, SCREEN_Y, 30.0f)) {
			throw new NyARException();
		}
		this._ar_param=i_param;
		this._ar_code=i_ar_code;
		this._capture.setOnCapture(this);
		// GL対応のRGBラスタオブジェクト
		this._cap_image = new GLNyARRaster_RGB(i_param, this._capture.getCaptureFormat());	
		this._view1=new OptimizeCompareTest(this,NyARSingleDetectMarker.PF_NYARTOOLKIT);
		this._view2=new OptimizeCompareTest(this,NyARSingleDetectMarker.PF_NYARTOOLKIT_ARTOOLKIT_FITTING);
		this._capture.start();
		return;
	}
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (this._sync_object) {
				this._cap_image.setBuffer(i_buffer);
				this._view1.updateCapture(this._cap_image);
				this._view2.updateCapture(this._cap_image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//GL API
	void glDrawCube(GL i_gl)
	{
		// Colour cube data.
		int polyList = 0;
		float fSize = 0.5f;// マーカーサイズに対して0.5倍なので、4cmの立方体
		int f, i;
		float[][] cube_vertices = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, -1.0f, 1.0f }, { -1.0f, -1.0f, 1.0f }, { -1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, -1.0f }, { 1.0f, -1.0f, -1.0f }, { -1.0f, -1.0f, -1.0f }, { -1.0f, 1.0f, -1.0f } };
		float[][] cube_vertex_colors = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
		int cube_num_faces = 6;
		short[][] cube_faces = new short[][] { { 3, 2, 1, 0 }, { 2, 3, 7, 6 }, { 0, 1, 5, 4 }, { 3, 0, 4, 7 }, { 1, 2, 6, 5 }, { 4, 5, 6, 7 } };

		if (polyList == 0) {
			polyList = i_gl.glGenLists(1);
			i_gl.glNewList(polyList, GL.GL_COMPILE);
			i_gl.glBegin(GL.GL_QUADS);
			for (f = 0; f < cube_num_faces; f++)
				for (i = 0; i < 4; i++) {
					i_gl.glColor3f(cube_vertex_colors[cube_faces[f][i]][0], cube_vertex_colors[cube_faces[f][i]][1], cube_vertex_colors[cube_faces[f][i]][2]);
					i_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				}
			i_gl.glEnd();
			i_gl.glColor3f(0.0f, 0.0f, 0.0f);
			for (f = 0; f < cube_num_faces; f++) {
				i_gl.glBegin(GL.GL_LINE_LOOP);
				for (i = 0; i < 4; i++)
					i_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				i_gl.glEnd();
			}
			i_gl.glEndList();
		}

		i_gl.glPushMatrix(); // Save world coordinate system.
		i_gl.glTranslatef(0.0f, 0.0f, 0.5f); // Place base of cube on marker surface.
		i_gl.glRotatef(0.0f, 0.0f, 0.0f, 1.0f); // Rotate about z axis.
		i_gl.glDisable(GL.GL_LIGHTING); // Just use colours.
		i_gl.glCallList(polyList); // Draw the cube.
		i_gl.glPopMatrix(); // Restore world coordinate system.
		return;
	}
	
}
/**
 * simpleLiteと同じようなテストプログラム 出来る限りARToolKitのサンプルと似せて作ってあります。 最も一致する"Hiro"マーカーを一つ選択して、その上に立方体を表示します。
 * 
 */
public class OptimizeCompareTest implements GLEventListener
{
	private final static int SCREEN_X = 320;

	private final static int SCREEN_Y = 240;

	private Animator _animator;
	
	private Program _parent;


//	private JmfCaptureDevice _capture;

	private GL _gl;
	private NyARGLUtil _glnya;

	// NyARToolkit関係
	private NyARSingleDetectMarker _nya;
	private NyARParam _ar_param;

	private double[] _camera_projection = new double[16];


	public OptimizeCompareTest(Program i_program,int i_pf) throws NyARException
	{
		this._parent=i_program;
		this._ar_param = i_program._ar_param;

		Frame frame = new Frame("["+i_pf+"]");

		// NyARToolkitの準備
		this._nya = new NyARSingleDetectMarker(this._ar_param, i_program._ar_code, 80.0,i_program._cap_image.getBufferType(),i_pf);
		this._nya.setContinueMode(true);// ここをtrueにすると、transMatContinueモード（History計算）になります。
		
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
		// NyARToolkitの準備
		try {
			// NyARToolkit用の支援クラス
			_glnya = new NyARGLUtil(_gl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// カメラパラメータの計算
		this._glnya.toCameraFrustumRH(this._ar_param,this._camera_projection);
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
		if (!this._parent._cap_image.hasData()) {
			return;
		}
		// 背景を書く
		this._gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		this._glnya.drawBackGround(this._parent._cap_image, 1.0);			
		try{
			synchronized(this._parent._sync_object){
				// マーカーがあれば、立方体を描画
				if (this._is_marker_exist){
					// マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
					// Projection transformation.
					_gl.glMatrixMode(GL.GL_PROJECTION);
					_gl.glLoadMatrixd(_camera_projection, 0);
					_gl.glMatrixMode(GL.GL_MODELVIEW);
					// Viewing transformation.
					_gl.glLoadIdentity();
					// 変換行列を取得
					_nya.getTransmationMatrix(transmat_result);
					// 変換行列をOpenGL形式に変換
					_glnya.toCameraViewRH(transmat_result, __display_wk);
					_gl.glLoadMatrixd(__display_wk, 0);
		
					// All other lighting and geometry goes here.
					this._parent.glDrawCube(_gl);
				}
			}
			Thread.sleep(1);// タスク実行権限を一旦渡す
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void updateCapture(GLNyARRaster_RGB i_img)
	{
		try {
			synchronized (this._parent._sync_object) {
				this._is_marker_exist =this._nya.detectMarkerLite(i_img, 110);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private final static String PARAM_FILE = "../Data/camera_para.dat";

	public static void main(String[] args)
	{
		try {
			NyARParam param = new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			param.changeScreenSize(SCREEN_X, SCREEN_Y);

			NyARCode code = new NyARCode(16, 16);
			code.loadARPattFromFile(CARCODE_FILE);

			new Program(param, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
