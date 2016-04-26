[English here!](README.EN.md "")

# NyARToolkit for Java

Copyright (C)2008-2016 Ryo Iizuka

http://nyatla.jp/nyartoolkit/  
airmail(at)ebony.plala.or.jp  
wm(at)nyatla.jp  



## NyARToolkit for Java

* NyARToolkitは、ARToolKit 5.3.2を基盤としたARアプリケーション向けのクラスライブラリです。  
ARToolKitについては、下記のURLをご覧ください。   
http://www.hitl.washington.edu/artoolkit/
* ネイティブコードを含まない純粋なJava言語のみで実装されています。J2SE7.0以上に対応しています。
* ARToolKitマーカ、NyIdマーカ、NFTマーカ(ARToolkit5互換)の3種類のターゲットを扱えます。
* ARToolKitの基本機能と、NyARToolKitオリジナルの拡張機能、フレームワークで構成しています。
* ライブラリは3部構成です。
 ** lib - コアライブラリを含む基幹部品です。J2MEのAPIのみで動作します。環境依存性がありません。
 ** utils - カメラ制御、3Dシステム制御等のヘルパークラスを提供します。環境依存性があります。
 ** sample - 最低限の機能を持つ、ARアプリケーション集です。


## 必要なライブラリ
NyARToolkitの使用する外部ライブラリは以下の通りです。新しいバージョンのものがあれば、そちらを使用してください。


1. Webcam Capture in Java (NyARToolKit/4.2.1以降ではリポジトリに含まれています。)  
utils.jmf,sample.joglの実行に必要です。  
URL: http://www.webcam-capture.sarxos.pl/  

2. Jogl2(NyARToolKit/5.0.4以降ではリポジトリに含まれています。)  
utils.jogl,sample.joglの実行に必要です。  
URL:http://jogamp.org/deployment/jogamp-current/archive/  
file:jogamp-all-platforms.7z  


以下の物は必要に応じてそろえてください。

1. JMF JavaTM Media Framework 2.1.1e
utils.jmf,sample.joglの実行に必要です。  
URL: http://www.oracle.com/technetwork/java/javase/download-142937.html

2. QuickTime 7.5  
utils.qtの実行に必要です。
URL: http://www.apple.com/quicktime/qtjava/

3. java3d  
utils.java3d,sample.java3dの実行に必要です。  
URL: https://java3d.dev.java.net/binary-builds.html  
file:    java3d-1_5_1-xxxx-i586.exe

4. Jogl(NyARToolKit/4.2.1以降ではリポジトリに含まれています。)  
utils.jogl,sample.joglの実行に必要です。  
URL: https://jogamp.org/  
file   : gluegen-old-1.0b6,jogl-old-1.1.1






## セットアップ方法

Eclipse環境に、NyARToolkit開発環境をインストールする方法を説明します。


1. Eclipseで空のワークスペースを作成します。
2. lib,sample,utilsディレクトリをワークスペースにインポートします。
3. インポートしたプロジェクトのエラーを修正します。多くの場合、エラーは文字コードの不一致と外部JARファイルの参照ミスです。  
文字コードの不一致は、プロジェクトの文字コードをUTF8に変更することで解決します。
外部JARファイルの参照ミスについては、外部ライブラリの章を参考にして下さい。
4. Webカメラをコンピュータに接続してください。
5. NyARToolkit.sample.joglの[WebcamCapture.java](https://github.com/nyatla/NyARToolkit/blob/master/sample/jogl/src/jp/nyatla/nyartoolkit/jogl/sample/sketch/webcamcapture/WebCamSample.java)を実行して、マーカを撮影します。立方体が現れれば、インストールは正しく完了しています。

1-4の手順については、http://sixwish.jp/Nyartoolkit/ に詳しい解説があります。

## プロジェクトの概要

各Eclipseプロジェクトの概要です。

* NyARToolkit  
NyARToolkitライブラリの本体です。基本的はJ2MEのAPIが有れば動きます。  
3つのソースフォルダがあります。srcには画像処理、数値計算クラス群があります。src.markersystemには、複数のマーカを簡単に扱う為のMarkerSystemがあります。src.rpfには、RealityPlatformを構成するクラス群があります。  
依存する外部ライブラリはありません。

* NyARToolkit.sample.java3d  
Java3dを出力先とするサンプルアプリケーションです。1個のサンプルプログラムがあります。  
外部ライブラリは、Java3DとJMFに依存しています。
* NyARToolkit.sample.jogl  
OpenGLでの代表的な利用方法を実装したサンプルです。そのまま動作するのは[WebcamCapture.java](https://github.com/nyatla/NyARToolkit/blob/master/sample/jogl/src/jp/nyatla/nyartoolkit/jogl/sample/sketch/webcamcapture/WebCamSample.java)だけです。
他のサンプルはキャプチャライブラリにJMFを使っているため、JMFをセットアップするか、キャプチャ方式を書き換える必要があります。
srcには、MarkerSystemを使ったサンプルがあります。OpenGLのスケッチを使ったサンプルと、使わないサンプルがあります。
src.oldには、以前の古い形式のサンプルプログラムがあります。
src.rpfには、RealityPlatformを使ったサンプルプログラムがあります。

* NyARToolkit.sandbox  
お砂場です。実験プログラムや作りかけのコードなどを埋蔵しています。  
品質は未保証です（不具合等が多く放置されています）。testソースフォルダにあるサンプルプログラムは、RealityPlatformの試験に役立つかもしれません。
* NyARToolkit.utils.j2se  
JavaSEに依存したヘルパークラス群と、テストプログラムがあります。  
BufferedImageをそのままNyARToolkitへ入力するためのクラスなどがあります。
* NyARToolkit.utils.java3d  
Java3Dに依存したヘルパークラス群と、テストプログラムがあります。  
NyARToolkitの出力値のJava3dへの入力を支援します。
* NyARToolkit.utils.jogl  
Joglに依存したヘルパークラス群と、テストプログラムがあります。Joglのドライバです。
* NyARToolkit.utils.qt  
QTJavaに依存したヘルパークラス群と、テストプログラムがあります。  
QuickTimeのドライバです。
* NyARToolkit.utils.webcampapture
Webcam Captureに関するファイルが有ります。




## License
NyARToolkitは、商用ライセンスとLGPLv3のデュアルライセンスを採用しています。

LGPLv3を承諾された場合には、商用、非商用にかかわらず、無償でご利用になれます。
LGPLv3を承諾できない場合には、商用ライセンスの購入をご検討ください。

* LGPLv3  
LGPLv3については、COPYING.txtをお読みください。
* 商用ライセンス  
商用ライセンスについては、ARToolWorks社に管理を委託しております。http://www.artoolworks.com/Home.html
