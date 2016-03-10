package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class kpmMatching {
	public static boolean kpmUtilGetPose_binary(NyARParam i_cparam, FeaturePairStack matchData, FreakMatchPointSetStack refDataSet,
			FreakFeaturePointStack inputDataSet, KpmResult kpmResult) {
		// ICPHandleT *icpHandle;
		// ICPDataT icpData;
		// ICP2DCoordT *sCoord;
		// ICP3DCoordT *wCoord;
		NyARDoubleMatrix44 initMatXw2Xc = new NyARDoubleMatrix44();
		// ARdouble err;
		int i;

		if (matchData.getLength() < 4) {
			return false;
		}
		NyARDoublePoint2d[] sCoord = NyARDoublePoint2d.createArray(matchData.getLength());
		NyARDoublePoint3d[] wCoord = NyARDoublePoint3d.createArray(matchData.getLength());
		for (i = 0; i < matchData.getLength(); i++) {
			sCoord[i].x = matchData.getItem(i).query.x;
			sCoord[i].y = matchData.getItem(i).query.y;

			wCoord[i].x = matchData.getItem(i).ref.pos3d.x;
			wCoord[i].y = matchData.getItem(i).ref.pos3d.y;
			wCoord[i].z = 0.0;
		}

		// icpData.num = i;
		// icpData.screenCoord = &sCoord[0];
		// icpData.worldCoord = &wCoord[0];
		NyARIcpPlane icp_planer = new NyARIcpPlane(i_cparam.getPerspectiveProjectionMatrix());
		if (!icp_planer.icpGetInitXw2Xc_from_PlanarData(sCoord, wCoord, matchData.getLength(), initMatXw2Xc)) {
			return false;
		}
		/*
		 * printf("--- Init pose ---\n"); for( int j = 0; j < 3; j++ ) { for( i = 0; i < 4; i++ ) printf(" %8.3f",
		 * initMatXw2Xc[j][i]); printf("\n"); }
		 */
		NyARIcpPoint icp_point = new NyARIcpPoint(i_cparam.getPerspectiveProjectionMatrix());
		icp_point.icpPoint(sCoord, wCoord, matchData.getLength(), initMatXw2Xc, kpmResult.camPose,
				kpmResult.resultparams);

		if (kpmResult.resultparams.last_error > 10.0f) {
			return false;
		}

		return true;
	}

}
