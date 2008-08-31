package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.core.raster.NyARRaster_BasicClass;
import jp.nyatla.nyartoolkit.core.raster.TNyRasterType;


public class NyARLabelingImage extends NyARRaster_BasicClass
{
	protected int[][] _ref_buf;
	private NyARLabelingLabelStack _label_list;

	public NyARLabelingImage(int i_width, int i_height)
	{
		this._ref_buf =new int[i_height][i_width];
		this._size.w = i_width;
		this._size.h = i_height;
		this._label_list = new NyARLabelingLabelStack();
		return;
	}

	public int[][] getBufferObject()
	{
		return this._ref_buf;
	}

	public int getBufferType()
	{
		return TNyRasterType.BUFFERFORMAT_INT2D;
	}
	/**
	 * ラベリング結果がインデックステーブルを持つ場合、その配列を
	 * 返します。持っていない場合、nullを返します。
	 * @return
	 */
	public int[] getLabelIndex()
	{
		return null;
	}
	public int getLabelIndexCount()
	{
		return -1;
	}
	public NyARLabelingLabelStack getLabelStack()
	{
		return this._label_list;
	}
}
