package jp.nyatla.nyartoolkit.core.types;


/**
 * 点の座標と、そのベクトルを格納します。
 *
 */
public class NyARPointVector2d
{
	public double x;
	public double y;
	public double dx;
	public double dy;
	public static NyARPointVector2d[] createArray(int i_length)
	{
		NyARPointVector2d[] r=new NyARPointVector2d[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new NyARPointVector2d();
		}
		return r;
	}
	/**
	 * 法線ベクトルを計算します。
	 * @param i_src
	 * 元のベクトルを指定します。この値には、thisを指定できます。
	 */
	public void OrthogonalVec(NyARPointVector2d i_src)
	{
		double w=this.dx;
		this.dx=i_src.dy;
		this.dy=-w;
	}
	public void setValue(NyARPointVector2d i_value)
	{
		this.dx=i_value.dx;
		this.dy=i_value.dy;
		this.x=i_value.x;
		this.y=i_value.y;
	}
	/**
	 * 2つのベクトルの作るCos値を返します。
	 * @param i_v1
	 * @param i_v2
	 * @return
	 */
	public static double getVecCos(NyARPointVector2d i_v1,NyARPointVector2d i_v2)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=i_v2.dx;
		double y2=i_v2.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
	}
	public static double getVecCos(NyARPointVector2d i_v1,double i_v2_x,double i_v2_y)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double d=(x1*i_v2_x+y1*i_v2_y)/Math.sqrt((x1*x1+y1*y1)*(i_v2_x*i_v2_x+i_v2_y*i_v2_y));
		return d;
	}	
}
