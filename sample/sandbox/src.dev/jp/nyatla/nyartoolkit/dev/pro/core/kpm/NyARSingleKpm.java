/* 
 * PROJECT: NyARToolkit Professional
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 * This work is based on the ARToolKit4.
 * Copyright 2010-2011 ARToolworks, Inc. All rights reserved.
 *
 */
package jp.nyatla.nyartoolkit.dev.pro.core.kpm;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.pro.core.integralimage.NyARIntegralImage;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARLinearFeatureSearch;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;
import jp.nyatla.nyartoolkit.pro.core.kpm.hest.INyARHomographyEst;
import jp.nyatla.nyartoolkit.pro.core.kpm.hest.NyARHomographyEst;
import jp.nyatla.nyartoolkit.pro.core.surf.NyARSurf;
import jp.nyatla.nyartoolkit.pro.core.surf.NyARSurfDescriptor;



public class NyARSingleKpm
{
	public final static int AREA_ALL=0;
	public final static int AREA_QLT=1;
	public final static int AREA_QLB=2;
	public final static int AREA_QRT=3;
	public final static int AREA_QRB=4;
	public final static int AREA_QCE=5;
	
	private NyARIntegralImage _int_image;
	private NyARKpmDataSet _ref_dataset;
	private NyARSurfAnnMatch _ann;

	INyARHomographyEst _hest;

	private NyARParam _cparam;

	public NyARSingleKpm(NyARParam i_ref_cparam, NyARKpmDataSet i_ref_dataset) throws NyARRuntimeException
	{
		this._hest = new NyARHomographyEst(MAX_SURF_RESULT);
		this._cparam = i_ref_cparam;
		this._ref_dataset = i_ref_dataset;
		NyARIntSize s = _cparam.getScreenSize();
		this._int_image = new NyARIntegralImage(s.w, s.h);
		assert (i_ref_dataset._featureset.length == 1);
		this._ann = new NyARLinearFeatureSearch(i_ref_dataset._featureset[0]);
		this._updated=false;
		this._surf = new NyARSurf(s, 1, 4,NyARSurf.DEFAULT_SURF_THRESH);
	}

	//
	private static final int MAX_SURF_RESULT = 1000;
	private final static int MAX_SURF_FEATURE = 4000;
	private NyARSurfDescriptor __surf_desc = new NyARSurfDescriptor(MAX_SURF_FEATURE);
	private NyARSurfAnnMatch.Result __match_result = new NyARSurfAnnMatch.Result(MAX_SURF_RESULT);
	private PartialMatchResult __partial_result = new PartialMatchResult(MAX_SURF_RESULT);
	private NyARSurf _surf;
	private INyARGrayscaleRaster _last_input=null;
	private boolean _updated;
	
