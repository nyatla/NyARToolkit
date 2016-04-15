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

package jp.nyatla.nyartoolkit.base.attoolkit5.ar2;
/**
 * AR2HandleをベースにしたSurfaceTrckingのレファレンス実装
 * 
 * 4.x->5.x変更点
 * ar2GetBestMatchingSubFine
 * 各種定数パラメタ
 * ARParamLT参照箇所
 * AR2TemplateTからwimgを削除してsumとvalidNumを追加
 * ar2GenTemplateSub関数でAR2TemplateTのパラメタ初期化を追加
 * ar2ScreenCoord2MarkerCoordに条件分岐追加
 * ar2GetBestMatchingSubFine
 */
import java.io.FileInputStream;

import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.core.icp.NyARIcp;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPointRobust;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftDataSet;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile.NyAR2FeatureCoord;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile.NyAR2FeaturePoints;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceDataSet;

import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;






public class AR2Handle
{
	class AR2TemplateCandidate extends NyARObjectStack<AR2TemplateCandidate.Item>
	{
		public final static int AR2_TRACKING_CANDIDATE_MAX=200;
		public class Item{
		    int     level;
		    int     num;
		    int     flag;
		    double   sx, sy;
			public void setValue(Item item)
			{
				this.level=item.level;
				this.num=item.num;
				this.flag=item.flag;
				this.sx=item.sx;
				this.sy=item.sy;
			}
		};
		public AR2TemplateCandidate(int i_length)
		{
			super(i_length,AR2TemplateCandidate.Item.class);
		}
		/**
		 * この関数は、配列要素を作成します。
		 */	
		protected AR2TemplateCandidate.Item createElement()
		{
			return new AR2TemplateCandidate.Item();
		}
		
		
	}	
	int                   contNum;
	public NyARDoubleMatrix44 trans1=new NyARDoubleMatrix44();
	public NyARDoubleMatrix44 trans2=new NyARDoubleMatrix44();
	public NyARDoubleMatrix44 trans3=new NyARDoubleMatrix44();	
	AR2TemplateCandidate prevFeature;
	class AR2TemplateT
	{
	    int          xsize, ysize;      /* template size         */
	    int          xts1, xts2;        /* template size         */
	    int          yts1, yts2;        /* template size         */
	    int[]          img1;              /* template for mode 0   */
	    int          vlen;              /* length of vector *img */
	/*--- working memory ---*/
	    int          sum;
	    int          validNum;
	    public AR2TemplateT(int ts1,int ts2)
	    {
	    	this.xts1 = this.yts1 = ts1;
	    	this.xts2 = this.yts2 = ts2;
	    	
	    	int xsize,ysize;
	    	this.xsize = xsize = this.xts1 + this.xts2 + 1;
	    	this.ysize = ysize = this.yts1 + this.yts2 + 1;
	    	this.img1=new int[xsize*ysize];
	    }
	}




	class AR2Tracking2DResultT{
	    double             sim;
	    double[]           pos2d=new double[2];
	    double[]           pos3d=new double[3];
	};
	
	/**
	 * 
	 * @param cparam
	 * @param trans
	 * @param pos
	 * [2]
	 * @param dpi
	 * [2]
	 * @return
	 */
	static int ar2GetResolution(NyARParam cparam,NyARDoubleMatrix44 trans, double[]  pos, double[] dpi)
	{
	    NyARDoubleMatrix44 mat=new NyARDoubleMatrix44();
	    double   mx, my, hx, hy, h;
	    double   x0, y0, x1, y1, x2, y2, d1, d2;

	    mat.mul(cparam.getPerspectiveProjectionMatrix(),trans);
//	    arUtilMatMuldff(cparam->mat, trans, mat );

	    mx = pos[0];
	    my = pos[1];
	    hx = mat.m00 * mx + mat.m01 * my + mat.m03;
	    hy = mat.m10 * mx + mat.m11 * my + mat.m13;
	    h  = mat.m20 * mx + mat.m21 * my + mat.m23;
	    x0 = hx / h;
	    y0 = hy / h;

	    mx = pos[0] + 10.0f;
	    my = pos[1];
	    hx = mat.m00 * mx + mat.m01 * my + mat.m03;
	    hy = mat.m10 * mx + mat.m11 * my + mat.m13;
	    h  = mat.m20 * mx + mat.m21 * my + mat.m23;
	    x1 = hx / h;
	    y1 = hy / h;

	    mx = pos[0];
	    my = pos[1] + 10.0f;
	    hx = mat.m00 * mx + mat.m01 * my + mat.m03;
	    hy = mat.m10 * mx + mat.m11 * my + mat.m13;
	    h  = mat.m20 * mx + mat.m21 * my + mat.m23;
	    x2 = hx / h;
	    y2 = hy / h;

	    d1 = (x1-x0)*(x1-x0) + (y1-y0)*(y1-y0);
	    d2 = (x2-x0)*(x2-x0) + (y2-y0)*(y2-y0);
	    if( d1 < d2 ) {
	        dpi[0] = Math.sqrt(d2) * 2.54f;
	        dpi[1] = Math.sqrt(d1) * 2.54f;
	    }
	    else {
	        dpi[0] = Math.sqrt(d1) * 2.54f;
	        dpi[1] = Math.sqrt(d2) * 2.54f;
	    }

	    return 0;
	}		
	
	
	
