package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;




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
		double dist=o_posvec.dx*o_posvec.dx+o_posvec.dy*o_posvec.dy;
		if(dist>0){
			o_posvec.dx/=dist;
			o_posvec.dy/=dist;
		}
		return;
	}	
}


/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public class ContourTargetSrcHolder extends NyARObjectPool<ContourTargetSrcHolder.ContourTargetSrcItem>
{	
	public static class ContourTargetSrcItem
	{
		public AreaTargetSrcHolder.AreaSrcItem _ref_area_src;
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public VectorPos[] vecpos=new VectorPos[30];
		public int vecpos_length;
		/*部分矩形の左上インデクス*/
		public NyARIntPoint2d image_lt=new NyARIntPoint2d();
		/**
		 * ベクトル配列を点集合に変換して返します。
		 * @param o_vertex
		 */
		public void getVertex(NyARIntPoint2d o_vertex)
		{
			//ベクトルに直行する2つのベクトルを90度傾けての交点を計算
			for(int i=0;i<vecpos_length-1;i++){
				
			}
		}
	}
	private final NyARContourPickup _cpickup=new NyARContourPickup();

	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public ContourTargetSrcHolder(int i_size,int i_cood_max) throws NyARException
	{
		this._coord=NyARIntPoint2d.createArray(i_cood_max);
		super.initInstance(i_size,ContourTargetSrcItem.class);
	}
	protected ContourTargetSrcItem createElement(Object i_param)
	{
		return new ContourTargetSrcItem();
	}
	private NyARIntPoint2d[] _coord;
	private PixelVectorReader _vec_reader;
		
	public ContourTargetSrcItem newSrcTarget(AreaTargetSrcHolder.AreaSrcItem i_item,HierarchyRect i_hrect,NyARGrayscaleRaster i_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		NyARIntPoint2d[] coord=this._coord;
		ContourTargetSrcItem item=this.newObject();
		if(item==null){
			return null;
		}
		PixelVectorReader vec_reader=this._vec_reader;
		item._ref_area_src=i_item;
		item.image_lt.x=i_hrect.x;
		item.image_lt.y=i_hrect.y;

		//輪郭抽出
		int coord_len=this._cpickup.getContour(i_raster,i_th,info.entry_x,info.clip_t,coord);
		if(coord_len==coord.length){
			//輪郭線MAXならなにもできないね。
			this.deleteObject(item);
			return null;
		}
		NyARIntRect tmprect=new NyARIntRect();
		//輪郭→ベクトルの変換
		

		//ベクトル化
		int skip=i_hrect.dot_skip;
		tmprect.w=tmprect.h=skip*2;


		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=i_hrect.x+(coord[0].x-1)*skip;
		tmprect.y=i_hrect.y+(coord[0].y-1)*skip;
		vec_reader.getAreaVector8(tmprect,item.vecpos[0]);

		int ccx=0;
		int ccy=0;
		VectorPos prev_vec_ptr    = null;
		VectorPos current_vec_ptr = item.vecpos[0];

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		for(int i=1;i<coord_len;i++){
			//ベクトル定義矩形を作る。
			tmprect.x=i_hrect.x+(coord[i].x-1)*skip;
			tmprect.y=i_hrect.y+(coord[i].y-1)*skip;
			//ベクトル取得
			prev_vec_ptr=current_vec_ptr;
			current_vec_ptr=item.vecpos[number_of_data];
			vec_reader.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			if(getVecCos(prev_vec_ptr,current_vec_ptr)<0.99){
				//相関なし
				number_of_data++;
			}else{
				//相関あり
				prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
				prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
				prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
				prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			}
			//輪郭中心を出すための計算
			ccx+=this._coord[i].x;
			ccy+=this._coord[i].y;
//輪郭ベクトルバッファの最大を超えたら失敗
		}
		//ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		prev_vec_ptr=item.vecpos[0];
		if(getVecCos(current_vec_ptr,prev_vec_ptr)<0.99){
			//相関なし
		}else{
			//相関あり
			prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
			prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
			prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
			prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			number_of_data--;
		}
		//輪郭中心位置の保存
		item.coord_center.x=ccx/coord_len;
		item.coord_center.y=ccy/coord_len;
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
}