package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;

public interface INyARMarkerSystemSquareDetect {
	public void detectMarkerCb(NyARSensor i_sensor,int i_th,NyARSquareContourDetector.CbHandler i_handler) throws NyARException;

}
