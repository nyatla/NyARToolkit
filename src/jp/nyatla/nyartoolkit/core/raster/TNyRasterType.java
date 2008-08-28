package jp.nyatla.nyartoolkit.core.raster;

public final class TNyRasterType
{
    /**
     * RGB24フォーマットで、全ての画素が0
     */
    public static final int BUFFERFORMAT_NULL_ALLZERO     =0x00000001;
    /**
     * byte[]で、R8G8B8の24ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE1D_R8G8B8_24  =0x00010001;
    /**
     * byte[]で、B8G8R8の24ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE1D_B8G8R8_24  =0x00010002;
    /**
     * byte[]で、R8G8B8X8の32ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE1D_B8G8R8X8_32=0x00010101;


    /**
     * int[][]で特に値範囲を定めない
     */
    public static final int BUFFERFORMAT_INT2D             =0x00020000;
    /**
     * int[][]で0-255のグレイスケール画像
     */
    public static final int BUFFERFORMAT_INT2D_GLAY_8      =0x00020001;
    /**
     * int[][]で0/1の2値画像
     */
    public static final int BUFFERFORMAT_INT2D_BIN_8      =0x00020002;

}
