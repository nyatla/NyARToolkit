package jp.nyatla.nyartoolkit.core.raster;

import jp.nyatla.nyartoolkit.core.types.*;
/**
 * NyARRasterインタフェイスの基本関数/メンバを実装したクラス
 * 
 *
 */
public abstract class NyARRaster_BasicClass implements INyARRaster 
{
    final protected TNyIntSize _size=new TNyIntSize();
    final public int getWidth()
    {
	return this._size.w;
    }
    final public int getHeight()
    {
	return this._size.h;
    }
    final public TNyIntSize getSize()
    {
	return this._size;
    }
}
