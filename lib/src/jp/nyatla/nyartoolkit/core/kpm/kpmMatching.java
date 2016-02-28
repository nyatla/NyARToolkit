package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class kpmMatching {
	private double error;

	public static int kpmUtilGetPose_binary(ARParamLT cparamLT, matchStack matchData, FreakMatchPointSetStack refDataSet,
			FreakFeaturePointStack inputDataSet, KpmResult kpmResult) {
		// ICPHandleT *icpHandle;
		// ICPDataT icpData;
		// ICP2DCoordT *sCoord;
		// ICP3DCoordT *wCoord;
		NyARDoubleMatrix44 initMatXw2Xc = new NyARDoubleMatrix44();
		// ARdouble err;
		int i;

		if (matchData.getLength() < 4) {
			return -1;
		}
		NyARDoublePoint2d[] sCoord = NyARDoublePoint2d.createArray(matchData.getLength());
		NyARDoublePoint3d[] wCoord = NyARDoublePoint3d.createArray(matchData.getLength());
		for (i = 0; i < matchData.getLength(); i++) {
			sCoord[i].x = inputDataSet.getItem(matchData.getItem(i).ins).x;
			sCoord[i].y = inputDataSet.getItem(matchData.getItem(i).ins).y;

			wCoord[i].x = refDataSet.getItem(matchData.getItem(i).ref).pos3d.x;
			wCoord[i].y = refDataSet.getItem(matchData.getItem(i).ref).pos3d.y;
			wCoord[i].z = 0.0;
		}

		// icpData.num = i;
		// icpData.screenCoord = &sCoord[0];
		// icpData.worldCoord = &wCoord[0];
		NyARIcpPlane icp_planer = new NyARIcpPlane(cparamLT.getPerspectiveProjectionMatrix());
		if (!icp_planer.icpGetInitXw2Xc_from_PlanarData(sCoord, wCoord, matchData.getLength(), initMatXw2Xc)) {
			return -1;
		}
		/*
		 * printf("--- Init pose ---\n"); for( int j = 0; j < 3; j++ ) { for( i = 0; i < 4; i++ ) printf(" %8.3f",
		 * initMatXw2Xc[j][i]); printf("\n"); }
		 */
		NyARIcpPoint icp_point = new NyARIcpPoint(cparamLT.getPerspectiveProjectionMatrix());
		icp_point.icpPoint(sCoord, wCoord, matchData.getLength(), initMatXw2Xc, kpmResult.camPose,
				kpmResult.resultparams);

		if (kpmResult.resultparams.last_error > 10.0f) {
			return -1;
		}

		return 0;
	}

}
