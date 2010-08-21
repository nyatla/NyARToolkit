package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

public class VectorPos
{
	public double x;
	public double y;
	public double dx;
	public double dy;
	public double dist;
	public static VectorPos[] createArray(int i_length)
	{
		VectorPos[] r=new VectorPos[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new VectorPos();
		}
		return r;
	}
	/* 直行するベクトルを計算する。
	 */
	public void OrthogonalVec(VectorPos i_src)
	{
		this.dx=i_src.dy;
		this.dy=-i_src.dx;
	}
}