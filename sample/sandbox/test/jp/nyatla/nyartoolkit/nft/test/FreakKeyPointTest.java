package jp.nyatla.nyartoolkit.nft.test;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;


import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;

import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;

import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;

import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;

import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.utils.j2se.sketch.AwtSketch;


public class FreakKeyPointTest extends AwtSketch{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			//カメラパラメータ
			new FreakKeyPointTest().run();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setup(Frame i_frame, Canvas i_canvas) throws Exception {
		int H=726,W=1024;
		INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(W,H);
		//試験画像の準備
		
		NyARBufferedImageRaster rgb=NyARBufferedImageRaster.loadFromFile("d:\\IMG_0232.jpg");
		INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
		filter.convert(gs);				
		
		DogFeaturePointStack _dog_feature_points = new DogFeaturePointStack(1000);
		FREAKExtractor mFeatureExtractor=new FREAKExtractor();
		int octerves=BinomialPyramid32f.octavesFromMinimumCoarsestSize(W,H,8);
		BinomialPyramid32f _pyramid=new BinomialPyramid32f(W,H,octerves,3);
		DoGScaleInvariantDetector _dog_detector = new DoGScaleInvariantDetector(W,H,octerves,3,3,4,300);
		
		query_keypoint = new FreakFeaturePointStack(300);
		//Freak Extract

		// Build the pyramid		
		_pyramid.build(gs);
		// Detect feature points
		_dog_detector.detect(_pyramid,_dog_feature_points);

		// Extract features
		query_keypoint.clear();
		mFeatureExtractor.extract(_pyramid,_dog_feature_points,query_keypoint);
		this._bg=rgb.getBufferedImage();
	}
	FreakFeaturePointStack query_keypoint;
	BufferedImage _bg;
	@Override
	public void draw(Canvas i_canvas) throws Exception {
		Graphics g=i_canvas.getGraphics();
		g.drawImage(_bg,0,0,this._frame);
		g.setColor(Color.red);
		for(int i=0;i<query_keypoint.getLength();i++){
			FreakFeaturePoint p=query_keypoint.getItem(i);
			int s=(int)p.scale*10;
			int h=(int)p.scale*10;
			g.drawArc((int)p.x-s/2,(int)p.y-s/2,s,s,0,360);
		}
		g.dispose();
		// TODO Auto-generated method stub
		
	}

}
