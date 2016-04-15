package jp.nyatla.nyartoolkit.core;

/**
 * メソッドが削除されて使用できなくなった時に発生する例外。
 */
public class NyARMethodDeplecatedException extends NyARRuntimeException{
	public NyARMethodDeplecatedException(String i_alternate_method){
		super("See '"+i_alternate_method+"'.");
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 7938270564442434938L;

}
