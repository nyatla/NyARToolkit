package jp.nyatla.nyartoolkit.dev.tracking.detail.fixedthreshold;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.tracking.outline.NyAROutlineTrackSrcTable;

public class NyARDetailFixedThresholdTrackSrcTable extends NyARObjectStack<NyARDetailFixedThresholdTrackSrcTable.Item>
{	
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARIntPoint2d ideal_center=new NyARIntPoint2d();
		public NyARLinear[] ideal_line=NyARLinear.createArray(4);	
		public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);
	}
	public NyARDetailFixedThresholdTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref) throws NyARException
	{
		super(i_length,NyARDetailFixedThresholdTrackSrcTable.Item.class);
	}
	protected NyARDetailFixedThresholdTrackSrcTable.Item createElement()
	{
		return new NyARDetailFixedThresholdTrackSrcTable.Item();
	}
}
