package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc.AppearSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8.NyARDoublePosVec2d;
class VectorPos
{
	double x;
	double y;
	double dx;
	double dy;
	public static VectorPos[] createArray(int i_length)
	{
		VectorPos[] r=new VectorPos[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new VectorPos();
		}
		return r;
	}
}
class PixelVectorReader
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public PixelVectorReader(INyARRaster i_ref_raster)
	{
		assert(i_ref_raster.getBufferType()==NyARBufferType.INT1D_GRAY_8);
		this._ref_buf=(int[])(i_ref_raster.getBuffer());
		this._ref_size=i_ref_raster.getSize();
	}
	/**
	 * 領域を指定した8近傍ベクトル
	 * @param i_gs
	 * @param i_area
	 * @param i_pos
	 * @param i_vec
	 */
	public void getAreaVector8(NyARIntRect i_area,VectorPos o_posvec)
	{
		int[] buf=this._ref_buf;
		int stride=this._ref_size.w;
		//x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		//x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x,sum_y,sum_wx,sum_wy,sum_vx,sum_vy;
		sum_x=sum_y=sum_wx=sum_wy=sum_vx=sum_vy=0;
		int vx,vy;
//クリッピングできるよね
		for(int i=i_area.h-1;i>=0;i--){
			for(int i2=i_area.w-1;i2>=0;i2--){
				//1ビット分のベクトルを計算
				int idx_0 =stride*(i+i_area.y)+(i2+i_area.x);
				int idx_p1=idx_0+stride;
				int idx_m1=idx_0-stride;
				int b=buf[idx_m1-1];
				int d=buf[idx_m1+1];
				int h=buf[idx_p1-1];
				int f=buf[idx_p1+1];
				vx=((buf[idx_0+1]-buf[idx_0-1])>>1)+((d-b+f-h)>>2);
				vy=((buf[idx_p1]-buf[idx_m1])>>1)+((f-d+h-b)>>2);	

				//加重はvectorの絶対値
				int wx=vx*vx;
				int wy=vy*vy;
				sum_wx+=wx;
				sum_wy+=wy;
				sum_vx+=wx*vx;
				sum_vy+=wy*vy;
				sum_x+=wx*(i2+i_area.x);
				sum_y+=wy*(i+i_area.y);
			}
		}
		//加重平均(posが0の場合の位置は中心)
		if(sum_x==0){
			o_posvec.x=i_area.x+(i_area.w>>1);
			o_posvec.dx=0;
		}else{
			o_posvec.x=(double)sum_x/sum_wx;			
			o_posvec.dx=(double)sum_vx/sum_wx;
		}
		if(sum_y==0){
			o_posvec.y=i_area.y+(i_area.h>>1);
			o_posvec.dy=0;
		}else{
			o_posvec.y=(double)sum_y/sum_wy;			
			o_posvec.dy=(double)sum_vy/sum_wy;
		}
		return;
	}	
	
	
}


/**
 * 輪郭ソースから、輪郭を得るんだけど・・・・・。輪郭の特徴って何だ？
 * 
 * @author nyatla
 *
 */
public class ContourTargetList extends NyARObjectStack<ContourTargetList.ContourTargetItem>
{
	//ベクタの類似度敷居値
	private static double VEC_THRESHOLD=0.99;


