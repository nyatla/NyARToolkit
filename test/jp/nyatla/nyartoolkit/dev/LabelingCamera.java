/**
 * VFM+ARToolkitテストプログラム
 * カメラから取り込んだデータからマーカーを検出して、一致度と変換行列を表示します。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.dev;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;


import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.processor.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasteranalyzer.*;
import jp.nyatla.nyartoolkit.core.rasteranalyzer.threshold.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.util.j2se.LabelingBufferdImage;

public class LabelingCamera extends Frame implements JmfCaptureListener {
    private JmfNyARRaster_RGB _raster;
    private JmfCameraCapture capture;


    public LabelingCamera() throws NyARException, NyARException
    {
	setBounds(0, 0, 640 + 64, 720 + 64);
	// キャプチャの準備
	capture = new JmfCameraCapture(320, 240, 30f,
		JmfCameraCapture.PIXEL_FORMAT_RGB);
	capture.setCaptureListener(this);

	// キャプチャイメージ用のラスタを準備
	this._raster = new JmfNyARRaster_RGB(320, 240);
    }
    
    private NyARBinRaster _binraster1=new NyARBinRaster(320,240);
    private NyARGlayscaleRaster _gsraster1=new NyARGlayscaleRaster(320,240);
    private NyARLabelingImage _limage=new NyARLabelingImage(320, 240);
    private LabelingBufferdImage _bimg=new LabelingBufferdImage(320, 240,LabelingBufferdImage.COLOR_256_MONO);
    private LabelingBufferdImage _bimg2=new LabelingBufferdImage(320, 240,LabelingBufferdImage.COLOR_256_MONO);

    public void onUpdateBuffer(Buffer i_buffer)
    {
	try {
	    // キャプチャしたバッファをラスタにセット
	    _raster.setBuffer(i_buffer);
	    
	    Graphics g = getGraphics();
	    //キャプチャ画像
	    BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
	    Image img = b2i.createImage(i_buffer);
	    this.getGraphics().drawImage(img, 32, 32, this);
	    
	    INyARRasterFilter_GsToBin filter_gs2bin;
	    //画像1
	    INyARRasterFilter_RgbToGs filter_rgb2gs=new NyARRasterFilter_RgbAve();
	    filter_rgb2gs.doFilter(_raster, _gsraster1);
	    this._bimg2.drawImage(this._gsraster1);
	    this.getGraphics().drawImage(this._bimg2, 32+320, 32,320+320+32,240+32,0,240,320,0, this);

	    //画像2
	    filter_gs2bin=new NyARRasterFilter_ARToolkitThreshold(128);
	    filter_gs2bin.doFilter(_gsraster1, _binraster1);
	    this._bimg.drawImage(_binraster1);
	    this.getGraphics().drawImage(this._bimg, 32, 32+240,320+32,240+32+240,0,240,320,0, this);
	    //画像3
	    //threshold.debugDrawHistgramMap(_workraster, _workraster2);
	    //this._bimg2.setImage(this._workraster2);
	    //this.getGraphics().drawImage(this._bimg2, 32+320, 32+240,320+32+320,240+32+240,0,240,320,0, this);

	    //画像4
	    NyARRasterThresholdAnalyzer_SlidePTile threshold=new NyARRasterThresholdAnalyzer_SlidePTile(15);
	    threshold.analyzeRaster(_gsraster1);
	    filter_gs2bin=new NyARRasterFilter_AreaAverage();
	    filter_gs2bin.doFilter(_gsraster1, _binraster1);
	    this._bimg.drawImage(_binraster1);
	    
	    NyARRasterDetector_QrCodeEdge detector=new NyARRasterDetector_QrCodeEdge(10000);
	    detector.analyzeRaster(_binraster1);
	    
	    this._bimg.overlayData(detector.geResult());
	    
	    this.getGraphics().drawImage(this._bimg, 32, 32+480,320+32,480+32+240,0,240,320,0, this);
	    //画像5
	    
	    
/*	    threshold2.debugDrawHistgramMap(_workraster, _workraster2);
	    this._bimg2.drawImage(this._workraster2);
	    this.getGraphics().drawImage(this._bimg2, 32+320, 32+480,320+32+320,480+32+240,0,240,320,0, this);
*/	    
	    
	    //	    this.getGraphics().drawImage(this._bimg, 32, 32, this);


	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
    private INyARLabeling labelingFactory(int i_idx)
    {
	switch(i_idx){
	case 0:{ARToolKitLabeling l=new ARToolKitLabeling();l.setThresh(4);return l;}
	case 1:{return new NyLineLabeling();}
	}
	return null;
	
	
    }

    private void startCapture()
    {
	try {
	    capture.start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	try {
	    LabelingCamera mainwin = new LabelingCamera();
	    mainwin.setVisible(true);
	    mainwin.startCapture();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
