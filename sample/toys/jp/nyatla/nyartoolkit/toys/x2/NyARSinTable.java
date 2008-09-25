package jp.nyatla.nyartoolkit.toys.x2;
/**
 * 単純Sinテーブル
 *
 */
public class NyARSinTable
{
	private double[] _table = null;
	private int _resolution;

	private void initTable(int i_resolution)
	{
		//解像度は4の倍数で無いとダメ
		assert(i_resolution%4==0);
		if (this._table == null) {
			this._table = new double[i_resolution];
			int d4 = i_resolution / 4;
			// テーブル初期化(0-2PIを0-1024に作成)
			for (int i = 1; i < i_resolution; i++) {
				this._table[i] = (Math.sin(2 * Math.PI * (double) i / (double) i_resolution));
			}
			this._table[0] = 0;
			this._table[d4 - 1] = 1;
			this._table[d4 * 2 - 1] = 0;
			this._table[d4 * 3 - 1] = -1;
		}
		return;
	}

	public NyARSinTable(int i_resolution)
	{
		initTable(i_resolution);
		this._resolution=i_resolution;
		return;
	}

	public double sin(double i_rad)
	{
		final int resolution=this._resolution;
		// 0～2PIを0～1024に変換
		int rad_index = (int) (i_rad * resolution / (2 * Math.PI));
		rad_index = rad_index % resolution;
		if (rad_index < 0) {
			rad_index += resolution;
		}
		// ここで0-1024にいる
		return this._table[rad_index];
	}

	public double cos(double i_rad)
	{
		final int resolution=this._resolution;
		// 0～Math.PI/2を 0～256の値空間に変換
		int rad_index = (int) (i_rad * resolution / (2 * Math.PI));
		// 90度ずらす
		rad_index = (rad_index + resolution / 4) % resolution;
		// 負の領域に居たら、+1024しておく
		if (rad_index < 0) {
			rad_index += resolution;
		}
		// ここで0-1024にいる
		return this._table[rad_index];
	}
	/**
	 * ラジアン角度をテーブルの角度インデックス番号に変換する。
	 * 角度インデックスは、0<=n<_resolutionの範囲の整数
	 * @param i_rad
	 * @return
	 */
	public int rad2tableIndex(double i_rad)
	{
		final int resolution=this._resolution;
		int rad_index = (int) (i_rad * resolution / (2 * Math.PI));
		rad_index = rad_index % resolution;
		if (rad_index < 0) {
			rad_index += resolution;
		}
		return rad_index;
	}
	public double sinByIdx(int i_rad_idx)
	{
		return 0;
	}
	public double cosByIdx(int i_rad_idx)
	{
		return 0;		
	}
	
}
