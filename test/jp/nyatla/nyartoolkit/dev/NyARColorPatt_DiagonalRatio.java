/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.dev;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.pickup.*;


class NyARDoubleLine2d
{
	double x,y;
	void set(NyARDoublePoint2d i_point_a,NyARDoublePoint2d i_point_b)
	{
		this.x=i_point_a.x-i_point_b.x;
		this.y=i_point_a.y-i_point_b.y;
		return;
	}
	void add(NyARDoubleLine2d i_param,NyARDoubleLine2d o_result)
	{
		o_result.x=this.x+i_param.x;
		o_result.y=this.y+i_param.y;
		return;
	}
	void sum(NyARDoubleLine2d i_param,NyARDoubleLine2d o_result)
	{
		o_result.x=this.x-i_param.x;
		o_result.y=this.y-i_param.y;
		return;
	}
	/**
	 * i_paramとの外積を計算する。
	 * @param i_param
	 * @return
	 */
	double cross(NyARDoubleLine2d i_param)
	{
		return this.x*i_param.y-this.y*i_param.x;
	}
	/**
	 * i_paramとの内積を計算する
	 * @param i_param
	 * @return
	 */
	double dot(NyARDoubleLine2d i_param)
	{
		return this.x*i_param.x+this.y*i_param.y;
	}
	/**
	 * このベクトルの絶対値を計算する
	 * @param i_param
	 * @return
	 */
	double dist()
	{
		return Math.sqrt(this.x*this.x+this.y*this.y);
	}
	
	
}


class LineParam
{
	public double a;
	public double b;
	public static LineParam[] createArray(int i_length)
	{
		LineParam[] result=new LineParam[i_length];
		for(int i=result.length-1;i>=0;i--){
			result[i]=new LineParam();
		}
		return result;
	}
}

class Complex
{
	public double i;
	public double r;
	public Complex()
	{
	}
	public Complex(double i_r,double i_i)
	{
		this.r=i_r;
		this.i=i_i;
	}
	public void add(Complex i_v)
	{
		this.r+=i_v.r;
		this.i+=i_v.i;
	}
	public void sub(Complex i_v)
	{
		this.r-=i_v.r;
		this.i-=i_v.i;
	}
	public void sub(Complex i_v1,Complex i_v2)
	{
		this.r=i_v1.r-i_v2.r;
		this.i=i_v1.i-i_v2.i;
	}
	
	public void mul(Complex i_v)
	{
		double r,i;
		r=this.r;
		i=this.i;
		final double d2=Math.sqrt(r*r+i*i);
		final double s2=Math.acos(r/d2);
		r=i_v.r;
		i=i_v.i;
		final double d1=Math.sqrt(r*r+i*i);
		final double s1=Math.acos(r/d1);
		
		this.r=d1*d2*Math.cos(s2+s1);
		this.i=d1*d2*Math.sin(s2+s1);
		return;
	}
	public void div(Complex i_v)
	{
		double r,i;
		r=this.r;
		i=this.i;
		final double d2=Math.sqrt(r*r+i*i);
		final double s2=Math.acos(r/d2);
		r=i_v.r;
		i=i_v.i;
		final double d1=Math.sqrt(r*r+i*i);
		final double s1=Math.acos(r/d1);
		
		this.r=d2/d1*Math.cos(s2/s1);
		this.i=d2/d1*Math.sin(s2/s1);
		return;
	}
	public void pow(Complex i_v,double i_base)
	{
		double r,i;
		r=i_v.r;
		i=i_v.i;
		double d=Math.sqrt(r*r+i*i);
		final double s=Math.acos(r/d)*i_base;
		d=Math.pow(d,i_base);
		this.r=d*Math.cos(s);
		this.i=d*Math.sin(s);
	}
	public void sqrt(Complex i_v)
	{
		double r,i;
		r=i_v.r;
		i=i_v.i;
		double d=Math.sqrt(r*r+i*i);
		final double s=Math.acos(r/d)*0.5;
		d=Math.sqrt(d);
		this.r=d*Math.cos(s);
		this.i=d*Math.sin(s);
	}
	public double dist()
	{
		return Math.sqrt(this.r*this.r+this.i*this.i);
	}
	
}


