package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class IntRectStack extends NyARObjectStack<NyARIntRect>
{
	protected NyARIntRect createElement()
	{
		return new NyARIntRect();
	}	
	public IntRectStack(int i_length) throws NyARException
	{
		super(i_length,NyARIntRect.class);
		return;
	}
	
}