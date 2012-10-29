package jp.nyatla.nyartoolkit.utils.j2se.sketch;


import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * このクラスは、Awtに対応した簡易スケッチシステムです。
 * 単一のウインドウに1つのOpenGLCanvasを定義します。
 * 継承して、{@link #draw(GL)}と{@link #setup(GL)}関数を実装して、スケッチを完成させます。
 * スケッチは、{@link #run}関数でスタートします。
 */
public abstract class AwtSketch implements MouseListener ,MouseMotionListener 
{
	private Canvas _canvas;
	protected Frame _frame;
	boolean _is_setup_done=false;
	public void run()
	{
		this._frame= new Frame("NyARTK Sketch");
		this._frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		Insets ins = this._frame.getInsets();
		this._canvas=new Canvas();
		this._frame.add(this._canvas);
		this._frame.setSize(320 + ins.left + ins.right,240 + ins.top + ins.bottom);		
		this._canvas.setBounds(0,0,320,240);		
		try {
			this._frame.setVisible(true);
			this.setup(this._frame,this._canvas);
			for(;;){this.draw(this._canvas);}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void title(String i_title)
	{
		this._frame.setTitle(i_title);
	}

	public void size(NyARIntSize i_s)
	{
		this.size(i_s.w,i_s.h);
	}
	public void size(int i_w,int i_h)
	{
		Insets ins = this._frame.getInsets();
		this._frame.setSize(i_w + ins.left + ins.right,i_h + ins.top + ins.bottom);		
		this._canvas.setBounds(ins.left,ins.top,i_w,i_h);
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseDragged(MouseEvent arg0) {}
	public void mouseMoved(MouseEvent arg0) {}
	public abstract void setup(Frame i_frame,Canvas i_canvas) throws Exception;
	public abstract void draw(Canvas i_canvas) throws Exception;
}
