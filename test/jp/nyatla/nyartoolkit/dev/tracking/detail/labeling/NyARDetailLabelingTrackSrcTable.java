package jp.nyatla.nyartoolkit.dev.tracking.detail.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.NyARDetailTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histogram;

public class NyARDetailLabelingTrackSrcTable extends NyARObjectStack<NyARDetailLabelingTrackSrcTable.Item>
{	
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARIntPoint2d ideal_center=new NyARIntPoint2d();
		public NyARLinear[] ideal_line=NyARLinear.createArray(4);	
		public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);	
	}

	private class SquareDetector extends NyARSquareContourDetector_Rle
	{
		public NyARDetailLabelingTrackSrcTable _parent;
		public SquareDetector(NyARIntSize i_size,NyARDetailLabelingTrackSrcTable i_parent) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		private NyARLinear[] _linear=NyARLinear.createArray(4);
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARIntPoint2d[] i_coord,int i_coorx_num,int[] i_vertex_index) throws NyARException
		{
			NyARIntPoint2d[] vertex=this.__ref_vertex;
			NyARDetailLabelingTrackSrcTable.Item item=this._parent.prePush();
			if(item==null){
				return;
			}
			//大きさが大体同じか確認するよ。1/2～2倍・・・かなぁ
			
			//線分検出。とりあえず。
			vertex[0]=i_coord[i_vertex_index[0]];
			vertex[1]=i_coord[i_vertex_index[1]];
			vertex[2]=i_coord[i_vertex_index[2]];
			vertex[3]=i_coord[i_vertex_index[3]];
		
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
			//中心位置計算
			item.ideal_center.x=(int)(cx/4);
			item.ideal_center.y=(int)(cy/4);
			return;
		}		
	}	
	private SquareDetector _sqdetect;
	private NyARRasterFilter_Rgb2Gs_AveAdd _rgb2gs;
	private NyARGrayscaleRaster _gs;
	private NyARRasterAnalyzer_Histogram _histogram_analyzer;
	private NyARHistogram _histogram;
	private NyARHistogramAnalyzer_SlidePTile _threshold_detector;
	private NyARCoord2Linear _coordline;
	public NyARDetailLabelingTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref,int i_raster_type) throws NyARException
	{
		super(i_length,NyARDetailLabelingTrackSrcTable.Item.class);

		this._sqdetect=new SquareDetector(i_screen_size,this);
		//上位と排他的にシェアしてね。
		this._coordline=new NyARCoord2Linear(i_screen_size,i_distfactor_ref);
		this._rgb2gs=new NyARRasterFilter_Rgb2Gs_AveAdd(i_raster_type);
		this._gs=new NyARGrayscaleRaster(i_screen_size.w,i_screen_size.h);
		this._histogram_analyzer=new NyARRasterAnalyzer_Histogram(this._gs.getBufferType(),4);
		this._histogram=new NyARHistogram(256);
		this._threshold_detector=new NyARHistogramAnalyzer_SlidePTile(20);
	}
		
	public boolean update(NyARDetailTrackItem i_item,INyARRgbRaster i_src) throws NyARException
	{
		
		NyARGrayscaleRaster gs=this._gs;
		NyARIntRect rect=new NyARIntRect();
		NyARRasterFilter_Rgb2Gs_AveAdd ave=this._rgb2gs;

		//頂点集合を包むRECTを計算
		rect.wrapVertex(i_item.estimate.ideal_vertex,4);
		
		//エリアを1.5倍
		int w=rect.w/2;
		int h=rect.h/2;
		rect.x-=w/2;
		rect.y-=h/2;
		rect.w+=w;
		rect.h+=h;
		//境界値制限
		rect.clip(0,0,320,240);
		//領域指定のGS化
		ave.doFilter(i_src, gs,rect.x,rect.y,rect.w,rect.h);
		//領域指定のヒストグラム抽出
		this._histogram_analyzer.analyzeRaster(gs,rect,this._histogram);
		int th=this._threshold_detector.getThreshold(this._histogram);
		//領域指定の矩形検出
		this._sqdetect.detectMarker(gs,rect,th);
		
		
		return true;
	}
	protected NyARDetailLabelingTrackSrcTable.Item createElement()
	{
		return new NyARDetailLabelingTrackSrcTable.Item();
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._gs;
		return ret;
	}
}