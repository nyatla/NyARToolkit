package jp.nyatla.nyartoolkit.core.types;

public class NyARBufferType
{
	private static final int T_BYTE1D =0x00010000;
	private static final int T_INT2D  =0x00020000;
	private static final int T_SHORT1D=0x00030000;
	private static final int T_INT1D  =0x00040000;
	private static final int T_OBJECT =0x00100000;
	private static final int T_USER   =0x00FF0000;
	//  24-31(8)予約
	//  16-27(8)型ID
	//      00:無効/01:byte[]/02:int[][]/03:short[]
	//  08-15(8)ビットフォーマットID
	//      00:24bit/01:32bit/02:16bit
	//  00-07(8)型番号
	//
	/**
	 * RGB24フォーマットで、全ての画素が0
	 */
	public static final int NULL_ALLZERO = 0x00000001;
	/**
	 * USER - USER+0xFFFFはユーザー定義型。実験用に。
	 */
	public static final int USER_DEFINE  = T_USER;

	/**
	 * byte[]で、R8G8B8の24ビットで画素が格納されている。
	 */
	public static final int BYTE1D_R8G8B8_24   = T_BYTE1D|0x0001;
	/**
	 * byte[]で、B8G8R8の24ビットで画素が格納されている。
	 */
	public static final int BYTE1D_B8G8R8_24   = T_BYTE1D|0x0002;
	/**
	 * byte[]で、R8G8B8X8の32ビットで画素が格納されている。
	 */
	public static final int BYTE1D_B8G8R8X8_32 = T_BYTE1D|0x0101;
	/**
	 * byte[]で、X8R8G8B8の32ビットで画素が格納されている。
	 */
	public static final int BYTE1D_X8R8G8B8_32 = T_BYTE1D|0x0102;

	/**
	 * byte[]で、RGB565の16ビット(little/big endian)で画素が格納されている。
	 */
	public static final int BYTE1D_R5G6B5_16LE = T_BYTE1D|0x0201;
    public static final int BYTE1D_R5G6B5_16BE = T_BYTE1D|0x0202;
	/**
	 * short[]で、RGB565の16ビット(little/big endian)で画素が格納されている。
	 */	
    public static final int WORD1D_R5G6B5_16LE = T_SHORT1D|0x0201;
    public static final int WORD1D_R5G6B5_16BE = T_SHORT1D|0x0202;

	
	/**
	 * int[][]で特に値範囲を定めない
	 */
	public static final int INT2D        = T_INT2D|0x0000;
	/**
	 * int[][]で0-255のグレイスケール画像
	 */
	public static final int INT2D_GRAY_8 = T_INT2D|0x0001;
	/**
	 * int[][]で0/1の2値画像
	 * これは、階調値1bitのBUFFERFORMAT_INT2D_GRAY_1と同じです。
	 */
	public static final int INT2D_BIN_8  = T_INT2D|0x0002;

	/**
	 * int[]で特に値範囲を定めない
	 */
	public static final int INT1D        = T_INT1D|0x0000;
	/**
	 * int[]で0-255のグレイスケール画像
	 */
	public static final int INT1D_GRAY_8 = T_INT1D|0x0001;
	/**
	 * int[]で0/1の2値画像
	 * これは、階調1bitのINT1D_GRAY_1と同じです。
	 */
	public static final int INT1D_BIN_8  = T_INT1D|0x0002;
	
	
	/**
	 * int[]で、XRGB32の32ビットで画素が格納されている。
	 */	
    public static final int INT1D_X8R8G8B8_32=T_INT1D|0x0102;

	/**
	 * H:9bit(0-359),S:8bit(0-255),V(0-255)
	 */
	public static final int INT1D_X7H9S8V8_32=T_INT1D|0x0103;
    

    /**
     * プラットフォーム固有オブジェクト
     */
	public static final int OBJECT_Java= T_OBJECT|0x0100;
	public static final int OBJECT_CS  = T_OBJECT|0x0200;
	public static final int OBJECT_AS3 = T_OBJECT|0x0300;
	
	/**
	 * JavaのBufferedImageを格納するラスタ
	 */
	public static final int OBJECT_Java_BufferedImage= OBJECT_Java|0x01;
	
	
	/**
	 * ActionScript3のBitmapDataを格納するラスタ
	 */
	public static final int OBJECT_AS3_BitmapData= OBJECT_AS3|0x01;

}
