/**
 * simpleLiteと同じようなテストプログラム
 * マーカーの一致度の最低値をチェックするところを抜いたので、同じマーカーを大量に
 * 検出すると面白いことになります。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jogl.sample;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import javax.media.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;

import com.sun.opengl.util.Animator;

import jp.nyatla.nyartoolkit.core.NyARCode;

import jp.nyatla.nyartoolkit.jmf.utils.JmfCameraCapture;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jogl.utils.*;


public class JavaSimpleLite implements GLEventListener,JmfCaptureListener
{
    private final String CARCODE_FILE ="../../Data/patt.hiro";
    private final String PARAM_FILE   ="../../Data/camera_para.dat";
       
    private final static int SCREEN_X=320;
    private final static int SCREEN_Y=240;
    private Animator animator;
    private GLNyARRaster_RGB cap_image;
    
    private JmfCameraCapture capture;
    private GL  gl;
    private NyARGLUtil glnya;


    //NyARToolkit関係
    private GLNyARSingleDetectMarker nya;
    private GLNyARParam ar_param;
    /**
     * 立方体を書く
     *
     */
    void drawCube()
    {
    	// Colour cube data.
    	int polyList = 0;
    	float fSize = 0.5f;//マーカーサイズに対して0.5倍なので、4cmのナタデココ
    	int f, i;	
    	float[][] cube_vertices=new float[][]{
    		{1.0f, 1.0f, 1.0f}, {1.0f, -1.0f, 1.0f}, {-1.0f, -1.0f, 1.0f}, {-1.0f, 1.0f, 1.0f},
    		{1.0f, 1.0f, -1.0f}, {1.0f, -1.0f, -1.0f}, {-1.0f, -1.0f, -1.0f}, {-1.0f, 1.0f, -1.0f}
    	};
    	float[][] cube_vertex_colors=new float[][]{
    	{1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f},
    	{1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}
    	};
    	int cube_num_faces = 6;
    	short[][] cube_faces =new short[][]{
    	{3, 2, 1, 0}, {2, 3, 7, 6}, {0, 1, 5, 4}, {3, 0, 4, 7}, {1, 2, 6, 5}, {4, 5, 6, 7}
    	};
    	
    	if (polyList==0) {
            polyList = gl.glGenLists (1);
            gl.glNewList(polyList, GL.GL_COMPILE);
            gl.glBegin(GL.GL_QUADS);
            for (f = 0; f < cube_num_faces; f++)
                for (i = 0; i < 4; i++) {
                    gl.glColor3f (cube_vertex_colors[cube_faces[f][i]][0], cube_vertex_colors[cube_faces[f][i]][1], cube_vertex_colors[cube_faces[f][i]][2]);
                    gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
                }
            gl.glEnd();
            gl.glColor3f(0.0f, 0.0f, 0.0f);
            for (f = 0; f < cube_num_faces; f++) {
            	gl.glBegin (GL.GL_LINE_LOOP);
            	for (i = 0; i < 4; i++)
            		gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
            	gl.glEnd ();
            }
            gl.glEndList ();
    	}
    	
    	gl.glPushMatrix(); // Save world coordinate system.
    	gl.glTranslatef(0.0f, 0.0f, 0.5f); // Place base of cube on marker surface.
    	gl.glRotatef(0.0f, 0.0f, 0.0f, 1.0f); // Rotate about z axis.
    	gl.glDisable(GL.GL_LIGHTING);	// Just use colours.
    	gl.glCallList(polyList);	// Draw the cube.
    	gl.glPopMatrix();	// Restore world coordinate system.
    	
    }
    
    
    
    public JavaSimpleLite()
    {
        Frame frame = new Frame("Java simpleLite with NyARToolkit");


        // 3Dを描画するコンポーネント
        GLCanvas canvas = new GLCanvas();
        frame.add(canvas);
        canvas.addGLEventListener(this);
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        frame.setVisible(true);
        Insets ins=frame.getInsets();
        frame.setSize(SCREEN_X+ins.left+ins.right,SCREEN_Y+ins.top+ins.bottom);
        canvas.setBounds(ins.left,ins.top,SCREEN_X,SCREEN_Y);
    }

    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //NyARToolkitの準備
        try{
            //キャプチャの準備
            capture=new JmfCameraCapture(320,240,15f,JmfCameraCapture.PIXEL_FORMAT_RGB);
            capture.setCaptureListener(this);
            //NyARToolkitの準備
            ar_param=new GLNyARParam();
            NyARCode ar_code  =new NyARCode(16,16);
            ar_param.loadFromARFile(PARAM_FILE);
            ar_param.changeSize(SCREEN_X,SCREEN_Y);
            nya=new GLNyARSingleDetectMarker(ar_param,ar_code,80.0);
            ar_code.loadFromARFile(CARCODE_FILE);
            //NyARToolkit用の支援クラス
            glnya=new NyARGLUtil(gl,ar_param);
            //GL対応のRGBラスタオブジェクト
            cap_image=new GLNyARRaster_RGB(gl,ar_param);
            //キャプチャ開始
            capture.start();
       }catch(Exception e){
            e.printStackTrace();
        }
        animator = new Animator(drawable);

        animator.start();

    }

    public void reshape(GLAutoDrawable drawable,
        int x, int y,
        int width, int height)
    {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0,  width, height);

        //視体積の設定
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        //見る位置
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable)
    {
        
        try{
            if(!cap_image.hasData()){
        	return;
            }    
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.          
            //画像チェックしてマーカー探して、背景を書く
            boolean is_marker_exist;
            synchronized(cap_image){
        	is_marker_exist=nya.detectMarkerLite(cap_image,100);
        	//背景を書く
        	glnya.drawBackGround(cap_image, 1.0);
            }
            //あったら立方体を書く
            if(is_marker_exist){
        	//マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
                // Projection transformation.
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glLoadMatrixd(ar_param.getCameraFrustumRH(),0);
                gl.glMatrixMode(GL.GL_MODELVIEW);
                // Viewing transformation.
                gl.glLoadIdentity();
                gl.glLoadMatrixd(nya.getCameraViewRH(),0);

            
                // All other lighting and geometry goes here.
                drawCube();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void onUpdateBuffer(Buffer i_buffer)
    {
	try{
	    synchronized(cap_image){
		cap_image.setBuffer(i_buffer, true);
	    }
	}catch(Exception e){
	    e.printStackTrace();
	}        
    }

    public void displayChanged(GLAutoDrawable drawable,
                               boolean modeChanged,
                               boolean deviceChanged) {}

    public static void main(String[] args) {
        new JavaSimpleLite();
    }
}

