package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.stack.*;


public class NyARSquareTrackSrcTable extends NyARObjectStack<NyARSquareTrackSrcTable.Item>
{	
	public class Item
	{
		public NyARIntPoint2d[] vertex2d=NyARIntPoint2d.createArray(4);
		public NyARLinear[] line=NyARLinear.createArray(4);
		public NyARIntPoint2d center=new NyARIntPoint2d();
	}	
	
	protected NyARSquareTrackSrcTable.Item createElement()
	{
		return new NyARSquareTrackSrcTable.Item();
	}
	
	public NyARSquareTrackSrcTable(int i_length) throws NyARException
	{
		super(i_length,NyARSquareTrackSrcTable.Item.class);
		return;
	}

}