	public final static int AR2_SEARCH_FEATURE_MAX=40;
	private final static int AR2_DEFAULT_SEARCH_SIZE=25;
	private final static int AR2_DEFAULT_SEARCH_FEATURE_NUM=10;
	private final static int AR2_DEFAULT_TS1=11;
	private final static int AR2_DEFAULT_TS2=11;
	private final static double AR2_DEFAULT_SIM_THRESH=0.6;
	private final static double AR2_DEFAULT_TRACKING_THRESH=2.0;
	//
	//<struct AR2Handle_T>
	private ARParamLT _cparam;
    private NyARIcpPoint _icp;
    private NyARIcpPointRobust _icp_r;
    int               searchSize;
    int               templateSize1;
    int               templateSize2;
    int               searchFeatureNum;
    double            simThresh;
    double            trackingThresh;
    /*--------------------------------*/
    NyARDoubleMatrix44 wtrans1=new NyARDoubleMatrix44();
    NyARDoubleMatrix44 wtrans2=new NyARDoubleMatrix44();
    NyARDoubleMatrix44 wtrans3=new NyARDoubleMatrix44();
    double[][]           pos=new double[AR2_SEARCH_FEATURE_MAX][2];
    NyARDoublePoint2d[]          pos2d=NyARDoublePoint2d.createArray(AR2_SEARCH_FEATURE_MAX);
    double[][]           pos3d=new double[AR2_SEARCH_FEATURE_MAX][3];
    AR2TemplateCandidate candidate;
    AR2TemplateCandidate candidate2;
    AR2TemplateCandidate usedFeature;
	//<struct AR2Handle_T>    
    private INyARGrayscaleRaster mfImage;
    private NyARSurfaceDataSet _surfaceset;
    public AR2Handle(NyARParam i_param_ref,NyARNftIsetFile i_iset,NyARNftFsetFile i_fset)
	{
    	this._surfaceset=new NyARSurfaceDataSet(i_iset,i_fset);
        candidate=new AR2TemplateCandidate(AR2TemplateCandidate.AR2_TRACKING_CANDIDATE_MAX+1);
        candidate2=new AR2TemplateCandidate(AR2TemplateCandidate.AR2_TRACKING_CANDIDATE_MAX+1);
        usedFeature=new AR2TemplateCandidate(AR2TemplateCandidate.AR2_TRACKING_CANDIDATE_MAX+1);
     
    	this._cparam=new ARParamLT(i_param_ref);
//		this->cparam=new ARParam();
//		*(this->cparam)         = *cparam;
    	this._icp=new NyARIcpPoint(i_param_ref.getPerspectiveProjectionMatrix());
    	this._icp_r=new NyARIcpPointRobust(i_param_ref.getPerspectiveProjectionMatrix());
    	this._icp.setInlierProbability(0);
//		this->icpHandle         = icpCreateHandle( cparam->mat );
//		icpSetInlierProbability( this->icpHandle, 0.0 );
//		this->pixFormat         = pixFormat;
		this.searchSize        = AR2_DEFAULT_SEARCH_SIZE;
		this.templateSize1     = AR2_DEFAULT_TS1;
		this.templateSize2     = AR2_DEFAULT_TS2;
		this.searchFeatureNum  = AR2_DEFAULT_SEARCH_FEATURE_NUM;
		if( this.searchFeatureNum > AR2_SEARCH_FEATURE_MAX ) {
			this.searchFeatureNum = AR2_SEARCH_FEATURE_MAX;
		}
		this.simThresh         = AR2_DEFAULT_SIM_THRESH;
		this.trackingThresh    = AR2_DEFAULT_TRACKING_THRESH;

		NyARIntSize s=this._cparam.getScreenSize();
		this.mfImage=NyARGrayscaleRaster.createInstance(s.w,s.h);
		this.prevFeature=new AR2TemplateCandidate(AR2_SEARCH_FEATURE_MAX+1);	

		return;   	
	}
    public void setInitialTransmat(NyARDoubleMatrix44 i_initial_mat)
    {
//        int    i, j;
        this.contNum = 1;
		this.trans1.setValue(i_initial_mat);
//		this._surfaceset.prevFeature = -1;
		this.prevFeature.clear();
        return;
    }    
    
    
    
    
    void ar2GetSearchPoint(ARParamLT cparam,NyARDoubleMatrix44 trans1,NyARDoubleMatrix44 trans2,NyARDoubleMatrix44 trans3,
    		 NyAR2FeatureCoord feature,int[][] search)
	{
	double    ox1, ox2, ox3;
	double    oy1, oy2, oy3;
	
	if( trans1 == null ) {
		search[0][0] = -1;
		search[0][1] = -1;
		search[1][0] = -1;
		search[1][1] = -1;
		search[2][0] = -1;
		search[2][1] = -1;
		return;
	}
	double mx = feature.mx;
	double my = feature.my;
	NyARDoublePoint2d o1=new NyARDoublePoint2d();
	ar2MarkerCoord2ScreenCoord( cparam, trans1, mx, my,o1);
	search[0][0] = (int)o1.x;
	search[0][1] = (int)o1.y;
	
	if( trans2 == null ) {
		search[1][0] = -1;
		search[1][1] = -1;
		search[2][0] = -1;
		search[2][1] = -1;
		return;
	}
	NyARDoublePoint2d o2=new NyARDoublePoint2d();
	
	ar2MarkerCoord2ScreenCoord( cparam, trans2, mx, my,o2 );
	search[1][0] = (int)(2*o1.x - o2.x);
	search[1][1] = (int)(2*o1.y - o2.y);
	
	if( trans3 == null) {
		search[2][0] = -1;
		search[2][1] = -1;
		return;
	}
	
	NyARDoublePoint2d o3=new NyARDoublePoint2d();
	ar2MarkerCoord2ScreenCoord( cparam, trans3, mx, my, o3);
	search[2][0] = (int)(3*o1.x - 3*o2.x + o3.x);
	search[2][1] = (int)(3*o1.y - 3*o2.y + o3.y);
	
	return;
	}    
    
    
	static int extractVisibleFeatures(ARParamLT cparam,NyARDoubleMatrix44 trans1, NyARSurfaceDataSet surfaceSet,
			  AR2TemplateCandidate candidate,
			  AR2TemplateCandidate candidate2)
	{
		NyARDoubleMatrix44 trans2=new NyARDoubleMatrix44();
//		double      sx, sy;
		double[]    wpos=new double[2];
		double[]	w=new double[2];
		double[]    vdir=new double[3];
		double vlen;
		int         xsize, ysize;
		int         i, j, k, l, l2;
		candidate.clear();
		candidate2.clear();

		xsize = cparam.getScreenSize().w;
		ysize = cparam.getScreenSize().h;

		l = l2 = 0;
//		for( i = 0; i < surfaceSet.surface.length; i++ )
//		{
			trans2.setValue(trans1);
//			for(j=0;j<3;j++){
//				for(k=0;k<4;k++){
//					trans2[j][k] = trans1[i][j][k];
//				}
//			}
//		}

		for( j = 0; j < surfaceSet.fset.list.length; j++ )
		{
			for( k = 0; k < surfaceSet.fset.list[j].coord.length; k++ )
			{
				NyARDoublePoint2d s=new NyARDoublePoint2d();
				if(ar2MarkerCoord2ScreenCoord2(
					cparam, trans2,surfaceSet.fset.list[j].coord[k].mx,
					surfaceSet.fset.list[j].coord[k].my,
							 s) < 0 ){
					continue;
				}
				if( s.x < 0 || s.x >= xsize ){
					continue;
				}
				if( s.y < 0 || s.y >= ysize ) 
				{
					continue;
				}
				
				vdir[0] = trans2.m00 * surfaceSet.fset.list[j].coord[k].mx+ trans2.m01 * surfaceSet.fset.list[j].coord[k].my+ trans2.m03;
				vdir[1] = trans2.m10 * surfaceSet.fset.list[j].coord[k].mx+ trans2.m11 * surfaceSet.fset.list[j].coord[k].my+ trans2.m13;
				vdir[2] = trans2.m20 * surfaceSet.fset.list[j].coord[k].mx+ trans2.m21 * surfaceSet.fset.list[j].coord[k].my+ trans2.m23;
				vlen = Math.sqrt( vdir[0]*vdir[0] + vdir[1]*vdir[1] + vdir[2]*vdir[2] );

				/*
				vdir[0] = trans2[0][0] * surfaceSet.surface[i].featureSet.list[j].coord[k].mx
	+ trans2[0][1] * surfaceSet.surface[i].featureSet.list[j].coord[k].my
	+ trans2[0][3];
vdir[1] = trans2[1][0] * surfaceSet.surface[i].featureSet.list[j].coord[k].mx
	+ trans2[1][1] * surfaceSet.surface[i].featureSet.list[j].coord[k].my
	+ trans2[1][3];
vdir[2] = trans2[2][0] * surfaceSet.surface[i].featureSet.list[j].coord[k].mx
	+ trans2[2][1] * surfaceSet.surface[i].featureSet.list[j].coord[k].my
	+ trans2[2][3];
vlen = sqrtf( vdir[0]*vdir[0] + vdir[1]*vdir[1] + vdir[2]*vdir[2] );
*/
				vdir[0] /= vlen;
				vdir[1] /= vlen;
				vdir[2] /= vlen;
				if( vdir[0]*trans2.m02 + vdir[1]*trans2.m12 + vdir[2]*trans2.m22 > -0.1 ){
					continue;
				}
				wpos[0] = surfaceSet.fset.list[j].coord[k].mx;
				wpos[1] = surfaceSet.fset.list[j].coord[k].my;
				ar2GetResolution( cparam, trans2, wpos, w );
				//if( w[0] <= surfaceSet->surface[i].featureSet->list[j].maxdpi
				// && w[0] >= surfaceSet->surface[i].featureSet->list[j].mindpi ) {
				if( w[1] <= surfaceSet.fset.list[j].maxdpi && w[1] >= surfaceSet.fset.list[j].mindpi )
				{
					AR2TemplateCandidate.Item item=candidate.prePush();
					if(item!=null){
//						item.snum  = 0;//=i;
						item.level = j;
						item.num   = k;
						item.sx    = s.x;
						item.sy    = s.y;
						item.flag  = 0;						
					}
//					if( l == AR2_TRACKING_CANDIDATE_MAX ) {
//	//					ARLOGe("### Feature candidates for tracking are overflow.\n");
//						candidate[l].flag = -1;
//						return -1;
//					}
//					candidate[l].snum  = i;
//					candidate[l].level = j;
//					candidate[l].num   = k;
//					candidate[l].sx    = sx;
//					candidate[l].sy    = sy;
//					candidate[l].flag  = 0;
//					l++;
				}else if( w[1] <= surfaceSet.fset.list[j].maxdpi*2 && w[1] >= surfaceSet.fset.list[j].mindpi/2 )
				{
					AR2TemplateCandidate.Item item=candidate2.prePush();
					if(item!=null){
//						item.snum  = 0;//=i;
						item.level = j;
						item.num   = k;
						item.sx    = s.x;
						item.sy    = s.y;
						item.flag  = 0;
//						l2++;
					}
				}
			}
		}
//		candidate[l].flag = -1;
//		candidate2[l2].flag = -1;
//		
		return 0;
	}
	class RepeatedRandomizer extends NyARLCGsRandomizer
	{
		private int _loop_max;
		private int _counter;
		public RepeatedRandomizer(int i_seed,int i_loop_max)
		{
			super(i_seed);
			this._counter=0;
			this._loop_max=i_loop_max;
		
		}
		public int rand(){
			int ret=super.rand();
			this._counter++;
			if(this._counter>=this._loop_max){
				this._rand_val=this._seed;
				this._counter=0;
			}
			return ret;
		}
	}
	
