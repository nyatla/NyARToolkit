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
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.hest;


import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch.ResultItem;
import jp.nyatla.nyartoolkit.pro.core.kpm.hest.utils.HomographyMatrix;



/**
 * @todo 0.5ピクセル補正は�?
 * @author nyatla
 *
 */
public class NyARHomographyEst implements INyARHomographyEst
{
	private int[] _temp_index_map;
	private int[] _dest_index_map;
	/**
	 * 
	 * @param i_max_input_points
	 * {@link #ransacEstimation}のpreRANSACに入力されるサンプルの�?大数
	 * @throws NyARRuntimeException
	 */
	public NyARHomographyEst(int i_max_input_points) throws NyARRuntimeException
	{
		this._temp_index_map=new int[i_max_input_points];
		this._dest_index_map=new int[i_max_input_points];
		this._aselector=new ApproximateIndexSelector();		
	}
	/**
	 * afterRANSACの数にあわせて、最大preRANSACの数と同じ数のサンプルセ�?トを返します�??
	 */
	public void ransacEstimation(NyARSurfAnnMatch.ResultPtr preRANSAC, NyARSurfAnnMatch.ResultPtr afterRANSAC) throws NyARRuntimeException
	{
		//戸数チェ�?ク
		assert(preRANSAC.getLength()<=this._temp_index_map.length);
		
		int[] index_map=this._dest_index_map;
		int len=preRANSAC.getLength();
		int ret_num;//出力する�?�イント数
		if(len < 5 ) {
			ret_num=len;
			for(int i=0;i<len;i++){
				index_map[i]=i;
			}
		}
		if(preRANSAC.getLength() < 10 ) {
			ret_num=this.ransacHomographyEst2(preRANSAC,index_map);
		}else{
			ret_num=this.ransacHomographyEst1( preRANSAC,index_map);
		}
		//�?大返却数の決�?
		if(afterRANSAC.getArraySize()<ret_num)
		{
			ret_num=afterRANSAC.getArraySize();
		}
		afterRANSAC.setLength(ret_num);
		NyARSurfAnnMatch.ResultItem[] pre_items=preRANSAC.getArray();
		NyARSurfAnnMatch.ResultItem[] aft_items=afterRANSAC.getArray();
		for(int i=0;i<ret_num;i++){
			aft_items[i]=(pre_items[index_map[i]]);
		}
		return;
	}
	private RansacSamples __ransac_sample=new RansacSamples(4);
	private ApproximateIndexSelector _aselector;
	private final static double P=1-0.99;
	private final static double E=0.70;
	
