package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.Geometry;

/**
 * 4ポイントを選択するテーブル
 *
 */
public class RansacPointTable
{
	public FeaturePairStack.Item[] indics;
	private int _num;
	public RansacPointTable(int i_size){
		this.indics=new FeaturePairStack.Item[i_size];
		this._num=0;
	}
	/**
	 * 値セットをテーブルにセットする。
	 * @param i_matches
	 * @param i_num
	 */
	public void setSequential(FeaturePairStack.Item[] i_matches,int i_num)
	{
		if(i_num>this.indics.length){
			this.indics=new FeaturePairStack.Item[i_num+10];
		}
		FeaturePairStack.Item[] b=this.indics;
		for(int i=0;i<i_num;i++){
			b[i]=i_matches[i];
		}
		this._num=i_num;
	}
	private int seed;
	public void setSeed(int i_seed){
		this.seed=i_seed;
	}
    /**
     * Shuffle the elements of an array.
     */
    public void shuffle(int i_num_of_shuffle)
    {
    	FeaturePairStack.Item[] b=this.indics;
    	int seed=this.seed;
    	int n=this._num;
        for(int i = 0; i < i_num_of_shuffle; i++)
        {
	        seed= (214013*seed+2531011);
	        int k=((seed>>16)&0x7FFF)%n;
	        FeaturePairStack.Item t=b[i];
            b[i]=b[k];
            b[k]=t;
        }
        this.seed=seed;
    }
    /**
     * Check the geometric consistency between four correspondences.
     * Ransacテーブルの4点についてHomography4PointsGeometricallyConsistent
     */
	public boolean geometricallyConsistent4Point()
	{
		FeaturePairStack.Item p1=this.indics[0];
		FeaturePairStack.Item p2=this.indics[1];
		FeaturePairStack.Item p3=this.indics[2];
		FeaturePairStack.Item p4=this.indics[3];
        if(((Geometry.LinePointSide(p1.ref,p2.ref, p3.ref) > 0) ^ (Geometry.LinePointSide(p1.query,p2.query,p3.query) > 0)) == true)
            return false;
        if(((Geometry.LinePointSide(p2.ref, p3.ref, p4.ref) > 0) ^ (Geometry.LinePointSide(p2.query, p3.query, p4.query) > 0)) == true)
            return false;
        if(((Geometry.LinePointSide(p3.ref, p4.ref, p1.ref) > 0) ^ (Geometry.LinePointSide(p3.query, p4.query, p1.query) > 0)) == true)
            return false;
        if(((Geometry.LinePointSide(p4.ref, p1.ref, p2.ref) > 0) ^ (Geometry.LinePointSide(p4.query, p1.query, p2.query) > 0)) == true)
            return false;
        return true;
    }		

}