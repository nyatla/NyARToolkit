package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;


public class NyARDetailTrackSrcTable extends NyARObjectStack<NyARDetailTrackSrcTable.Item>
{	
	public class Item
	{
		public NyAROutlineTrackSrcTable.Item ref_outline;
		public NyARLinear[] line=NyARLinear.createArray(4);
		
		public NyARIntPoint2d center=new NyARIntPoint2d();
		
	}	
	private NyARCoord2Linear _coordline;
	public boolean push(NyAROutlineTrackSrcTable.Item i_ref_outline,NyARIntPoint2d[] i_coord,int i_coord_num,int[] i_vertex_index) throws NyARException
	{
		NyARDetailTrackSrcTable.Item item=this.prePush();
		if(item==null){
			return false;
		}
		item.ref_outline=i_ref_outline;
		for(int i=0;i<4;i++){			
			this._coordline.coord2Line(i_vertex_index[i],i_vertex_index[(i+3)%4],i_coord, i_coord_num,item.line[i]);
		}
		return true;
	}
	protected NyARDetailTrackSrcTable.Item createElement()
	{
		return new NyARDetailTrackSrcTable.Item();
	}
	
	public NyARDetailTrackSrcTable(int i_length) throws NyARException
	{
		super(i_length,NyARDetailTrackSrcTable.Item.class);
		return;
	}

}