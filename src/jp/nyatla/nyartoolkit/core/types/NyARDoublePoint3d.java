package jp.nyatla.nyartoolkit.core.types;

public class NyARDoublePoint3d
{
	public double x;
	public double y;
	public double z;
	/**
	 * 配列ファクトリ
	 * @param i_number
	 * @return
	 */
	public static NyARDoublePoint3d[] createArray(int i_number)
	{
		NyARDoublePoint3d[] ret=new NyARDoublePoint3d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARDoublePoint3d();
		}
		return ret;
	}
}
