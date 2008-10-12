package jp.nyatla.nyartoolkit.sandbox.x2;

public class NyMath
{
	public final static long FIXEDFLOAT24_1=0x1000000L;
	public final static long FIXEDFLOAT24_0_25=FIXEDFLOAT24_1/4;
	public final static long FIXEDFLOAT16_1=0x10000L;	
	public final static long FIXEDFLOAT8_1=0x100L;	

	private final static int FF16_PI=(int)(Math.PI*FIXEDFLOAT16_1);
	private final static int FF16_2PI=(int)(2 *FF16_PI);
	private final static int FF16_05PI=(int)(FF16_PI/2);
	private final static int SIN_RESOLUTION=1024;
	private final static int ACOS_RESOLUTION=256;
	/* sinテーブルは0-2PIを1024分割
	 * acosテーブルは0-1を256分割
	 */
	private final static int[] sin_table=new int[SIN_RESOLUTION];
	private final static int[] acos_table=new int[ACOS_RESOLUTION+1];
	private final static int SQRT_LOOP=10;
	/**
	 * http://www.geocities.co.jp/SiliconValley-PaloAlto/5438/
	 * 参考にしました。
	 * 少数点部が16bitの変数の平方根を求めます。
	 * 戻り値の小数点部分は16bitです。
	 * @param i_v
	 * @return
	 */
	public static long sqrtFixdFloat16(long i_ff16)
	{
		long t=0,s;
		s=i_ff16>0?i_ff16:-i_ff16;
		if(i_ff16==0){
			return 0;
		}
		for(int i=SQRT_LOOP;i>0;i--){
			t = s;
			s = (t+((i_ff16<<16)/t))>>1;
			if(s==t){
				break;
			}
		};
		return t;
	}
	public static long sqrtFixdFloat(long i_ff,int i_bit)
	{
		long t=0,s;
		s=i_ff>0?i_ff:-i_ff;
		if(i_ff==0){
			return 0;
		}
		for(int i=SQRT_LOOP;i>0;i--){
			t = s;
			s = (t+((i_ff<<i_bit)/t))>>1;
			if(s==t){
				break;
			}
		}
		return t;
	}
	public static int acosFixedFloat16(int i_ff24)
	{
/*		int abs_ff24=i_ff24>0?i_ff24:-i_ff24;
		if(abs_ff24<FIXEDFLOAT24_0_25){
			//0.25までの範囲は、一次の近似式
			return FF16_05PI-(i_ff24>>8);
		}else{
			// 0～1を0～512に変換
			int idx = (int)(i_ff24>>16);//S8
			if (idx < 0) {
				return FF16_PI-acos_table[-idx];
			}else{
				return acos_table[idx];
			}
		}
*/		return (int)(Math.acos((double)i_ff24/0x1000000)*0x10000);
	}	
	public static int sinFixedFloat24(int i_ff16)
	{
		// 0～2PIを0～1024に変換
/*		int rad_index = (int) (i_ff16 * SIN_RESOLUTION / FF16_2PI);
		rad_index = rad_index % SIN_RESOLUTION;
		if (rad_index < 0) {
			rad_index += SIN_RESOLUTION;
		}
		// ここで0-1024にいる
		return sin_table[rad_index];
*/		return (int)(Math.sin((double)i_ff16/0x10000)*0x1000000);
	}
	public static int cosFixedFloat24(int i_ff16)
	{
/*		// 0～Math.PI/2を 0～256の値空間に変換
		int rad_index = (int) (i_ff16* SIN_RESOLUTION / FF16_2PI);
		// 90度ずらす
		rad_index = (rad_index + SIN_RESOLUTION / 4) % SIN_RESOLUTION;
		// 負の領域に居たら、+1024しておく
		if (rad_index < 0) {
			rad_index += SIN_RESOLUTION;
		}
		// ここで0-1024にいる
		return sin_table[rad_index];	
*/		return (int)(Math.cos((double)i_ff16/0x10000)*0x1000000);
	}
	public static void initialize()
	{
		//解像度は4の倍数で無いとダメ
		assert(SIN_RESOLUTION%4==0);

		int d4 = SIN_RESOLUTION / 4;
		//sinテーブル初期化(0-2PIを0-1024に作成)
		for (int i = 1; i < SIN_RESOLUTION; i++) {
			sin_table[i] =(int)((Math.sin(2 * Math.PI * (double) i / (double) SIN_RESOLUTION))*FIXEDFLOAT24_1);
		}
		sin_table[0] = 0;
		sin_table[d4 - 1] = 0x1000000;
		sin_table[d4 * 2 - 1] = 0;
		sin_table[d4 * 3 - 1] = -0x1000000;
		//acosテーブル初期化
		for (int i = 1; i < ACOS_RESOLUTION; i++) {
			acos_table[i] =(int)((Math.acos((double) i / (double) ACOS_RESOLUTION))*FIXEDFLOAT16_1);
		}
		acos_table[0]=FF16_PI;
		acos_table[ACOS_RESOLUTION]=0;
		return;
	}
	public static void printF16(long i_value)
	{
		System.out.println((double)i_value/0x10000);
		return;
	}
	public static void printF24(long i_value)
	{
		System.out.println((double)i_value/0x1000000);
		return;
	}
}
