package jp.nyatla.nyartoolkit.core.types;


/**
 * 点の座標と、そのベクトルで定義する直線を格納します。
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
	 * このベクトルと指定したベクトルの作るCos値を返します。
	 * @param i_v1
	 * @return
	 */
	public double getVecCos(NyARPointVector2d i_v1)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=this.dx;
		double y2=this.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
	}
	/**
	 * 個のベクトルと指定したベクトルの作るCos値を返します。
	 * @param i_v2_x
	 * @param i_v2_y
	 * @return
	 */
	public double getVecCos(double i_v2_x,double i_v2_y)
	{
		double x1=this.dx;
		double y1=this.dy;
		double d=(x1*i_v2_x+y1*i_v2_y)/Math.sqrt((x1*x1+y1*y1)*(i_v2_x*i_v2_x+i_v2_y*i_v2_y));
		return d;
	}
	public double getAbsVecCos(double i_v2_x,double i_v2_y)
	{
		double x1=this.dx;
		double y1=this.dy;
		double d=(x1*i_v2_x+y1*i_v2_y)/Math.sqrt((x1*x1+y1*y1)*(i_v2_x*i_v2_x+i_v2_y*i_v2_y));
		return d>=0?d:-d;
	}	
	/**
	 * 交点を求めます。
	 * @param i_vector1
	 * @param i_vector2
	 * @param o_point
	 * @return
	 */
	public final static boolean crossPos(NyARPointVector2d i_vector1,NyARPointVector2d i_vector2,NyARDoublePoint2d o_point)
	{
		double a1= i_vector1.dy;
		double b1=-i_vector1.dx;
		double c1=(i_vector1.dx*i_vector1.y-i_vector1.dy*i_vector1.x);
		double a2= i_vector2.dy;
		double b2=-i_vector2.dx;
		double c2=(i_vector2.dx*i_vector2.y-i_vector2.dy*i_vector2.x);
		final double w1 = a1 * b2 - a2 * b1;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (b1 * c2 - b2 * c1) / w1;
		o_point.y = (a2 * c1 - a1 * c2) / w1;
		return true;
	}	
}
