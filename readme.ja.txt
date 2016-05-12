======================================================================
NyARToolkit
 version 5.0.5
======================================================================

Copyright (C)2008-2016 Ryo Iizuka

http://nyatla.jp/nyartoolkit/
airmail(at)ebony.plala.or.jp
wm(at)nyatla.jp

----------------------------------------------------------------------
 About NyARToolkit
----------------------------------------------------------------------
 * NyARToolkitは、ARToolKit 5.3.2を元に実装したPureJavaなクラスライブラリです。
   J2SE7.0以上に対応しています。
 * ARToolKit互換のマーカのほかに、画像をそのままマーカを使うことができます。
   また、NyId規格のIDマーカを使用できます。
 * ARToolKit5以降のNFT(自然特徴点とラッキング)を使用できます。
 * Webcamcapture,JMF等の複数のカメラライブラリに対応しています。
   BufferedImage等の静止画も利用できます。
 * 複数マーカに対応した独自のフレームワークがあります。
 * OpenGL向けの簡易なスケッチシステムがあります。数十行のコードを実装するだけで、アプリケーション
   を作ることができます。


 ARToolKitについては、下記のURLをご覧ください。
 http://www.hitl.washington.edu/artoolkit/



----------------------------------------------------------------------
 NyARToolkit License
----------------------------------------------------------------------


NyARToolkitは、商用ライセンスとLGPLv3のデュアルライセンスを採用して
います。

LGPLv3を承諾された場合には、商用、非商用にかかわらず、無償でご利用にな
れます。LGPLv3を承諾できない場合には、商用ライセンスの購入をご検討くだ
さい。


 * LGPLv3
   LGPLv3については、COPYING.txtをお読みください。

 * 商用ライセンス
   商用ライセンスについては、ARToolWorks社に管理を委託しております。
   http://www.artoolworks.com/Home.html





----------------------------------------------------------------------
 外部ライブラリ
----------------------------------------------------------------------
NyARToolkitの使用する外部ライブラリは、以下の通りです。
新しいバージョンのものがあれば、そちらを使用してください。

実行するプロジェクトによっては、全ての外部ライブラリを揃える必要は
ありません。必要なものだけをインストールしてください。


 1. Webcam Capture
 	汎用のWebcam captureライブラリです。同梱されています。
	http://webcam-capture.sarxos.pl/

 2. Jogl2
    utils.jogl,sample.joglの実行に必要です。
    一部の同梱されています。不足する場合はダウンロードしてください。
    URL:http://jogamp.org/deployment/jogamp-current/archive/
    file:jogamp-all-platforms.7z


以下の物は必要に応じてそろえてください。

 1. JMF JavaTM Media Framework 2.1.1e
    utils.jmf,sample.jmfの実行に必要です。
    URL: http://www.oracle.com/technetwork/java/javase/download-142937.html

 2. QuickTime 7.5
    utils.qtの実行に必要です。
    URL: http://www.apple.com/quicktime/qtjava/

 3. java3d
    utils.java3d,sample.java3dの実行に必要です。
    URL: https://java3d.dev.java.net/binary-builds.html
    file:    java3d-1_5_1-xxxx-i586.exe

 4.Jogl1
    一部同梱されています。古いバージョン(utils.jogl.utils.jogl)を使う場合に必要です。
    URL: http://download.java.net/media/jogl/builds/archive/
    file   : jogl-1.1.1-rc8-xxxx-xxx.zip 




----------------------------------------------------------------------
 Getting started
----------------------------------------------------------------------
Eclipse環境に、NyARToolkit開発環境をインストールする方法を説明します。


 1.Eclipseで空のワークスペースを作成します。

 2.lib,sample,utilsディレクトリをワークスペースにインポートします。

 3.インポートしたプロジェクトのエラーを修正します。多くの場合、エラーは
   文字コードの不一致と外部JARファイルの参照ミスです。文字コードの不一致
   は、プロジェクトの文字コードをUTF8に変更することで解決します。
   外部JARファイルの参照ミスについては、外部ライブラリの章を参考にして
   下さい。
   
 4.Webカメラをコンピュータに接続してください。

 5.NyARToolkit.sample.joglのSimpleLiteMStandard.javaを実行して、
   マーカを撮影します。立方体が現れれば、インストールは正しく完了しています。

 1-4の手順については、http://sixwish.jp/Nyartoolkit/ に詳しい解説があります。

