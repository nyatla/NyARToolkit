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
import javax.media.control.*;

import java.io.IOException;

public class MonitorCDS extends PushBufferDataSource
{

	private PushBufferDataSource delegate = null;

	private PushBufferStream[] delStreams = null;

	private MonitorStream monitorStream = null;

	private PushBufferStream[] monitorStreams = null;

	boolean delStarted = false; // variable used by MonitorStream also

	private Control[] controls;

	public MonitorCDS(DataSource ds)
	{
		// Get the stream from the actual datasource
		// and create a MonitorStream from it
		// Export the MonitorControl interface of the MonitorStream
		if (ds instanceof PushBufferDataSource) {
			delegate = (PushBufferDataSource) ds;
			delStreams = delegate.getStreams();
			monitorStream = new MonitorStream(delStreams[0], this);
			monitorStreams = new PushBufferStream[] { monitorStream };
		}
	}

	public Object[] getControls()
	{
		return controls;
	}

	public Object getControl(String value)
	{
		if (value.equals("jmfsample.MonitorStream") || value.equals("javax.media.control.MonitorControl")){
			return monitorStream;
		}else{
			return null;
		}
	}

	public javax.media.CaptureDeviceInfo getCaptureDeviceInfo()
	{
		return ((CaptureDevice) delegate).getCaptureDeviceInfo();
	}

	public FormatControl[] getFormatControls()
	{
		return ((CaptureDevice) delegate).getFormatControls();
	}

	public String getContentType()
	{
		return delegate.getContentType();
	}

	public void connect() throws IOException
	{
		if (delegate == null)
			throw new IOException("Incompatible DataSource");
		// Delegate is already connected
	}

	public void disconnect()
	{
		monitorStream.setEnabled(false);
		delegate.disconnect();
	}

	public synchronized void start() throws IOException
	{
		startDelegate();
		delStarted = true;
	}

	public synchronized void stop() throws IOException
	{
		if (!monitorStream.isEnabled()) {
			stopDelegate();
		}
		delStarted = false;
	}

	public Time getDuration()
	{
		return delegate.getDuration();
	}

	public PushBufferStream[] getStreams()
	{
		return monitorStreams;
	}

	void startDelegate() throws IOException
	{
		delegate.start();
	}

	void stopDelegate() throws IOException
	{
		delegate.stop();
	}

}
