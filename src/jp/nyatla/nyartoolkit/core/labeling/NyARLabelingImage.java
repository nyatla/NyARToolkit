package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.core.types.*;

public class NyARLabelingImage
{
    private TNyARIntSize _size;
    private int[][] _image;
    private NyARLabelingLabelList _label_list;
    /**
     * 
     * @param i_width
     * @param i_height
     */
    public NyARLabelingImage(int i_width,int i_height)
    {
	this._size=new TNyARIntSize();
	this._size.w=i_width;
	this._size.h=i_height;
	this._image=new int[i_height][i_width];
	this._label_list=new NyARLabelingLabelList();
    }
    public TNyARIntSize getSize()
    {
	return this._size;
    }
    public int[][] getImage()
    {
	return this._image;
    }
    public NyARLabelingLabelList getLabelList()
    {
	return this._label_list;
    }
}
