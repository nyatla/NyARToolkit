/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceFeatures;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceTransMatrixSet;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * スクリーン上で可視な特徴を選択するクラスです。
 *
 */
public class NyARVisibleFeatureExtractor implements INyARVisibleFeatureExtractor
{
	private NyARDoublePoint2d __ide2d=new NyARDoublePoint2d();
	private NyARDoublePoint2d __obs2d=new NyARDoublePoint2d();
	private NyARDoublePoint2d __rideal2d=new NyARDoublePoint2d();
	private INyARCameraDistortionFactor _ref_dist_factor;
	private NyARIntSize _ref_size;
	public NyARVisibleFeatureExtractor(NyARIntSize i_ref_screen_size,INyARCameraDistortionFactor i_ref_dist_factor)
	{
		this._ref_size=i_ref_screen_size;
		this._ref_dist_factor=i_ref_dist_factor;
	}
	@Override
	public void extractVisibleFeatures(
		NyARNftFsetFile i_fset,NyARSurfaceTransMatrixSet i_ctrans,
		NyARSurfaceFeatures candidate,NyARSurfaceFeatures candidate2)
	{
		//get work objects
		NyARDoublePoint2d ide2d=this.__ide2d;
		NyARDoublePoint2d obs2d=this.__obs2d;
		NyARDoublePoint2d rideal2d=this.__rideal2d;
		
//		trans1.setCoefficient(i_cparam.getPerspectiveProjectionMatrix(), trans1);
		candidate.clear();
		candidate2.clear();

		int xsize = this._ref_size.w;
		int ysize = this._ref_size.h;
		INyARCameraDistortionFactor df=this._ref_dist_factor;


		for(int j = 0; j < i_fset.list.length; j++ )
		{
			NyARNftFsetFile.NyAR2FeaturePoints fpoint_ptr=i_fset.list[j];
			for(int k = 0; k < fpoint_ptr.coord.length; k++ )
			{
				NyARNftFsetFile.NyAR2FeatureCoord coord_ptr=fpoint_ptr.coord[k];
				
				//理想画面点を計算
				i_ctrans.calculate2dPos(coord_ptr.mx,coord_ptr.my,ide2d);
				df.ideal2Observ(ide2d.x,ide2d.y, obs2d);

				//観察座標に変換後、画面内にあるか確認
				if( obs2d.x < 0 || obs2d.x >= xsize ){
					continue;
				}
				if( obs2d.y < 0 || obs2d.y >= ysize ) 
				{
					continue;
				}
				//逆変換可能か確認
				df.observ2Ideal(obs2d, rideal2d);
				if(ide2d.sqDist(rideal2d)>1.0){
					continue;
				}

				
				//原点からのベクトルを計算
				//Z軸が+だとつかえないので判定？
				if(i_ctrans.calculateVd(coord_ptr.mx, coord_ptr.my)>-0.1){
					continue;
				}
//				double vd0 = trans1.m00 * coord_ptr.mx+ trans1.m01 * coord_ptr.my+ trans1.m03;
//				double vd1 = trans1.m10 * coord_ptr.mx+ trans1.m11 * coord_ptr.my+ trans1.m13;
//				double vd2 = trans1.m20 * coord_ptr.mx+ trans1.m21 * coord_ptr.my+ trans1.m23;
//				if( (vd0*trans1.m02 + vd1*trans1.m12 + vd2*trans1.m22)/Math.sqrt( vd0*vd0 + vd1*vd1 + vd2*vd2 ) > -0.1 ){
//					continue;
//				}
				
				
				//撮影箇所のdpiを計算(x,y方向で計算して、大・小の順番で格納？)
				double dpi=i_ctrans.ar2GetMinResolution(coord_ptr);

				//dpiによってコレクトする候補を分離
				if( dpi <= fpoint_ptr.maxdpi && dpi >= fpoint_ptr.mindpi )
				{
					NyARSurfaceFeatureItem item=candidate.prePush();
					if(item==null){
						return;
					}
					item.ref_feature = coord_ptr;
					item.scale=fpoint_ptr.scale;
					item.x    = obs2d.x;
					item.y    = obs2d.y;
				}else if( dpi <= fpoint_ptr.maxdpi*2 && dpi >= fpoint_ptr.mindpi/2 )
				{
					NyARSurfaceFeatureItem item=candidate2.prePush();
					if(item==null){
						return;
					}
					item.ref_feature = coord_ptr;
					item.scale=fpoint_ptr.scale;
					item.x    = obs2d.x;
					item.y    = obs2d.y;
				}
			}
		}
		return;
	}

	


}