	/**
	 * 仮定した行�?�につ�?て、誤差の小さ�?イン�?クス�?けを�?める�?
	 * @author nyatla
	 *
	 */
	private class ApproximateIndexSelector
	{
		private final static int T_SQUARE=10;// t = sqrt(6)* sigma and set sigma = sqrt(6)
		NyARDoubleMatrix33 __invH=new NyARDoubleMatrix33();
		/**
		 * i_inから、Hに対して誤差の少な�?�?ータを選択します�??
		 * o_indicesの数は、i_inの数と同じである�?要があります�??
		 * @param H
		 * @param i_in
		 * @return
		 */
		public int selectApproximateIndex(NyARDoubleMatrix33 H,NyARSurfAnnMatch.ResultPtr i_in,int[] o_indices)
		{
			assert(i_in.getArraySize()==o_indices.length);
			NyARSurfAnnMatch.ResultItem[] items=i_in.getArray();
			
			int i_len=i_in.getLength();
			NyARDoubleMatrix33 invH=this.__invH;
			invH.inverse(H);
			int ret=0;

			// use d^2_transfer as distance measure
			for(int i = 0 ; i < i_len; i++ )
			{
				NyARSurfAnnMatch.ResultItem item=items[i];
				int x1 = item.feature.coord3DI.x;
				int y1 = item.feature.coord3DI.y;
				double x2 = item.key.ipoint.x;
				double y2 = item.key.ipoint.y;

				// calculate x_trans = H * x
				double w2Trans = H.m20 * x1 + H.m21 * y1 + H.m22;
				double x2Trans = x2-((H.m00 * x1 + H.m01 * y1 + H.m02)/w2Trans);
				double y2Trans = y2-((H.m10 * x1 + H.m11 * y1 + H.m12)/w2Trans);
				double dist2x2AndHx1 = (x2Trans)*(x2Trans) + (y2Trans)*(y2Trans);
				if(dist2x2AndHx1>=T_SQUARE){
					continue;
				}
				// calculate x'_trans = H^(-1) * x'
				double w1Trans = invH.m20 * x2 + invH.m21 * y2 + invH.m22;
				double x1Trans = x1-((invH.m00 * x2 + invH.m01 * y2 + invH.m02)/ w1Trans);
				double y1Trans = y1-((invH.m10 * x2 + invH.m11 * y2 + invH.m12)/ w1Trans);

				// calculate the square distance (symmetric transfer error)
				double dist2x1AndInvHx2 = (x1Trans)*(x1Trans) + (y1Trans)*(y1Trans);
				double dist2Trans = dist2x1AndInvHx2 + dist2x2AndHx1;

				if( dist2Trans < T_SQUARE ){
					o_indices[ret]=i;
					ret++;
				}
			}
			return ret;
		}
	}


	
	/**
	 * 
	 * @param i_in_map
	 * @param o_index_of_map
	 * @return
	 * @throws NyARRuntimeException
	 */
	private int ransacHomographyEst1(NyARSurfAnnMatch.ResultPtr i_in_map, int[] o_index_of_map) throws NyARRuntimeException
	{
		RansacSamples ransac_sample=this.__ransac_sample;
		double e = E;
		int sampleCount = 0;
		assert(i_in_map.getLength()>=5);
		//set work objects
		ApproximateIndexSelector tempInlierMap=this._aselector;
		//
		int num_of_in_map=i_in_map.getLength();
		int maxNumOfInliers = 0;	
		double N = 1000.0f;
		int retry_sampling=0;
		while( sampleCount < N )
		{
			ransac_sample.sampling(i_in_map);
			if(!ransac_sample.isGoodSamples()){
				//サンプリングに�?定回数以上失敗したら終�?
				retry_sampling++;
				if(retry_sampling>500){
					break;
				}
				continue;
			}
				
			
			this.__Htmp.computeHomography4Points(ransac_sample.ref_pt1,ransac_sample.ref_pt2);

			// calculate the distance for each correspondences
			// compute the number of inliers	
			//誤差の少な�?ポイントだけを�?めた、�?時的なRansacResultを作�??
			int num_of_inliers=tempInlierMap.selectApproximateIndex(this.__Htmp,i_in_map,this._temp_index_map);

			//�?大のポイント数のも�?�を�?�力�?��?�に保�?
			// choose H with the largest number of inliears
			if(num_of_inliers >= maxNumOfInliers )
			{
				maxNumOfInliers=num_of_inliers;
				System.arraycopy(this._temp_index_map,0,o_index_of_map,0,num_of_inliers);
			}
			
			// adaptive algorithm for determining the number of RANSAC samples
			// textbook algorithm 4.6
			double outlierProb = 1.0 - ((double)maxNumOfInliers /num_of_in_map);
			e = (e < outlierProb) ? e : outlierProb;
			N = Math.log(P) / Math.log(1 - Math.pow((1 - e), 4));
			sampleCount ++;
		}
		return maxNumOfInliers;
	}
	private int ransacHomographyEst2(NyARSurfAnnMatch.ResultPtr i_in_map,int[]  o_index_of_map) throws NyARRuntimeException
	{
		RansacSamples ransac_sample=this.__ransac_sample;
		ApproximateIndexSelector tempInlierMap=this._aselector;
		int num_of_samples=i_in_map.getLength();
		assert(num_of_samples>=5);
		int         maxNumOfInliers = 0;
		for( int i1 =    0; i1 < num_of_samples; i1++ ) {
			ransac_sample.setSample(0, i1, i_in_map);
			for( int i2 = i1+1; i2 < num_of_samples; i2++ ) {
				ransac_sample.setSample(1, i2, i_in_map);
				for( int i3 = i2+1; i3 < num_of_samples; i3++ ) {
					ransac_sample.setSample(2, i3, i_in_map);
					for( int i4 = i3+1; i4 < num_of_samples; i4++ ) {
						ransac_sample.setSample(3, i4, i_in_map);
						
						//良�?サンプルでなければ無�?
						if(!ransac_sample.isGoodSamples())
						{
							continue;
						}
						//仮計�?
						this.__Htmp.computeHomography4Points(ransac_sample.ref_pt1,ransac_sample.ref_pt2);
						//誤差の少な�?ポイントだけを�?めた、�?時的なRansacResultを作�??
						int num_of_inliers=tempInlierMap.selectApproximateIndex(this.__Htmp,i_in_map,this._temp_index_map);

						//�?大のポイント数のも�?�を�?�力�?��?�に保�?
						// choose H with the largest number of inliears
						if(num_of_inliers >= maxNumOfInliers )
						{
							maxNumOfInliers = num_of_inliers;
							System.arraycopy(this._temp_index_map,0,o_index_of_map,0,num_of_inliers);
						}
					}
				}
			}
		}
		return maxNumOfInliers;
	}
	private HomographyMatrix __Htmp=new HomographyMatrix();

}

