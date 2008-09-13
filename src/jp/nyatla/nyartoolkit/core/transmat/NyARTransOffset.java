package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.core.types.*;


final public class NyARTransOffset
{
	public NyARDoublePoint3d[] vertex=NyARDoublePoint3d.createArray(4);
	public NyARDoublePoint3d point=new NyARDoublePoint3d();	
	/**
	 * 中心位置と辺長から、オフセット情報を作成して設定する。
	 * @param i_width
	 * @param i_center
	 */
	public void setSquare(double i_width,NyARDoublePoint2d i_center)
	{
		final double w_2 = i_width / 2.0;
		
		NyARDoublePoint3d vertex3d_ptr;
		vertex3d_ptr= this.vertex[0];
		vertex3d_ptr.x = -w_2;
		vertex3d_ptr.y =  w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[1];
		vertex3d_ptr.x = w_2;
		vertex3d_ptr.y = w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[2];
		vertex3d_ptr.x =  w_2;
		vertex3d_ptr.y = -w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[3];
		vertex3d_ptr.x = -w_2;
		vertex3d_ptr.y = -w_2;
		vertex3d_ptr.z = 0.0;
		
		this.point.x=-i_center.x;
		this.point.y=-i_center.y;
		this.point.z=0;

	}
}
