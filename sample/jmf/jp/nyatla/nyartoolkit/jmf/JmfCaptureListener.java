/**
 * JMFお手軽キャプチャ用リスナ
 * (c)2008 R.Iizuka
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf;

import javax.media.Buffer;

public interface  JmfCaptureListener{
    public void onUpdateBuffer(Buffer i_buffer);
    
}