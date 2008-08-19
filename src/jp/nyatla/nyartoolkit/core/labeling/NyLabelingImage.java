package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.core.types.*;

public class NyLabelingImage
{
    private TNyIntSize _size;
    private int[][] _image;
    private NyARLabelList _label_list;
    /**
     * 
     * @param i_width
     * @param i_height
     */
    public NyLabelingImage(int i_width,int i_height)
    {
	this._size=new TNyIntSize();
	this._size.w=i_width;
	this._size.h=i_height;
	this._image=new int[i_height][i_width];
	this._label_list=new NyARLabelList();
    }
    public TNyIntSize getSize()
    {
	return this._size;
    }
    public int[][] getImage()
    {
	return this._image;
    }
    public NyARLabelList getLabelList()
    {
	return this._label_list;
    }
}
