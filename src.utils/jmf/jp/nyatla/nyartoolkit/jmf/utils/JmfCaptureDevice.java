package jp.nyatla.nyartoolkit.jmf.utils;

import java.awt.Dimension;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import jp.nyatla.nyartoolkit.*;

public class JmfCaptureDevice
{
	private JmfCaptureListener _capture_listener;
	private MonitorStream _jmf_monitor_stream;
	private Processor _jmf_processor;
	private CaptureDeviceInfo _info;
	private VideoFormat _capture_format;
	private static final String[] _enc_str={"RGB","YUV"};	
	public static final int PIXEL_FORMAT_RGB =0;// "RGB";
	public static final int PIXEL_FORMAT_YUV =1;// "YUV";
	public JmfCaptureDevice(CaptureDeviceInfo i_capinfo) throws NyARException
	{
		this._info = i_capinfo;
		this._capture_format = null;
		return;
	}

	/**
	 * サポートしているフォーマットの一覧を返します。
	 * @return
	 */
	public Format[] getSupportFormats()
	{
		return this._info.getFormats();
	}

	public final VideoFormat getCaptureFormat()
	{
		return this._capture_format;
	}

	/**
	 * このキャプチャデバイスの提供する、i_index番目のフォーマットをキャプチャフォーマットに指定します。
	 * @param i_index
	 */
	public void setCaptureFormat(int i_index)
	{
		this._capture_format = (VideoFormat)this._info.getFormats()[i_index];
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
	protected boolean setCaptureFormat(int i_encode, Dimension i_size, float i_rate) throws NyARException
	{
		if (this._jmf_processor != null){
			throw new NyARException();
		}
		Format[] formats = this._info.getFormats();
		VideoFormat f = new VideoFormat(_enc_str[i_encode], i_size, Format.NOT_SPECIFIED, null, i_rate);
		for (int i = 0; i < formats.length; i++){
			if (formats[i].matches(f)) {
				//[暫定実装]RGBの場合のみ、24bit-BGRAを強制する。他のフォーマットも取りたいときは要改造
				//これはMacOSのJMF等で問題が出るかもしれない。問題が出たら教えて下さい。
				if(formats[i] instanceof RGBFormat){
					RGBFormat fmt_ref=(RGBFormat)formats[i];
					if(fmt_ref.getBitsPerPixel()!=24 || fmt_ref.getBlueMask()!=1 || fmt_ref.getGreenMask()!=2 || fmt_ref.getRedMask()!=3){
						continue;
					}
				}
				f =(VideoFormat)formats[i].intersects(f);
				this._capture_format = null;
				this._capture_format = f;
				return true;
			}
		}
		//ない。
		return false;
	}
	/**
	 * キャプチャ画像のエンコード、サイズ、レートを引数とするsetCaptureFormat関数です。
	 * @param i_encode
	 * PIXEL_FORMAT_XXXで定義される定数値を指定して下さい。
	 * @param i_size_x
	 * キャプチャする画像の横幅
	 * @param i_size_y
	 * キャプチャする画像の縦幅
	 * @param i_rate
	 * フレームレート
	 * @return
	 * 関数の実行結果を真偽値で返します。
	 * @throws NyARException
	 */	
	public boolean setCaptureFormat(int i_encode,int i_size_x,int i_size_y, float i_rate) throws NyARException
	{
		return setCaptureFormat(i_encode,new Dimension(i_size_x,i_size_y),i_rate);
	}
	/**
	 * キャプチャ画像のサイズ、レートを引数とするsetCaptureFormat関数です。
	 * キャプチャ画像のエンコードは、RGB→YUVの順で検索します。
	 * @param i_size_x
	 * キャプチャする画像の横幅
	 * @param i_size_y
	 * キャプチャする画像の縦幅
	 * @param i_rate
	 * フレームレート
	 * @return
	 * 関数の実行結果を真偽値で返します。
	 * @throws NyARException
	 */
	public boolean setCaptureFormat(int i_size_x,int i_size_y, float i_rate) throws NyARException
	{
		Dimension d=new Dimension(i_size_x,i_size_y);
		if(setCaptureFormat(PIXEL_FORMAT_RGB,d,i_rate)){
			return true;
		}
		if(setCaptureFormat(PIXEL_FORMAT_YUV,d,i_rate)){
			return true;
		}
		return false;
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