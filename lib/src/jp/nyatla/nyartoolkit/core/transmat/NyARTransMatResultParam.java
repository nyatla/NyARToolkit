package jp.nyatla.nyartoolkit.core.transmat;

public class NyARTransMatResultParam
{
	/**
	 * 観測値とのずれを示すエラーレート値です。{@link INyARTransMat}が更新します。
	 * エラーレートの意味は、実装クラスごとに異なることに注意してください。
	 * ユーザからは読出し専用です。
	 * {@link #has_value}がtrueの時に使用可能です。
	 */
	public double last_error;
}
