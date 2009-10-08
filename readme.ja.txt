ARToolKit Java class library NyARToolkit.
Copyright (C)2008 R.Iizuka

version 2.4.0

http://nyatla.jp/nyartoolkit/
airmail(at)ebony.plala.or.jp
wm(at)nyatla.jp
--------------------------------------------------




・NyARToolkit/2.4

NyARToolkitは、Pure Javaで実装したARToolKitクラスライブラリです。

ARToolKit 2.72.1をベースに構築されています。

J2SEでのみ動作を確認しました。
J2MEやMIDP2.0にはそのうち対応します。


ARToolkitは加藤博一先生とHuman Interface Technology Labにより
開発されたAugmented Reality (AR) ライブラリです。
詳しくはこちらをご覧下さい。
http://www.hitl.washington.edu/artoolkit/


・NyARToolkitの特徴

 -ARToolKitと同等な処理シーケンスを、クラスベースで再構築しています。

 -処理構造の最適化により、ARToolKitと比較して可読性に優れています。

 -ARToolKit互換モードと、NyARToolkit最適化モードを搭載しています。(Version/2.4.0より)
　 
  --NyARToolkit最適化モード
    いくつかのアルゴリズムをARToolKitのものと差換え、高速化・精度の向上を図ります。
    ARToolKit比で、約2倍高速です。（JIT有効時）複数マーカー取り扱い時は、
    更に高速になります。ただし、計算結果はARToolKitのそれと若干ズレがでます。

  --ARToolKit互換モード
    ARToolKitのアルゴリズムを最適化し、高速化を図ります。
    ARToolKit比で、約1倍高速です。（JIT有効時）

 -取り扱える画像サイズに制限がありません。
 -取り扱えるマーカー個数の最大値が可変です。
 -Idマーカシステム(NyId)が利用できます。(Version/2.3.0より)







・構成

+-----------------------------------------------+
|                   Application                 |
+-------+---------+--------+--------+-----------+
|NyARJMF|CaptureQT| NyARJoglNyARJ3d |           |
+-------+---------+--------+--------+           |
|  JMF  |QuickTime|  JOGL  | Java3D |NyARToolkit|
+-------+---------+--------+--------+           |
|      Camera     |       3D        |           |
------------------------------------+-----------+


映像キャプチャにはJMF、又はQuickTimeを使用することが出来ます。

3D描画にはJOGL又はJava3Dを使用することが出来ます。

NyARJMF/CaptureQT/NyARJog/NyARJ3dは、下位のキャプチャモジュール
や3Dライブラリを使いやすくするためのラッパークラス群です。

各モジュールとNyARToolkitは分離可能であり、個々を単独で使用する
ことも可能です。




・サンプルなど

１．動作させる前に、JMFとJOGLかJava3Dをインストールしてください。
　　QuickTimeを使う場合には、QuickTime for Javaも必要です。


動作確認したバージョンと入手先はこちらです。

JMF JavaTM Media Framework 2.1.1e
http://java.sun.com/products/java-media/jmf/index.jsp

jogl-1.1.1-pre-20080328-xxxx-i586.zip
https://jogl.dev.java.net/

java3d-1_5_1-xxxx-i586.exe
https://java3d.dev.java.net/binary-builds.html

QuickTime 7.5
http://www.apple.com/quicktime/qtjava/



２．eclipseで空のワークスペースを作成し、sample,src,src.utils
　　ディレクトリをインポートしてください。

srcディレクトリには、NyARToolkit本体（計算クラス群）があります。
src.utilsディレクトリには、カメラキャプチャクラスや、3Dライブラリ用のラッパークラス群があります。
sampleディレクトリには、NyARToolkitを使用したサンプルプログラムがあります。


・プロジェクトの説明

ライブラリ

NyARToolkit
　NyARToolkitライブラリの本体です。依存する外部モジュールはありません。

NyARToolkit.utils.jmf
　JMFからの画像をNyARToolkitに取り込むクラス群があります。
　外部ライブラリは、JMFに依存します。

NyARToolkit.utils.qt
　QuickTimeからの画像をNyARToolkitに取り込むクラス群があります。
　外部ライブラリは、JMF、QuickTime for Javaに依存します。

NyARToolkit.utils.jogl
　OpenGLとNyARToolkitのインタフェイスクラス群があります。
　外部ライブラリは、JMF,JOGLに依存します。

NyARToolkit.utils.java3d
　Java3DとNyARToolkitのインタフェイスクラス群があります。
　外部ライブラリは、JMF,Java3Dに依存します。


サンプル

NyARToolkit.sample.jogl
　JOGL/JMFを使ったサンプルプログラムがあります。

  -jp.nyatla.nyartoolkit.jogl.sample.JavaSimpleLite
   単一のARToolKit用マーカーを認識するARToolkitのsimpleLite相当のサンプルです。
  -jp.nyatla.nyartoolkit.jogl.sample.JavaSimpleLite2
   複数のARToolKit用マーカーを認識するサンプルです。1～100個程度のマーカーを
   同時に認識します。
  -jp.nyatla.nyartoolkit.jogl.sample.SingleNyIdMarker
   単一のNyIdマーカを認識するためのサンプルです。アプリケーションフレームワーク
   SingleNyIdMarkerProcesserのリファレンス実装です。


NyARToolkit.sample.java3d
　JOGL/JMFを使ったサンプルプログラムです。
　
  -jp.nyatla.nyartoolkit.java3d.sample
   simpleLiteをJava3Dで動かすサンプルがあります。

NyARToolkit.sample.jmf
  JMFを使ったサンプルプログラムです。

  -jp.nyatla..nyartoolkit.jmf.sample
　 JMFでキャプチャした画像をNyARToolkitで処理するサンプルプログラムです。
　

NyARToolkit.sample.qt
　Quicktime for Javaを使ったサンプルプログラムです。

　-jp.nyatla.nyartoolkit.qt.sample
　 QuickTimeでキャプチャした画像をNyARToolkitで処理するサンプルプログラムです。
　




・足りない機能等

カメラキャリブレーション、マーカーのセーブ機能等が相変わらずありません。
今後実装していきます。




・ライセンス

NyARToolkitは、商用ライセンスとGPLv3以降のデュアルライセンスを採用しています。
(Version/2.4.0より、GPLv3ライセンスになりました。)

 -GPL
 GPLについては、LICENCE.txtをお読みください。

 -商用ライセンス
 商用ライセンスについては、ARToolWorks社に管理を委託しております。
 http://www.artoolworks.com/Home.html

 日本国内での販売については、下記にお問い合わせ下さい。
 http://www.msoft.co.jp/pressrelease/press090928-1.html



・謝辞

arc@dmzさん
http://digitalmuseum.jp/

QuickTimeキャプチャモジュールを提供をして頂きました。有難うございます。