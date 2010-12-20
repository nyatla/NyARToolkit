
package jp.nyatla.nyartoolkit.dev;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve192;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

import jp.nyatla.nyartoolkit.utils.j2se.*;


class Main_Labeling extends NyARLabeling_Rle
{
	private NyARIntPoint2d[] vx=NyARIntPoint2d.createArray(100);
	public NyARGrayscaleRaster current_gs;
	public int current_th;
	public Main_Labeling(int i_width,int i_height) throws NyARException
	{
		super(i_width,i_height);
	}
	/**
	 * @Override
	 */
	protected void onLabelFound(NyARRleLabelFragmentInfo iRefLabel)throws NyARException
	{
		NyARContourPickup ct=new NyARContourPickup();
		Date d2 = new Date();
		
		for(int i=0;i<100000;i++){
			int c=ct.getContour(this.current_gs,this.current_th, iRefLabel.entry_x,iRefLabel.clip_t, this.vx);
		}
		Date d = new Date();
		NyARDoublePoint3d ang=new NyARDoublePoint3d();
		System.out.println(d.getTime() - d2.getTime());

		return;
	}
}

/**
 * ラべリングと、輪郭線抽出のテストコード
 * @author nyatla
 *
 */
public class LabelingTest extends Frame
{
	private final String source_file = "../Data/ラべリングのエントリポイントエラー.png";
	private BufferedImage _src_image;
	public LabelingTest() throws NyARException,Exception
	{
		this._src_image = ImageIO.read(new File(source_file));
		
		INyARRgbRaster ra =new NyARRgbRaster_RGB(this._src_image.getWidth(),this._src_image.getHeight());
		NyARRasterImageIO.copy(this._src_image,ra);
		//GS値化
		NyARGrayscaleRaster gs=new NyARGrayscaleRaster(this._src_image.getWidth(),this._src_image.getHeight());
		NyARRasterFilter_Rgb2Gs_RgbAve192 filter=new NyARRasterFilter_Rgb2Gs_RgbAve192(ra.getBufferType());
		filter.doFilter(ra,gs);
		//ラべリングの試験
		Main_Labeling lv=new Main_Labeling(ra.getWidth(),ra.getHeight());
		lv.current_gs=gs;
		lv.current_th=230;
		lv.labeling(gs,lv.current_th);
		//画像をストア
		
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		//ペイント
	}

	public static void main(String[] args)
	{
		try {
			LabelingTest app = new LabelingTest();
			app.setVisible(true);
			app.setBounds(0, 0, 640, 480);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
