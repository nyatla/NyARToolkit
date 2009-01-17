/*
 * Copyright (c) 1996-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package jp.nyatla.nyartoolkit.jmf.utils;

import javax.media.*;
import javax.media.protocol.*;

import javax.media.util.BufferToImage;
import java.io.IOException;
import java.awt.*;

public class MonitorStream implements PushBufferStream, BufferTransferHandler
{

	JmfCaptureListener img_listener;

	PushBufferStream actual = null;

	boolean dataAvailable = false;

	boolean terminate = false;

	boolean enabled = false;

	Object bufferLock = new Object();

	Buffer cbuffer = new Buffer();

	BufferTransferHandler transferHandler = null;

	Component component = null;

	MonitorCDS cds;

	BufferToImage bti = null;

	MonitorStream(PushBufferStream actual, MonitorCDS cds)
	{
		this.actual = actual;
		actual.setTransferHandler(this);
		this.cds = cds;
	}

	public javax.media.Format getFormat()
	{
		return actual.getFormat();
	}

	/**
	 * 非同期READ
	 */
	public void read(Buffer buffer) throws IOException
	{
		// Wait for data to be available
		// Doesn't get used much because the transferData
		// call is made when data IS available. And most
		// Processors/Players read the data in the same
		// thread that called transferData, although that's
		// not a safe assumption to make
		if (!dataAvailable) {
			synchronized (bufferLock) {
				while (!dataAvailable && !terminate) {
					try {
						bufferLock.wait(100);
					} catch (InterruptedException ie) {
					}
				}
			}
		}

		if (dataAvailable) {
			synchronized (bufferLock) {
				// Copy the buffer attributes, but swap the data
				// attributes so that no extra copy is made.
				buffer.copy(cbuffer, true);
				//dataAvailable = false;
			}
		}
		//	return;
	}

	public void setCaptureListener(JmfCaptureListener i_listener)
	{
		img_listener = i_listener;
	}

	public void transferData(PushBufferStream pbs)
	{
		// Get the data from the original source stream
		synchronized (bufferLock) {
			try {
				pbs.read(cbuffer);
			} catch (IOException ioe) {
				return;
			}
			dataAvailable = true;
			bufferLock.notifyAll();
		}
		if (img_listener != null) {
			img_listener.onUpdateBuffer(cbuffer);
		}

		// Maybe synchronize this with setTransferHandler() ?
		if (transferHandler != null && cds.delStarted)
			transferHandler.transferData(this);
	}

	public void setTransferHandler(BufferTransferHandler transferHandler)
	{
		this.transferHandler = transferHandler;
	}

	public boolean setEnabled(boolean value)
	{
		enabled = value;
		if (value == false) {
			if (!cds.delStarted) {
				try {
					cds.stopDelegate();
				} catch (IOException ioe) {
				}
			}
		} else {
			// Start the capture datasource if the monitor is enabled
			try {
				cds.startDelegate();
			} catch (IOException ioe) {
			}
		}
		return enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public float setPreviewFrameRate(float rate)
	{
		System.err.println("TODO");
		return rate;
	}

	public ContentDescriptor getContentDescriptor()
	{
		return actual.getContentDescriptor();
	}

	public long getContentLength()
	{
		return actual.getContentLength();
	}

	public boolean endOfStream()
	{
		return actual.endOfStream();
	}

	public Object[] getControls()
	{
		return new Object[0];
	}

	public Object getControl(String str)
	{
		return null;
	}

}