/**
 * 24ビットカラーのマーカーを保持するために使うクラスです。 このクラスは、ARToolkitのパターンと、ラスタから取得したパターンを保持します。
 * 演算順序を含む最適化をしたもの
 * 
 */
public class NyARColorPatt_DiagonalRatio implements INyARColorPatt
{
	private int[] _patdata;
	private NyARBufferReader _buf_reader;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	private NyARIntSize _size;
		
	public final int getWidth()
	{
		return this._size.w;
	}
	
	public final int getHeight()
	{
		return this._size.h;
	}
	
	public final NyARIntSize getSize()
	{
		return 	this._size;
	}
	
	public final INyARBufferReader getBufferReader()
	{
		return this._buf_reader;
	}
	
	public final INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	NyARDoubleMatrix44 _invmat=new NyARDoubleMatrix44();
	/**
	 * @param i_width
	 * @param i_height
	 */
	public NyARColorPatt_DiagonalRatio(int i_model)
	{
		int resolution=(1<<i_model)+1;
		this._size=new NyARIntSize(resolution,resolution);
		this._patdata = new int[resolution*resolution];
		this._buf_reader=new NyARBufferReader(this._patdata,NyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32);
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
		
		this._vertex_map=NyARDoublePoint2d.create2dArray(this._size.h,this._size.w);

		return;
	}	
	public NyARDoublePoint2d[][] _vertex_map;
	public boolean pickFromRaster(INyARRgbRaster image, NyARSquare i_square)throws NyARException
	{
		NyARDoublePoint2d center=new NyARDoublePoint2d();
		//中心を取得
		solvCrossPoint(i_square.sqvertex[0],i_square.sqvertex[2],i_square.sqvertex[1],i_square.sqvertex[3],center);
		
		int[] rgb_tmp=new int[3];
		INyARRgbPixelReader reader=image.getRgbPixelReader();
		//頂点マトリクスの計算(2=2分割,3=4分割,4=8分割)
		NyARDoublePoint2d[][] vertex_map=this._vertex_map;
		solvLinePointArray(center,this._size.h-1,i_square.sqvertex,vertex_map);
		for(int i=0;i<this._size.h;i++){
			for(int i2=0;i2<this._size.w;i2++){
				if(vertex_map[i][i2].x>320||vertex_map[i][i2].y>240||vertex_map[i][i2].x<0||vertex_map[i][i2].y<0)
				{
					//System.out.println(vertex_map[i][i2].x+","+vertex_map[i][i2].y);
					this._patdata[i2+i*this._size.w]=0;
					continue;
				}
				reader.getPixel((int)vertex_map[i][i2].x,(int)vertex_map[i][i2].y,rgb_tmp);
				this._patdata[i2+i*this._size.w]=(rgb_tmp[0]<<16)|(rgb_tmp[1]<<8)|rgb_tmp[2];				
			}			
		}
		return true;
	}
	/**
	 * 直線をscaleで2^n分割した配列を計算する。
	 * @param i_p1
	 * @param i_p2
	 * @param o_param
	 * @return
	 */
	private void solvLinePointArray4(NyARDoublePoint2d i_center,int i_div,NyARDoublePoint2d[] i_vertex,NyARDoublePoint2d[][] o_result)
	{
		//分割直線の計算(2=2分割,3=4分割,4=8分割)
		//中心コピー
		o_result[0][0].setValue(i_center);
		
		
		//[0]->[1]のベクトルを計算
		NyARDoublePoint2d vec01=new NyARDoublePoint2d();
		NyARDoublePoint2d vec12=new NyARDoublePoint2d();
		NyARDoublePoint2d vec03=new NyARDoublePoint2d();
		NyARDoublePoint2d vec32=new NyARDoublePoint2d();
		vec01.vecSub(i_vertex[1],i_vertex[0]);
		vec12.vecSub(i_vertex[2],i_vertex[1]);
		vec03.vecSub(i_vertex[3],i_vertex[0]);
		vec32.vecSub(i_vertex[2],i_vertex[3]);

		//中心点から[0]->[1]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec01_ep=new NyARDoublePoint2d();
		vec01_ep.vecAdd(vec01,i_center);
		//中心点から[3]->[2]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec32_ep=new NyARDoublePoint2d();
		vec32_ep.vecAdd(vec32,i_center);		
		//平均値
		NyARDoublePoint2d cx_e=new NyARDoublePoint2d();
		cx_e.x=(vec01_ep.x+vec32_ep.x)/2;
		cx_e.y=(vec01_ep.y+vec32_ep.y)/2;
		
		//ベクトル[1]->[2]との交差点を計算
		solvCrossPoint(i_center,cx_e,i_vertex[1],i_vertex[2],o_result[1][2]);
		//ベクトル[3]->[0]との交差点を計算
		solvCrossPoint(i_center,cx_e,i_vertex[3],i_vertex[0],o_result[1][0]);
		

		
		//中心点から[1]->[2]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec12_ep=new NyARDoublePoint2d();
		vec12_ep.vecAdd(vec12,i_center);
		//中心点から[0]->[3]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec03_ep=new NyARDoublePoint2d();
		vec03_ep.vecAdd(vec03,i_center);		
		//平均値
		NyARDoublePoint2d cx_e2=new NyARDoublePoint2d();
		cx_e2.x=(vec12_ep.x+vec03_ep.x)/2;
		cx_e2.y=(vec12_ep.y+vec03_ep.y)/2;
		
		//cx_e2とベクトル[0]->[1]との交差点を計算
		solvCrossPoint(i_center,cx_e2,i_vertex[0],i_vertex[1],o_result[0][1]);
		//ベクトル[3]->[2]との交差点を計算
		solvCrossPoint(i_center,cx_e2,i_vertex[3],i_vertex[2],o_result[2][1]);
		
		
		
		
		return;
	}
	private void solvLinePointArray(NyARDoublePoint2d i_center,int i_div,NyARDoublePoint2d[] i_vertex,NyARDoublePoint2d[][] o_result)
	{
		//中心コピー
		o_result[0][0].setValue(i_center);

		//[0]+[1]+[c]
		NyARDoublePoint2d vt=new NyARDoublePoint2d();
		vt.x=(i_vertex[0].x+i_vertex[1].x)/2;
		vt.y=(i_vertex[0].y+i_vertex[1].y)/2;
		//[2]+[3]+[c]
		NyARDoublePoint2d vb=new NyARDoublePoint2d();
		vb.x=(i_vertex[2].x+i_vertex[3].x)/2;
		vb.y=(i_vertex[2].y+i_vertex[3].y)/2;
		
		vt.vecSub(vb);
		vt.vecAdd(i_center);

		//[0][1]->[2][3]ベクトル
		solvCrossPoint(vt,i_center,i_vertex[0],i_vertex[1],o_result[1][2]);
		//[0][1]->[2][3]ベクトル:v[3][0]
		solvCrossPoint(vt,i_center,i_vertex[3],i_vertex[2],o_result[1][0]);

		
		
/*		
		
		//[0]->[1]のベクトルを計算
		NyARDoublePoint2d vec01=new NyARDoublePoint2d();
		NyARDoublePoint2d vec12=new NyARDoublePoint2d();
		NyARDoublePoint2d vec03=new NyARDoublePoint2d();
		NyARDoublePoint2d vec32=new NyARDoublePoint2d();
		vec01.vecSub(i_vertex[1],i_vertex[0]);
		vec12.vecSub(i_vertex[2],i_vertex[1]);
		vec03.vecSub(i_vertex[3],i_vertex[0]);
		vec32.vecSub(i_vertex[2],i_vertex[3]);

		//中心点から[0]->[1]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec01_ep=new NyARDoublePoint2d();
		vec01_ep.vecSum(vec01,i_center);
		//中心点から[3]->[2]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec32_ep=new NyARDoublePoint2d();
		vec32_ep.vecSum(vec32,i_center);		
		//平均値
		NyARDoublePoint2d cx_e=new NyARDoublePoint2d();
		cx_e.x=(vec01_ep.x+vec32_ep.x)/2;
		cx_e.y=(vec01_ep.y+vec32_ep.y)/2;
		
		//ベクトル[1]->[2]との交差点を計算
		solvCrossPoint(i_center,cx_e,i_vertex[1],i_vertex[2],o_result[1][2]);
		//ベクトル[3]->[0]との交差点を計算
		solvCrossPoint(i_center,cx_e,i_vertex[3],i_vertex[0],o_result[1][0]);
		

		
		//中心点から[1]->[2]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec12_ep=new NyARDoublePoint2d();
		vec12_ep.vecSum(vec12,i_center);
		//中心点から[0]->[3]と平行なベクトルの終点を計算
		NyARDoublePoint2d vec03_ep=new NyARDoublePoint2d();
		vec03_ep.vecSum(vec03,i_center);		
		//平均値
		NyARDoublePoint2d cx_e2=new NyARDoublePoint2d();
		cx_e2.x=(vec12_ep.x+vec03_ep.x)/2;
		cx_e2.y=(vec12_ep.y+vec03_ep.y)/2;
		
		//cx_e2とベクトル[0]->[1]との交差点を計算
		solvCrossPoint(i_center,cx_e2,i_vertex[0],i_vertex[1],o_result[0][1]);
		//ベクトル[3]->[2]との交差点を計算
		solvCrossPoint(i_center,cx_e2,i_vertex[3],i_vertex[2],o_result[2][1]);
		
		
*/		
		
		return;
	}
	
	
	
