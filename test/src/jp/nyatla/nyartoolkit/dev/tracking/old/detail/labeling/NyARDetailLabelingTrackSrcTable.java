package jp.nyatla.nyartoolkit.dev.tracking.old.detail.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.fixedthreshold.NyARDetailFixedThresholdTrackSrcTable;
import jp.nyatla.nyartoolkit.dev.tracking.old.outline.NyAROutlineTrackSrcTable;
import jp.nyatla.nyartoolkit.sandbox.x2.NyMath;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_DiscriminantThreshold;
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histogram;


public class NyARDetailLabelingTrackSrcTable extends NyARObjectStack<NyARDetailLabelingTrackSrcTable.Item>
{
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARIntPoint2d center=new NyARIntPoint2d();
		public NyARLinear[] ideal_line=NyARLinear.createArray(4);	
		public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);
		/**
		 * この要素の矩形エリア
		 */
		public NyARIntRect rect_area=new NyARIntRect();
	}
	
	private class SquareDetector extends NyARSquareContourDetector_Rle
	{
		public NyARDetailLabelingTrackSrcTable _parent;
		private NyARDetailLabelingTrackItem _handler_param;
		public SquareDetector(NyARIntSize i_size,NyARDetailLabelingTrackSrcTable i_parent) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		public void detectMarker(NyARGrayscaleRaster i_raster,NyARIntRect i_area,int i_th,NyARDetailLabelingTrackItem i_param) throws NyARException
		{
			this._handler_param=i_param;
			super.detectMarker(i_raster,i_area,i_th);
		}
		public void detectMarker(NyARBinRaster i_bin_raster,NyARDetailLabelingTrackItem i_param) throws NyARException
		{
			this._handler_param=i_param;
			super.detectMarker(i_bin_raster);
		}
		
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		protected void onSquareDetect(NyARIntPoint2d[] i_coord,int i_coorx_num,int[] i_vertex_index) throws NyARException
		{
			NyARDetailLabelingTrackItem target=(NyARDetailLabelingTrackItem)this._handler_param;
			NyARIntPoint2d[] vertex=this.__ref_vertex;
			
			
			
			NyARDetailLabelingTrackSrcTable.Item item=this._parent.prePush();
			if(item==null){
				System.out.println("Drop stack full");
				this._parent.pop();
				return;
			}
			
			//線分を配列に配置
			vertex[0]=i_coord[i_vertex_index[0]];
			vertex[1]=i_coord[i_vertex_index[1]];
			vertex[2]=i_coord[i_vertex_index[2]];
			vertex[3]=i_coord[i_vertex_index[3]];

			//頂点を包括するRECTを計算
			item.rect_area.setAreaRect(vertex,4);
			//頂点の散らばる範囲を計算して、概ね70～130%の範囲のみ、対象とする。
			int ratio;
			ratio=target.rect_area.w*10/item.rect_area.w;
			if(ratio<7 || 13<ratio){
				this._parent.pop();
				return;
			}
			ratio=target.rect_area.h*10/item.rect_area.h;
			if(ratio<7 || 13<ratio){
				this._parent.pop();
				return;
			}
			//中心位置を計算する
			item.center.setCenterPos(vertex,4);
			
			//既に登録済のRECTで、近い物が無いかを探す。(占有エリアがほぼ同じことが保証されているので、距離が8dot以内の場合、同一とみなす
			for(int i=this._parent.getLength()-2;i>=0;i--){
				if(NyARMath.sqNorm(this._parent.getItem(i).center,item.center)<64){
					this._parent.pop();
					return;
				}
			}
			//
			for(int i=0;i<4;i++){
				this._parent._coordline.coord2Line(i_vertex_index[i],i_vertex_index[(i+1)%4],i_coord,i_coorx_num,item.ideal_line[i]);
			}
			double cx,cy;
			cx=cy=0;
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				NyARDoublePoint2d v_ptr=item.ideal_vertex[i];
				if(!NyARLinear.crossPos(item.ideal_line[i],item.ideal_line[(i + 3) % 4],v_ptr)){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
				cx+=v_ptr.x;
				cy+=v_ptr.y;
			}
			item.center.x=(int)cx/4;
			item.center.y=(int)cy/4;
//this._ref_distfactor.ideal2Observ(vertex[i2], vertex[i2]);			
			return;
		}		
	}	
	private SquareDetector _sqdetect;
	private NyARRasterFilter_Rgb2Gs_RgbAve _rgb2gs;
	private NyARGrayscaleRaster _gs;
	private NyARRasterAnalyzer_Histogram _histogram_analyzer;
	private NyARHistogram _histogram;
	private NyARHistogramAnalyzer_DiscriminantThreshold _threshold_detector;
	private NyARCoord2Linear _coordline;


	protected NyARDetailLabelingTrackSrcTable.Item createElement()
	{
		return new NyARDetailLabelingTrackSrcTable.Item();
	}	
	public NyARDetailLabelingTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref,int i_raster_type) throws NyARException
	{
		super(i_length,NyARDetailLabelingTrackSrcTable.Item.class);

		this._sqdetect=new SquareDetector(i_screen_size,this);
		//上位と排他的にシェアしてね。
		this._coordline=new NyARCoord2Linear(i_screen_size,i_distfactor_ref);
		this._rgb2gs=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster_type);
		this._gs=new NyARGrayscaleRaster(i_screen_size.w,i_screen_size.h);
		this._histogram_analyzer=new NyARRasterAnalyzer_Histogram(this._gs.getBufferType(),4);
		this._histogram=new NyARHistogram(256);
		this._threshold_detector=new NyARHistogramAnalyzer_DiscriminantThreshold();
	}
	public void initDetectionBatch()
	{
	}
	public boolean update(NyARDetailLabelingTrackItem[] i_items,int i_number_of_item,INyARRgbRaster i_src) throws NyARException
	{
		
		NyARGrayscaleRaster gs=this._gs;
		NyARRasterFilter_Rgb2Gs_RgbAve ave=this._rgb2gs;

		for(int i=0;i<i_number_of_item;i++)
		{
			NyARIntRect rect=i_items[i].estimate.search_area;
			//領域指定のGS化
			ave.doFilter(i_src, rect,gs);
			//領域指定のヒストグラム抽出
			int skip=1;//rect.w*rect.h/4096;
			this._histogram_analyzer.setVerticalInterval(skip<1?1:skip);
			this._histogram_analyzer.analyzeRaster(gs,rect,this._histogram);
			int th=this._threshold_detector.getThreshold(this._histogram);
			//領域指定の矩形検出(最低領域サイズより小さかったらやんない)
			if(rect.w*rect.h<64){
				continue;
			}
			this._sqdetect.detectMarker(gs,rect,th,i_items[i]);
		}
		
		
		return true;
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._gs;
		return ret;
	}
}