/**
 * Ransacのサンプラ
 */
class RansacSamples
{
	public NyARIntPoint2d[] ref_pt1;
	public NyARIntPoint2d[] ref_pt2;
	public int length;
	public RansacSamples(int i_length)
	{
		this.length=i_length;
		this.ref_pt1=new NyARIntPoint2d[i_length];
		this.ref_pt2=new NyARIntPoint2d[i_length];
	}
	private long _rand_val=1;
	public void sampling(NyARSurfAnnMatch.ResultPtr i_in_map)
	{
		int len=this.length;
		int number_of_items=i_in_map.getLength();
		//乱数値のロー�?
		long rand_val=this._rand_val;
		// pick corresponding points
		for(int i = 0 ; i < len ; i++ )
		{
			//rand?
			rand_val = (rand_val * 214013L + 2531011L);
			int pos = (int)((rand_val >>16) & 0x7FFF); // select random positions
			pos=pos%number_of_items;
			NyARSurfAnnMatch.ResultItem item=i_in_map.getItem(pos);
			this.ref_pt1[i]=item.feature.coord3DI;
			this.ref_pt2[i]=item.key.ipoint;
		}
		//乱数値の保�?
		this._rand_val=rand_val;	
	}
	/**
	 * i_index番目の要�?に、i_in_mapのi_sample_idの要�?をセ�?トします�??
	 * @param i_index
	 * @param i_sample_id
	 * @param i_in_map
	 */
	public void setSample(int i_index,int i_sample_id,NyARSurfAnnMatch.ResultPtr i_in_map)
	{
		NyARSurfAnnMatch.ResultItem item=i_in_map.getItem(i_sample_id);
		this.ref_pt1[i_index]=item.feature.coord3DI;
		this.ref_pt2[i_index]=item.key.ipoint;
	}
	/**
	 * 配�?�に含まれる要�?が�?��?�線状で無�?か確認します�??
	 * @param i_item
	 * @param num
	 * @return
	 */
	public boolean isGoodSamples()
	{
		int i, j, k;

		i = 0;
		j = i + 1;
		k = j + 1;
		int num=this.length;

		// check colinearity recursively
		while(true)
		{
			// set point vectors
			if(isColinear(this.ref_pt1[i],this.ref_pt1[j],this.ref_pt1[k])){
				return false;
			}
			if(isColinear(this.ref_pt2[i],this.ref_pt2[j],this.ref_pt2[k])){
				return false;
			}
			// update point index
			if( k < num - 1 )
			{
				k += 1;
			}
			else
			{
				if( j < num - 2 )
				{
					j += 1;
					k = j + 1;
				}
				else
				{
					if( i < num - 3 )
					{
						i += 1;
						j = i + 1;
						k = j + 1;
					}
					else
					{
						break;
					}
				}
			}
		}
		return true;
	}			
	final static double EPS=0.5;

	
	//
	// function : IsColinear
	// usage : r = IsColinear(A, B, C);
	// --------------------------------------
	// This function checks the colinearity of
	// the given 3 points A, B, and C.
	// If these are colinear, it returns false. (true ?��?��ﾈｯ?�?��ｾ?���?��ｴ?��ｰ?�? ?��?�??��?�??)
	//
	private static boolean isColinear(NyARIntPoint2d i_A,NyARIntPoint2d i_B,NyARIntPoint2d i_C)
	{
		//cross product(V1(i_A.x,i_A.y,1),V2(i_B.x,i_B.y,1))
		//dot product(V1(i_C.x,i_C.y,1),V2(abx,aby,abz))
		int d=i_C.x*(i_A.y - i_B.y)+i_C.y*(i_B.x - i_A.x)+(i_A.x*i_B.y - i_A.y*i_B.x);
		if( (d < EPS) && (d > -EPS) ){
			return true;
		}
		return false;
	}
}


