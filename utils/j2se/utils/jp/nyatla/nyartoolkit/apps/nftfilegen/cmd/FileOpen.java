package jp.nyatla.nyartoolkit.apps.nftfilegen.cmd;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;

public class FileOpen extends Cmd{

	
	public FileOpen(Component c)
	{
		super(c);
	}
	public File openFile()
	{
	    JFileChooser filechooser = new JFileChooser();

	    int selected = filechooser.showOpenDialog(this._parent);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	return filechooser.getSelectedFile();
	    }else{
			return null;
	    }
	}
	public BufferedImage openImage() throws IOException
	{
		File f=this.openFile();
		if(f==null){
			return null;
		}
		return ImageIO.read(f);
	}
	public NyARBufferedImageRaster openNyARRaster() throws IOException
	{
		BufferedImage b=this.openImage();
		if(b==null){
			return null;
		}
		return new NyARBufferedImageRaster(b);
	}
}
