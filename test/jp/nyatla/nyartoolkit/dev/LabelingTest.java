package jp.nyatla.nyartoolkit.dev;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;

import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BGRA;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.types.*;
import jp.nyatla.nyartoolkit.core2.rasteranalyzer.*;
import jp.nyatla.utils.j2se.*;

public class LabelingTest extends Frame
{
    private final String data_file  ="../Data/320x240ABGR.raw";
    public void drawImage() throws Exception
    {
	File f=new File(data_file);
	FileInputStream fs=new FileInputStream(data_file);
	byte[] buf=new byte[(int)f.length()];
	fs.read(buf);
	NyARRgbRaster_BGRA ra=NyARRgbRaster_BGRA.wrap(buf, 320, 240);
	NyARLabelingImage limage=new NyARLabelingImage(320,240);
	INyARLabeling labeling=new NyARLabeling_ARToolKit();
//	INyARLabeling labeling=new NyLineLabeling();
	INyARRasterReaderFactory rf=new NyARRasterReaderFactory_RgbTotal();
	labeling.attachDestination(limage);
	labeling.labeling(rf.createReader(ra));
	LabelingBufferdImage img=new LabelingBufferdImage(320,240,LabelingBufferdImage.COLOR_125_COLOR);
	img.setLabelingImage(limage);
	this.getGraphics().drawImage(img, 32,32,this);
    }
    public static void main(String[] args)
    {
	try{
	    LabelingTest app=new LabelingTest();
	    app.setVisible(true);
	    app.setBounds(0,0,640,480);
	    app.drawImage();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