	private void solvLinePointArray3(NyARDoublePoint2d i_canter,int i_div,NyARDoublePoint2d[] i_vertex,NyARDoublePoint2d[][] o_result)
	{
		NyARDoublePoint2d scale=new NyARDoublePoint2d();
		//分割直線の計算(2=2分割,3=4分割,4=8分割)
		int d=i_div;
		NyARDoublePoint2d[] r=o_result[d/2];
		//対角線Aを計算
		r[0].x=i_vertex[0].x;
		r[0].y=i_vertex[0].y;
		r[d].x=i_vertex[2].x;
		r[d].y=i_vertex[2].y;
		scale.x=(i_canter.x-i_vertex[0].x)/(i_vertex[2].x-i_vertex[0].x);
		scale.y=(i_canter.y-i_vertex[0].y)/(i_vertex[2].y-i_vertex[0].y);

		solvLinePointArray_b(scale,0,d,r);
		//対角線上にコピー
		for(int i=0;i<=d;i++){
			o_result[i][i].x=r[i].x;	
			o_result[i][i].y=r[i].y;
		}
		//対角線Bを計算
		r[0].x=i_vertex[3].x;
		r[0].y=i_vertex[3].y;
		r[d].x=i_vertex[1].x;
		r[d].y=i_vertex[1].y;
		scale.x=(i_canter.x-i_vertex[3].x)/(i_vertex[1].x-i_vertex[3].x);
		scale.y=(i_canter.y-i_vertex[3].y)/(i_vertex[1].y-i_vertex[3].y);
		solvLinePointArray_b(scale,0,d,r);
		//対角線上にコピー
		for(int i=0;i<=d;i++){
			o_result[d-i][i].x=r[i].x;
			o_result[d-i][i].y=r[i].y;
		}
		//マップ作成
		for(int i=0;i<=d;i++){
			final NyARDoublePoint2d y1=o_result[i][i];
			final NyARDoublePoint2d y2=o_result[d-i][i];
			if(i==d/2){
				continue;
			}
			for(int i2=0;i2<=d;i2++){
				if(i==i2){
					continue;
				}
				if(i==d-i2){
					continue;
				}
				if(i2==d/2){
					continue;
				}
				final NyARDoublePoint2d x1=o_result[i2][i2];
				final NyARDoublePoint2d x2=o_result[i2][d-i2];
				solvCrossPoint(y1,y2,x1,x2,o_result[i2][i]);
			}
		}

		return;
	}	
	/**
	 * 直線をscaleで2^n分割した配列を計算する。
	 * @param i_p1
	 * @param i_p2
	 * @param o_param
	 * @return
	 */
	private void solvLinePointArray2(NyARDoublePoint2d i_canter,int i_div,NyARDoublePoint2d[] i_vertex,NyARDoublePoint2d[][] o_result)
	{
		NyARDoublePoint2d scale=new NyARDoublePoint2d();
		//分割直線の計算(2=2分割,3=4分割,4=8分割)
		int d=i_div;
		NyARDoublePoint2d[] r=o_result[d/2];
		//対角線Aを計算
		r[0].x=i_vertex[0].x;
		r[0].y=i_vertex[0].y;
		r[d].x=i_vertex[2].x;
		r[d].y=i_vertex[2].y;
//		scale.x=(i_canter.x-i_vertex[0].x)/(i_vertex[2].x-i_vertex[0].x);
//		scale.y=(i_canter.y-i_vertex[0].y)/(i_vertex[2].y-i_vertex[0].y);
		double sx,kx,lx,sy,ky,ly;

		sx=i_vertex[0].x;
		kx=solvK(i_canter.x-sx,i_vertex[2].x-sx);
		lx=solvL(kx,i_canter.x-sx);

		sy=i_vertex[0].y;
		ky=solvK(i_canter.y-sy,i_vertex[2].y-sy);
		ly=solvL(kx,i_canter.y-sy);
		
		solvLinePointArray_b(scale,0,d,r);
		//対角線上にコピー
		for(int i=0;i<=d;i++){
			o_result[i][i].x=sx+kx*lx;
			o_result[i][i].y=sy+ky*ly;
			kx*=kx;
			ky*=ky;
		}
		
		sx=i_vertex[3].x;
		kx=solvK(i_canter.x-sx,i_vertex[1].x-sx);
		lx=solvL(kx,i_canter.x-sx);

		sy=i_vertex[3].y;
		ky=solvK(i_canter.y-sy,i_vertex[1].y-sy);
		ly=solvL(kx,i_canter.y-sy);
		
		solvLinePointArray_b(scale,0,d,r);
		//対角線上にコピー
		for(int i=0;i<=d;i++){
			o_result[d-i][i].x=sx+kx*lx;
			o_result[d-i][i].y=sy+ky*ly;
			kx*=kx;
			ky*=ky;
		}
		//マップ作成
		for(int i=0;i<=d;i++){
			final NyARDoublePoint2d y1=o_result[i][i];
			final NyARDoublePoint2d y2=o_result[d-i][i];
			if(i==d/2){
				continue;
			}
			for(int i2=0;i2<=d;i2++){
				if(i==i2){
					continue;
				}
				if(i==d-i2){
					continue;
				}
				if(i2==d/2){
					continue;
				}
				final NyARDoublePoint2d x1=o_result[i2][i2];
				final NyARDoublePoint2d x2=o_result[i2][d-i2];
				solvCrossPoint(y1,y2,x1,x2,o_result[i2][i]);
			}
		}

		return;
	}	

