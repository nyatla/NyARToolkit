/**
 * Java3Dサンプルプログラム
 * シングルマーカー追跡用のBehaviorを使って、背景と１個のマーカーに連動したTransformGroup
 * を動かします。
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.sample;

import java.awt.BorderLayout;
import javax.media.j3d.*;

import com.sun.j3d.utils.universe.*;
import java.awt.*;
import javax.swing.JFrame;
import javax.vecmath.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.java3d.utils.*;

import com.sun.j3d.utils.geometry.ColorCube;

public class NyARJava3D extends JFrame implements NyARSingleMarkerBehaviorListener 
{
    private final String CARCODE_FILE ="../../Data/patt.hiro";
    private final String PARAM_FILE   ="../../Data/camera_para.dat";
    //NyARToolkit関係
    private NyARSingleMarkerBehaviorHolder nya_behavior;
    private J3dNyARParam ar_param;
    //universe関係
    private Canvas3D canvas;
    private Locale locale;
    private VirtualUniverse universe;
    public static void main(String[] args)
    {
	try{
            NyARJava3D frame = new NyARJava3D();
                
            frame.setVisible(true);
            Insets ins=frame.getInsets();
            frame.setSize(320+ins.left+ins.right,240+ins.top+ins.bottom);
            frame.startCapture();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
    public void onUpdate(boolean i_is_marker_exist,Transform3D i_transform3d)
    {
	/*
	 * TODO:Please write your behavior operation code here.
	 * マーカーの姿勢を元に他の３Dオブジェクトを操作するときは、ここに処理を書きます。*/
	
    }
    public void startCapture() throws Exception
    {
	nya_behavior.start();       	
    }
    public NyARJava3D() throws Exception
    {
	super("Java3D Example NyARToolkit");

        //NyARToolkitの準備
        NyARCode ar_code  =new NyARCode(16,16);
        ar_code.loadFromARFile(CARCODE_FILE);
        ar_param=new J3dNyARParam();
        ar_param.loadFromARFile(PARAM_FILE);
        ar_param.changeSize(320,240);
        
    	//localeの作成とlocateとviewの設定
        universe = new VirtualUniverse();
        locale = new Locale( universe );
        canvas=new Canvas3D( SimpleUniverse.getPreferredConfiguration());
        View view = new View();
        ViewPlatform viewPlatform = new ViewPlatform();
        view.attachViewPlatform( viewPlatform );
        view.addCanvas3D(canvas);
        view.setPhysicalBody( new PhysicalBody() );
        view.setPhysicalEnvironment( new PhysicalEnvironment());

        //視界の設定(カメラ設定から取得)
        Transform3D camera_3d=ar_param.getCameraTransform();
        view.setCompatibilityModeEnable(true);
        view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
        view.setLeftProjection(camera_3d);
        
        //視点設定(0,0,0から、Y軸を180度回転してZ+方向を向くようにする。)
        TransformGroup viewGroup = new TransformGroup();
        Transform3D viewTransform = new Transform3D();
        viewTransform.rotY(Math.PI);
        viewTransform.setTranslation(new Vector3d(0.0,0.0,0.0));
        viewGroup.setTransform( viewTransform );
        viewGroup.addChild( viewPlatform );
        BranchGroup viewRoot = new BranchGroup();
        viewRoot.addChild( viewGroup );
        locale.addBranchGraph( viewRoot );

        
	//バックグラウンドの作成
        Background background =new Background();
 	BoundingSphere bounds = new BoundingSphere();
        bounds.setRadius( 10.0 ); 
    	background.setApplicationBounds(bounds);
    	background.setImageScaleMode(Background.SCALE_FIT_ALL);
    	background.setCapability(Background.ALLOW_IMAGE_WRITE);
    	BranchGroup root = new BranchGroup();
    	root.addChild(background);
    	
    	//TransformGroupで囲ったシーングラフの作成
    	TransformGroup transform=new TransformGroup();
   	transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
   	transform.addChild(createSceneGraph());
    	root.addChild(transform);
    	    	
        //NyARToolkitのBehaviorを作る。(マーカーサイズはメートルで指定すること)
    	nya_behavior=new NyARSingleMarkerBehaviorHolder(ar_param,30f,ar_code,0.08);
    	//Behaviorに連動するグループをセット
    	nya_behavior.setTransformGroup(transform);
    	nya_behavior.setBackGround(background);
    	
    	//出来たbehaviorをセット
    	root.addChild(nya_behavior.getBehavior());
    	nya_behavior.setUpdateListener(this);
    	
    	//表示ブランチをLocateにセット
    	locale.addBranchGraph(root);
      
    	//ウインドウの設定
    	setLayout(new BorderLayout());
    	add(canvas,BorderLayout.CENTER);
    }
    /**
     * シーングラフを作って、そのノードを返す。
     * このノードは40mmの色つきナタデココを表示するシーン。ｚ軸を基準に20mm上に浮かせてる。
     * @return
     */
    private Node createSceneGraph()
    {
	TransformGroup tg=new TransformGroup();
        Transform3D mt=new Transform3D();
        mt.setTranslation(new Vector3d(0.00,0.0,20*0.001));
        // 大きさ 40mmの色付き立方体を、Z軸上で20mm動かして配置）
        tg.setTransform(mt);
        tg.addChild(new ColorCube(20*0.001));
        return tg;
    }
}