	RepeatedRandomizer _rand=new RepeatedRandomizer(0,128);
	static int ar2GetVectorAngle( double[]  p1, double[]  p2, SinCos o_sincos)
	{
	    double    l;

	    l = Math.sqrt( (p2[0]-p1[0])*(p2[0]-p1[0]) + (p2[1]-p1[1])*(p2[1]-p1[1]) );
	    if( l == 0.0f ) return -1;

	    o_sincos.sin = (p2[1] - p1[1]) / l;
	    o_sincos.cos = (p2[0] - p1[0]) / l;
	    return 0;
	}
	class SinCos
	{
		double sin;
		double cos;
	}
	static double  ar2GetTriangleArea( double[]  p1, double[]  p2, double[]  p3 )
	{
	    double   x1, y1, x2, y2;
	    double   s;

	    x1 = p2[0] - p1[0];
	    y1 = p2[1] - p1[1];
	    x2 = p3[0] - p1[0];
	    y2 = p3[1] - p1[1];

	    s = (x1 * y2 - x2 * y1) / 2.0f;
	    if( s < 0.0f ) s = -s;

	    return s;
	}
	
	/**
	 * pos[4][2]
	 * @return
	 */
	static double  ar2GetRegionArea( double[][] pos, int q1, int r1, int r2 )
	{
	    double     s;

	    s  = ar2GetTriangleArea( pos[0], pos[q1], pos[r1] );
	    s += ar2GetTriangleArea( pos[0], pos[r1], pos[r2] );

	    return s;
	}	
	/**
	int ar2SelectTemplate( AR2TemplateCandidateT *candidate, AR2TemplateCandidateT *prevFeature, int num,
            double  pos[4][2], int xsize, int ysize )
	 * 
	 * @return
	 */
	int ar2SelectTemplate(AR2TemplateCandidate candidate, AR2TemplateCandidate prevFeature, int num,double[][] pos, int xsize, int ysize )
	{
		assert(num>=0);
		if( num == 0 ) {
			int j=-1;
			double dmax = 0.0f;
			
			for(int i = 0;i<candidate.getLength(); i++ ) {
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if(item.flag != 0 ){
					continue;
				}
				if( item.sx < xsize/8 || item.sx > xsize*7/8 || item.sy < ysize/8 || item.sy > ysize*7/8 ){
					continue;
				}
				double d = (item.sx - xsize/2)*(item.sx - xsize/2)+ (item.sy - ysize/2)*(item.sy - ysize/2);
				if( d > dmax )
				{
					dmax = d; j = i;
				}
			}
			if( j != -1 ){
				candidate.getItem(j).flag=1;
			}
			return j;
		}else if( num == 1 ){
			double dmax=0;
			int j = -1;
			for(int i = 0;i<candidate.getLength(); i++){
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if(item.flag != 0 ){
					continue;
				}
				if( item.sx < xsize/8 || item.sx > xsize*7/8 || item.sy < ysize/8 || item.sy > ysize*7/8 ){
					continue;
				}
				double d = (item.sx - pos[0][0])*(item.sx - pos[0][0])+ (item.sy - pos[0][1])*(item.sy - pos[0][1]);
				if( d > dmax ){
					dmax = d; j = i;
				}
			}
			if( j != -1 ){
				candidate.getItem(j).flag=1;
			}
			return j;
		}else if( num == 2 ) {
			double dmax = 0.0f;
			int j = -1;
			for(int i = 0;i<candidate.getLength(); i++){
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if(item.flag != 0 ){
					continue;
				}
				if(item.sx < xsize/8 || item.sx > xsize*7/8 || item.sy < ysize/8 || item.sy > ysize*7/8 ){
					continue;
				}
				double d = ((item.sx - pos[0][0])*(pos[1][1] - pos[0][1]) - (item.sy - pos[0][1])*(pos[1][0] - pos[0][0]));
				d = d * d;
				if( d > dmax ){
					dmax = d; j = i;
				}
			}
			if( j != -1 ){
				candidate.getItem(j).flag=1;
			}
			return j;
		}else if( num == 3 ){
//			double  p2sinf, p2cosf, p3sinf, p3cosf, p4sinf, p4cosf;
			double  smax, s;
			int    q1, r1, r2;
			int    i, j;
			SinCos p2sincos=new SinCos();
			SinCos p3sincos=new SinCos();
			SinCos p4sincos=new SinCos();
			ar2GetVectorAngle(pos[0], pos[1],p2sincos);
			ar2GetVectorAngle(pos[0], pos[2],p3sincos);

			j = -1;
			smax = 0.0f;
			for( i = 0;i<candidate.getLength(); i++ )
			{
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if( item.flag != 0 ){
					continue;
				}
				if( item.sx < xsize/8 || item.sx > xsize*7/8  || item.sy < ysize/8 || item.sy > ysize*7/8 ){
					continue;
				}			
				 pos[3][0] = item.sx;
				 pos[3][1] = item.sy;
				 ar2GetVectorAngle(pos[0], pos[3],p4sincos);
				 if( ((p3sincos.sin*p2sincos.cos - p3sincos.cos*p2sincos.sin) >= 0.0f) && ((p4sincos.sin*p2sincos.cos - p4sincos.cos*p2sincos.sin) >= 0.0f) ) {
					 if( p4sincos.sin*p3sincos.cos - p4sincos.cos*p3sincos.sin >= 0.0f ) {
//			 if( ((p3sinf*p2cosf - p3cosf*p2sinf) >= 0.0f) && ((p4sinf*p2cosf - p4cosf*p2sinf) >= 0.0f) ) {
//					     if( p4sinf*p3cosf - p4cosf*p3sinf >= 0.0f ) {
						 q1 = 1; r1 = 2; r2 = 3;
					 }
					 else {
						 q1 = 1; r1 = 3; r2 = 2;
					 }
				 }else if( ((p4sincos.sin*p3sincos.cos - p4sincos.cos*p3sincos.sin) >= 0.0f) && ((p2sincos.sin*p3sincos.cos - p2sincos.cos*p3sincos.sin) >= 0.0f) ) {
				     if( p4sincos.sin*p2sincos.cos - p4sincos.cos*p2sincos.sin >= 0.0f ) {
				         q1 = 2; r1 = 1; r2 = 3;
				     }
//				 }else if( ((p4sinf*p3cosf - p4cosf*p3sinf) >= 0.0f) && ((p2sinf*p3cosf - p2cosf*p3sinf) >= 0.0f) ) {
//				     if( p4sinf*p2cosf - p4cosf*p2sinf >= 0.0f ) {
//				         q1 = 2; r1 = 1; r2 = 3;
//				     }
				     
			     else {
			         q1 = 2; r1 = 3; r2 = 1;
			     }
			 }else if( ((p2sincos.sin*p4sincos.cos - p2sincos.cos*p4sincos.sin) >= 0.0f) && ((p3sincos.sin*p4sincos.cos - p3sincos.cos*p4sincos.sin) >= 0.0f) ) {
			     if( p3sincos.sin*p2sincos.cos - p3sincos.cos*p2sincos.sin >= 0.0f ) {
			         q1 = 3; r1 = 1; r2 = 2;
			     }
//			 }else if( ((p2sinf*p4cosf - p2cosf*p4sinf) >= 0.0f) && ((p3sinf*p4cosf - p3cosf*p4sinf) >= 0.0f) ) {
//			     if( p3sinf*p2cosf - p3cosf*p2sinf >= 0.0f ) {
//			         q1 = 3; r1 = 1; r2 = 2;
//			     }			     
			     else {
			         q1 = 3; r1 = 2; r2 = 1;
			     }
			 }else{
				 continue;
			 }
/*
			 if( ((p3sinf*p2cosf - p3cosf*p2sinf) >= 0.0f) && ((p4sinf*p2cosf - p4cosf*p2sinf) >= 0.0f) ) {
			     if( p4sinf*p3cosf - p4cosf*p3sinf >= 0.0f ) {
			         q1 = 1; r1 = 2; r2 = 3;
			     }
			     else {
			         q1 = 1; r1 = 3; r2 = 2;
			     }
			 }else if( ((p4sinf*p3cosf - p4cosf*p3sinf) >= 0.0f) && ((p2sinf*p3cosf - p2cosf*p3sinf) >= 0.0f) ) {
			     if( p4sinf*p2cosf - p4cosf*p2sinf >= 0.0f ) {
			         q1 = 2; r1 = 1; r2 = 3;
			     }
			     else {
			         q1 = 2; r1 = 3; r2 = 1;
			     }
			 }else if( ((p2sinf*p4cosf - p2cosf*p4sinf) >= 0.0f) && ((p3sinf*p4cosf - p3cosf*p4sinf) >= 0.0f) ) {
			     if( p3sinf*p2cosf - p3cosf*p2sinf >= 0.0f ) {
			         q1 = 3; r1 = 1; r2 = 2;
			     }
			     else {
			         q1 = 3; r1 = 2; r2 = 1;
			     }
			 }else{
				 continue;
			 }
 */			 
			 s = ar2GetRegionArea( pos, q1, r1, r2 );
			 if( s > smax ){
				 smax = s; j = i;
				 
			 }
			}
			if(j!=-1){
				candidate.getItem(j).flag=1;				
			}
			return j;
		}else{
			int j;
//			static  int s = 0;
			
			for(int i = 0; i<prevFeature.getLength(); i++ )
			{
				AR2TemplateCandidate.Item prev_item=prevFeature.getItem(i);
				if(prev_item.flag != 0 ){
					continue;
				}
				prev_item.flag = 1;
				for( j = 0; j<candidate.getLength(); j++ ) {
					AR2TemplateCandidate.Item item=candidate.getItem(j);
					if( item.flag == 0 /*&& prev_item.snum == item.snum*/ && prev_item.level == item.level && prev_item.num == item.num ){
						if(item.flag != -1 ) {
							item.flag = 1;
							return j;
						}
					}
				}
			}
//			prevFeature.clear();

//			prevFeature[0].flag = -1;
			
//			if( s == 0 ) 
//			{
//				srand(0);
//			}
//			//if( s == 0 ) srand((unsigned int)time(NULL));
//			s++;
//			if( s == 128 ){
//				s = 0;
//			}
	
			//candidateの0な数を数えてる
			for(int i = j = 0; i<candidate.getLength(); i++ ) {
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if( item.flag == 0 ){
					j++;
				}
			}
			if( j == 0 ){
				return -1;
			}
	
			//0なcandidateのうち、k番目のものを探して1にしてる？
			int k = (int)((double )j * this._rand.rand() / (RepeatedRandomizer.RAND_MAX + 1.0f));
			for(int i = j = 0; i<candidate.getLength(); i++ ) {
				AR2TemplateCandidate.Item item=candidate.getItem(i);
				if( item.flag != 0 ){
					continue;
				}
				if( j == k ) {
					item.flag = 1;
					return i;
				}
				j++;
			}
			return -1;
		}
	}
	static int call=0;
	public boolean ar2Tracking(INyARGrayscaleRaster i_raster,NyARDoubleMatrix44 trans)
	{
		call++;
		int                     num;
		int                     i, j, k;
		NyARSurfaceDataSet surfaceSet=this._surfaceset;
		double err=0;
//		*err = 0.0f;

		this.wtrans1.setValue(this.trans1);
		if( this.contNum > 1 ){
			this.wtrans1.setValue(this.trans2);
		}
		if( this.contNum > 2 ){
			this.wtrans1.setValue(this.trans3);
		}
//		for( i = 0; i < surfaceSet.iset.length; i++ ) {
//			arUtilMatMulf( surfaceSet.trans1, surfaceSet.surface[i].trans, this->wtrans1[i] );
//		
//			if( surfaceSet.contNum > 1 ){
//				arUtilMatMulf( surfaceSet.trans2, surfaceSet.surface[i].trans, this->wtrans2[i] );
//			}
//			if( surfaceSet.contNum > 2 ){
//				arUtilMatMulf( surfaceSet.trans3, surfaceSet.surface[i].trans, this->wtrans3[i] );
//			}
//		}

		
		extractVisibleFeatures(
				this._cparam,
				this.wtrans1, 
				surfaceSet, this.candidate, this.candidate2);

		AR2TemplateCandidate candidatePtr = this.candidate;
		NyARIntSize s=this._cparam.getScreenSize();

		num = 0;

		for( i = 0; i < this.searchFeatureNum; i++ ) {

			k = ar2SelectTemplate( candidatePtr, this.prevFeature, num, this.pos,s.w,s.h);
			if( k < 0 ) {
				if( candidatePtr == this.candidate ) {
					candidatePtr = this.candidate2;
					//ポインタでなんかやっとるわ。
					k = ar2SelectTemplate( candidatePtr, this.prevFeature, num, this.pos,s.w,s.h);
					if(k<0){
						break;
					}
				}
				else{
					break;
				}
			}
			AR2Tracking2DResultT    result=new AR2Tracking2DResultT();
			if(this.ar2Tracking2d(surfaceSet,candidatePtr.getItem(k), i_raster, result ) < 0 )
			{
				continue;
			}



			if( result.sim > this.simThresh )
			{
				this._cparam.arParamObserv2IdealLTf(result.pos2d[0],result.pos2d[1], this.pos2d[num]);

//				arParamObserv2Ideal(this->cparam->dist_factor, result.pos2d[0],  result.pos2d[1],
//									&this->pos2d[num][0], &this->pos2d[num][1], this->cparam->dist_function_version);
				this.pos3d[num][0] = result.pos3d[0];
				this.pos3d[num][1] = result.pos3d[1];
				this.pos3d[num][2] = result.pos3d[2];
				this.pos[num][0] = candidatePtr.getItem(k).sx;
				this.pos[num][1] = candidatePtr.getItem(k).sy;
//				this.usedFeature.getItem(num).snum  = candidatePtr.getItem(k).snum;
				this.usedFeature.getItem(num).level = candidatePtr.getItem(k).level;
				this.usedFeature.getItem(num).num   = candidatePtr.getItem(k).num;
				this.usedFeature.getItem(num).flag = 0;

				num++;
			}
		}
		this.prevFeature.clear();		
		for( i = 0; i < num; i++ ){
			AR2TemplateCandidate.Item item=this.prevFeature.prePush();
			item.setValue(this.usedFeature.getItem(i));
		}
//		surfaceSet.prevFeature.getItem(num).flag = -1;


		if( num < 3 ) {
			this.contNum = 0;
			return false;
		}
		err = ar2GetTransMat(this._icp, this.trans1, this.pos2d, this.pos3d, num, trans);
//CheckPoint(20160307)
//		System.out.println(err==2.1303838027020645);
	

//
		if(err > this.trackingThresh) {
			this._icp_r.setInlierProbability(0.8);
			this._icp.setInlierProbability(0.8);
			err = ar2GetTransMat(this._icp_r,trans, this.pos2d, this.pos3d, num, trans);

			if( err > this.trackingThresh ){
				this._icp_r.setInlierProbability(0.6);
				this._icp.setInlierProbability(0.6);
				err = ar2GetTransMat(this._icp_r, trans, this.pos2d, this.pos3d, num, trans);

				if( err > this.trackingThresh ) {
					this._icp_r.setInlierProbability(0.4);
					this._icp.setInlierProbability(0.4);
					err = ar2GetTransMat(this._icp_r, trans, this.pos2d, this.pos3d, num, trans);

					if( err > this.trackingThresh ) {
						this._icp_r.setInlierProbability(0.0);
						this._icp.setInlierProbability(0.0);
						err = ar2GetTransMat(this._icp_r,trans, this.pos2d, this.pos3d, num, trans);

						if(err > this.trackingThresh ) {
							this.contNum = 0;
							return false;
						}
					}
				}
			}
		}



		this.contNum++;
		this.trans3.setValue(this.trans2);
		this.trans2.setValue(this.trans1);
		this.trans1.setValue(trans);
//		for( j = 0; j < 3; j++ ) {
//			for( i = 0; i < 4; i++ ) surfaceSet..trans3[j][i] = surfaceSet.trans2[j][i];
//		}
//		for( j = 0; j < 3; j++ ) {
//			for( i = 0; i < 4; i++ ) surfaceSet.trans2[j][i] = surfaceSet.trans1[j][i];
//		}
//		for( j = 0; j < 3; j++ ) {
//			for( i = 0; i < 4; i++ ) surfaceSet.trans1[j][i] = trans[j][i];
//		}
//		c++;
//		System.out.println(c+":"+err);
//		if(err==1.4048801338335002){
//			System.out.println("Tracker:[OK]");
//		}
		return true;
	}
	static int c=0;
	static double  ar2GetTransMat(NyARIcp _icp, NyARDoubleMatrix44 initConv, NyARDoublePoint2d[]  pos2d, double[][]  pos3d, int num,
			  NyARDoubleMatrix44  conv)
	{
		NyARDoublePoint2d[] screenCoord=NyARDoublePoint2d.createArray(num);
		NyARDoublePoint3d[] worldCoord=NyARDoublePoint3d.createArray(num);
		double dx,dy,dz;
	    dx = dy = dz = 0.0f;
	    for(int i = 0; i < num; i++ ) {
	        dx += pos3d[i][0];
	        dy += pos3d[i][1];
	        dz += pos3d[i][2];
	    }
	    dx /= num;
	    dy /= num;
	    dz /= num;
	    
	    for(int i = 0; i < num; i++ ) {
	        screenCoord[i].x = pos2d[i].x;
	        screenCoord[i].y = pos2d[i].y;
	        worldCoord[i].x  = pos3d[i][0] - dx;
	        worldCoord[i].y  = pos3d[i][1] - dy;
	        worldCoord[i].z  = pos3d[i][2] - dz;
	    }


	    NyARDoubleMatrix44 initMat=new NyARDoubleMatrix44();
	    initMat.setValue(initConv);
	    initMat.m03=(initConv.m00 * dx + initConv.m01 * dy + initConv.m02 * dz + initConv.m03);
	    initMat.m13=(initConv.m10 * dx + initConv.m11 * dy + initConv.m12 * dz + initConv.m13);
	    initMat.m23=(initConv.m20 * dx + initConv.m21 * dy + initConv.m22 * dz + initConv.m23);

	
//	#ifndef ARDOUBLE_IS_FLOAT
//	for (r = 0; r < 3; r++) for (c = 0; c < 4; c++) initConvTemp[r][c] = (ARdouble)initConv[r][c];
//	#endif
	NyARTransMatResultParam o_ret_param=new NyARTransMatResultParam();
	if(!_icp.icpPoint(screenCoord, worldCoord, num, initMat, conv,o_ret_param)){
		return Double.MAX_VALUE;
	}
	
	
//	if( robustMode == 0 ) {
//	if( icpPoint( icpHandle, &data, initConvTemp, convTemp, &err ) < 0 )
//	{
//	err = 100000000.0f;
//	}
//	}
//	else {
//	if( icpPointRobust( icpHandle, &data, initConvTemp, convTemp, &err ) < 0 )
//	{
//	err = 100000000.0f;
//	}
//	}
	
//	#ifndef ARDOUBLE_IS_FLOAT
//	for (r = 0; r < 3; r++) for (c = 0; c < 4; c++) conv[r][c] = (double)convTemp[r][c];
//	#endif
	
//	free( data.screenCoord );
//	free( data.worldCoord );


    conv.m03 = (conv.m03 - conv.m00 * dx - conv.m01 * dy - conv.m02 * dz);
    conv.m13 = (conv.m13 - conv.m10 * dx - conv.m11 * dy - conv.m12 * dz);
    conv.m23 = (conv.m23 - conv.m20 * dx - conv.m21 * dy - conv.m22 * dz);
	
	
	
	return o_ret_param.last_error;
	
	}

	
	
	
	AR2TemplateT ar2GenTemplate(ARParamLT cparam, NyARDoubleMatrix44 trans, NyARNftIsetFile imageSet,
			NyAR2FeaturePoints featurePoints, int num,int ts1, int ts2 )
	{
		int           xsize, ysize;
		AR2TemplateT _template=new AR2TemplateT(ts1,ts2);
		//	arMalloc(_template, AR2TemplateT, 1 );
		//	_template.xts1 = _template.yts1 = ts1;
		//	_template.xts2 = _template.yts2 = ts2;
		//	
		//	_template.xsize = xsize = _template.xts1 + _template.xts2 + 1;
		//	_template.ysize = ysize = _template.yts1 + _template.yts2 + 1;
		//	arMalloc( _template->img1,  int,  xsize*ysize );
		//	arMalloc( _template->wimg1, int,  xsize*ysize );
		//	arMalloc( _template->wimg2, int,  xsize*ysize );
	
		if( ar2SetTemplateSub(cparam, trans, imageSet, featurePoints, num, _template) < 0 ) {
//			ar2FreeTemplate( _template );
			return null;
		}
		return _template;
	}
 