----------------------------------------------------------------------
 プロジェクトの概要
----------------------------------------------------------------------
Eclipseプロジェクトの概要です。

 * NyARToolkit
   NyARToolkitライブラリの本体です。基本的はJ2MEのAPIが有れば動きます。
   3つのソースフォルダがあります。srcには画像処理、数値計算クラス群が
   あります。src.markersystemには、複数のマーカを簡単に扱う為のMarkerSystem
   があります。src.rpfには、RealityPlatformを構成するクラス群があります。
   依存する外部ライブラリはありません。

 * NyARToolkit.sample.java3d
   Java3dを出力先とするサンプルアプリケーションです。1個のサンプル
   プログラムがあります。外部ライブラリは、Java3DとJMFに依存しています。

 * NyARToolkit.sample.jogl
   OpenGLでの代表的な利用方法を実装したサンプルです。
   srcには、MarkerSystemを使ったサンプルがあります。OpenGLのスケッチを
   使ったサンプルと、使わないサンプルがあります。
   src.oldには、以前の古い形式のサンプルプログラムがあります。
   src.rpfには、RealityPlatformを使ったサンプルプログラムがあります。

   通常は、src以下のサンプルだけで足りると思います。

   src/sketchの下には、色々なサンプルがあります。
   

 * NyARToolkit.sandbox
   お砂場です。実験プログラムや作りかけのコードなどを埋蔵しています。
   品質は未保証です（不具合等が多く放置されています）。
   testソースフォルダにあるサンプルプログラムは、RealityPlatformの試験
   に役立つかもしれません。

 * NyARToolkit.utils.j2se
   JavaSEに依存したヘルパークラス群と、テストプログラムがあります。
   BufferedImageをそのままNyARToolkitへ入力するためのクラスなどが
   あります。appsパッケージにはマーカの作成をサポートするツール類があります。

 * NyARToolkit.utils.java3d
   Java3Dに依存したヘルパークラス群と、テストプログラムがあります。
   NyARToolkitの出力値のJava3dへの入力を支援します。

 * NyARToolkit.utils.jogl
   Joglに依存したヘルパークラス群と、テストプログラムがあります。
   NyARToolkitの出力値のJoglへの入力を支援します。

 * NyARToolkit.utils.qt
   QTJavaに依存したヘルパークラス群と、テストプログラムがあります。
   QuickTimeからのキャプチャを支援します。

----------------------------------------------------------------------
 NyARToolkitのパフォーマンス
----------------------------------------------------------------------

NyARToolkitは、ARToolKitの処理系のいくつかを差し替え、高速化を図ってい
ます。1マーカ検出時の性能では、アルゴリズムレベルで4倍、単純な速度比で
約2倍程度高速です。



----------------------------------------------------------------------
 FAQ
----------------------------------------------------------------------
 *Q1.Windows7でJMFの設定が保存できません。
   >JMFRegistryを、管理者権限で実行することで、保存ができるようになります。
   >現在はwebcamcaptureの利用を推奨します。

 *Q2.2.5.3以前のNyARToolkitとそのまま差し替えできません。
   >いくつかの関数で、引数が変更になりました。サンプルファイルを参考に、
    関数コールを修正してください。
     
 *Q3.MarkerSystemとはなんですか。
  >複数のID/ARマーカを、出来る限り簡単に扱う為のフレームワークです。
  座標変換や画像取得などを簡単に行うことが出来ます。
  Processingのスケッチシステムを参考にしたスケッチシステムと組み合わせる
  ことで、楽にプログラムを作ることができます。

 
----------------------------------------------------------------------
 お問い合わせ
----------------------------------------------------------------------
NyARToolkitに関するお問い合わせは、wm(at)nyatla.jp までご連絡ください。
状況により、お返事が遅れることもあります。ご了承ください。

----------------------------------------------------------------------
 Special thanks
----------------------------------------------------------------------
加藤博一先生 (Hirokazu Kato, Ph. D.)
 http://www.hitl.washington.edu/artoolkit/

Prof. Mark Billinghurst
 http://www.hitlabnz.org/

arc@dmzさん
 http://digitalmuseum.jp/

DAQRI LCC
 http://daqri.com/
 DAQRI LCC is a sponsor of NyARToolKit Project.