	/**
	 * マッチしたキーポイントセ�?ト�?��?ち、�?�モグラフィ行�?�を構�?�する�?�に適したポイントセ�?トを返します�??
	 * @param i_region_id
	 * 抽出したキーセ�?�?
	 * @param o_result
	 * @return
	 * @throws NyARRuntimeException
	 */
	public boolean getRansacMatchPoints(int i_region_id,NyARSurfAnnMatch.ResultPtr o_result) throws NyARRuntimeException
	{
		NyARSurfAnnMatch.Result match_result = this.__match_result;// �?大Result数も適�?
		PartialMatchResult partial_result = this.__partial_result;

		// 1ペ�?�ジしか�?らな�?ので、pageFeatureNum=i_resultの数
		int feature_num = match_result.getLength();

		if(i_region_id==AREA_ALL){
			if (feature_num < 6) {
				return false;
			}
			this._hest.ransacEstimation(match_result,o_result);
		}else{
			if(this._updated){
				this.__partial_result.makeQuarters(match_result, this._cparam.getScreenSize());
				this._updated=false;
			}
			NyARSurfAnnMatch.ResultPtr rp=null;
			switch(i_region_id)
			{
			case AREA_QLT:
				rp=partial_result.lt_quarter;
				break;
			case AREA_QLB:
				rp=partial_result.lb_quarter;
				break;
			case AREA_QRT:
				rp=partial_result.rt_quarter;
				break;
			case AREA_QRB:
				rp=partial_result.rb_quarter;
				break;
			case AREA_QCE:
				rp=partial_result.ce_quarter;
				break;
			default:
				throw new NyARRuntimeException();
			}
			if (rp.getLength() < 6) {
				return false;
			}
			this._hest.ransacEstimation(rp,o_result);
		}
		return true;
	}
	/**
	 * i_rasterとi_ref_datasetの間でキーポイント�?�ッチングを実行して、�?致�?報を更新します�??
	 * @param i_raster
	 * @param i_pose
	 * @return
	 * @throws NyARRuntimeException
	 */
	public void updateMatching(INyARGrayscaleRaster i_raster) throws NyARRuntimeException
	{
		//�?要に応じてラスタドライバ�?�再構�?
		if(this._last_input!=i_raster){
			this._surf.surfThresh(this._ref_dataset.surfThresh);
			this._last_input=i_raster;
		}
		this._int_image.genIntegralImage(i_raster);
		
		NyARSurfDescriptor surf_desc = this.__surf_desc;
		// SURF特徴点の取�?(歪み保障付で)
		this._surf.makeDescripter(this._int_image,this._cparam.getDistortionFactor(), surf_desc);

		
		// マッチする特徴点のクエリ・�?ンプレートセ�?トを取�?
		this.__match_result.clear();
		//�?致�?報を記録
		this._ann.match(surf_desc, this.__match_result);
		this._updated=true;
		return;
	}
}

/**
 * 1/4リージョンごとに区�?った部�?�?合�??
 */
class PartialMatchResult {
	public NyARSurfAnnMatch.ResultPtr lt_quarter;
	public NyARSurfAnnMatch.ResultPtr rt_quarter;
	public NyARSurfAnnMatch.ResultPtr lb_quarter;
	public NyARSurfAnnMatch.ResultPtr rb_quarter;
	public NyARSurfAnnMatch.ResultPtr ce_quarter;

	public PartialMatchResult(int i_max_feature) throws NyARRuntimeException {
		this.lt_quarter = new NyARSurfAnnMatch.ResultPtr(i_max_feature);
		this.rt_quarter = new NyARSurfAnnMatch.ResultPtr(i_max_feature);
		this.lb_quarter = new NyARSurfAnnMatch.ResultPtr(i_max_feature);
		this.rb_quarter = new NyARSurfAnnMatch.ResultPtr(i_max_feature);
		this.ce_quarter = new NyARSurfAnnMatch.ResultPtr(i_max_feature);
	}

	public void makeQuarters(NyARSurfAnnMatch.ResultPtr i_source, NyARIntSize i_screen_size) {
		this.lt_quarter.clear();
		this.rt_quarter.clear();
		this.lb_quarter.clear();
		this.rb_quarter.clear();
		this.ce_quarter.clear();
		int cx = i_screen_size.w / 2;
		int cy = i_screen_size.h / 2;
		int qx = i_screen_size.w / 4;
		int qy = i_screen_size.h / 4;
		int qx2 = i_screen_size.w * 3 / 4;
		int qy2 = i_screen_size.h * 3 / 4;

		for (int i = 0; i < i_source.getLength(); i++) {
			NyARSurfAnnMatch.ResultItem match_item = i_source.getItem(i);
			int x1 = (int) (match_item.key.x + 0.5);
			int y1 = (int) (match_item.key.y + 0.5);
			if (x1 < cx) {
				if (y1 < cy) {
					// 左�?
					this.lt_quarter.push(match_item);
				} else {
					// 左�?
					this.lb_quarter.push(match_item);
				}
			} else {
				if (y1 < cy) {
					// 右�?
					this.rt_quarter.push(match_item);
				} else {
					// 右�?
					this.rb_quarter.push(match_item);
				}
			}
			// 真ん中
			if (x1 > qx && x1 < qx2 && y1 > qy && y1 < qy2) {
				this.ce_quarter.push(match_item);
			}
		}
	}
}