	private void solvLinePointArray_b(NyARDoublePoint2d i_scale,int i_si,int i_ei,NyARDoublePoint2d[] o_result)
	{
		int ci=(i_ei-i_si)/2+i_si;
		o_result[ci].x=i_scale.x*(o_result[i_ei].x-o_result[i_si].x)+o_result[i_si].x;
		o_result[ci].y=i_scale.y*(o_result[i_ei].y-o_result[i_si].y)+o_result[i_si].y;
		
		if(ci-i_si==1){
			return;
		}
		solvLinePointArray_b(i_scale,i_si,ci,o_result);
		solvLinePointArray_b(i_scale,ci,i_ei,o_result);
		return;
	}
	
	private void solvCrossPoint(NyARIntPoint2d i_p1,NyARIntPoint2d i_p2,NyARIntPoint2d i_p3,NyARIntPoint2d i_p4,NyARDoublePoint2d o_result)
	{
		NyARDoublePoint2d va=new NyARDoublePoint2d(i_p2);
		NyARDoublePoint2d vb=new NyARDoublePoint2d(i_p4);
		va.vecSub(i_p1);
		vb.vecSub(i_p3);
		o_result.setValue(i_p3);
		o_result.vecSub(i_p1);
		va.vecMul(va, vb.vecCross(o_result)/vb.vecCross(va));
		o_result.setValue(va);
		o_result.vecAdd(i_p1);
		return;
		//V a=p2-p1;
		//V b=p4-p3;
		//return a1 + a * cross(b, b1-a1) / cross(b, a);
	}
	private void solvCrossPoint(NyARDoublePoint2d i_p1,NyARDoublePoint2d i_p2,NyARDoublePoint2d i_p3,NyARDoublePoint2d i_p4,NyARDoublePoint2d o_result)
	{
		NyARDoublePoint2d va=new NyARDoublePoint2d(i_p2);
		NyARDoublePoint2d vb=new NyARDoublePoint2d(i_p4);
		va.vecSub(i_p1);
		vb.vecSub(i_p3);
		o_result.setValue(i_p3);
		o_result.vecSub(i_p1);
		va.vecMul(va, vb.vecCross(o_result)/vb.vecCross(va));
		o_result.setValue(va);
		o_result.vecAdd(i_p1);
		return;
		//V a=p2-p1;
		//V b=p4-p3;
		//return a1 + a * cross(b, b1-a1) / cross(b, a);
	}	
	double pow_1_3(double a)
	{
		double x = Math.pow(a, 1./3);
		return (2*x+a/x/x)/3; // modifier
	}
	/*
	 * 
	 * 
	 * */
	//(Sqrt(((3*Z*((-Z)/(3*Z))^2-(2*Z^2)/(3*Z)+Z-1)/Z)^3/27+(Z*((-Z)/(3*Z))^2+Z*((-Z)/(3*Z))^3-((Z-1)*Z)/(3*Z)+Z-1)^2/(4*Z^2))-(Z*((-Z)/(3*Z))^2+Z*((-Z)/(3*Z))^3-((Z-1)*Z)/(3*Z)+Z-1)/(2*Z))^(1/3)+(-((Z*((-Z)/(3*Z))^2+Z*((-Z)/(3*Z))^3-((Z-1)*Z)/(3*Z)+Z-1)/(2*Z)+Sqrt(((3*Z*((-Z)/(3*Z))^2-(2*Z^2)/(3*Z)+Z-1)/Z)^3/27+(Z*((-Z)/(3*Z))^2+Z*((-Z)/(3*Z))^3-((Z-1)*Z)/(3*Z)+Z-1)^2/(4*Z^2))))^(1/3)-Z/(3*Z)
	private double solvK(double mp,double vp)
	{
		double Z=mp/vp;
		double a=(Z-1);
		double b=(3.0*Z);
		double c=(a*Z);
		double d=(2.0*Z*Z);//(2*Z^2)
		double e=(4.0*Z*Z);//(4*Z^2)
		double f=((-Z)/b);
		double g=(Z*f*f+Z*f*f*f-c/b+Z-1);//(Z*f^2+Z*f^3-c/b+Z-1)
		double h=(3.0*Z*f*f-d/b+Z-1.0);//(3*Z*f^2-d/b+Z-1)
		double i=(h/Z);
		Complex j=new Complex(0,i*i*i/27.0+g*g/e);//(0,i*i*i/27.0+g*g/e);
//		j.r=0;
//		j.i=Math.sqrt(-i*i*i/27.0+g*g/e);
//		j.sqrt(j);
		Complex k=new Complex();
		k.r=j.r-g/(2.0*Z);
		k.i=j.i;
		Complex l=new Complex();
		l.r=-(g/(2.0*Z)+j.r);
		l.i=-j.i;
		
		k.pow(k,1.0/3);
		l.pow(l,1.0/3);
		double fi=k.dist()+l.dist()-Z/b;
		return fi;
	}
	private double solvL(double k,double mp)
	{
		return 100/(1+k*k+k*k*k+k);
	}
	
	
	public static void main(String[] args)
	{

		try {
			NyARColorPatt_DiagonalRatio t = new NyARColorPatt_DiagonalRatio(3);
			double k=t.solvK(60,100);
			double l=t.solvL(k,60);
			
			// t.Test_arGetVersion();
			NyARSquare s=new NyARSquare();
			s.sqvertex[0].x=10;
			s.sqvertex[0].y=10;
			s.sqvertex[1].x=90;
			s.sqvertex[1].y=0;
			s.sqvertex[2].x=100;
			s.sqvertex[2].y=100;
			s.sqvertex[3].x=0;
			s.sqvertex[3].y=100;
			//t.getLineCrossPoint(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}