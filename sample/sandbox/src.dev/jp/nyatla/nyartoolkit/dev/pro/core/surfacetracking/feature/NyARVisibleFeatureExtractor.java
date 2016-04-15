package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceTransMatrixSet;


/**
 * スクリーン上で可視な特徴を選択するクラスです�??
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
	public void extractVisibleFeatures(
		NyARSurfaceFeatureSet i_fset,NyARSurfaceTransMatrixSet i_ctrans,
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
			NyARSurfaceFeatureSet.NyAR2FeaturePoints fpoint_ptr=i_fset.list[j];
			for(int k = 0; k < fpoint_ptr.coord.length; k++ )
			{
				NyARSurfaceFeatureSet.NyAR2FeatureCoord coord_ptr=fpoint_ptr.coord[k];
				
				//�?想画面点を計�?
				i_ctrans.calculate2dPos(coord_ptr.mx,coord_ptr.my,ide2d);
				df.ideal2Observ(ide2d.x,ide2d.y, obs2d);

				//観察座標に変換後�?�画面�?にあるか確�?
				if( obs2d.x < 0 || obs2d.x >= xsize ){
					continue;
				}
				if( obs2d.y < 0 || obs2d.y >= ysize ) 
				{
					continue;
				}
				//�?変換可能か確�?
				df.observ2Ideal(obs2d, rideal2d);
				if(ide2d.sqDist(rideal2d)>1.0){
					continue;
				}

				
				//原点からのベクトルを計�?
				//Z軸�?+�?とつかえな�?ので判定�?
				if(i_ctrans.calculateVd(coord_ptr.mx, coord_ptr.my)>-0.1){
					continue;
				}
//				double vd0 = trans1.m00 * coord_ptr.mx+ trans1.m01 * coord_ptr.my+ trans1.m03;
//				double vd1 = trans1.m10 * coord_ptr.mx+ trans1.m11 * coord_ptr.my+ trans1.m13;
//				double vd2 = trans1.m20 * coord_ptr.mx+ trans1.m21 * coord_ptr.my+ trans1.m23;
//				if( (vd0*trans1.m02 + vd1*trans1.m12 + vd2*trans1.m22)/Math.sqrt( vd0*vd0 + vd1*vd1 + vd2*vd2 ) > -0.1 ){
//					continue;
//				}
				
				
				//撮影�?�?のdpiを計�?(x,y方向で計算して、大・小�?��?番で格納�?)
				double dpi=i_ctrans.ar2GetMinResolution(coord_ptr);

				//dpiによってコレクトする�?�補を�?離
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
