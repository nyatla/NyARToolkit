package jp.nyatla.nyartoolkit.dev.tracking.outline;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NyAROutlineTrackSrcRefTable extends NyARPointerStack<NyAROutlineTrackSrcTable.Item>
{	
	public NyAROutlineTrackSrcRefTable(int i_length) throws NyARException
	{
		super(i_length,NyAROutlineTrackSrcTable.Item.class);
		return;
	}
}