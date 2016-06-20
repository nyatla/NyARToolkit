/**
 * このパッケージは、NyARToolkitの基本クラスを格納します。
 * ARToolKitと同様の計算を行うために必要なすべての基本機能を含みます。
 * 依存している外部ライブラリはJava 1.6SEのみです。
 * coreには基本クラスのうちプラットフォーム依存性の低い（異種プラットフォームでもそのまま使える）機能を実装します。
 * Rawデータアクセス、ファイルIO等の依存性の高い機能は、{@link jp.nyatla.nyartoolkit.j2se}パッケージに実装します。
 * (ファイルIOの一部は完全には分離されていません。)
 */
package jp.nyatla.nyartoolkit.core;
