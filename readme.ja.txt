ARToolkit Java class library NyARToolkit.
Copyright (C)2008 R.Iizuka

version Alpha 0.8.20080412.0

http://nyatla.jp/
airmail(at)ebony.plala.or.jp
--------------------------------------------------




・NyARToolkit

NyARToolkitは、nativeなコードを一切使用しない、Pure Javaのみで
構成されたARToolkitクラスライブラリです。

ARToolkit 2.72.1をベースに構築されています。

J2SEでのみ動作を確認しました。
J2MEやMIDP2.0にはそのうち対応します。


ARToolkitは加藤博一先生とHuman Interface Technology Labにより
開発されたAugmented Reality (AR) ライブラリです。
詳しくはこちらをご覧下さい。
http://www.hitl.washington.edu/artoolkit/



・構成

+-------------------------------------------+
|               Application                 |
+-------+---------+-------+-----------------+
|NyARJMF| NyARJogl|NyARJ3d|                 |
+-------+---------+-------+                 |
|  JMF  |  JOGL   |Java3D |Java3DNyARToolkit|
+-------+---------+-------+                 |
|Camera |       3D        |                 |
--------------------------------------------+

映像キャプチャにはJMFを使用し、3D描画にはJOGL又はJava3Dを使用しています。
NyARJMFとNyJogl,NyJ3dは、これらのエクステンションをApplicationや
NyARToolKitから使いやすくするためのラッパーです。

これらとNyARToolkitは完全に分離していますので、入力・出力ともに容易に
差し替えが出来ると思います。




・サンプルなど

動作させる前に、JMFとJOGLかJava3Dをインストールしてください。
動作確認したバージョンと入手先はこちらです。

JMF JavaTM Media Framework 2.1.1e
http://java.sun.com/products/java-media/jmf/index.jsp

jogl-1.1.1-pre-20080328-xxxx-i586.zip
https://jogl.dev.java.net/

java3d-1_5_1-xxxx-i586.exe
https://java3d.dev.java.net/binary-builds.html



サンプルは以下のディレクトリにあります。

./src
 NyARToolkitのEclipseプロジェクトがあります。
 jp.nyatla.nyartoolkit.sampleパッケージに、Rawイメージから
 変換行列を求めるサンプルがあります。

./sample
NyARToolkitのアプリケーションサンプルEclipseプロジェクトがあります。

 jmf
  jp.nyatla.nyartoolkit.jmf.sample
  ビデオキャプチャの試験プログラム　と、マーカー検出プログラムがあります。

 jogl
  jp.nyatla.nyartoolkit.jogl.sample
  ARToolkitのsimpleLite相当のサンプルがあります。

 java3d
  jp.nyatla.nyartoolkit.java3d.sample
  simpleLiteのようなものをJava3Dで動かすサンプルがあります。


NyARJMFのプロジェクトはNyARToolKitに依存し、NyARJOGLとJava3Dのプロジェクトは
NyARToolKitとNyARJMFに依存しています。
zipを展開すると多分参照関係が壊れてますので、再設定してください。



・サンプル内のパッケージ注意点
sampleディレクトリ以下にあるソースはAPIは仕様が固まっていないので、
時々APIそのものを変更しています。

自前のプロジェクトの部品として使われる場合は、パッケージをコピーして
使用することをお勧めします。



・NyARToolkitとオリジナルの差分

オリジナルと演算結果に互換性がありますが、関数構成を再設計した
ため、関数名や関数コールの手順の互換性がほとんどありません。

クラスは関数機能毎にまとめた作りになっていますので、オリジナルの
コード読んだことがあれば、なんとなく判ると思います。




・足りない機能等

マーカーのセーブ機能と、複数マーカーの認識機能が未実装です。

今後実装していきます。




・ライセンス
GPLです。詳しくはLICENCE.txtをみてください。




・お願い
NyARToolkitを使って面白いものが出来たら、是非教えてください。

それと強制では有りませんが、NyARToolkitを使った感想などを
送ってくれると、今後の励みになります。



ではでは、楽しく遊んでくださいネ。

2008.03.29 R.Iizuka A虎＠nyatla.jp
