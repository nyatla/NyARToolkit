package jp.nyatla.nyartoolkit.apps.nftfilegen.cmd;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;

public class FileOpen extends Cmd{

	
	public FileOpen(Component c)
	{
		super(c);
	}

	public File saveFile(FileFilter[] ff)
	{
	    JFileChooser filechooser = new JFileChooser();
	    if(ff!=null){
	    	for(FileFilter  i:ff){
	    		filechooser.addChoosableFileFilter(i);
	    	}
	    }
	    int selected = filechooser.showSaveDialog(this._parent);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	return filechooser.getSelectedFile();
	    }else{
			return null;
	    }
	}
	public File openFile(FileFilter[] ff)
	{
	    JFileChooser filechooser = new JFileChooser();
	    if(ff!=null){
	    	for(FileFilter  i:ff){
	    		filechooser.addChoosableFileFilter(i);
	    	}
	    }	    
	    int selected = filechooser.showOpenDialog(this._parent);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	return filechooser.getSelectedFile();
	    }else{
			return null;
	    }
	}
	public BufferedImage openImage() throws IOException
	{
		FileFilter filter[] = {
			new FileNameExtensionFilter("Jpeg File", "jpeg", "jpg"),
			new FileNameExtensionFilter("Png File", "png")
		};
		
		File f=this.openFile(filter);
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
