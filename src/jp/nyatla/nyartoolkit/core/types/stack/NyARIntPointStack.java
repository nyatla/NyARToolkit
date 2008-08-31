package jp.nyatla.nyartoolkit.core.types.stack;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.util.NyObjectStack;

public class NyARIntPointStack extends NyObjectStack
{
	public NyARIntPointStack(int i_length)
	{
		super(new TNyARIntPoint[i_length]);

	}

	protected void onReservRequest(int i_start, int i_end, Object[] i_buffer)
	{
		for (int i = i_start; i < i_end; i++) {
			i_buffer[i] = new TNyARIntPoint();
		}
	}

	public TNyARIntPoint[] getArray()
	{
		return (TNyARIntPoint[]) this._items;
	}

	public TNyARIntPoint prePush() throws NyARException
	{
		return (TNyARIntPoint) super.prePush();
	}
}
