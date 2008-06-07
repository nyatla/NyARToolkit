/**
 * JMFお手軽キャプチャクラス
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf.utils;



import javax.media.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.format.*;
import java.awt.*;
import java.util.*;
import javax.media.protocol.DataSource;

import jp.nyatla.nyartoolkit.NyARException;





public class JmfCameraCapture
{
    private Dimension image_size;
    private JmfCaptureListener capture_listener;
    private DataSource jmf_data_source;
    private MonitorStream jmf_monitor_stream;
    private Processor jmf_processor;
    private VideoFormat jmf_video_format;
    
    private Buffer read_buf=new Buffer();
    public static final String PIXEL_FORMAT_RGB="RGB";
    public JmfCameraCapture(int i_width,int i_height,float i_rate,String i_pixcel_format)
    {
        String encoding = i_pixcel_format;//comboEncoding.getSelectedItem();
        image_size = new Dimension(i_width,i_height);
        jmf_video_format = new VideoFormat(encoding, image_size, Format.NOT_SPECIFIED,null,i_rate);
    }
    public Dimension getSize()
    {
	return image_size;
    }
    public javax.media.Buffer readBuffer() throws NyARException
    {
	if(jmf_monitor_stream==null){
            throw new NyARException();
	}
        try{
            jmf_monitor_stream.read(read_buf);
        }catch(Exception e){
            throw new NyARException(e);
        }
        return read_buf;
    }
    public void setCaptureListener(JmfCaptureListener i_listener) throws NyARException
    {
	if(jmf_processor!=null){
	    throw new NyARException();
	}
	capture_listener=i_listener;
	
    }
    public void start() throws NyARException
    {
        
        DataSource ds=getCaptureDS(jmf_video_format);
        VideoFormat[] formats=new VideoFormat[]{new VideoFormat(null)};
        ProcessorModel pm = new ProcessorModel(ds,formats,null);//, formats, ftd);
        Processor processor;
        try {
            processor = Manager.createRealizedProcessor(pm);
        } catch (Exception e){
            // Make sure the capture devices are released
            ds.disconnect();
            throw new NyARException(e);
        }
        // Get the monitor control:
        // Since there are more than one MonitorControl objects
        // exported by the DataSource, we get the specific one
        // that is also the MonitorStream object.
        jmf_monitor_stream=(MonitorStream)ds.getControl("jmfsample.MonitorStream");
	jmf_monitor_stream.setCaptureListener(capture_listener);
        jmf_data_source=ds;
        jmf_processor=processor;
        jmf_processor.start();
    }
    public void stop()
    {
        jmf_processor.stop();
        jmf_processor.close();
        jmf_processor = null;
        
    }
    protected void finalize()
    {
        if(jmf_processor!=null){
        jmf_processor.stop();
        jmf_processor.close();
        jmf_processor = null;
        }
    }
    private static DataSource getCaptureDS(VideoFormat vf) {
	DataSource dsVideo = null;
	DataSource ds = null;

	// Create a capture DataSource for the video
	// If there is no video capture device, then exit with null
	if (vf != null) {
	    dsVideo = createDataSource(vf);
	    if (dsVideo == null)
		return null;
	}


	// Create the monitoring datasource wrapper
	if (dsVideo != null) {
	    dsVideo = new MonitorCDS(dsVideo);
	    return dsVideo;
	}

	// Merge the data sources, if both audio and video are available
	try {
	    ds = Manager.createMergingDataSource(new DataSource[]{dsVideo});
	} catch (IncompatibleSourceException ise){
	    return null;
	}

	return ds;
    }

    private static DataSource createDataSource(Format format) {
	DataSource ds;
	Vector devices;
	CaptureDeviceInfo cdi;
	MediaLocator ml;

	// Find devices for format
	devices = CaptureDeviceManager.getDeviceList(format);
	if (devices.size() < 1) {
	    System.err.println("! No Devices for " + format);
	    return null;
	}
	// Pick the first device
	cdi = (CaptureDeviceInfo) devices.elementAt(0);

	ml = cdi.getLocator();

	try {
	    ds = Manager.createDataSource(ml);
	    ds.connect();
	    if (ds instanceof CaptureDevice)
	    {
		setCaptureFormat((CaptureDevice) ds, format);
	    }
	} catch (Exception e) {
	    System.err.println(e);
	    return null;
	}
	return ds;
    }

    private static void setCaptureFormat(CaptureDevice cdev, Format format) {
	FormatControl [] fcs = cdev.getFormatControls();
	if (fcs.length < 1){
	    return;
	}
	FormatControl fc = fcs[0];
	Format [] formats = fc.getSupportedFormats();
	for (int i = 0; i < formats.length; i++) {
	    if (formats[i].matches(format)){
		format = formats[i].intersects(format);
		fc.setFormat(format);
		break;
	    }
	}
    }
}