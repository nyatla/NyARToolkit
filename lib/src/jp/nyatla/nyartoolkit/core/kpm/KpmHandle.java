package jp.nyatla.nyartoolkit.core.kpm;

import java.util.List;
import java.util.ArrayList;
import jp.nyatla.nyartoolkit.core.kpm.vision.facade.VisualDatabaseFacade;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class KpmHandle {
	private final static int FREAK_SUB_DIMENSION = 96;
	VisualDatabaseFacade freakMatcher;
	final ARParamLT cparamLT;
	int poseMode;
	int xsize;
	int ysize;
	int procMode;
	int detectedMaxFeature;
	KpmRefData[] refDataSet;
	KpmInputDataSet inDataSet=new KpmInputDataSet();
	KpmResult[] result;
	int resultNum;
	final public static int KpmPose6DOF = 1;
	final public static int KpmPoseHomography = 2;

	final public static int KpmProcFullSize = 1;
	final public static int KpmProcHalfSize = 2;
	final public static int KpmProcQuatSize = 3;
	final public static int KpmProcOneThirdSize = 4;
	final public static int KpmProcTwoThirdSize = 5;
	final public static int KpmDefaultProcMode = KpmProcFullSize;

	public KpmHandle(ARParamLT cparamLT) {
		this(cparamLT, cparamLT.getScreenSize(), KpmPose6DOF);
	}

	public KpmHandle(ARParamLT cparamLT, NyARIntSize size, int poseMode) {
		this.freakMatcher = new VisualDatabaseFacade();

		this.cparamLT = cparamLT;
		this.poseMode = poseMode;
		this.xsize = size.w;
		this.ysize = size.h;
		this.procMode = KpmDefaultProcMode;
		this.detectedMaxFeature = -1;

		this.refDataSet=null;

		this.inDataSet.coord = null;
		this.inDataSet.num = 0;

		this.result = new KpmResult[1];
		this.result[0]=new KpmResult();
		this.resultNum = 0;

	}

	public int kpmMatching(INyARGrayscaleRaster inImage) {

		int procMode;
		FeatureVector featureVector = new FeatureVector();
		int i, j;
		int ret;

		procMode = this.procMode;

		// if (procMode == KpmProcFullSize && (kpmHandle->pixFormat ==
		// AR_PIXEL_FORMAT_MONO || kpmHandle->pixFormat == AR_PIXEL_FORMAT_420v
		// || kpmHandle->pixFormat == AR_PIXEL_FORMAT_420f ||
		// kpmHandle->pixFormat == AR_PIXEL_FORMAT_NV21)) {
		// inImageBW = inImage;
		// } else {
		// inImageBW = kpmUtilGenBWImage( inImage, kpmHandle->pixFormat, xsize,
		// ysize, procMode, &xsize2, &ysize2 );
		// if( inImageBW == NULL ) return -1;
		// }

		// #if BINARY_FEATURE
		// kpmHandle->freakMatcherOpencv->query(inImageBW, xsize ,ysize);
		this.freakMatcher.query(inImage);
		this.inDataSet.num = featureVector.num = (int) this.freakMatcher
				.getQueryFeaturePoints().getLength();
		/*
		 * #else surfSubExtractFeaturePoint( kpmHandle->surfHandle, inImageBW,
		 * kpmHandle->skipRegion.region, kpmHandle->skipRegion.regionNum );
		 * kpmHandle->skipRegion.regionNum = 0; kpmHandle->inDataSet.num =
		 * featureVector.num = surfSubGetFeaturePointNum( kpmHandle->surfHandle
		 * ); #endif
		 */

		if (this.inDataSet.num != 0) {
			// if( this.inDataSet.coord != null ) free(this->inDataSet.coord);
			/*
			 * #if !BINARY_FEATURE if( kpmHandle->preRANSAC.match != NULL )
			 * free(kpmHandle->preRANSAC.match); if( kpmHandle->aftRANSAC.match
			 * != NULL ) free(kpmHandle->aftRANSAC.match); #endif
			 */
			// arMalloc( kpmHandle->inDataSet.coord, KpmCoord2D,
			// kpmHandle->inDataSet.num );
			this.inDataSet.coord = KpmCoord2D.creaeArray(this.inDataSet.num);
			/*
			 * #if !BINARY_FEATURE arMalloc( kpmHandle->preRANSAC.match,
			 * KpmMatchData, kpmHandle->inDataSet.num ); arMalloc(
			 * kpmHandle->aftRANSAC.match, KptmMatchData,
			 * kpmHandle->inDataSet.num ); #endif
			 */
			// #if BINARY_FEATURE
			// arMalloc( featureVector.sf, FreakFeature,
			// kpmHandle->inDataSet.num );
			featureVector.sf = FreakFeature.createArray(this.inDataSet.num);
			/*
			 * #else arMalloc( featureVector.sf, SurfFeature,
			 * kpmHandle->inDataSet.num ); arMalloc( preRANSAC.mp, MatchPoint,
			 * kpmHandle->inDataSet.num ); arMalloc( inlierIndex, int,
			 * kpmHandle->inDataSet.num );
			 * 
			 * knn = 1; arMalloc( annMatch2, int, kpmHandle->inDataSet.num*knn);
			 * #endif
			 */

			// #if BINARY_FEATURE
			FeaturePointStack points = this.freakMatcher
					.getQueryFeaturePoints();
			byte[] descriptors = this.freakMatcher.getQueryDescriptors();
			// #endif
			if (procMode == KpmProcFullSize) {
				for (i = 0; i < this.inDataSet.num; i++) {

					// #if BINARY_FEATURE
					double x = points.getItem(i).x, y = points.getItem(i).y;
					for (j = 0; j < FREAK_SUB_DIMENSION; j++) {
						featureVector.sf[i].v[j] = descriptors[i
								* FREAK_SUB_DIMENSION + j];
					}
					/*
					 * #else float x, y, *desc; surfSubGetFeaturePosition(
					 * kpmHandle->surfHandle, i, &x, &y ); desc =
					 * surfSubGetFeatureDescPtr( kpmHandle->surfHandle, i );
					 * for( j = 0; j < SURF_SUB_DIMENSION; j++ ) {
					 * featureVector.sf[i].v[j] = desc[j]; }
					 * featureVector.sf[i].l = surfSubGetFeatureSign(
					 * kpmHandle->surfHandle, i ); #endif
					 */
					if (this.cparamLT != null) {
						NyARDoublePoint2d tmp = new NyARDoublePoint2d();
						this.cparamLT.arParamIdeal2ObservLTf(x, y, tmp);
						this.inDataSet.coord[i].x =  tmp.x;
						this.inDataSet.coord[i].y =  tmp.y;
					} else {
						this.inDataSet.coord[i].x = x;
						this.inDataSet.coord[i].y = y;
					}
				}
			}
			// else if( procMode == KpmProcTwoThirdSize ) {
			// for( i = 0 ; i < kpmHandle.inDataSet.num; i++ ) {
			// // #if BINARY_FEATURE
			// float x = points[i].x, y = points[i].y;
			// for( j = 0; j < FREAK_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = descriptors[i*FREAK_SUB_DIMENSION+j];
			// }
			// /* #else
			// float x, y, *desc;
			// surfSubGetFeaturePosition( kpmHandle->surfHandle, i, &x, &y );
			// desc = surfSubGetFeatureDescPtr( kpmHandle->surfHandle, i );
			// for( j = 0; j < SURF_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = desc[j];
			// }
			// featureVector.sf[i].l = surfSubGetFeatureSign(
			// kpmHandle->surfHandle, i );
			// #endif*/
			// if( kpmHandle->cparamLT != NULL ) {
			// arParamObserv2IdealLTf( &(kpmHandle->cparamLT->paramLTf), x*1.5f,
			// y*1.5f, &(kpmHandle->inDataSet.coord[i].x),
			// &(kpmHandle->inDataSet.coord[i].y) );
			// }
			// else {
			// kpmHandle->inDataSet.coord[i].x = x*1.5f;
			// kpmHandle->inDataSet.coord[i].y = y*1.5f;
			// }
			// }
			// }
			// else if( procMode == KpmProcHalfSize ) {
			// for( i = 0 ; i < kpmHandle->inDataSet.num; i++ ) {
			// // #if BINARY_FEATURE
			// float x = points[i].x, y = points[i].y;
			// for( j = 0; j < FREAK_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = descriptors[i*FREAK_SUB_DIMENSION+j];
			// }
			// /* #else
			// float x, y, *desc;
			// surfSubGetFeaturePosition( kpmHandle->surfHandle, i, &x, &y );
			// desc = surfSubGetFeatureDescPtr( kpmHandle->surfHandle, i );
			// for( j = 0; j < SURF_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = desc[j];
			// }
			// featureVector.sf[i].l = surfSubGetFeatureSign(
			// kpmHandle->surfHandle, i );
			// #endif*/
			// if( kpmHandle->cparamLT != NULL ) {
			// arParamObserv2IdealLTf( &(kpmHandle->cparamLT->paramLTf), x*2.0f,
			// y*2.0f, &(kpmHandle->inDataSet.coord[i].x),
			// &(kpmHandle->inDataSet.coord[i].y) );
			// }
			// else {
			// kpmHandle->inDataSet.coord[i].x = x*2.0f;
			// kpmHandle->inDataSet.coord[i].y = y*2.0f;
			// }
			// }
			// }
			// else if( procMode == KpmProcOneThirdSize ) {
			// for( i = 0 ; i < kpmHandle->inDataSet.num; i++ ) {
			// // #if BINARY_FEATURE
			// float x = points[i].x, y = points[i].y;
			// for( j = 0; j < FREAK_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = descriptors[i*FREAK_SUB_DIMENSION+j];
			// }
			// /* #else
			// float x, y, *desc;
			// surfSubGetFeaturePosition( kpmHandle->surfHandle, i, &x, &y );
			// desc = surfSubGetFeatureDescPtr( kpmHandle->surfHandle, i );
			// for( j = 0; j < SURF_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = desc[j];
			// }
			// featureVector.sf[i].l = surfSubGetFeatureSign(
			// kpmHandle->surfHandle, i );
			// #endif*/
			// if( kpmHandle->cparamLT != NULL ) {
			// arParamObserv2IdealLTf( &(kpmHandle->cparamLT->paramLTf), x*3.0f,
			// y*3.0f, &(kpmHandle->inDataSet.coord[i].x),
			// &(kpmHandle->inDataSet.coord[i].y) );
			// }
			// else {
			// kpmHandle->inDataSet.coord[i].x = x*3.0f;
			// kpmHandle->inDataSet.coord[i].y = y*3.0f;
			// }
			// }
			// }
			// else { // procMode == KpmProcQuatSize
			// for( i = 0 ; i < kpmHandle->inDataSet.num; i++ ) {
			// // #if BINARY_FEATURE
			// float x = points[i].x, y = points[i].y;
			// for( j = 0; j < FREAK_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = descriptors[i*FREAK_SUB_DIMENSION+j];
			// }
			// /* #else
			// float x, y, *desc;
			// surfSubGetFeaturePosition( kpmHandle->surfHandle, i, &x, &y );
			// desc = surfSubGetFeatureDescPtr( kpmHandle->surfHandle, i );
			// for( j = 0; j < SURF_SUB_DIMENSION; j++ ) {
			// featureVector.sf[i].v[j] = desc[j];
			// }
			// featureVector.sf[i].l = surfSubGetFeatureSign(
			// kpmHandle->surfHandle, i );
			// #endif*/
			// if( kpmHandle->cparamLT != NULL ) {
			// arParamObserv2IdealLTf( &(kpmHandle->cparamLT->paramLTf), x*4.0f,
			// y*4.0f, &(kpmHandle->inDataSet.coord[i].x),
			// &(kpmHandle->inDataSet.coord[i].y) );
			// }
			// else {
			// kpmHandle->inDataSet.coord[i].x = x*4.0f;
			// kpmHandle->inDataSet.coord[i].y = y*4.0f;
			// }
			// }
			// }

			/*
			 * #if !BINARY_FEATURE ann2 = (CAnnMatch2*)kpmHandle->ann2;
			 * ann2->Match(&featureVector, knn, annMatch2); for(int pageLoop =
			 * 0; pageLoop < kpmHandle->resultNum; pageLoop++ ) {
			 * kpmHandle->preRANSAC.num = 0; kpmHandle->aftRANSAC.num = 0;
			 * 
			 * kpmHandle->result[pageLoop].pageNo =
			 * kpmHandle->refDataSet.pageInfo[pageLoop].pageNo;
			 * kpmHandle->result[pageLoop].camPoseF = -1; if(
			 * kpmHandle->result[pageLoop].skipF ) continue;
			 * 
			 * int featureNum = 0; int *annMatch2Ptr = annMatch2; int pageNo =
			 * kpmHandle->refDataSet.pageInfo[pageLoop].pageNo; for( i = 0; i <
			 * kpmHandle->inDataSet.num; i++ ) { for( j = 0; j < knn; j++ ) {
			 * if( *annMatch2Ptr >= 0 &&
			 * kpmHandle->refDataSet.refPoint[*annMatch2Ptr].pageNo == pageNo )
			 * { kpmHandle->preRANSAC.match[featureNum].inIndex = i;
			 * kpmHandle->preRANSAC.match[featureNum].refIndex = *annMatch2Ptr;
			 * preRANSAC.mp[featureNum].x1 = kpmHandle->inDataSet.coord[i].x;
			 * preRANSAC.mp[featureNum].y1 = kpmHandle->inDataSet.coord[i].y;
			 * preRANSAC.mp[featureNum].x2 =
			 * kpmHandle->refDataSet.refPoint[*annMatch2Ptr].coord3D.x;
			 * preRANSAC.mp[featureNum].y2 =
			 * kpmHandle->refDataSet.refPoint[*annMatch2Ptr].coord3D.y;
			 * featureNum++; annMatch2Ptr += knn-j; break; } annMatch2Ptr++; } }
			 * //printf("Page[%d] %d\n", pageLoop, featureNum); preRANSAC.num =
			 * featureNum; if( featureNum < 6 ) continue;
			 * 
			 * if( kpmRansacHomograhyEstimation(&preRANSAC, inlierIndex,
			 * &inlierNum, h) < 0 ) { inlierNum = 0; }
			 * //printf(" --> page[%d] %d  pre:%3d, aft:%3d\n", pageLoop,
			 * kpmHandle->inDataSet.num, preRANSAC.num, inlierNum); if(
			 * inlierNum < 6 ) continue;
			 * 
			 * kpmHandle->preRANSAC.num = preRANSAC.num;
			 * kpmHandle->aftRANSAC.num = inlierNum; for( i = 0; i < inlierNum;
			 * i++ ) { kpmHandle->aftRANSAC.match[i].inIndex =
			 * kpmHandle->preRANSAC.match[inlierIndex[i]].inIndex;
			 * kpmHandle->aftRANSAC.match[i].refIndex =
			 * kpmHandle->preRANSAC.match[inlierIndex[i]].refIndex; }
			 * //printf(" ---> %d %d %d\n", kpmHandle->inDataSet.num,
			 * kpmHandle->preRANSAC.num, kpmHandle->aftRANSAC.num); if(
			 * kpmHandle->poseMode == KpmPose6DOF ) {
			 * //printf("----- Page %d ------\n", pageLoop); ret =
			 * kpmUtilGetPose(kpmHandle->cparamLT, &(kpmHandle->aftRANSAC),
			 * &(kpmHandle->refDataSet), &(kpmHandle->inDataSet),
			 * kpmHandle->result[pageLoop].camPose,
			 * &(kpmHandle->result[pageLoop].error) );
			 * ARLOGi("Pose - %s",arrayToString2
			 * (kpmHandle->result[pageLoop].camPose).c_str());
			 * //printf("----- End. ------\n"); } else { ret =
			 * kpmUtilGetPoseHomography(&(kpmHandle->aftRANSAC),
			 * &(kpmHandle->refDataSet), &(kpmHandle->inDataSet),
			 * kpmHandle->result[pageLoop].camPose,
			 * &(kpmHandle->result[pageLoop].error) ); } if( ret == 0 ) {
			 * kpmHandle->result[pageLoop].camPoseF = 0;
			 * kpmHandle->result[pageLoop].inlierNum = inlierNum;
			 * ARLOGi("Page[%d]  pre:%3d, aft:%3d, error = %f\n", pageLoop,
			 * preRANSAC.num, inlierNum, kpmHandle->result[pageLoop].error); } }
			 * free(annMatch2); #else
			 */
			for (int pageLoop = 0; pageLoop < this.resultNum; pageLoop++) {

//				this.result[pageLoop].pageNo = this.refDataSet[pageLoop].pageNo;
				this.result[pageLoop].camPoseF = -1;
//				if (this.result[pageLoop].skipF)
//					continue;

				matchStack matches = this.freakMatcher.inliers();
				int matched_image_id = this.freakMatcher.matchedId();
				if (matched_image_id < 0)
					continue;

				ret = kpmMatching.kpmUtilGetPose_binary(this.cparamLT, matches,
						this.freakMatcher.get3DFeaturePoints(matched_image_id),
						this.freakMatcher.getQueryFeaturePoints(),
						this.result[pageLoop]);
				// ARLOGi("Pose (freak) - %s",arrayToString2(kpmHandle->result[pageLoop].camPose).c_str());
				if (ret == 0) {
					this.result[pageLoop].camPoseF = 0;
					this.result[pageLoop].inlierNum = (int) matches.getLength();
					// ARLOGi("Page[%d]  pre:%3d, aft:%3d, error = %f\n",
					// pageLoop, (int)matches.getLength(),
					// (int)matches.getLength(),this.result[pageLoop].error);
				}
			}
			// #endif
			// free(featureVector.sf);
			/*
			 * #if !BINARY_FEATURE free(preRANSAC.mp); free(inlierIndex); #endif
			 */
		} else {
			/*
			 * #if !BINARY_FEATURE kpmHandle->preRANSAC.num = 0;
			 * kpmHandle->aftRANSAC.num = 0; #endif
			 */
			for (i = 0; i < this.resultNum; i++) {
				this.result[i].camPoseF = -1;
			}
		}

		for (i = 0; i < this.resultNum; i++) {
			this.result[i].skipF = false;
		}

		// if (inImageBW != inImage) free( inImageBW );

		return 0;
	}
	public int kpmSetRefDataSet(NyARNftFreakFsetFile i_refDataSet)
	{
//	    FeatureVector       featureVector;
//	    this.refDataSet=new KpmRefData[1];
//	    this.refDataSet[0]=i_refDataSet;


//	    this.refDataSet.pageNum = 1;

    	this.resultNum=1;


	    // Create feature vectors.

//        featureVector.num = kpmHandle->refDataSet.num;
        
        int db_id = 0;
        for (int k = 0; k < i_refDataSet.page_info.length; k++) {
            for (int m = 0; m < i_refDataSet.page_info[k].image_info.length; m++)
            {
            	int image_no=i_refDataSet.page_info[k].image_info[m].image_no;
        		List<Point3d> p3dl=new ArrayList<Point3d>();
        		List<FeaturePoint> fpl=new ArrayList<FeaturePoint>();
        		int desc_len=0;
            	for(int i=0;i<i_refDataSet.ref_point.length;i++){
            		if(i_refDataSet.ref_point[i].refImageNo==image_no){
            			NyARNftFreakFsetFile.RefDataSet t=i_refDataSet.ref_point[i];
            			FeaturePoint fp=new FeaturePoint(); 
            			fp.x=t.coord2D.x;
            			fp.y=t.coord2D.y;
            			fp.angle=t.featureVec.angle;
            			fp.scale=t.featureVec.scale;
            			fp.maxima=t.featureVec.maxima>0?true:false;
            			fpl.add(fp);
            			//descripterはサイズだけ先に計算
            			desc_len+=i_refDataSet.ref_point[i].featureVec.v.length;
            			//
            			Point3d point3d=new Point3d();
            			point3d.x=t.coord3D.x;
            			point3d.y=t.coord3D.y;
            			p3dl.add(point3d);
            			
            		}
            	}
            	//descripterの生成
        		byte[] dl=new byte[desc_len];
            	int l=0;
            	for(int i=0;i<i_refDataSet.ref_point.length;i++){
            		if(i_refDataSet.ref_point[i].refImageNo==image_no){
            			for(int j=0;j<i_refDataSet.ref_point[i].featureVec.v.length;j++){
	            			dl[l]=i_refDataSet.ref_point[i].featureVec.v[j];
	            			l++;
            			}
            		}
            	}
            	FeaturePointStack fps=new FeaturePointStack(fpl.size());
            	for(FeaturePoint i:fpl){
            		FeaturePoint p=fps.prePush();
            		p.set(i);
            	}
            	Point3dVector p3v=new Point3dVector(p3dl.size());
            	for(Point3d i:p3dl){
            		Point3d p=p3v.prePush();
            		p.x=i.x;
            		p.y=i.y;
            		p.z=i.z;
            	}
            	this.freakMatcher.addFreakFeaturesAndDescriptors(
            			fps,
            			dl,
                        p3v,
                        i_refDataSet.page_info[k].image_info[m].w,
                        i_refDataSet.page_info[k].image_info[m].h,
                        db_id++);
            }
        }
	    return 0;
	}
}
