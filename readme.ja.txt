ARToolKit Java class library NyARToolkit.
Copyright (C)2008-2010 Ryo Iizuka

version 2.5.2

http://nyatla.jp/nyartoolkit/
airmail(at)ebony.plala.or.jp
wm(at)nyatla.jp
--------------------------------------------------




[[NyARToolkit/2.5]]

NyARToolkitは、Pure Javaで実装したARToolKitクラスライブラリです。
ARToolKit 2.72.1をベースに作られています。


※ARToolkitは加藤博一先生とHuman Interface Technology Labにより
　開発されたAugmented Reality (AR) ライブラリです。
　詳しくは、下記URLをご覧ください。
　http://www.hitl.washington.edu/artoolkit/


[[NyARToolkitの特徴]]

 -ARToolKitと同等な機能を、クラスベースAPIで提供します。
 -計算器チューニングにより、ARToolKitと比較して、処理性能が
  向上しています。より高い性能を持つNyARToolkit最適化モード
  と、ARToolKitと互換性のある互換モードを搭載しています。

  --NyARToolkit最適化モード
    いくつかのアルゴリズムをARToolKitのものと差換え、高速化・精度の向上を図ります。
    ARToolKit比で、約2倍の性能があります。（JIT有効時）
    ただし、計算結果はARToolKitのそれと若干ズレがでます。

  --ARToolKit互換モード
    ARToolKitのアルゴリズムを最適化し、高速化を図ります。
    ARToolKitとほぼ同等の処理性能です。（JIT有効時）

 -取り扱える画像サイズに制限がなく、静止画も扱えます。
 -取り扱えるマーカー個数の最大値が可変です。
 -Idマーカシステム(NyId)が利用できます。



[[構成]]

NyARToolkitは、環境に依存しない計算部分の"NyARToolkit"
と、カメラ/3Dレンダラに接続するための、インタフェイス
クラスモジュールで作られています。



+-----------------------------------------------+
|                   Application                 |
+-------+---------+--------+--------+-----------+
|NyARJMF|CaptureQT| NyARJoglNyARJ3d |           |
+-------+---------+--------+--------+           |
|  JMF  |QuickTime|  JOGL  | Java3D |NyARToolkit|
+-------+---------+--------+--------+           |
|      Camera     |       3D        |           |
------------------------------------+-----------+


-カメラインタフェイス
 カメラインタフェイスは、イメージソースからリアルタイムに画像を
 取り込む為のモジュールです。
 JMF(NyARJMF)、又はQuickTime(CaptureQT)が用意されています。

-3Dレンダラインタフェイス
 3Dレンダラインタフェイスは、3Dレンダリングシステムに、画像や、
 計算値を設定するためのモジュールです。
 JOGL(NyARJolg)又はJava3D(NyARJ3d)を使用することが出来ます。

-NyARToolkitコア
 NyARToolkitの中心的なモジュールです。数学処理、画像処理、管理機能
 などが、機能毎にクラス化されています。


[[サンプルの動かし方]]

1.動作させる前に、JMFとJOGLかJava3Dをインストールしてください。
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



2.eclipseで空のワークスペースを作成し、sample,src,src.utils
  ディレクトリをインポートしてください。
  各ディレクトリの説明については、

-ディレクトリの説明
  --srcディレクトリには、NyARToolkitコアモジュールを配置してあります。
  --src.utilsディレクトリには、カメラキャプチャクラスや、3Dライブラリ
    用のラッパークラス等の、インタフェイスモジュールが配置してあります。
    があります。
  --sampleディレクトリには、NyARToolkitを使用したサンプルプログラムがあります。


[[プロジェクトの説明]]

[[[ライブラリ]]]

-NyARToolkit
 NyARToolkitライブラリの本体です。依存する外部モジュールはありません。

-NyARToolkit.utils.jmf
 JMF用のインタフェイスモジュールです。
 JMFからの画像をNyARToolkitに取り込むクラス群があります。
 外部ライブラリは、JMFに依存します。

-NyARToolkit.utils.qt
 QuickTime用のインタフェイスモジュールです。
 QuickTimeからの画像をNyARToolkitに取り込むクラス群があります。
  外部ライブラリは、JMF、QuickTime for Javaに依存します。

-NyARToolkit.utils.jogl
 OpenGL用のインタフェイスモジュールです。
 OpenGLとNyARToolkitのインタフェイスクラス群があります。
 外部ライブラリは、JMF,JOGLに依存します。

-NyARToolkit.utils.java3d
 Java3D用のインタフェイスモジュールです。
 Java3DとNyARToolkitのインタフェイスクラス群があります。
 外部ライブラリは、JMF,Java3Dに依存します。


[[[サンプル]]]

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
 JAVA3D/JMFを使ったサンプルプログラムです。

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


NyARToolkit.sandbox
 正式サポートされていない開発中の実験クラス群です。動作しないものもあります。




[[足りない機能等]]

カメラキャリブレーション、マーカーのセーブ機能等が相変わらずありません。
今後実装していきます。




[[ライセンス]]

NyARToolkitは、商用ライセンスとGPLv3以降のデュアルライセンスを採用しています。
(Version/2.4.0より、GPLv3になりました。)

 -GPLv3
 GPLv3については、LICENCE.txtをお読みください。

 -商用ライセンス
 商用ライセンスについては、ARToolWorks社に管理を委託しております。
 http://www.artoolworks.com/Home.html

 -日本国内での販売については、下記にお問い合わせ下さい。
 http://www.msoft.co.jp/pressrelease/press090928-1.html



・謝辞

arc@dmzさん
http://digitalmuseum.jp/

QuickTimeキャプチャモジュールを提供をして頂きました。有難うございます。