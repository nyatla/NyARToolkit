package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.gl;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.Buffer;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;
import jp.nyatla.nyartoolkit.dev.rpf.mklib.RawbitSerialIdTable;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityTargetList;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource_Jmf;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;

import com.sun.opengl.util.Animator;

/**
 * NyARRealityシステムのサンプル。
 * 複数のIDマーカを同時に区別するサンプルです。同一画面内に同じIDが複数あってもOK
 *
 * サンプル実装なのでまだ全然動かないよ。
 * @author nyatla
 *
 */
public class NyARRealityGlTest_CaptureImage implements GLEventListener, JmfCaptureListener
{

	private final static int SCREEN_X = 320;
	private final static int SCREEN_Y = 240;

	private Animator _animator;
	private JmfCaptureDevice _capture;

	private GL _gl;
	private GLU _glu;

	private Object _sync_object=new Object();

	NyARRealityGl _reality;
	NyARRealitySource_Jmf _src;
	RawbitSerialIdTable _mklib;

	public NyARRealityGlTest_CaptureImage(NyARParam i_param) throws NyARException
	{
		Frame frame = new Frame("NyARReality on OpenGL");
		
		// キャプチャの準備
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		this._capture = devlist.getDevice(0);
		if (!this._capture.setCaptureFormat(SCREEN_X, SCREEN_Y, 30.0f)) {
			throw new NyARException();
		}
		this._capture.setOnCapture(this);
		//Realityの構築
		i_param.changeScreenSize(SCREEN_X, SCREEN_Y);	
		//キャプチャ画像と互換性のあるRealitySourceを構築
		this._src=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),i_param.getDistortionFactor(),2,100);
		//OpenGL互換のRealityを構築
		this._reality=new NyARRealityGl(i_param.getPerspectiveProjectionMatrix(),SCREEN_X, SCREEN_Y,0.1,100,3,3);
		//マーカライブラリ(NyId)の構築
		this._mklib= new RawbitSerialIdTable(10);
		//マーカサイズテーブルの作成(とりあえず全部4cm)
		this._mklib.addAnyItem(80);
				
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
		this._glu=new GLU();
		this._gl.glEnable(GL.GL_DEPTH_TEST);
		this._gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
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

	private double[] __display_wk = new double[16];

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
				
				NyARGLUtil.drawBackGround(this._glu,this._src.refRgbSource(), 1.0);			
				// Projection transformation.
				this._gl.glMatrixMode(GL.GL_PROJECTION);
				this._gl.glLoadMatrixd(this._reality.refGlFrastumRH(), 0);
				//ターゲットリストを走査して、画面に内容を反映
				NyARRealityTargetList tl=this._reality.refTargetList();
				for(int i=tl.getLength()-1;i>=0;i--){
					NyARRealityTarget t=tl.getItem(i);
					switch(t.getTargetType())
					{
					case NyARRealityTarget.RT_KNOWN:
						this._gl.glMatrixMode(GL.GL_MODELVIEW);
						// Viewing transformation.
						this._gl.glLoadIdentity();
						// 変換行列をOpenGL形式に変換(ここ少し変えるかも)
						NyARGLUtil.toCameraViewRH(t.refTransformMatrix(), __display_wk);
						_gl.glLoadMatrixd(__display_wk, 0);
						// All other lighting and geometry goes here.
						drawCube();
						break;
					case NyARRealityTarget.RT_UNKNOWN:
						
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
//				this._reality.changeTargetToKnown(t,1,80);

				//ターゲットに一致するデータを検索
				RawbitSerialIdTable.SelectResult r=new RawbitSerialIdTable.SelectResult();
				if(this._mklib.selectTarget(t,this._src,r)){
					//テーブルにターゲットが見つかったので遷移する。
					if(!this._reality.changeTargetToKnown(t,r.artk_direction,r.marker_width)){
					//遷移の成功チェック
						return;//失敗
					}
					//遷移に成功したので、tagにユーザ定義情報を書きこむ。
					t.tag=new Long(r.serial);
				}else{
					//一致しないので、このターゲットは捨てる。
					this._reality.changeTargetToDead(t);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}
	/**
	 * 現在の位置に立方体を書く関数です。
	 */
	void drawCube()
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
	
	private final static String PARAM_FILE = "../Data/camera_para.dat";

	public static void main(String[] args)
	{
		try {
			NyARParam param = new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			new NyARRealityGlTest_CaptureImage(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
