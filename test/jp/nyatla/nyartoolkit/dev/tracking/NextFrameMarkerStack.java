package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NextFrameMarkerStack extends NyARObjectStack<NextFrameMarkerStack.Item>
{
	public class Item
	{
		public int oid;
		public NyARDoublePoint2d center=new NyARDoublePoint2d();
		public NyARDoublePoint2d vertex0=new NyARDoublePoint2d();
		/**
		 * 探索距離
		 */
		public double dist;
		
		
	}
	protected NextFrameMarkerStack.Item createElement()
	{
		return new NextFrameMarkerStack.Item();
	}	
	public NextFrameMarkerStack(int i_length) throws NyARException
	{
		super(i_length,NextFrameMarkerStack.Item.class);
		return;
	}
	
}