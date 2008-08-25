package jp.nyatla.nyartoolkit.core.types;

import jp.nyatla.nyartoolkit.NyARException;

public class TNyARIntSize
{
    public int h;
    public int w;
    /**
     * サイズが同一であるかを確認する。
     * @param i_width
     * @param i_height
     * @return
     * @throws NyARException
     */
    public boolean isEqualSize(int i_width,int i_height)
    {
	if(i_width==this.w && i_height==this.h)
	{
	    return true;
	}
	return false;
    }
    /**
     * サイズが同一であるかを確認する。
     * @param i_width
     * @param i_height
     * @return
     * @throws NyARException
     */    
    public boolean isEqualSize(TNyARIntSize i_size)
    {
	if(i_size.w==this.w && i_size.h==this.h)
	{
	    return true;
	}
	return false;

    }

}
