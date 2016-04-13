/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
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