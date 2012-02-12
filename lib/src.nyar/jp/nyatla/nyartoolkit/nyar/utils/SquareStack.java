package jp.nyatla.nyartoolkit.nyar.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;


public class SquareStack extends NyARObjectStack<SquareStack.Item>
{
	public class Item extends NyARSquare
	{
		public NyARIntPoint2d center2d=new NyARIntPoint2d();
		/** 検出座標系の値*/
		public NyARIntPoint2d[] ob_vertex=NyARIntPoint2d.createArray(4);
		/** 頂点の分布範囲*/
		public NyARIntRect vertex_area=new NyARIntRect();
		/** rectの面積*/
		public int rect_area;
	}
	public SquareStack(int i_length) throws NyARException
	{
		super.initInstance(i_length,SquareStack.Item.class);
	}
	protected SquareStack.Item createElement() throws NyARException
	{
		return new SquareStack.Item();
	}		
}