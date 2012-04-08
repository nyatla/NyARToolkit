/**
 * このパッケージは、同時に1個マーカを認識するマーカベースARの制御クラスです。
 * ここに宣言されるクラスは、マーカ検出処理をイベントドリブンなシーケンスに変換します。
 * 同時に出現するマーカの数が1個であれば、これらのクラスを継承したアプリケーションは、
 * マーカ状態が変化した時のイベントのみで実装できます。
 * 
 * <p>sample - 
 * <ul>
 * <li>NyARToolkit.sample.jogl Project {@link jp.nyatla.nyartoolkit.jogl.sample.old.SingleARMarker}</li>
 * <li>NyARToolkit.sample.jogl Project {@link jp.nyatla.nyartoolkit.jogl.sample.old.SingleNyIdMarker}</li>
 * </ul>
 *
 * このパッケージの定義は古くなりました。
 * 特別な事情が無い限り、{@link jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem}を使うべきです。
 */
package jp.nyatla.nyartoolkit.processor;
