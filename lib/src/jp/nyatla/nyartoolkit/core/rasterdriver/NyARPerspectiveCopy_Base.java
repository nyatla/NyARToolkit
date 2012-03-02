/* 
 * PROJECT: NyARToolkit(Extension)
 * -------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.rasterdriver;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.*;

/**
 * このクラスは、INyARPerspectiveCopyの基本機能を実装したベースクラスです。
 * {@link #onePixel(int, int, double[], INyARRaster)}と{@link #multiPixel(int, int, double[], int, INyARRaster)}
 * を実装して、クラスを完成させます。
 * 
 */
public abstract class NyARPerspectiveCopy_Base implements INyARPerspectiveCopy
{
	private static final int LOCAL_LT=1;
	protected NyARPerspectiveParamGenerator _perspective_gen;
	protected final double[] __pickFromRaster_cpara=new double[8];	
	protected NyARPerspectiveCopy_Base()
	{
		this._perspective_gen=new NyARPerspectiveParamGenerator_O1(LOCAL_LT,LOCAL_LT);		
	}
	public boolean copyPatt(double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		NyARIntSize out_size=i_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4,this.__pickFromRaster_cpara)) {
				return false;
			}
			this.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_out);
		}
		return true;
	}

	public boolean copyPatt(NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		return this.copyPatt(i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, i_edge_x, i_edge_y, i_resolution, i_out);
	}
	public boolean copyPatt(NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		return this.copyPatt(i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, i_edge_x, i_edge_y, i_resolution, i_out);
	}
	protected abstract boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException;
	protected abstract boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException;

}