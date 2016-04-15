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
package jp.nyatla.nyartoolkit.sandbox.quadx2;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import javax.media.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;

import com.sun.opengl.util.Animator;

import jp.nyatla.nyartoolkit.core.marker.artk.NyARCode;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.utils.*;
/**
 * simpleLiteと同じようなテストプログラム
 * 出来る限りARToolKitのサンプルと似せて作ってあります。
 * 最も一致する"Hiro"マーカーを一つ選択して、その上に立方体を表示します。
 * 
 */
public class JavaSimpleLite_Quad implements GLEventListener, JmfCaptureListener
{
	private final String CARCODE_FILE = "../../Data/patt.hiro";

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private final static int SCREEN_X = 320;

	private final static int SCREEN_Y = 240;

	private Animator _animator;

	private GLNyARRaster_RGB _cap_image;

	private JmfCaptureDevice _capture;

	private GL _gl;

	private NyARGLUtil _glnya;

	//NyARToolkit関係
	private NyARSingleDetectMarker_Quad _nya;

	private NyARParam _ar_param;

	/**
	 * 立方体を書く
	 *
	 */
	void drawCube()
	{
		// Colour cube data.
		int polyList = 0;
		float fSize = 0.5f;//マーカーサイズに対して0.5倍なので、4cmの立方体
		int f, i;
		float[][] cube_vertices = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, -1.0f, 1.0f }, { -1.0f, -1.0f, 1.0f }, { -1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, -1.0f }, { 1.0f, -1.0f, -1.0f }, { -1.0f, -1.0f, -1.0f }, { -1.0f, 1.0f, -1.0f } };
		float[][] cube_vertex_colors = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
		int cube_num_faces = 6;
		short[][] cube_faces = new short[][] { { 3, 2, 1, 0 }, { 2, 3, 7, 6 }, { 0, 1, 5, 4 }, { 3, 0, 4, 7 }, { 1, 2, 6, 5 }, { 4, 5, 6, 7 } };

		if (polyList == 0) {
			polyList = _gl.glGenLists(1);
			_gl.glNewList(polyList, GL.GL_COMPILE);
			_gl.glBegin(GL.GL_QUADS);
			for (f = 0; f < cube_num_faces; f++)
				for (i = 0; i < 4; i++) {
					_gl.glColor3f(cube_vertex_colors[cube_faces[f][i]][0], cube_vertex_colors[cube_faces[f][i]][1], cube_vertex_colors[cube_faces[f][i]][2]);
					_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				}
			_gl.glEnd();
			_gl.glColor3f(0.0f, 0.0f, 0.0f);
			for (f = 0; f < cube_num_faces; f++) {
				_gl.glBegin(GL.GL_LINE_LOOP);
				for (i = 0; i < 4; i++)
					_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				_gl.glEnd();
			}
			_gl.glEndList();
		}

		_gl.glPushMatrix(); // Save world coordinate system.
		_gl.glTranslatef(0.0f, 0.0f, 0.5f); // Place base of cube on marker surface.
		_gl.glRotatef(0.0f, 0.0f, 0.0f, 1.0f); // Rotate about z axis.
		_gl.glDisable(GL.GL_LIGHTING); // Just use colours.
		_gl.glCallList(polyList); // Draw the cube.
		_gl.glPopMatrix(); // Restore world coordinate system.

	}

	public JavaSimpleLite_Quad()
	{
		Frame frame = new Frame("Java simpleLite with NyARToolkit");

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
		_gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//NyARToolkitの準備
		try {
			//キャプチャの準備
			JmfCaptureDeviceList list=new JmfCaptureDeviceList();
			_capture=list.getDevice(0);
			_capture.setCaptureFormat(SCREEN_X, SCREEN_Y, 15f);
			_capture.setOnCapture(this);
			//NyARToolkitの準備
			_ar_param = new NyARParam();
			NyARCode ar_code = new NyARCode(16, 16);
			_ar_param.loadARParamFromFile(PARAM_FILE);
			_ar_param.changeScreenSize(SCREEN_X, SCREEN_Y);
			_nya = new NyARSingleDetectMarker_Quad(_ar_param, ar_code, 80.0);
			_nya.setContinueMode(false);//ここをtrueにすると、transMatContinueモード（History計算）になります。
			ar_code.loadARPattFromFile(CARCODE_FILE);
			//NyARToolkit用の支援クラス
			_glnya = new NyARGLUtil(_gl);
			//GL対応のRGBラスタオブジェクト
			_cap_image = new GLNyARRaster_RGB(_ar_param,_capture.getCaptureFormat());
			//キャプチャ開始
			_capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//カメラパラメータの計算
		_glnya.toCameraFrustumRH(_ar_param,_camera_projection);
		_animator = new Animator(drawable);

		_animator.start();

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
	private double[] _camera_projection=new double[16];
	private NyARTransMatResult __display_transmat_result=new NyARTransMatResult();
	private double[] __display_wk=new double[16];
	public void display(GLAutoDrawable drawable)
	{
		NyARTransMatResult transmat_result=__display_transmat_result;
		try {
			if (!_cap_image.hasData()) {
				return;
			}
			_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.          
			//画像チェックしてマーカー探して、背景を書く
			boolean is_marker_exist;
			synchronized (_cap_image) {
				is_marker_exist = _nya.detectMarkerLite(_cap_image, 110);
				//背景を書く
				_glnya.drawBackGround(_cap_image, 1.0);
			}
			//マーカーがあれば、立方体を描画
			if (is_marker_exist) {
				//マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
				// Projection transformation.
				_gl.glMatrixMode(GL.GL_PROJECTION);
				_gl.glLoadMatrixd(_camera_projection, 0);
				_gl.glMatrixMode(GL.GL_MODELVIEW);
				// Viewing transformation.
				_gl.glLoadIdentity();
				//変換行列を取得
				_nya.getTransmationMatrix(transmat_result);
				//変換行列をOpenGL形式に変換
				_glnya.toCameraViewRH(transmat_result, __display_wk);
				_gl.glLoadMatrixd(__display_wk, 0);

				// All other lighting and geometry goes here.
				drawCube();
			}
			Thread.sleep(1);//タスク実行権限を一旦渡す
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		new JavaSimpleLite_Quad();
	}
}
