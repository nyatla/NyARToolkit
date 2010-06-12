package jp.nyatla.nyartoolkit.dev.tracking.detail.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.NyARDetailTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;

public class NyARDetailLabelingTrackSrcTable extends NyARObjectStack<NyARDetailLabelingTrackSrcTable.Item>
{	
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARIntPoint2d ideal_center=new NyARIntPoint2d();
		public NyARLinear[] ideal_line=NyARLinear.createArray(4);	
		public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);	
	}
	private NyARCoord2Linear _coordline;
	public NyARDetailLabelingTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref) throws NyARException
	{
		super(i_length,NyARDetailLabelingTrackSrcTable.Item.class);
		this._gs=new NyARGrayscaleRaster(i_screen_size.w,i_screen_size.h);
		this._coordline=new NyARCoord2Linear(i_screen_size,i_distfactor_ref);
	}
	public void wrapVertex(NyARDoublePoint2d i_vertex[],int i_num_of_vertex,NyARIntRect o_rect)
	{
		//エリアを求める。
		int xmax,xmin,ymax,ymin;
		xmin=xmax=(int)i_vertex[i_num_of_vertex-1].x;
		ymin=ymax=(int)i_vertex[i_num_of_vertex-1].y;
		for(int i=i_num_of_vertex-2;i>=0;i--){
			if(i_vertex[i].x<xmin){
				xmin=(int)i_vertex[i].x;
			}else if(i_vertex[i].x>xmax){
				xmax=(int)i_vertex[i].x;
			}
			if(i_vertex[i].y<ymin){
				ymin=(int)i_vertex[i].y;
			}else if(i_vertex[i].y>ymax){
				ymax=(int)i_vertex[i].y;
			}
		}
		
		if(xmax>320){
			xmax=320;
		}
		if(xmin<0){
			xmin=0;
		}
		if(ymax>240){
			ymax=240;
		}
		if(ymin<0){
			ymin=0;
		}
		o_rect.h=ymax-ymin;
		o_rect.x=xmin;
		o_rect.w=xmax-xmin;
		o_rect.y=ymin;
	}
	private NyARGrayscaleRaster _gs;
	public boolean update(NyARDetailTrackItem i_item,INyARRgbRaster i_src) throws NyARException
	{
		
		NyARGrayscaleRaster gs=this._gs;
//		NyARLabeling_Rle labeling;
		NyARIntRect rect=new NyARIntRect();
		NyARRasterFilter_Rgb2Gs_AveAdd ave=new NyARRasterFilter_Rgb2Gs_AveAdd(i_src.getBufferType(),NyARBufferType.INT1D_GRAY_8);
		int th;

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
		
		ave.doFilter(i_src, gs,rect.x,rect.y,rect.w,rect.h);
		//2.探索エリアの環境取得
		//3.探索
		/*
		NyARDetailLabelingTrackSrcTable.Item item=this.prePush();
		if(item==null){
			return false;
		}
		item.ref_outline=i_ref_outline;
		for(int i=0;i<4;i++){			
			this._coordline.coord2Line(i_vertex_index[i],i_vertex_index[(i+1)%4],i_coord, i_coord_num,item.ideal_line[i]);
		}
		double cx,cy;
		cx=cy=0;
		for (int i = 0; i < 4; i++) {
			//直線同士の交点計算
			NyARDoublePoint2d v_ptr=item.ideal_vertex[i];
			if(!NyARLinear.crossPos(item.ideal_line[i],item.ideal_line[(i + 3) % 4],v_ptr)){
				throw new NyARException();//普通失敗しない。
			}
			cx+=v_ptr.x;
			cy+=v_ptr.y;
		}
		//中心位置計算
		item.ideal_center.x=(int)(cx/4);
		item.ideal_center.y=(int)(cy/4);*/
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