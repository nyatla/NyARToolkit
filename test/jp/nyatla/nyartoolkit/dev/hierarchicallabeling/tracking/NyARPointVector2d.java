package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

public class NyARPointVector2d
{
	public double x;
	public double y;
	public double dx;
	public double dy;
	public double dist;
	public static NyARPointVector2d[] createArray(int i_length)
	{
		NyARPointVector2d[] r=new NyARPointVector2d[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new NyARPointVector2d();
		}
		return r;
	}
	/* 直行するベクトルを計算する。
	 */
	public void OrthogonalVec(NyARPointVector2d i_src)
	{
		this.dx=i_src.dy;
		this.dy=-i_src.dx;
	}
}