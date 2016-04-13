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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm;

import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.FeaturePoint;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class kpmMatching {
	private double error;
	public static int kpmUtilGetPose_binary(ARParamLT cparamLT,
			matchStack matchData, Point3dVector refDataSet,
			FeaturePointStack inputDataSet, KpmResult kpmResult)
	{
//	    ICPHandleT    *icpHandle;
//	    ICPDataT       icpData;
//	    ICP2DCoordT   *sCoord;
//	    ICP3DCoordT   *wCoord;
	    NyARDoubleMatrix44       initMatXw2Xc=new NyARDoubleMatrix44();
//	    ARdouble       err;
	    int            i;
	    
	    if( matchData.getLength() < 4 ){
	    	return -1;
	    }
	    NyARDoublePoint2d[] sCoord=NyARDoublePoint2d.createArray(matchData.getLength());
	    NyARDoublePoint3d[] wCoord=NyARDoublePoint3d.createArray(matchData.getLength());
	    for( i = 0; i < matchData.getLength(); i++ ) {
	        sCoord[i].x = inputDataSet.getItem(matchData.getItem(i).ins).x;
	        sCoord[i].y = inputDataSet.getItem(matchData.getItem(i).ins).y;

	        wCoord[i].x = refDataSet.getItem(matchData.getItem(i).ref).x;
	        wCoord[i].y = refDataSet.getItem(matchData.getItem(i).ref).y;
	        wCoord[i].z = 0.0;
	    }
	    
//	    icpData.num = i;
//	    icpData.screenCoord = &sCoord[0];
//	    icpData.worldCoord  = &wCoord[0];
	    NyARIcpPlane icp_planer=new NyARIcpPlane(cparamLT.getPerspectiveProjectionMatrix());
	    if(!icp_planer.icpGetInitXw2Xc_from_PlanarData(sCoord, wCoord, matchData.getLength(), initMatXw2Xc)){
	    	return -1;
	    }
	    /*
	    printf("--- Init pose ---\n");
	    for( int j = 0; j < 3; j++ ) {
	        for( i = 0; i < 4; i++ )  printf(" %8.3f", initMatXw2Xc[j][i]);
	        printf("\n");
	    }
	    */
	    NyARIcpPoint icp_point=new NyARIcpPoint(cparamLT.getPerspectiveProjectionMatrix());
	    icp_point.icpPoint(sCoord, wCoord, matchData.getLength(), initMatXw2Xc, kpmResult.camPose, kpmResult.resultparams);


	    if(kpmResult.resultparams.last_error> 10.0f ){
	    	return -1;
	    }
	    
	    return 0;
	}

}
