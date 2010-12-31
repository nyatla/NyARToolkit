/* 
 * PROJECT: NyARToolkit Quicktime utilities.
 * --------------------------------------------------------------------------------
 * Copyright (C)2008 arc@dmz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	
 *	<arc(at)digitalmuseum.jp>
 * 
 */

package jp.nyatla.nyartoolkit.qt.utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Timer;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.QTFile;
import quicktime.qd.PixMap;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;
import quicktime.util.RawEncodedImage;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * QuickTimeキャプチャクラス
 *
 */
public class QtCameraCapture implements ActionListener
{

	private Dimension image_size;

	private QtCaptureListener capture_listener;

	protected float fps = 30; // キャプチャ画像を取得するfps

	protected byte[] pixels; // キャプチャ画像の実データを保持するバイト型配列

	// キャプチャに使うタイマー
	private Timer timer;

	// QTJava用のあれこれ
	private QDGraphics graphics;

	private QDRect bounds;

	private SequenceGrabber grabber;

	private SGVideoChannel channel;

	private RawEncodedImage rawEncodedImage;

	private Movie movie;

	// ピクセルフォーマット変換用の一時変数
	private int[] pixels_int;

	public static final int PIXEL_FORMAT_RGB = quicktime.util.EndianOrder.isNativeLittleEndian() ? QDConstants.k32BGRAPixelFormat : QDGraphics.kDefaultPixelFormat;

	public QtCameraCapture(int i_width, int i_height, float i_rate)
	{
		image_size = new Dimension(i_width, i_height);
		fps = i_rate;
	}

	public Dimension getSize()
	{
		return image_size;
	}

	public byte[] readBuffer() throws NyARException
	{
		if (grabber == null) {
			throw new NyARException();
		}
		return pixels;
	}

	public void setCaptureListener(QtCaptureListener i_listener) throws NyARException
	{
		if (grabber != null) {
			throw new NyARException();
		}
		capture_listener = i_listener;

	}

	/**
	 * @param input
	 * @throws QTException
	 */
	public void prepSetInput(Object input) throws QTException
	{
		QTSession.open();
		bounds = new QDRect(image_size.width, image_size.height);
		graphics = new QDGraphics(quicktime.util.EndianOrder.isNativeLittleEndian() ? QDConstants.k32BGRAPixelFormat : QDGraphics.kDefaultPixelFormat, bounds);
		if (input != null && input.getClass().equals(File.class)) {
			movie = quicktime.std.movies.Movie.fromDataRef(new DataRef(new QTFile((File) input)), StdQTConstants.newMovieActive);
		} else {
			grabber = new SequenceGrabber();
			grabber.setGWorld(graphics, null);
			//Please check WinVDIG if you got couldntGetRequiredComponent exception on Windows.
			channel = new SGVideoChannel(grabber);
			channel.setBounds(bounds);

			// seqGrabPreview == 2, Processingでmagic numberとしてハードコートされていた…
			channel.setUsage(StdQTConstants.seqGrabPreview);

			if (input != null) {
				try {
					channel.setDevice(input.toString());
				} catch (QTException e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}

	/**
	 * Webcamの設定ダイアログを表示する。
	 * 既定のWebcamでは駄目な場合(複数のWebcamが接続されているPCなど)ではこれを実行するとよい。
	 */
	public void prepShowDialog() throws QTException
	{
		channel.settingsDialog();
	}

	public void start() throws NyARException
	{
		try {

			if (grabber == null)
				prepSetInput(null);

			if (movie == null) {
				grabber.prepare(true, false); // あってもなくてもよさそう
				grabber.startPreview();
			} else {
				movie.preroll(0, 1.0f);
				while (movie.maxLoadedTimeInMovie() == 0)
					movie.task(100);
				movie.setRate(1);
				movie.getPict(movie.getTime()).draw(graphics, bounds);
			}
			PixMap pixmap = graphics.getPixMap();
			rawEncodedImage = pixmap.getPixelData();

			image_size.width = rawEncodedImage.getRowBytes() / 4;
			pixels = new byte[image_size.width * image_size.height * 3];
			pixels_int = new int[image_size.width * image_size.height];
		} catch (QTException e) {
			QTSession.close();
			throw new NyARException(e);
		}

		// キャプチャイメージを定期的に更新するタイマー
		timer = new Timer((int) (1000 / fps), this);
		timer.start();
	}

	public void stop()
	{
		finalize();
	}

	/** タイマー処理。キャプチャイメージの更新結果をリスナに伝える。 */
	public void actionPerformed(ActionEvent event)
	{

		// 画像をQTJavaのRawEncodedImageとして取得
		try {
			if (movie == null) {
				grabber.idle();
			} else {
				if (movie.isDone())
					movie.goToBeginning();
				movie.getPict(movie.getTime()).draw(graphics, bounds);
			}
		} catch (QTException e) {
			QTSession.close();
			e.printStackTrace();
		}

		// RawEncodedImageをint列に落とし込む
		rawEncodedImage.copyToArray(0, pixels_int, 0, pixels_int.length);

		// バイト列を生成する
		int idx_byte = 0;
		for (int idx = 0; idx < image_size.width * image_size.height; idx++) {
			pixels[idx_byte++] = (byte) (pixels_int[idx] >> 16);
			pixels[idx_byte++] = (byte) (pixels_int[idx] >> 8 & 0xff);
			pixels[idx_byte++] = (byte) (pixels_int[idx] & 0xff);
		}

		// 各リスナに更新されたバイト列を渡す
		capture_listener.onUpdateBuffer(pixels);
	}

	protected void finalize()
	{
		try {
			if (movie == null) {
				grabber.stop();
				grabber.release();
				grabber.disposeChannel(channel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			QTSession.close();
		}
		timer.stop();
	}

}
