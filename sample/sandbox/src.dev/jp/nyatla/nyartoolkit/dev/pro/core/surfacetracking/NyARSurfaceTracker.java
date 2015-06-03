package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.pro.core.rasterdriver.INyARTemplateMatchingDriver;
import jp.nyatla.nyartoolkit.pro.core.rasterdriver.NyARTemplateMatchingDriver_INT1D;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.INyARVisibleFeatureExtractor;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureIndexSelector;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureItem;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatures;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeaturesPtr;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARVisibleFeatureExtractor;

/**
 * 過去に入力された�?大3個�?�姿勢行�?�を�?に、サーフェイス特徴を追跡し�?�三次�?座標とのペアを生成する�??
 *
 */
public class NyARSurfaceTracker
{
	/**
	 * パッチ�?�候補点の保持クラス�?
	 */
	private class PatchImagePositions
	{
		/**
		 * パッチ�?�候補点。観察座標�??
		 */
		public NyARIntPoint2d[] pos=NyARIntPoint2d.createArray(3);
		private NyARDoublePoint2d _tmp=new NyARDoublePoint2d();
		private INyARCameraDistortionFactor _ref_df;
		public PatchImagePositions(INyARCameraDistortionFactor i_ref_distortionFactor)
		{
			this._ref_df=i_ref_distortionFactor;
		}
		/**
		 * �?影変換行�?�ログから、�?�補点を計算して、メンバ変数posへ格納する�??
		 * @param i_log
		 * @return
		 * 候補点の数
		 */
		public int makeCandidatePos(NyARSurfaceFeatureItem i_cai,NyARSurfaceTransMatrixSetFifo i_log)
		{
			NyARDoublePoint2d tmp=this._tmp;
			NyARIntPoint2d[] p=this.pos;
			double x=i_cai.x;
			double y=i_cai.y;
			p[0].x=(int)x;
			p[0].y=(int)y;
			int num_of_log=i_log.num_of_item;
			if(num_of_log>=3){
				i_log.items[1].calculate2dPos(i_cai.ref_feature.mx,i_cai.ref_feature.my, tmp);
				this._ref_df.ideal2Observ(tmp, tmp);
				p[1].x = (int) (2 * x - tmp.x);
				p[1].y = (int) (2 * y - tmp.y);
				double x1=tmp.x;
				double y1=tmp.y;
				i_log.items[2].calculate2dPos(i_cai.ref_feature.mx,i_cai.ref_feature.my, tmp);
				this._ref_df.ideal2Observ(tmp, tmp);
				p[2].x = (int) (3 * x - 3 * x1 + tmp.x);
				p[2].y = (int) (3 * y - 3 * y1 + tmp.y);
				
				return 3;
			}else if(num_of_log>=2){
				i_log.items[1].calculate2dPos(i_cai.ref_feature.mx,i_cai.ref_feature.my, tmp);
				this._ref_df.ideal2Observ(tmp, tmp);
				p[1].x = (int) (2 * x - tmp.x);
				p[1].y = (int) (2 * y - tmp.y);
				return 2;
			}
			return 1;
		}
	}
	
	public final static int AR2_DEFAULT_SEARCH_FEATURE_NUM = 10;
	private final static int AR2_DEFAULT_TS1 = 11;
	private final static int AR2_DEFAULT_TS2 = 11;
	private final static double AR2_DEFAULT_SIM_THRESH = 0.6;

	private NyARParam _ref_cparam;
	private double simThresh;
	private NyARSurfaceFeatures _candidate;
	private NyARSurfaceFeatures _candidate2;
	private INyARVisibleFeatureExtractor _feature_selector;
	private int searchFeatureNum;
	public NyARSurfaceTracker(NyARParam i_param_ref,int i_max_search_feature_num) throws NyARException
	{
		this._candidate = new NyARSurfaceFeatures(NyARSurfaceFeatures.AR2_TRACKING_CANDIDATE_MAX + 1);
		this._candidate2 = new NyARSurfaceFeatures(NyARSurfaceFeatures.AR2_TRACKING_CANDIDATE_MAX + 1);
		this._feature_selector = new NyARVisibleFeatureExtractor(i_param_ref.getScreenSize(),i_param_ref.getDistortionFactor());
		this.__pcpoints=new PatchImagePositions(i_param_ref.getDistortionFactor());

		this._ref_cparam = i_param_ref;
		this.searchFeatureNum = AR2_DEFAULT_SEARCH_FEATURE_NUM;
		this.simThresh = AR2_DEFAULT_SIM_THRESH;

		this._ctrans_log =new NyARSurfaceTransMatrixSetFifo(3);
		this._prev_selected_features=new NyARFeatureCoordPtrList(this.searchFeatureNum);
		this.__selected_features=new NyARSurfaceFeaturesPtr(this.searchFeatureNum);
		return;
	}



	/**
	 * トラ�?キング状態をリセ�?トする�??
	 */
	public void resetLog()
	{
		this._ctrans_log.init();
		this._prev_selected_features.clear();
		return;
	}

