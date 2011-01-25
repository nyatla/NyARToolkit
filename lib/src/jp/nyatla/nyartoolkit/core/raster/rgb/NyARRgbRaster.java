package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

/**
 * このクラスは、指定形式のバッファを持つRGBラスタです。
 * 外部参照バッファ、内部バッファの両方に対応します。
 * <p>
 * 対応しているバッファタイプ-
 * <ul>{@link NyARBufferType#INT1D_X8R8G8B8_32}
 * <li>{@link NyARBufferType#BYTE1D_B8G8R8X8_32}
 * <li>{@link NyARBufferType#BYTE1D_R8G8B8_24}
 * <li>{@link NyARBufferType#BYTE1D_B8G8R8_24}
 * <li>{@link NyARBufferType#BYTE1D_X8R8G8B8_32}
 * <li>{@link NyARBufferType#WORD1D_R5G6B5_16LE}
 * </ul>
 * </p>
 */
public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	/** バッファオブジェクト*/
	protected Object _buf;
	/** ピクセルリーダ*/
	protected INyARRgbPixelReader _reader;
	/** バッファオブジェクトがアタッチされていればtrue*/
	protected boolean _is_attached_buffer;
	
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータとバッファ形式を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_raster_type
	 * ラスタのバッファ形式。
	 * {@link NyARBufferType}に定義された定数値を指定してください。
	 * 指定できる値は、クラスの説明を見てください。
	 * @param i_is_alloc
	 * バッファを外部参照にするかのフラグ値。
	 * trueなら内部バッファ、falseなら外部バッファを使用します。
	 * falseの場合、初期のバッファはnullになります。インスタンスを生成したのちに、{@link #wrapBuffer}を使って割り当ててください。
	 * @throws NyARException
	 */
	public NyARRgbRaster(int i_width, int i_height,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,i_raster_type);
		if(!initInstance(this._size,i_raster_type,i_is_alloc)){
			throw new NyARException();
		}
	}
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータとバッファ形式を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_raster_type
	 * ラスタのバッファ形式。
	 * {@link NyARBufferType}に定義された定数値を指定してください。
	 * 指定できる値は、クラスの説明を見てください。
	 * @throws NyARException
	 */
	public NyARRgbRaster(int i_width, int i_height,int i_raster_type) throws NyARException
	{
		super(i_width,i_height,i_raster_type);
		if(!initInstance(this._size,i_raster_type,true)){
			throw new NyARException();
		}
	}
	/**
	 * Readerとbufferを初期化する関数です。コンストラクタから呼び出します。
	 * 継承クラスでこの関数を拡張することで、対応するバッファタイプの種類を増やせます。
	 * @param i_size
	 * ラスタのサイズ
	 * @param i_raster_type
	 * バッファタイプ
	 * @param i_is_alloc
	 * 外部参照/内部バッファのフラグ
	 * @return
	 * 初期化が成功すると、trueです。
	 */
	protected boolean initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc)
	{
		switch(i_raster_type)
		{
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._buf=i_is_alloc?new int[i_size.w*i_size.h]:null;
				this._reader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32((int[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*4]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_B8G8R8X8_32((byte[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*3]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_R8G8B8_24((byte[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_B8G8R8_24:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*3]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_B8G8R8_24((byte[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_X8R8G8B8_32:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*4]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_X8R8G8B8_32((byte[])this._buf,i_size);
				break;
			case NyARBufferType.WORD1D_R5G6B5_16LE:
				this._buf=i_is_alloc?new short[i_size.w*i_size.h]:null;
				this._reader=new NyARRgbPixelReader_WORD1D_R5G6B5_16LE((short[])this._buf,i_size);
				break;
			default:
				return false;
		}
		this._is_attached_buffer=i_is_alloc;
		return true;
	}
	/**
	 * この関数は、画素形式によらない画素アクセスを行うオブジェクトへの参照値を返します。
	 * @return
	 * オブジェクトの参照値
	 * @throws NyARException
	 */	
	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式は、コンストラクタに指定した形式と同じです。
	 */	
	public Object getBuffer()
	{
		return this._buf;
	}
	/**
	 * インスタンスがバッファを所有するかを返します。
	 * コンストラクタでi_is_allocをfalseにしてラスタを作成した場合、
	 * バッファにアクセスするまえに、バッファの有無をこの関数でチェックしてください。
	 * @return
	 * インスタンスがバッファを所有すれば、trueです。
	 */		
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファの時にだけ使えます。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=i_ref_buf;
		//ピクセルリーダーの参照バッファを切り替える。
		this._reader.switchBuffer(i_ref_buf);
	}
}