	static int ar2MarkerCoord2ScreenCoord2(ARParamLT cparam, NyARDoubleMatrix44 trans, double  mx, double  my,NyARDoublePoint2d o_s)
	{
		NyARDoubleMatrix44 wtrans=new NyARDoubleMatrix44();
		NyARDoublePoint2d tmp=new NyARDoublePoint2d();
	    double   hx, hy, h;
	    double   ix, iy;
	    double   ix1, iy1;
//	    ARdouble sxTemp, syTemp, ix1Temp, iy1Temp;

	    wtrans.mul(cparam.getPerspectiveProjectionMatrix(),trans);

	    hx = wtrans.m00 * mx + wtrans.m01 * my  + wtrans.m03;
	    hy = wtrans.m10 * mx + wtrans.m11 * my + wtrans.m13;
	    h  = wtrans.m20 * mx + wtrans.m21 * my + wtrans.m23;

	    ix = hx / h;
	    iy = hy / h;	    
	    cparam.arParamIdeal2ObservLTf(ix,iy, o_s);
	    cparam.arParamObserv2IdealLTf(o_s.x,o_s.y, tmp);
//	    arParamIdeal2Observ( cparam->dist_factor, ix, iy, sx, sy, cparam->dist_function_version );
//	    arParamObserv2Ideal( cparam->dist_factor, *sx, *sy, &ix1, &iy1, cparam->dist_function_version);
	    if( (ix-tmp.x)*(ix-tmp.x) + (iy-tmp.y)*(iy-tmp.y) > 1.0f ){
	    	return -1;
	    }
	    return 0;
	}	
	static int ar2MarkerCoord2ScreenCoord(ARParamLT cparam, NyARDoubleMatrix44 trans, double  mx, double  my,NyARDoublePoint2d o_s)
	{
		NyARDoubleMatrix44 wtrans=new NyARDoubleMatrix44();
	    double   hx, hy, h;
//	    double   ix, iy;
//	    ARdouble sxTemp, syTemp;
	    if(cparam!=null){
		    wtrans.mul(cparam.getPerspectiveProjectionMatrix(),trans);
	//	    arUtilMatMuldff( cparam->mat, trans, wtrans );
	
		    hx = wtrans.m00 * mx + wtrans.m01 * my + wtrans.m03;
		    hy = wtrans.m10 * mx + wtrans.m11 * my + wtrans.m13;
		    h  = wtrans.m20 * mx + wtrans.m21 * my + wtrans.m23;
	
		    double ix = hx / h;
		    double iy = hy / h;
		    
		    cparam.arParamIdeal2ObservLTf(ix,iy, o_s);
	//	    arParamIdeal2Observ( cparam->dist_factor, (ARdouble)ix, (ARdouble)iy, &sxTemp, &syTemp, cparam->dist_function_version );
	//	    o_s.x = (double)sxTemp;
	//	    o_s.y = (double)syTemp;
		    return 0;
	    }else{
		    hx = trans.m00 * mx + trans.m01 * my + trans.m03;
		    hy = trans.m10 * mx + trans.m11 * my + trans.m13;
		    h  = trans.m20 * mx + trans.m21 * my + trans.m23;
		    o_s.x=hx/h;
		    o_s.x=hy/h;
		    return 0;
	    }
	}
	static void ar2MarkerCoord2ImageCoord( int xsize, int ysize, double dpi,NyARDoublePoint2d m,NyARDoublePoint2d o_out)
	{
		o_out.x = m.x * dpi / 25.4f;
		o_out.y = ysize - m.y * dpi / 25.4f;
	    return;
	}
	static int ar2ScreenCoord2MarkerCoord(ARParamLT cparam,NyARDoubleMatrix44 trans, double  sx, double  sy,NyARDoublePoint2d o_out)
	{
	    double   ix, iy;
	    NyARDoubleMatrix44 wtrans=new NyARDoubleMatrix44();
	    double   c11, c12, c21, c22, b1, b2;
	    double   m;
	    NyARDoublePoint2d in=new NyARDoublePoint2d();
	    if( cparam == null ) {
	        c11 = trans.m20 * sx - trans.m00;
	        c12 = trans.m21 * sx - trans.m01;
	        c21 = trans.m20 * sy - trans.m10;
	        c22 = trans.m21 * sy - trans.m11;
	        b1  = trans.m03 - trans.m23* sx;
	        b2  = trans.m13 - trans.m23* sy;
	    }else{
		    cparam.arParamObserv2IdealLTf(sx,sy,in);
	//
	//	    arParamObserv2Ideal( cparam->dist_factor, (ARdouble)sx, (ARdouble)sy, &ix, &iy, cparam->dist_function_version);	    
	//	    arUtilMatMuldff(cparam.->mat, trans, wtrans );
		    wtrans.mul(cparam.getPerspectiveProjectionMatrix(),trans);
		    c11 = wtrans.m20 * in.x - wtrans.m00;
		    c12 = wtrans.m21 * in.x - wtrans.m01;
		    c21 = wtrans.m20 * in.y - wtrans.m10;
		    c22 = wtrans.m21 * in.y - wtrans.m11;
		    b1  = wtrans.m03 - wtrans.m23 * in.x;
		    b2  = wtrans.m13 - wtrans.m23 * in.y;
	    }
	    m = c11 * c22 - c12 * c21;
	    if( m == 0.0 ) return -1;
	    o_out.x = (c22 * b1 - c12 * b2) / m;
	    o_out.y = (c11 * b2 - c21 * b1) / m;

	    return 0;
	}	
	static int ar2GetImageValue(ARParamLT cparam,NyARDoubleMatrix44 trans, NyARNftIsetFile.ReferenceImage image,double sx, double sy)
	{
//	    double   mx, my;
//	    double   iix, iiy;
	    int     ix, iy;
	    NyARDoublePoint2d m=new NyARDoublePoint2d();
	    NyARDoublePoint2d ii=new NyARDoublePoint2d();

	    ar2ScreenCoord2MarkerCoord(cparam, trans, sx, sy,m);
	    ar2MarkerCoord2ImageCoord( image.width, image.height, image.dpi,m,ii);
	    ix = (int)(ii.x + 0.5);
	    iy = (int)(ii.y + 0.5);

	    //座標計算と値取得は分けよう。
	    if( ix < 0 || ix >= image.width || iy < 0 || iy >= image.height ){
	    	return -1;
	    }
	    return 0xff & image.img[iy*image.width+ix];
//	    *pBW = image->imgBW[iy*image->xsize+ix];
//
//	    return 0;
	}

//	#endif	
	final static int KEEP_NUM=3;
	final static int AR2_TEMP_SCALE=2;
	final static int AR2_TEMPLATE_NULL_PIXEL=2000000000;
	final static int SKIP_INTERVAL=3;
	class ar2GetBestMatchingResult
	{
		int bx;
		int by;
		double val;
	}
	static void updateCandidate( int x, int y, int wval,int[] keep_num, int[] cx, int[] cy, int[] cval)
	{
		int    m, n;

		int keep_num_tmp=keep_num[0];
		if(keep_num_tmp == 0 ) {
			cx[0]   = x;
			cy[0]   = y;
			cval[0] = wval;
			keep_num_tmp= 1;
			keep_num[0]=keep_num_tmp;
			return;
		}

		int l;
		for(l = 0; l < keep_num_tmp; l++) {
			if( cval[l] < wval ){
				break;
			}
		}
		if( l == keep_num_tmp ) {
			if( l < KEEP_NUM )  {
				cx[l]   = x;
				cy[l]   = y;
				cval[l] = wval;
				keep_num_tmp++;
			}
			keep_num[0]=keep_num_tmp;
			return;
		}

		if(keep_num_tmp == KEEP_NUM ) {
			m = KEEP_NUM - 1;
		}else{
			m = keep_num_tmp;
			keep_num_tmp++;
		}

		for( n = m; n > l; n-- ) {                
			cx[n]   =  cx[n-1];
			cy[n]   =  cy[n-1];
			cval[n] =  cval[n-1]; 
		}
		cx[n]   = x;
		cy[n]   = y;
		cval[n] = wval;
		keep_num[0]=keep_num_tmp;
		return;
	}
	private final static int USE_SEARCH1=1;
	private final static int USE_SEARCH2=1;
	private final static int USE_SEARCH3=1;
	/**
	 * 
	 * @param i_rtaster
	 * @param mfImage
	 * @param mtemp
	 * @param rx
	 * @param ry
	 * @param search[3][2]
	 * @return
	 * @throws NyARException 
	 */
	int ar2GetBestMatching(INyARGrayscaleRaster i_rtaster, INyARGrayscaleRaster mfImage,
            AR2TemplateT mtemp, int rx, int ry,
             int[][] search,ar2GetBestMatchingResult o_result)
	{
		int              search_flag[] = {USE_SEARCH1, USE_SEARCH2, USE_SEARCH3};
		int              yts1, yts2;
		int              keep_num;
		int[]            cx=new int[KEEP_NUM];
		int[]            cy=new int[KEEP_NUM];
		int[]            cval=new int[KEEP_NUM];
		int              wval2;
		int              i, j, l;
		int              ii;
		int              ret;
//		search[0][0]=558;
//		search[0][1]=139;
		
		
	
		keep_num = 0;
		NyARIntSize s=i_rtaster.getSize();
		yts1 = mtemp.yts1;
		yts2 = mtemp.yts2;
	
		for( ii = 0; ii < 3; ii++ ) {
			if( search_flag[ii] == 0 ){
				continue;
			}
			if( search[ii][0] < 0 ){
				break;
			}
			
			int px = (search[ii][0]/(SKIP_INTERVAL+1))*(SKIP_INTERVAL+1) + (SKIP_INTERVAL+1)/2;
			int py = (search[ii][1]/(SKIP_INTERVAL+1))*(SKIP_INTERVAL+1) + (SKIP_INTERVAL+1)/2;
		
			int sx = px - rx;
			if( sx < 0 ) sx = 0;
			int ex = px + rx;
//			if( ex >= xsize ) ex = xsize-1;
			if( ex >= s.w ){
				ex = s.w-1;
			}
			
			int sy = py - ry;
			if( sy < 0 ){
				sy = 0;
			}
			int ey = py + ry;
			if( ey >= s.h ){
				ey = s.h-1;
			}
			
//			if( ey >= ysize ){
//				ey = ysize-1;
//			}
			
			for( j = sy; j <= ey; j++ ) {
	//			pmf = &mfImage[j*xsize+sx];
				for( i = sx; i <= ex; i++ ) {
					this.mfImage.setPixel(i,j,0);
	//				*(pmf++) = 0;
				}
			}
		}
	
		ret = 1;
		for( ii = 0; ii < 3; ii++ ) {      
			if( search_flag[ii] == 0 ){
				continue;
			}
			if( search[ii][0] < 0 ) {
//					if( ret ){
				if( ret!=0){
					return -1;
				}else{
					break;
				}
			}
	
			int px = (search[ii][0]/(SKIP_INTERVAL+1))*(SKIP_INTERVAL+1) + (SKIP_INTERVAL+1)/2;
			int py = (search[ii][1]/(SKIP_INTERVAL+1))*(SKIP_INTERVAL+1) + (SKIP_INTERVAL+1)/2;
	
			for( j = py - ry; j <= py + ry; j += SKIP_INTERVAL+1 ) {
				if( j - yts1*AR2_TEMP_SCALE <  0     ){
					continue;
				}
//					if( j + yts2*AR2_TEMP_SCALE >= ysize ){
				if( j + yts2*AR2_TEMP_SCALE >= s.h){
					break;
				}
				for( i = px - rx; i <= px + rx; i += SKIP_INTERVAL+1 ) {
					if( i - mtemp.xts1*AR2_TEMP_SCALE <  0     ){
						continue;
					}
//						if( i + mtemp.xts2*AR2_TEMP_SCALE >= xsize ){
						if( i + mtemp.xts2*AR2_TEMP_SCALE >= s.w){
						break;
					}
					if(mfImage.getPixel(i, j)!=0){
					//mfImage[j*xsize+i] ){
						continue;
					}
					mfImage.setPixel(i, j, 1);
					int[] wval=new int[1];
					if( ar2GetBestMatchingSubFine(i_rtaster,mtemp,i,j,wval) < 0 ) {
						continue;
					}
					ret = 0;
					int[] keep_num_tmp=new int[1];
					keep_num_tmp[0]=keep_num;
					updateCandidate(i, j, (int)wval[0],keep_num_tmp, cx, cy, cval);
					keep_num=keep_num_tmp[0];
				}
			}
		}

		wval2 = 0;
		ret = -1;
		for(l = 0; l < keep_num; l++) {
			for( j = cy[l]-SKIP_INTERVAL; j <= cy[l]+SKIP_INTERVAL; j++ ) {
				if( j-mtemp.yts1*AR2_TEMP_SCALE <  0     ){
					continue;
				}
//				if( j+mtemp.yts2*AR2_TEMP_SCALE >= ysize ){
				if( j+mtemp.yts2*AR2_TEMP_SCALE >= s.h){
					break;
				}
				for( i = cx[l]-SKIP_INTERVAL; i <= cx[l]+SKIP_INTERVAL; i++ ) {
				    if( i-mtemp.xts1*AR2_TEMP_SCALE <  0     ){
				    	continue;
				    }
//				    if( i+mtemp.xts2*AR2_TEMP_SCALE >= xsize ){
				    if( i+mtemp.xts2*AR2_TEMP_SCALE >= s.w){
				    	break;
				    }
				    int[] wval=new int[1];
				    if( ar2GetBestMatchingSubFine(i_rtaster,mtemp,i,j,wval) < 0 ) {
				        continue;
				    }
				    if( wval[0] > wval2 ) {
				    	o_result.bx    =  i;
				    	o_result.by    =  j;
				         wval2 =  wval[0];
				         o_result.val   = (double)wval[0] / 10000;
				        ret = 0;
				    }
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param img
	 * @param mtemp
	 * @param sx
	 * @param sy
	 * @param val
	 * int[1]
	 * @return
	 * 整合性チェックOK
	 */
	static int ar2GetBestMatchingSubFine( INyARGrayscaleRaster img,
	                                      AR2TemplateT mtemp, int sx, int sy, int[] val)
	{
		int                  sum1, sum2, sum3;
		 sum1 = sum2 = sum3 = 0;
		int xsize=img.getWidth();
        int        ssx, eex, ssy, eey;
        int p3_ptr;
        int p1_ptr=0;
        int p2_ptr;
        ssx = -(mtemp.xts1);
        eex =   mtemp.xts2;
        ssy = -(mtemp.yts1);
        eey =   mtemp.yts2;
        int[] p1=mtemp.img1;
        int[] p2=(int[])img.getBuffer();
        for(int j = ssy; j <= eey; j++ ) {
            p2_ptr = p3_ptr = ((sy + j*AR2_TEMP_SCALE)*xsize + sx + ssx*AR2_TEMP_SCALE);
            for(int i = ssx; i <= eex; i++ ) {
                if( p1[p1_ptr] != AR2_TEMPLATE_NULL_PIXEL ) {
                    sum1 += p2[p2_ptr];
                    sum2 += p2[p2_ptr] * p2[p2_ptr];
                    sum3 += p2[p2_ptr] * p1[p1_ptr];
                }
                p2_ptr += AR2_TEMP_SCALE;
                p1_ptr++;
            }
        }		
        sum3 -= sum1 * mtemp.sum / mtemp.validNum;
        int _vlen = sum2 - sum1*sum1/mtemp.validNum;
        if( _vlen == 0 ) val[0] = 0;
        else            val[0] = sum3 * 100 / mtemp.vlen * 100 / (int)Math.sqrt(_vlen);


	
	    return 0;
	}
	static int ar2SetTemplateSub(ARParamLT cparam, NyARDoubleMatrix44 trans, NyARNftIsetFile imageSet,
			  NyAR2FeaturePoints featurePoints, int num,AR2TemplateT template_ )
	{
		double    mx, my;
		
	//	int     *img1;
		int      ave;
		int      vlen;
//		int  pixel;
		int      ix, iy,iy2,ix2;
		int      ret;
		int      i, j, k;
		
		NyARDoubleMatrix44 wtrans=new NyARDoubleMatrix44();
		wtrans.mul(cparam.getPerspectiveProjectionMatrix(),trans);
		
		mx = featurePoints.coord[num].mx;
		my = featurePoints.coord[num].my;
		NyARDoublePoint2d s=new NyARDoublePoint2d();
	//	double    sx, sy;
		ar2MarkerCoord2ScreenCoord( cparam, trans, mx, my,s);
		ix = (int)(s.x + 0.5);
		iy = (int)(s.y + 0.5);
		int sum,sum2;
		int[] img1 = template_.img1;
		int img1_ptr=0;
		k = 0;
        sum = sum2 = 0;
        k = 0;
        iy2 = iy - (template_.yts1)*AR2_TEMP_SCALE;
        for( j = -(template_.yts1); j <= template_.yts2; j++, iy2+=AR2_TEMP_SCALE ) {
            ix2 = ix - (template_.xts1)*AR2_TEMP_SCALE;
            for( i = -(template_.xts1); i <= template_.xts2; i++, ix2+=AR2_TEMP_SCALE ) {

                if(cparam.arParamObserv2IdealLTf(ix2, iy2,s) < 0 ) {
                    img1[img1_ptr++] = AR2_TEMPLATE_NULL_PIXEL;
                    continue;
                }

                int pixel = ar2GetImageValue(null, wtrans,

                		imageSet.items[featurePoints.scale], s.x, s.y);
                if( pixel < 0 ) {
                	img1[img1_ptr++] = AR2_TEMPLATE_NULL_PIXEL;
                }
                else {
                	img1[img1_ptr++] = pixel;
                    sum  += pixel;
                    sum2 += pixel*pixel;
                    k++;
                }
            }
        }
        if( k == 0 ) return -1;

        vlen = sum2 - sum*sum/k;
        template_.vlen = (int)Math.sqrt(vlen);
        template_.sum = sum;
        template_.validNum = k;		
		return 0;
	}

	final static double AR2_DEFALUT_TRACKING_SD_THRESH=5.0;


	static int k=0;
	int ar2Tracking2d (NyARSurfaceDataSet surfaceSet, AR2TemplateCandidate.Item candidate,INyARGrayscaleRaster dataPtr, AR2Tracking2DResultT result )
	{
		AR2TemplateT         template_;
		
		int                   snum, level, fnum;
		int [][]              search=new int[3][2];
		int                   bx, by;
		
		
//		snum  = candidate.snum;
		level = candidate.level;
		fnum  = candidate.num;
		


		template_ = ar2GenTemplate( this._cparam,
			this.wtrans1,
			surfaceSet.iset,
			surfaceSet.fset.list[level],
			fnum,
			this.templateSize1,
			this.templateSize2);
		if( template_ == null ){
			return -1;
		}

		
		
		if( template_.vlen * template_.vlen < (template_.xts1+template_.xts2+1) * (template_.yts1+template_.yts2+1) * AR2_DEFALUT_TRACKING_SD_THRESH * AR2_DEFALUT_TRACKING_SD_THRESH )
		{
//			ar2FreeTemplate( template_ );
			return -1;
		}


		if( this.contNum == 1 ) {
			ar2GetSearchPoint( this._cparam,this.wtrans1, null, null,surfaceSet.fset.list[level].coord[fnum],search );
		}else if( this.contNum == 2 ) {
			ar2GetSearchPoint( this._cparam,this.wtrans1,this.wtrans2, null,surfaceSet.fset.list[level].coord[fnum],search );
		}else{
			ar2GetSearchPoint( this._cparam,
				   this.wtrans1,
				   this.wtrans2,
				   this.wtrans3,
				   surfaceSet.fset.list[level].coord[fnum],
				   search );
		}



		NyARIntSize s=this._cparam.getScreenSize();
		ar2GetBestMatchingResult b=new ar2GetBestMatchingResult();
		
//		k++;
//		if(k==8){
//			System.out.print("a");
//		}
		if( ar2GetBestMatching( dataPtr,
							this.mfImage,
//							s.xsize,
//							s.ysize,
							template_,
							this.searchSize,
							this.searchSize,
							search,
							b) < 0 )
		{
			return -1;
		}
		result.sim=b.val;



		result.pos2d[0] = b.bx;
		result.pos2d[1] = b.by;
		result.pos3d[0] = surfaceSet.fset.list[level].coord[fnum].mx;
		result.pos3d[1] = surfaceSet.fset.list[level].coord[fnum].my;
		result.pos3d[2] = 0;
		
		
		//ar2FreeTemplate( template_ );
		
		return 0;
	}	
	//	int NyAR2Tracking::ar2Tracking2d (AR2SurfaceSetT *surfaceSet, AR2TemplateCandidateT *candidate,
//					ARUint8 *dataPtr, AR2Tracking2DResultT *result );
//
//	int NyAR2Tracking::ar2Tracking2d(NyAR2SurfaceSet &surfaceSet, AR2TemplateCandidateT *candidate,
//						ARUint8 *dataPtr, AR2Tracking2DResultT *result );
	public void ar2SetTrackingThresh(double d) {
		this.trackingThresh=d;
		
	}
	public void ar2SetSimThresh(double d) {
		this.simThresh=d;
	}
	public void ar2SetSearchFeatureNum(int i) {
		this.searchFeatureNum=i;
		
	}
	public void ar2SetSearchSize(int i) {
		this.searchSize=i;
	}
	public void ar2SetTemplateSize1(int i) {
		this.templateSize1=i;
	}
	public void ar2SetTemplateSize2(int i) {
		this.templateSize2=i;
		
	}
	/**
	 * テストパターン
	 * @param args
	 */
	public static void main(String[] args)
	{
		NyARDoubleMatrix44 DEST_MAT=new NyARDoubleMatrix44(
				new double[]{
						0.9832165682361184,0.004789697223621061,-0.18237945710280384,-190.59060790299358,
						0.012860184615056927,-0.9989882709616935,0.04309419210331572,64.04490277502563,
						-0.18198852802987958,-0.044716355753573425,-0.9822833548209547,616.6427596804766,
				0,0,0,1});
		NyARDoubleMatrix44 SRC_MAT=new NyARDoubleMatrix44(new double[]{
			0.984363556,	0.00667689135,	-0.176022261,	-191.179672,
			0.0115975942,	-0.999569774,	0.0269410834,	63.0028076,
			-0.175766647,	-0.0285612550,	-0.984017432,	611.758728,
			0,0,0,1});

		try {
			String img_file="../Data/testcase/test.raw";
			String cparam=	"../Data/testcase/camera_para5.dat";
			String fsetfile="../Data/testcase/pinball.fset";
			String isetfile="../Data/testcase/pinball.iset5";
			//カメラパラメータ
			NyARParam param=NyARParam.loadFromARParamFile(new FileInputStream(cparam),640,480,NyARParam.DISTFACTOR_RAW);
			NyARDoublePoint2d d=new NyARDoublePoint2d();
			param.getDistortionFactor().ideal2Observ(100,100, d);
			param.getDistortionFactor().observ2Ideal(100,100, d);
			
			INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(640,480);
			//試験画像の準備
			{
				INyARRgbRaster rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8X8_32);
				FileInputStream fs = new FileInputStream(img_file);
				fs.read((byte[])rgb.getBuffer());
				INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
				filter.convert(gs);			
			}
			NyARDoubleMatrix44 ret=new NyARDoubleMatrix44();
			NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(new FileInputStream(fsetfile));
			NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(new FileInputStream(isetfile));
			AR2Handle tracking=new AR2Handle(param,iset,fset);
			tracking.ar2SetTrackingThresh(5.0);
			tracking.ar2SetSimThresh(0.50);
			tracking.ar2SetSearchFeatureNum(16);
			tracking.ar2SetSearchSize(12);
			tracking.ar2SetTemplateSize1(6);
			tracking.ar2SetTemplateSize2(6);
			//validation test
			for(int j=0;j<10;j++){
				long s=System.currentTimeMillis();
				for(int i=0;i<3000;i++){
					tracking.setInitialTransmat(SRC_MAT);
					ret.setValue(SRC_MAT);
					tracking.ar2Tracking(gs, ret);
				}
				System.out.println(System.currentTimeMillis()-s);
				System.out.println(ret.equals(DEST_MAT));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
}
