package jp.nyatla.nyartoolkit.dev.tracking.detail;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;


public class NyARDetailTrackSrcTable extends NyARObjectStack<NyARDetailTrackSrcTable.Item>
{	
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARIntPoint2d ideal_center=new NyARIntPoint2d();
		public NyARLinear[] ideal_line=NyARLinear.createArray(4);	
		public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);	
	}
	private NyARCoord2Linear _coordline;
	public NyARDetailTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref) throws NyARException
	{
		super(i_length,NyARDetailTrackSrcTable.Item.class);
		this._coordline=new NyARCoord2Linear(i_screen_size,i_distfactor_ref);
	}
	public boolean push(NyAROutlineTrackSrcTable.Item i_ref_outline,NyARIntPoint2d[] i_coord,int i_coord_num,int[] i_vertex_index) throws NyARException
	{
		NyARDetailTrackSrcTable.Item item=this.prePush();
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
		item.ideal_center.y=(int)(cy/4);
		return true;
	}
	protected NyARDetailTrackSrcTable.Item createElement()
	{
		return new NyARDetailTrackSrcTable.Item();
	}

}