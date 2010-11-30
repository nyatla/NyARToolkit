package jp.nyatla.nyartoolkit.dev;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPickup;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.sample.RawFileTest;
import jp.nyatla.nyartoolkit.utils.j2se.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.pickup.*;


public class PattPickupTestS extends Frame
{
	private final String code_file = "../Data/flarlogo.pat";

	private final static String SAMPLE_FILES = "../Data/flarlogo_45.png";

	private final String camera_file = "../Data/camera_para.dat";



	public void Test_arDetectMarkerLite() throws Exception
	{
		// AR用カメラパラメタファイルをロード
		NyARParam ap = new NyARParam();
		ap.loadARParamFromFile(camera_file);


		// AR用のパターンコードを読み出し
		NyARCode code = new NyARCode(16, 16);
		code.loadARPattFromFile(code_file);

		// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
		BufferedImage src_image = ImageIO.read(new File(SAMPLE_FILES));
		INyARRgbRaster ra  = new NyARRgbRaster_RGB(src_image.getWidth(),src_image.getHeight(),true);
		NyARRasterImageIO.copy(src_image, ra);
		
		
		// NyARToolkitの準備
		ap.changeScreenSize(src_image.getWidth(),src_image.getHeight());


		// Blank_Raster ra=new Blank_Raster(320, 240);

		// １パターンのみを追跡するクラスを作成
		NyARSingleDetectMarker ar = new NyARSingleDetectMarker(
				ap, code, 80.0,ra.getBufferType(),NyARSingleDetectMarker.PF_NYARTOOLKIT);
		NyARTransMatResult result_mat = new NyARTransMatResult();
		ar.setContinueMode(false);
		ar.detectMarkerLite(ra, 100);
		ar.getTransmationMatrix(result_mat);

		NyARDoublePoint3d ang=new NyARDoublePoint3d();
		result_mat.getZXYAngle(ang);
		NyARRasterImageIO.copy(((INyARColorPatt)(ar._getProbe()[0])),b);
		
	}
	BufferedImage b=new BufferedImage(16,16,ColorSpace.TYPE_RGB);

	public void drawImage(){
		Graphics g=this.getGraphics();
		g.drawImage(b,50,50,100,100,null);
	};	
	public static void main(String[] args)
	{
		try {
			PattPickupTestS app = new PattPickupTestS();
			app.setVisible(true);
			app.setBounds(0, 0, 640, 480);
			app.Test_arDetectMarkerLite();
			app.drawImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
