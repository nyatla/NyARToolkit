package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


public class J2SeSketch
{
	private Frame _frame;
	public J2SeSketch()
	{
		this._frame= new Frame("NyARTK J2SE Sketch");
		this._frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		
		this._canvas = new Canvas();
		this._canvas.addGLEventListener(this);
		Insets ins = this._frame.getInsets();
		this._frame.setSize(320 + ins.left + ins.right,240 + ins.top + ins.bottom);		
		this._canvas.setBounds(ins.left, ins.top,320,240);
		this._frame.add(this._canvas);
		this._frame.setVisible(true);
	}
	public void fps(int i_fps)
	{
		
	}
	public void size(NyARIntSize i_s)
	{
		this.size(i_s.w,i_s.h);
	}
	public void size(int i_w,int i_h)
	{
		Insets ins = this._frame.getInsets();
		this._frame.setSize(i_w + ins.left + ins.right,i_h + ins.top + ins.bottom);		
		this._canvas.setBounds(ins.left, ins.top, i_w,i_h);
	}
	public final void init(GLAutoDrawable drawable)
	{
		try {
			GL gl=drawable.getGL();
			this.setup(gl);
			Animator animator = new Animator(drawable);
			animator.start();
			this._is_setup_done=true;
		} catch (NyARException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL gl=drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, width, height);
		return;
	}
	public final void display(GLAutoDrawable drawable)
	{
		try {
			if(this._is_setup_done){
				this.draw(drawable.getGL());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2)
	{
	}
	public void setup(GL i_gl) throws NyARException
	{
		
	}
	public void draw(GL i_gl)
	{
		
	}
}
