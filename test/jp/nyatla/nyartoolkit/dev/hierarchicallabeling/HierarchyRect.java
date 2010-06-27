package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import jp.nyatla.nyartoolkit.core.types.NyARIntRect;

/**
 * 階層構造のRECTを保持します。
 * @author nyatla
 *
 */
public class HierarchyRect extends NyARIntRect
{
	int id;
	/**
	 * 縮小率(dotのスキップすうと同値です。)
	 */
	int dot_skip;
	/**
	 * 子イメージの参照配列。非NULLで有効
	 */
	HierarchyRect[] ref_children;
	/**
	 * 指定した矩形を含む子アイテムを返す。
	 * @param i_x
	 * @param i_y
	 * @param i_w
	 * @param i_h
	 * @return
	 */
	public HierarchyRect getInnerChild(int i_x,int i_y,int i_w,int i_h)
	{
		if(this.ref_children==null){
			return null;
		}
		for(int i=this.ref_children.length-1;i>=0;i--)
		{
			if(this.ref_children[i].isInnerRect(i_x, i_y, i_w, i_h)){
				return this.ref_children[i];
			}
		}
		return null;
	}	
}
