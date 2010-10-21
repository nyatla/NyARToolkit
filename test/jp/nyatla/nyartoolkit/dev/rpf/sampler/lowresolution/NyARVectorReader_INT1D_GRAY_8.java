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
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatusPool;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.VecLinear;

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
		this._coord_buf=new NyARIntCoordinates((i_ref_raster.getWidth()+i_ref_raster.getHeight())*4);
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
	 * 320*240の場合、RECTの範囲は(x>=0 && x<319 x+w>=0 && x+w<319),(y>=0 && y<239 x+w>=0 && x+w<319)となります。
	 * @param i_gs
	 * @param ix
	 * ピクセル取得を行う位置を設定します。
	 * @param iy
	 * ピクセル取得を行う位置を設定します。
	 * @param iw
	 * ピクセル取得を行う範囲を設定します。
	 * @param ih
	 * ピクセル取得を行う範囲を設定します。
	 * @param o_posvec
	 * エッジ中心とベクトルを返します。
	 */
	public void getAreaVector33(int ix,int iy,int iw,int ih,NyARPointVector2d o_posvec)
	{
		assert(ih>=3 && iw>=3);
		assert((ix>=0) && (iy>=0) && (ix+iw)<=this._ref_size.w && (iy+ih)<=this._ref_size.h);
		int[] buf=this._ref_buf;
		int stride=this._ref_size.w;
		//x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		//x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x,sum_y,sum_wx,sum_wy,sum_vx,sum_vy;
		sum_x=sum_y=sum_wx=sum_wy=sum_vx=sum_vy=0;
		int vx,vy;
		for(int i=ih-3;i>=0;i--){
			for(int i2=iw-3;i2>=0;i2--){
				//1ビット分のベクトルを計算
				int idx_0 =stride*(i+1+iy)+(i2+1+ix);
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
				sum_x+=wx*(i2+1+ix);
				sum_y+=wy*(i+1+iy);
			}
		}
		//加重平均(posが0の場合の位置は中心)
		if(sum_x==0){
			o_posvec.x=ix+(iw>>1);
			o_posvec.dx=0;
		}else{
			o_posvec.x=(double)sum_x/sum_wx;			
			o_posvec.dx=(double)sum_vx/sum_wx;
		}
		if(sum_y==0){
			o_posvec.y=iy+(ih>>1);
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
	private NyARIntCoordinates _coord_buf;
	public final NyARContourPickup _cpickup=new NyARContourPickup();
	private final double _MARGE_ANG_TH=NyARMath.COS_DEG_8;
	
	public boolean traceConture(LrlsGsRaster i_rob_raster,int i_th,NyARIntPoint2d i_entry,VecLinear o_coord) throws NyARException
	{
		NyARIntCoordinates coord=this._coord_buf;
		//Robertsラスタから輪郭抽出
		if(!this._cpickup.getContour(i_rob_raster,i_th,i_entry.x,i_entry.y,coord)){
			//輪郭線MAXならなにもできないね。
			return false;
			
		}
		//輪郭線のベクトル化
		return traceConture(coord,i_rob_raster.resolution,i_rob_raster.resolution*2,o_coord);
	}
	/**
	 * 指定した点を結ぶ直線を計算して、輪郭に保存します。
	 * @param i_x0
	 * @param i_y0
	 * @param i_x1
	 * @param i_y1
	 * @param o_coord
	 * @return
	 */
	private int setlineCoord(int i_x0,int i_y0,int i_x1,int i_y1,NyARIntPoint2d[] o_coord)
	{
		//線分を定義
		int dx = (i_x1 > i_x0) ? i_x1 - i_x0 : i_x0 - i_x1;
		int dy = (i_y1 > i_y0) ? i_y1 - i_y0 : i_y0 - i_y1;
		int sx = (i_x1 > i_x0) ? 1 : -1;
		int sy = (i_y1 > i_y0) ? 1 : -1;

		
		//Bresenham
		int idx=0;
		if (dx >= dy){
			// 傾きが1以下の場合
			if(dx>=o_coord.length)
			{
				return -1;
			}
			int E = -dx;
			for (int i = 0; i <= dx; i++){
				o_coord[idx].x=i_x0;
				o_coord[idx].y=i_y0;
				idx++;
				i_x0 += sx;
				E += 2 * dy;
				if (E >= 0) {
					i_y0 += sy;
					E -= 2 * dx;
				}
			}
		} else {
			// 傾きが1より大きい場合
			if(dy>=o_coord.length)
			{
				return -1;
			}
			int E = -dy;
			for (int i = 0; i <= dy; i++) {
				o_coord[idx].x=i_x0;
				o_coord[idx].y=i_y0;
				idx++;
				i_y0 += sy;
				E += 2 * dx;
				if (E >= 0) {
					i_x0 += sx;
					E -= 2 * dy;
				}
			}
		}
		return idx;
	}
	/**
	 * 点1と点2の間に線分を定義して、その線分上のベクトルを得ます。点は、画像の内側でなければなりません。
	 * 320*240の場合、(x>=0 && x<320 x+w>0 && x+w<320),(y>0 && y<240 y+h>=0 && y+h<=319)となります。
	 * @param i_pos1
	 * 点1の座標です。
	 * @param i_pos2
	 * 点2の座標です。
	 * @param i_area
	 * ベクトルを検出するカーネルサイズです。1の場合(n*2-1)^2のカーネルになります。
	 * 点2の座標です。
	 * @param o_coord
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * @throws NyARException
	 */
	public boolean traceLine(NyARIntPoint2d i_pos1,NyARIntPoint2d i_pos2,int i_edge ,VecLinear o_coord)
	{
		NyARIntCoordinates coord=this._coord_buf;
		//(i_area*2)の矩形が範囲内に収まるように線を引く
		//移動量
		
		//点間距離を計算
		int dist=(int)Math.sqrt(i_pos1.sqNorm(i_pos2));
		//最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if(dist<4){
			return false;
		}
		//dist最大数の決定
		if(dist>14){
			dist=14;
		}
		//サンプリングサイズを決定(移動速度とサイズから)
		int s=i_edge*2+1;
		int dx=(i_pos2.x-i_pos1.x);
		int dy=(i_pos2.y-i_pos1.y);
		int r=this._ref_size.w-s;
		int b=this._ref_size.h-s;
		
		//最大14点を定義して、そのうち両端を除いた点を使用する。
		for(int i=3;i<dist-1;i++){
			int x=i*dx/dist+i_pos1.x-i_edge;
			int y=i*dy/dist+i_pos1.y-i_edge;
			//limit
			coord.items[i-3].x=x<0?0:(x>=r?r:x);
			coord.items[i-3].y=y<0?0:(y>=b?b:y);
		}
		
		coord.length=dist-4;
		//点数は10点程度を得る。
		return traceConture(coord,1,s,o_coord);
	}
	public boolean traceLine(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2,int i_edge ,VecLinear o_coord)
	{
		NyARIntCoordinates coord=this._coord_buf;
		//(i_area*2)の矩形が範囲内に収まるように線を引く
		//移動量
		
		//点間距離を計算
		int dist=(int)Math.sqrt(i_pos1.sqNorm(i_pos2));
		//最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if(dist<4){
			return false;
		}
		//dist最大数の決定
		if(dist>14){
			dist=14;
		}
		//サンプリングサイズを決定(移動速度とサイズから)
		int s=i_edge*2+1;
		int dx=(int)(i_pos2.x-i_pos1.x);
		int dy=(int)(i_pos2.y-i_pos1.y);
		int r=this._ref_size.w-s;
		int b=this._ref_size.h-s;
		
		//最大14点を定義して、そのうち両端の2個を除いた点を使用する。
		for(int i=dist-1;i>=0;i--){
			int x=(int)((i+2)*dx/dist+i_pos1.x-i_edge);
			int y=(int)((i+2)*dy/dist+i_pos1.y-i_edge);
			//limit
			coord.items[i].x=x<0?0:(x>=r?r:x);
			coord.items[i].y=y<0?0:(y>=b?b:y);
		}
		
		coord.length=dist-4;
		//点数は10点程度を得る。
		return traceConture(coord,1,s,o_coord);
	}		
	/**
	 * 輪郭点の画像ベクトルを取得します。
	 * @param i_coord
	 * 輪郭点の配列
	 * @param i_coordlen
	 * 輪郭点配列の長さ
	 * @param i_pos_mag
	 * 座標系の倍率です。
	 * @param i_cell_size
	 * ベクトル解析を行う正方形のカーネルサイズです。3以上を指定してください。
	 * @param o_coord
	 * @return
	 */
	public boolean traceConture(NyARIntCoordinates i_coord,int i_pos_mag,int i_cell_size,VecLinear o_coord)
	{
		//ベクトル化
		VecLinear.CoordData[] array_of_vec=o_coord.items;
		int MAX_COORD=o_coord.items.length;
		//検出RECTは、x,yと(x+w),(y+h)の間にあるものになる。

		
		VecLinear.CoordData prev_vec_ptr,current_vec_ptr,tmp_ptr;		
		VecLinear.CoordData[] tmp_cd=VecLinear.CoordData.createArray(3);
		current_vec_ptr=tmp_cd[0];
		
		int i_coordlen=i_coord.length;
		NyARIntPoint2d[] coord=i_coord.items;


		int number_of_data=1;
		int sum=1;
		//0個目のベクトル
		this.getAreaVector33(coord[0].x*i_pos_mag,coord[0].y*i_pos_mag,i_cell_size,i_cell_size,current_vec_ptr);
		array_of_vec[0].setValue(current_vec_ptr);
		//[2]に0番目のバックアップを取る。
		tmp_cd[2].setValue(current_vec_ptr);
		

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		int cdx=1;
		for(int i=1;i<i_coordlen;i++){
			prev_vec_ptr=current_vec_ptr;
			current_vec_ptr=tmp_cd[cdx%2];
			cdx++;

			//ベクトル取得
			this.getAreaVector33(coord[i].x*i_pos_mag,coord[i].y*i_pos_mag,i_cell_size,i_cell_size,current_vec_ptr);
			
			//類似度判定
			tmp_ptr=array_of_vec[number_of_data-1];
			if(prev_vec_ptr.getVecCos(current_vec_ptr)<_MARGE_ANG_TH){
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
			//輪郭中心を出すための計算
			if(number_of_data==MAX_COORD){
				//輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		//ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		//current_vec_ptrには最後のベクトルが入ってる。temp_cdには開始のベクトルが入ってる。
		if(current_vec_ptr.getVecCos(tmp_cd[2])<_MARGE_ANG_TH || number_of_data==1){
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
		o_coord.length=number_of_data;
		return true;
	}	
}