	private final static double AR2_DEFALUT_TRACKING_SD_THRESH = 5.0;
	private NyARTemplatePatchImage __template_patch=new NyARTemplatePatchImage(AR2_DEFAULT_TS1,AR2_DEFAULT_TS2); 
	private NyARSurfaceFeatureIndexSelector __index_selecter=new NyARSurfaceFeatureIndexSelector();
	private NyARSurfaceFeaturesPtr __selected_features;
	private NyARSurfaceTransMatrixSetFifo _ctrans_log;
	private NyARFeatureCoordPtrList _prev_selected_features;
	private PatchImagePositions __pcpoints;	
	private INyARTemplateMatchingDriver _last_driver;
	private INyARGrayscaleRaster _last_raster=null;
	/**
	 * i_rasiterの画像から�?�i_surfaceにマッチするパターンを検�?�して、その�?想座標と?��次�?座標セ�?トを返す�?
	 * 検�?�した頂点セ�?ト�?�、o_pos2dとo_pos3dへ�?大i_num個�?�力する�??
	 * @param i_raster
	 * 現在の画�?
	 * @param i_surface
	 * 検�?�すべきサーフェイスセ�?�?
	 * @param i_trans
	 * 現在の姿勢変換行�??
	 * @param o_pos2d
	 * 出力パラメータ。画面上�?��?想点�?
	 * オブジェクト�?�配�?�を�?定すること�?
	 * @param o_pos3d
	 * 出力パラメータ。三次�?サーフェイス座標�??
	 * オブジェクト�?�配�?�を�?定すること�?
	 * @param i_num
	 * 返却数。この数値は、コンストラクタに与えた最大数以下である�?要がある。o_pos2dとo_pos3dは、この数値より大きい配�?�でなければならな�?�?
	 * @return
	 * 検�?�した頂点セ�?ト�?�数�?
	 * @throws NyARException
	 */
	public int tracking(INyARGrayscaleRaster i_raster,NyARSurfaceDataSet i_surface,NyARDoubleMatrix44 i_trans,NyARDoublePoint2d[] o_pos2d,NyARDoublePoint3d[] o_pos3d,int i_num) throws NyARException
	{
		//�?ンプレートドライバ�?�更新
		INyARTemplateMatchingDriver tmd;
		if(this._last_raster!=i_raster){
			tmd=this._last_driver=new NyARTemplateMatchingDriver_INT1D(i_raster);
		}else{
			tmd=this._last_driver;
		}
		//�?影変換行�?��?�計算とログへの追�?
		NyARSurfaceTransMatrixSet tlog=this._ctrans_log.preAdd();
		tlog.setValue(this._ref_cparam.getPerspectiveProjectionMatrix(),i_trans);

		
		//可視な候補を選択する�??(�?時リス�?)
		this._feature_selector.extractVisibleFeatures(i_surface.fset,tlog, this._candidate, this._candidate2);
		PatchImagePositions pcpoints=this.__pcpoints;
		
		//load screen size.
		NyARIntSize s = this._ref_cparam.getScreenSize();
		
		//頂点選択クラス類�?�初期�?
		NyARSurfaceFeatureIndexSelector index_selecter=this.__index_selecter;
		NyARSurfaceFeaturesPtr selected_features=this.__selected_features;
		selected_features.clear();
		//�?大返却数の決�?
		int max_feature=i_num>this.__selected_features.getArraySize()?this.__selected_features.getArraySize():i_num;

		int num = 0;
		NyARSurfaceFeatures current_candidate = this._candidate;
		for (int i = max_feature-1; i>=0 ;i--){
			//高精度を優先して探索。なければ低精度に�?り替える。�??替は1度�?け�?��?�力�?�座標集合�??
			int k = index_selecter.ar2SelectTemplate(current_candidate, this._prev_selected_features, selected_features, s);
			if (k<0) {
				if (current_candidate == this._candidate2) {
					break;
				}
				current_candidate = this._candidate2;
				//未選択なら終�?
				k = index_selecter.ar2SelectTemplate(current_candidate, this._prev_selected_features, selected_features, s);
				if (k < 0){
					break;
				}
			}
			//候補kを確�?
			NyARSurfaceFeatureItem cai = current_candidate.getItem(k);

			
			//可視な点につ�?て、トラ�?キングするためのパッチ画像を生�??
			NyARTemplatePatchImage template_ = this.__template_patch;			
			template_.makeFromReferenceImage((int) (cai.x + 0.5), (int) (cai.y + 0.5),tlog.ctrans,this._ref_cparam.getDistortionFactor(),i_surface.iset.items[cai.scale]);

			//パッチ画像�?��?容をチェ�?ク?�?
			if (template_.vlen * template_.vlen >= (template_.xsize) * (template_.ysize) * AR2_DEFALUT_TRACKING_SD_THRESH * AR2_DEFALUT_TRACKING_SD_THRESH)
			{
				//�?影変換行�?�ログから候補点を作る�?
				int number_of_point=pcpoints.makeCandidatePos(cai, this._ctrans_log);			
	
				//画像からテンプレートを検索
				double sim=tmd.ar2GetBestMatching(template_,pcpoints.pos,number_of_point,o_pos2d[num]);
				//類似値が�?定以上なら�?�保�?
				if (sim > this.simThresh) {
					if(selected_features.push(cai)==null){
						break;//�?大値に達したら終わ�?
					}
					this._ref_cparam.getDistortionFactor().observ2Ideal(o_pos2d[num],o_pos2d[num]);
					o_pos3d[num].x= cai.ref_feature.mx;
					o_pos3d[num].y = cai.ref_feature.my;
					o_pos3d[num].z = 0;
					//選択した得量を記録
					num++;
				}
			}
			//選択された候補を取り外す�?
			current_candidate.remove(k);
			
		}
		// 過去ログへ記録
		this._prev_selected_features.clear();
		for (int i = 0; i < selected_features.getLength(); i++) {
			this._prev_selected_features.push(selected_features.getItem(i).ref_feature);
		}
		return num;
	}
}
