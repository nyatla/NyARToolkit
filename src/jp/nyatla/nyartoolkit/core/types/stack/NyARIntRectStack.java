package jp.nyatla.nyartoolkit.core.types.stack;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.TNyARIntPoint;
import jp.nyatla.nyartoolkit.core.types.TNyARIntRect;
import jp.nyatla.util.NyObjectStack;

public class NyARIntRectStack extends NyObjectStack
{
	public NyARIntRectStack(int i_length)
	{
		super(new TNyARIntRect[i_length]);

	}

	protected void onReservRequest(int i_start, int i_end, Object[] i_buffer)
	{
		for (int i = i_start; i < i_end; i++) {
			i_buffer[i] = new TNyARIntRect();
		}
	}

	public TNyARIntRect[] getArray()
	{
		return (TNyARIntRect[]) this._items;
	}

	public TNyARIntRect prePush() throws NyARException
	{
		return (TNyARIntRect) super.prePush();
	}
}
