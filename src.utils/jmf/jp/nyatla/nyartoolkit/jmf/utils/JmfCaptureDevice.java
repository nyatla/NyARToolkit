package jp.nyatla.nyartoolkit.jmf.utils;

import java.awt.Dimension;

import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;

import jp.nyatla.nyartoolkit.NyARException;

public class JmfCaptureDevice
{
	private JmfCaptureListener _capture_listener;
	private MonitorStream _jmf_monitor_stream;
	private Processor _jmf_processor;
	private CaptureDeviceInfo _info;
	private Format _capture_format;
	public static final String PIXEL_FORMAT_RGB = "RGB";
	public static final String PIXEL_FORMAT_YUV = "YUV";
	

	public JmfCaptureDevice(CaptureDeviceInfo i_capinfo) throws NyARException
	{
		this._info = i_capinfo;
		this._capture_format = null;
		return;
	}

	public Format[] getSupportFormats()
	{
		return this._info.getFormats();
	}

	public final Format getCaptureFormat()
	{
		return this._capture_format;
	}

	/**
	 * このキャプチャデバイスの提供する、i_index番目のフォーマットをキャプチャフォーマットに指定します。
	 * @param i_index
	 */
	public void setCaptureFormat(int i_index)
	{
		this._capture_format = this._info.getFormats()[i_index];
		return;
	}
	/**
	 * キャプチャ画像のフォーマットを指定した形式にしようと試みます。
	 * @param i_encode
	 * キャプチャする画像フォーマットを指定します。フォーマットはこのクラスに宣言される定数値を使ってください。
	 * @param i_size
	 * キャプチャ画像サイズを指定します。
	 * @param i_rate
	 * キャプチャレートをFPS単位で指定します。
	 * @return
	 * 指定に成功するとTRUEを返します。失敗するとFALSEを返します。
	 */	
	public boolean setCaptureFormat(String i_encode, Dimension i_size, float i_rate) throws NyARException
	{
		if (this._jmf_processor != null) {
			throw new NyARException();
		}
		Format[] formats = this._info.getFormats();
		Format f = new VideoFormat(i_encode, i_size, Format.NOT_SPECIFIED, null, i_rate);
		for (int i = 0; i < formats.length; i++) {
			if (formats[i].matches(f)) {
				f = formats[i].intersects(f);
				this._capture_format = null;
				this._capture_format = f;
				return true;
			}
		}
		//ない。
		return false;
	}
	public boolean setCaptureFormat(String i_encode,int i_size_x,int i_size_y, float i_rate) throws NyARException
	{
		return setCaptureFormat(i_encode,new Dimension(i_size_x,i_size_y),i_rate);
	}
	/**
	 * 画像のキャプチャイベントを受信するリスナクラスを指定します。
	 * @param i_listener
	 * リスナークラス
	 * @throws NyARException
	 */
	public void setOnCapture(JmfCaptureListener i_listener) throws NyARException
	{
		if (this._jmf_processor != null) {
			throw new NyARException();
		}
		this._capture_listener = i_listener;
		return;
	}
	/**
	 * キャプチャーを開始します。stop関数を呼び出すまでの間、setOnCaptureで指定したリスナークラスに、
	 * フォーマットで指定したキャプチャ画像が通知されます。
	 * @throws NyARException
	 */
	public void start() throws NyARException
	{
		// startしていたらエラー
		if (this._jmf_processor != null) {
			throw new NyARException();
		}
		DataSource ds;
		final MediaLocator ml = this._info.getLocator();
		try {
			ds = Manager.createDataSource(ml);
			ds.connect();
			// ここでフォーマットを作成
			if (ds instanceof CaptureDevice) {
				FormatControl[] fcs = ((CaptureDevice) ds).getFormatControls();
				if (fcs.length < 1) {
					return;
				}
				FormatControl fc = fcs[0];
				fc.setFormat(this._capture_format);
			}
		} catch (Exception e) {
			throw new NyARException(e);
		}
		try{
			if(ds==null){
				//Merge the data sources, if both audio and video are available
				ds = Manager.createMergingDataSource(new DataSource[] { null });				
			}else{
				// Create the monitoring datasource wrapper
				ds = new MonitorCDS(ds);				
			}
		}catch(IncompatibleSourceException e){
			throw new NyARException(e);
		}
		
		// データソース完成
		try {
			// Merge the data sources, if both audio and video are available
			VideoFormat[] formats = new VideoFormat[] { new VideoFormat(null) };
			ProcessorModel pm = new ProcessorModel(ds, formats, null);// ,
			Processor processor;
			processor = Manager.createRealizedProcessor(pm);
			this._jmf_monitor_stream = (MonitorStream) ds.getControl("jmfsample.MonitorStream");
			this._jmf_monitor_stream.setCaptureListener(this._capture_listener);
			this._jmf_processor = processor;
			this._jmf_processor.start();
		} catch (Exception e) {
			ds.disconnect();
			throw new NyARException(e);
		}
		return;
	}

	public void stop()
	{
		this._jmf_processor.stop();
		this._jmf_processor.close();
		this._jmf_processor = null;
		return;
	}
	protected void finalize()
	{
		if (this._jmf_processor != null) {
			this._jmf_processor.stop();
			this._jmf_processor.close();
			this._jmf_processor = null;
		}
		return;
	}
}