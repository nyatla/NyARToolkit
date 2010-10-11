/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatusPool;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatus.CoordData;

public class NyARVectorReader_INT1D_GRAY_8
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	private NyARIntRect _clip_rect=new NyARIntRect();
	public NyARVectorReader_INT1D_GRAY_8(LrlsGsRaster i_ref_raster)
	{
		assert(i_ref_raster.getBufferType()==NyARBufferType.INT1D_GRAY_8);
		this._ref_buf=(int[])(i_ref_raster.getBuffer());
		this._ref_size=i_ref_raster.getSize();
		this._clip_rect.x=this._clip_rect.y=1;
		this._clip_rect.w=this._ref_size.w-2;
		this._clip_rect.h=this._ref_size.h-2;
	}
	/**
	 * 画素の4近傍の画素ベクトルを取得します。
	 * 取得可能な範囲は、Rasterの1ドット内側です。
	 * 0 ,-1, 0    0, 0, 0
	 * 0 , x, 0　+ -1, y,+1  
	 * 0 ,+1, 0    0, 0, 0
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
	public void getPixelVector4(int x,int y,NyARIntPoint2d o_v)
	{
		assert((x>0) && (y>0) && (x)<this._ref_size.w && (y)<this._ref_size.h);
		int[] buf=this._ref_buf;
		int w=this._ref_size.w;
		int idx=w*y+x;
		o_v.x=(buf[idx+1]-buf[idx-1])>>1;
		o_v.y=(buf[idx+w]-buf[idx-w])>>1;
	}
	/**
	 * 画素の8近傍画素ベクトルを取得します。
	 * 取得可能な範囲は、Rasterの1ドット内側です。
	 * -1,-2,-1    -1, 0,+1
	 *  0, y, 0  + -2, x,+2
	 * +1,+2,+1    -1, 0,+1
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
	public void getPixelVector8(int x,int y,NyARIntPoint2d o_v)
	{
		assert((x>0) && (y>0) && (x)<this._ref_size.w && (y)<this._ref_size.h);
		int[] buf=this._ref_buf;
		NyARIntSize s=this._ref_size;
		int idx_0 =s.w*y+x;
		int idx_p1=idx_0+s.w;
		int idx_m1=idx_0-s.w;
		int b=buf[idx_m1-1];
		int d=buf[idx_m1+1];
		int h=buf[idx_p1-1];
		int f=buf[idx_p1+1];
		o_v.x=((buf[idx_0+1]-buf[idx_0-1])>>1)+((d-b+f-h)>>2);
		o_v.y=((buf[idx_p1]-buf[idx_m1])>>1)+((f-d+h-b)>>2);
	}
	/**
	 * RECT範囲内の画素ベクトルの合計値と、ベクトルのエッジ中心を取得します。
	 * @param i_gs
	 * @param i_area
	 * ピクセル取得を行う範囲を設定します。i_area.w,hは、それぞれ3以上の数値である必要があります。
	 * 
	 * 320*240の場合、RECTの範囲は(x>0 && x<319 x+w>0 && x+w<319),(y>0 && y<239 x+w>0 && x+w<319)となります。
	 * @param i_pos
	 * @param i_vec
	 */
	public void getAreaVector8(NyARIntRect i_area,NyARPointVector2d o_posvec)
	{
		assert(i_area.h>=3 && i_area.w>=3);
		assert((i_area.x>=0) && (i_area.y>=0) && (i_area.x+i_area.w)<=this._ref_size.w && (i_area.y+i_area.h)<=this._ref_size.h);
		int[] buf=this._ref_buf;
		int stride=this._ref_size.w;
		//x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		//x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x,sum_y,sum_wx,sum_wy,sum_vx,sum_vy;
		sum_x=sum_y=sum_wx=sum_wy=sum_vx=sum_vy=0;
		int vx,vy;
		for(int i=i_area.h-3;i>=0;i--){
			for(int i2=i_area.w-3;i2>=0;i2--){
				//1ビット分のベクトルを計算
				int idx_0 =stride*(i+1+i_area.y)+(i2+1+i_area.x);
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
				sum_x+=wx*(i2+1+i_area.x);
				sum_y+=wy*(i+1+i_area.y);
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
	/**
	 * 参照している画素バッファを、i_ref_bufferに切り替えます。この関数は、コンストラクタでセットしたラスタ
	 * のバッファが切り替わった時に呼び出してください。
	 * @param i_ref_buffer
	 * @throws NyARException
	 */
	public void switchBuffer(Object i_ref_buffer) throws NyARException
	{
		assert(((int[])i_ref_buffer).length>=this._ref_size.w*this._ref_size.h);
		this._ref_buf=(int[])i_ref_buffer;
	}
	/**
	 * ワーク変数
	 */
	private NyARIntPoint2d[] _coord_buf=NyARIntPoint2d.createArray((160+120)*2);
	public final NyARContourPickup _cpickup=new NyARContourPickup();
	private final double _ANG_TH=0.99;
	
	public boolean traceConture(LrlsGsRaster i_rob_raster,int i_th,NyARIntPoint2d i_entry,NyARContourTargetStatus.VectorCoords o_coord) throws NyARException
	{
		NyARIntPoint2d[] coord=this._coord_buf;
		//Robertsラスタから輪郭抽出
		int coord_len=this._cpickup.getContour(i_rob_raster,i_th,i_entry.x,i_entry.y,coord);
		if(coord_len==coord.length){
			//輪郭線MAXならなにもできないね。
			return false;
		}
		NyARIntRect tmprect=new NyARIntRect();
		//ベクトル化
		NyARContourTargetStatus.CoordData[] array_of_vec=o_coord.item;
		int MAX_COORD=o_coord.item.length;
		int skip=i_rob_raster.resolution;
		//検出RECTのサイズは、1ドット内側になる。
		tmprect.w=tmprect.h=skip*2;
		
		NyARContourTargetStatus.CoordData prev_vec_ptr,current_vec_ptr,tmp_ptr;		
		CoordData[] tmp_cd=CoordData.createArray(3);
		current_vec_ptr=tmp_cd[0];


		int number_of_data=1;
		int sum=1;
		//0個目のベクトル
		tmprect.x=coord[0].x*skip;
		tmprect.y=coord[0].y*skip;
		this.getAreaVector8(tmprect,current_vec_ptr);
		array_of_vec[0].setValue(current_vec_ptr);
		//[2]に0番目のバックアップを取る。
		tmp_cd[2].setValue(current_vec_ptr);
		

//		int ccx=0;
//		int ccy=0;

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		int cdx=1;
		for(int i=1;i<coord_len;i++){
			prev_vec_ptr=current_vec_ptr;
			current_vec_ptr=tmp_cd[cdx%2];
			cdx++;

			//ベクトル定義矩形を作る。
			tmprect.x=coord[i].x*skip;
			tmprect.y=coord[i].y*skip;

			//ベクトル取得
			this.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			tmp_ptr=array_of_vec[number_of_data-1];
			if(NyARPointVector2d.getVecCos(prev_vec_ptr,current_vec_ptr)<_ANG_TH){
				//相関なし
				//前回までのベクトル和の調整
				tmp_ptr.x/=sum;
				tmp_ptr.y/=sum;
				//新しいベクトル値として保管
				array_of_vec[number_of_data].setValue(current_vec_ptr);
				//確定したので個数を+1
				number_of_data++;
				sum=1;
			}else{
				//相関あり(ベクトルの統合)
				tmp_ptr.x+=current_vec_ptr.x;
				tmp_ptr.y+=current_vec_ptr.y;
				tmp_ptr.dx+=current_vec_ptr.dx;
				tmp_ptr.dy+=current_vec_ptr.dy;
				sum++;
			}
//			//輪郭中心を出すための計算
//			ccx+=this._coord[i].x;
//			ccy+=this._coord[i].y;
			if(number_of_data==MAX_COORD){
				//輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		
		//ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
//		prev_vec_ptr=tmp_cd[2];
		//current_vec_ptrには最後のベクトルが入ってる。temp_cdには開始のベクトルが入ってる。
		if(NyARPointVector2d.getVecCos(current_vec_ptr,tmp_cd[2])<_ANG_TH){
			//相関なし->最後尾のクローズのみ
			tmp_ptr=array_of_vec[number_of_data-1];
			tmp_ptr.x/=sum;
			tmp_ptr.y/=sum;
		}else{
			//相関あり(ベクトルの統合)
			sum++;
			prev_vec_ptr=array_of_vec[0];
			current_vec_ptr=array_of_vec[number_of_data-1];
			prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/sum;
			prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/sum;
			prev_vec_ptr.dx=prev_vec_ptr.dx+current_vec_ptr.dx;
			prev_vec_ptr.dy=prev_vec_ptr.dy+current_vec_ptr.dy;
			number_of_data--;
		}
		//輪郭中心位置の保存
//		item.coord_center.x=ccx/coord_len;
//		item.coord_center.y=ccy/coord_len;
		//vectorのsq_distを必要なだけ計算
		double d=0;
		for(int i=number_of_data-1;i>=0;i--)
		{
			current_vec_ptr=array_of_vec[i];
			//ベクトルの法線を取る。
			current_vec_ptr.OrthogonalVec(current_vec_ptr);
			//sqdistを計算
			current_vec_ptr.sq_dist=current_vec_ptr.dx*current_vec_ptr.dx+current_vec_ptr.dy*current_vec_ptr.dy;
			d+=current_vec_ptr.sq_dist;
		}
		//sq_distの合計を計算
//		item.sq_dist_sum=d;
		o_coord.length=number_of_data;
		return true;
	}	
	
	
	/**
	 * 2点間に線を仮定して、付近の線分のベクトルを計算します。
	 * @param i_p1
	 * @param i_p2
	 * @param o_coord
	 * 輪郭ベクトルの出力バッファです。
	 * @param i_number_of_sample
	 * サンプリング数です。o_coordの要素数以下の値を指定してください。
	 */
	public void traceLineVector(NyARDoublePoint2d i_p1,NyARDoublePoint2d i_p2,int i_cellsize,NyARContourTargetStatus.VectorCoords o_coord,int i_number_of_sample)
	{
		assert(i_number_of_sample>1);
		//ベクトル化
		NyARContourTargetStatus.CoordData prev_vec_ptr,current_vec_ptr,tmp_ptr;		
		NyARContourTargetStatus.CoordData[] array_of_vec=o_coord.item;
		NyARIntRect tmprect=new NyARIntRect();
		tmprect.w=tmprect.h=i_cellsize;
		int half_cell_size=i_cellsize/2;
		
		CoordData[] tmp_cd=CoordData.createArray(3);
		current_vec_ptr=tmp_cd[0];
		int bottom=this._ref_size.h-2;
		int right=this._ref_size.w-2;


		int number_of_data=1;
		int sum=1;
		//0個目のベクトル
		tmprect.x=(int)(i_p1.x-half_cell_size);
		tmprect.y=(int)(i_p1.y-half_cell_size);
		tmprect.clip(1,1,right,bottom);
		this.getAreaVector8(tmprect,current_vec_ptr);
		array_of_vec[0].setValue(current_vec_ptr);
		double w=i_p2.x-i_p1.x;
		double h=i_p2.y-i_p1.y;

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		int cdx=1;
		prev_vec_ptr=null;
		for(int i=1;i<i_number_of_sample;i++){
			prev_vec_ptr=current_vec_ptr;
			current_vec_ptr=tmp_cd[cdx%2];
			cdx++;

			//ベクトル定義矩形を作る。
			tmprect.x=(int)(i_p1.x+(w*i/i_number_of_sample))-half_cell_size;
			tmprect.y=(int)(i_p1.y+(h*i/i_number_of_sample))-half_cell_size;
			tmprect.w=tmprect.h=i_cellsize;
			tmprect.clip(1,1,right,bottom);
			//ベクトル取得
			this.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			tmp_ptr=array_of_vec[number_of_data-1];
			if(NyARPointVector2d.getVecCos(prev_vec_ptr,current_vec_ptr)<_ANG_TH){
				//相関なし
				//前回までのベクトル和の調整
				tmp_ptr.x/=sum;
				tmp_ptr.y/=sum;
				//新しいベクトル値として保管
				array_of_vec[number_of_data].setValue(current_vec_ptr);
				//確定したので個数を+1
				number_of_data++;
				sum=1;
			}else{
				//相関あり(ベクトルの統合)
				tmp_ptr.x+=current_vec_ptr.x;
				tmp_ptr.y+=current_vec_ptr.y;
				tmp_ptr.dx+=current_vec_ptr.dx;
				tmp_ptr.dy+=current_vec_ptr.dy;
				sum++;
			}
		}
		for(int i=number_of_data-1;i>=0;i--)
		{
			current_vec_ptr=array_of_vec[i];
			//ベクトルの法線を取る。
			current_vec_ptr.OrthogonalVec(current_vec_ptr);
			//sqdistを計算
			current_vec_ptr.sq_dist=current_vec_ptr.dx*current_vec_ptr.dx+current_vec_ptr.dy*current_vec_ptr.dy;
		}		
		//閉じる
		tmp_ptr=array_of_vec[number_of_data-1];
		tmp_ptr.x/=sum;
		tmp_ptr.y/=sum;		
		o_coord.length=number_of_data;
		return;
	}		
}