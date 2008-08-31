package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.util.*;

/**
 * NyLabelの予約型動的配列
 * 
 */
public class NyARLabelingLabelStack extends NyObjectStack
{
	private final static int ARRAY_MAX = 1024 * 16;	
	public NyARLabelingLabelStack()
	{
		super(new NyARLabelingLabel[ARRAY_MAX]);

	}

	protected void onReservRequest(int i_start, int i_end, Object[] i_buffer)
	{
		for (int i = i_start; i < i_end; i++) {
			i_buffer[i] = new NyARLabelingLabel();
		}
	}
	public NyARLabelingLabel[] getArray()
	{
		return (NyARLabelingLabel[]) this._items;
	}

	public NyARLabelingLabel prePush() throws NyARException
	{
		return (NyARLabelingLabel) super.prePush();
	}	
}
