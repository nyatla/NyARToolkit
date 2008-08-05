package jp.digitalmuseum.capture.sample;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;

import jp.digitalmuseum.capture.AbstractVideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDummy;
import jp.digitalmuseum.capture.VideoCaptureListener;
import jp.digitalmuseum.capture.VideoCaptureQT;


class MyFrame extends JFrame {
	private AbstractVideoCapture capture;
	private WritableRaster raster;
	private BufferedImage image;

	public MyFrame() {
		// QuickTimeでキャプチャ
		capture = new VideoCaptureQT(Const.CAP_WIDTH, Const.CAP_HEIGHT);
		try {
			capture.prepSetInput(null);
			capture.start();

		// 失敗したらダミーでキャプチャ
		} catch (Exception e) {
			e.printStackTrace();
			capture = new VideoCaptureDummy(Const.CAP_WIDTH, Const.CAP_HEIGHT);
			try {
				capture.start();

			// ダミーすら初期化できなかったら終了
			} catch (Exception e1) {
				e1.printStackTrace();
				dispose();
			}
		}

		// キャンバスの初期化
		MyCanvas canvas = new MyCanvas();
		canvas.setSize(capture.getWidth(), capture.getHeight());
		capture.addVideoCaptureListener(canvas);
		add(canvas);
		pack();

		// メインウィンドウの初期化
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("sample");
		setResizable(false);
		setVisible(true);
	}

	/**
	 * 終了処理
	 */
	public void dispose() {
		capture.dispose();
		System.exit(0);
	}

	class MyCanvas extends Canvas implements VideoCaptureListener {
		/**
		 * キャプチャ画像の描画
		 */
		public void paint(Graphics g) {
			if (image != null)
				g.drawImage(image, 0, 0, null);
		}
		public void repaint(Graphics g) { paint(g); }
		public void update(Graphics g) { paint(g); }

		/**
		 * キャプチャ画像のオブジェクト化
		 */
		public void imageUpdated(byte[] pixels) {
			// 実データを画像オブジェクトに変換する準備
			if (raster == null) {
				raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
						capture.getWidth(), capture.getHeight(),
						capture.getWidth()*3, 3,
						new int[] { 0, 1, 2 }, null); 
				image = new BufferedImage(capture.getWidth(), capture.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
			}

			// 実データを画像オブジェクトに変換
			raster.setDataElements(0, 0, capture.getWidth(), capture.getHeight(), pixels);
			image.setData(raster);
			repaint();
		}

	}

}
