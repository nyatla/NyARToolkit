package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;


/**
 * ピラミッド画像を使ったラべリングをするクラスです。
 *
 */
public abstract class HierarchyLabeling
{
	class NextStack extends NyARPointerStack<HierarchyRect>
	{
		public NextStack(int i_length) throws NyARException
		{
			super.initInstance(i_length, HierarchyRect.class);
		}
		protected HierarchyRect createElement()
		{
			return new HierarchyRect();
		}
		/**
		 * 指定した領域を含むRECTがあるか返す。
		 * @param i_x
		 * @param i_y
		 * @param i_w
		 * @param i_h
		 * @return
		 */
		public boolean hasInnerItem(int i_x,int i_y,int i_w,int i_h)
		{
			for(int i=this._length-1;i>=0;i--)
			{
				if(this._items[i].isInnerRect(i_x, i_y, i_w, i_h)){
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * 1層分のラべリングをするクラス
	 */

	class LabelingUnit extends NyARLabeling_Rle
	{
		protected int _th;
		private int _depth;
		public NextStack next_stack;
		HierarchyRect _target_image;
		private NyARIntSize _half_size=new NyARIntSize();
		protected NyARGrayscaleRaster _gs;
		
		private LabelingUnit _labeling_tree;
		protected HierarchyLabeling _parent;

		/**
		 * 
		 * @param i_parent
		 * @param i_width
		 * @param i_height
		 * @param i_raster_type
		 * @param i_depth
		 * @throws NyARException
		 */
		public LabelingUnit(HierarchyLabeling i_parent,int i_width,int i_height,int i_raster_type,int i_depth) throws NyARException
		{
			super(i_width,i_height);
			this._half_size.w=i_width/3;
			this._half_size.h=i_height/3;
			this._parent=i_parent;
			this._depth=i_depth;
			this._gs=new NyARGrayscaleRaster(i_width,i_height);
			//ラべリングツリーを作る。
			int length_resolution=i_parent._image_map.resolution.length;
			if(i_depth>2){
				//下階層を持つ場合のラべリング用
				int r=i_parent._image_map.resolution[length_resolution-i_depth+1];
				this._labeling_tree=new LabelingUnit(i_parent,i_width,i_height,i_raster_type,i_depth-1);
				this.next_stack=new NextStack(r*r);
			}else if(i_depth==2){
				//ツリーの末端ラべリング用
				int r=i_parent._image_map.resolution[length_resolution-i_depth+1];
				this._labeling_tree=new FinalLabelingUnit(i_parent,i_width,i_height,i_raster_type,i_depth-1);
				this.next_stack=new NextStack(r*r);
			}else{
				this.next_stack=null;
			}
			return;
		}
		/**
		 * 末端の領域範囲を基準に、現在の領域範囲を計算して、設定します。
		 * この階層の範囲は、指定値を1/(4^(depth-1))倍した値が設定されます。
		 * 例えば、depth=4の場合に100を設定すると、下位階層のレンジには、100/64,100/16,100/4,100/1の順に設定します。
		 * @param i_max
		 * @param i_min
		 */
		public void setAreaRange(int i_max,int i_min)
		{
			int n=(int)Math.pow(4,this._depth-1);
			int max=i_max/n;
			int min=i_min/n;
			super.setAreaRange(max<1?1:max,min<1?1:min);
			if(this._labeling_tree!=null){
				this._labeling_tree.setAreaRange(i_max,i_min);
			}
		}
		public void labeling(NyARGrayscaleRaster i_raster,HierarchyRect i_imagemap,int i_th) throws NyARException
		{
			//next_stackを全部クリア
			LabelingUnit la=this;
			while(la.next_stack!=null){
				la.next_stack.clear();
				la=la._labeling_tree;
			}
			//1段目をラべリング
			la=this;
			la.labeling_impl(i_raster, i_imagemap, i_th);
			while(la.next_stack!=null)
			{
				//次段をラべリング
				HierarchyRect[] infos=la.next_stack.getArray();
				for(int i=la.next_stack.getLength()-1;i>=0;i--){
					la._labeling_tree.labeling_impl(i_raster, infos[i], i_th);
				}				
				la=la._labeling_tree;
			}
		}
		
		public void labeling_impl(NyARGrayscaleRaster i_raster,HierarchyRect i_imagemap,int i_th) throws NyARException
		{
			this._th=i_th;
			this._target_image=i_imagemap;

			//GS化
			NyARGrayscaleRaster.copy(i_raster, i_imagemap.x, i_imagemap.y, i_imagemap.dot_skip, this._gs);
			//ラべリング
			super.labeling(this._gs,i_th);
			//末端の解像度なら終了
			if(i_imagemap.dot_skip==1){
				return;
			}
		}
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label) throws NyARException
		{
			HierarchyRect imagemap=this._target_image;
			
			// クリップ領域が画面の枠(4面)に接していれば除外
			if (i_label.clip_l==0 || i_label.clip_t==0 || i_label.clip_r+1==this._parent._image_map.window_size.w || i_label.clip_b+1==this._parent._image_map.window_size.h){
				return;
			}
			int w=i_label.clip_r-i_label.clip_l+1;
			int h=i_label.clip_b-i_label.clip_t+1;
			// 1/4より小さければ、下位の検出に回す
			if(this._half_size.isInnerSize(w,h)){
				int skip=imagemap.dot_skip;
				//矩形の見つかった実座標を計算
				int tl=i_label.clip_l*skip+imagemap.x;
				int tt=i_label.clip_t*skip+imagemap.y;
				int tw=w*skip;
				int th=h*skip;
				//既に下位に検出依頼を出している？
				if(this.next_stack.hasInnerItem(tl,tt,tw,th))
				{
					return;
				}
				//追加するマップを検索
				HierarchyRect next_map=imagemap.getInnerChild(tl,tt,tw,th);
				if(next_map!=null){
					this.next_stack.push(next_map);
					return;
				}
			}
			//矩形を検出した。情報その他を関数に通知
			this._parent.onLabelFound(imagemap,this._gs,this._th,i_label);
		}
	}
	/**
	 * 末端のラべリングクラス
	 * @author nyatla
	 *
	 */
	class FinalLabelingUnit extends LabelingUnit
	{
		public FinalLabelingUnit(HierarchyLabeling i_parent,int i_width,int i_height,int i_raster_type,int i_depth) throws NyARException
		{
			super(i_parent,i_width,i_height,i_raster_type,i_depth);
		}
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label) throws NyARException
		{
			HierarchyRect imagemap=this._target_image;
			
			// クリップ領域が画面の枠(4面)に接していれば除外
			if (i_label.clip_l==0 || i_label.clip_t==0 || i_label.clip_r+1==this._parent._image_map.window_size.w || i_label.clip_b+1==this._parent._image_map.window_size.h){
				return;
			}
			//矩形を検出した。情報その他を関数に通知
			this._parent.onLabelFound(imagemap,this._gs,this._th,i_label);
		}		
	}
	
	private LabelingUnit _labeling;
	private QsHsHierachyRectMap _image_map;
	public HierarchyLabeling(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		this._image_map=new QsHsHierachyRectMap(i_width,i_height,i_depth);
		if(i_depth>1){
			this._labeling=new LabelingUnit(this,this._image_map.window_size.w,this._image_map.window_size.h,i_raster_type,i_depth);
		}else{
			this._labeling=new FinalLabelingUnit(this,this._image_map.window_size.w,this._image_map.window_size.h,i_raster_type,i_depth);
		}
		this._labeling.setAreaRange(320*240,10*10);
	}
	public void detectOutline(NyARGrayscaleRaster i_raster,int i_th) throws NyARException
	{
		this._labeling.labeling(i_raster,this._image_map.top_image,i_th);
	}
	protected abstract void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_parcial_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException;
}