	public static class ContourTargetItem extends TrackTarget
	{
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		public int area_sq_diagonal;
		public VectorPos[] vecpos=new VectorPos[30];
		//
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public boolean isMatchContoure(ContourTargetSrc.ContourTargetSrcItem i_item)
		{
			//輪郭中心地の距離2乗
			int d2=NyARMath.sqNorm(i_item.coord_center,coord_center);
			int max_dist=i_item.area_sq_diagonal/100;//(10%)の2乗
			//輪郭線の中央位置を比較して、一定の範囲内であれば、同じ輪郭線であるとする。
			if(d2>max_dist){
				return false;//範囲外
			}
			return true;
		}
	}
	public ContourTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,ContourTargetItem.class);
	}
	protected ContourTargetItem createElement() throws NyARException
	{
		return new ContourTargetItem();
	}	
	public ContourTargetItem pushTarget(NewTargetItem i_item)
	{
		ContourTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		item.area_sq_diagonal=i_item.area_sq_diagonal;
		return item;
	}
	private double getVecCos(VectorPos i_v1,VectorPos i_v2)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=i_v2.dx;
		double y2=i_v2.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
		
	}
	public void updateTarget(PixelVectorReader i_vec_reader,int i_index,long i_tick,ContourTargetSrc.ContourTargetSrcItem i_src)
	{
		ContourTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.area.x=i_src.area.x;
		item.area.y=i_src.area.y;
		item.area.w=i_src.area.w;
		item.area.h=i_src.area.h;
		item.area_center.x=i_src.area_center.x;
		item.area_center.y=i_src.area_center.y;
		item.coord_center.x=i_src.coord_center.x;
		item.coord_center.y=i_src.coord_center.y;
		
		//
		int n;
		NyARIntRect tmprect=new NyARIntRect();
		//輪郭→ベクトルの変換
		

		//輪郭線を出す
//		n=this._cpickup.getContour(i_raster,i_th,info.entry_x,info.clip_t,this._coord);
		//元画像からベクトルを拾う。
		int skip=i_src.skip;
		tmprect.w=skip*2;
		tmprect.h=skip*2;

		//ベクトル配列を作る

		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=i_src.image_lt.x+(i_src.coord[0].x-1)*skip;
		tmprect.y=i_src.image_lt.y+(i_src.coord[0].y-1)*skip;
		i_vec_reader.getAreaVector8(tmprect,item.vecpos[0]);

		VectorPos prev_vec_ptr    = null;
		VectorPos current_vec_ptr = item.vecpos[0];
		
		//ベクトルデータを作成
		for(int i=1;i<n;i++){
//ベクトル定義矩形を作る。
			tmprect.x=i_src.image_lt.x+(i_src.coord[i].x-1)*skip;
			tmprect.y=i_src.image_lt.y+(i_src.coord[i].y-1)*skip;
//矩形の位置をずらさないとね
//クリップ位置の補正
			//ベクトル取得
			VectorPos prev_vec_ptr=item.vecpos[number_of_data];
			VectorPos current_vec_ptr=item.vecpos[number_of_data];
			i_vec_reader.getAreaVector8(tmprect,vec_ptr);
			
			//類似度判定
			if(getVecCos(item.vecpos[number_of_data-1],vec_ptr)<0.99){
				//相関なし
				number_of_data++;
			}else{
				//相関あり
				pva[number_of_data-1].x=(pva[number_of_data-1].x+pva[number_of_data].x)/2;
				pva[number_of_data-1].y=(pva[number_of_data-1].y+pva[number_of_data].y)/2;
				pva[number_of_data-1].dx=(pva[number_of_data-1].dx+pva[number_of_data].dx);
				pva[number_of_data-1].dy=(pva[number_of_data-1].dy+pva[number_of_data].dy);
			}
		}
		
		
		
		return;
	}	
	public int getMatchTargetIndex(ContourTargetSrc.ContourTargetSrcItem i_item)
	{
		for(int i=this._length-1;i>=0;i--)
		{
			if(this._items[i].isMatchContoure(i_item)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 一致する矩形を検索する。一致する矩形の判定は、Areaの重なり具合
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AppearTargetSrc.AppearSrcItem i_item)
	{
		ContourTargetItem iitem;
		//許容距離誤差の2乗を計算(50%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/2)^2
		int dist_rate2=(i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h)/4;

		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i];
			//大きさチェック(50%誤差)
			double ratio;
			ratio=((double)iitem.area.w)/i_item.area.w;
			if(ratio<0.25 || 2.25<ratio){
				continue;
			}
			//距離チェック
			int d=NyARMath.sqNorm(i_item.area_center,iitem.area_center);
			if(d>dist_rate2)
			{
				continue;
			}
			//多分同じ対象物
			return i;
		}
		return -1;
	}

}
