======================================================================
NyARToolkit
 version 4.1.1
======================================================================
Copyright (C)2008-2012 Ryo Iizuka

http://nyatla.jp/nyartoolkit/
airmail(at)ebony.plala.or.jp
wm(at)nyatla.jp

Translated by Bao-Wen Chen

----------------------------------------------------------------------
 About NyARToolkit
----------------------------------------------------------------------

 * NyARToolkit is a class library for Augmented Reality (AR) application 
   based on the ARToolkit 2.72.1.
 * Compatible with J2SE5.0 and above.
 * Composed of basic features of ARToolkit, NyARToolkit's orginal features 
   and framework.
 * Consisting of three modules: an independent "lib" module, 
   an environment-dependent "utils" module, and a "sample" module that 
   can package sample applications.
 * The lib module only work under J2ME API environment.
 * The util module provides controls for camera, 3D system, and helper class 
   for interfacing external modules.
 * The sample module is a collection of AR applications, with a minimal 
   amout of features.
 
  For more info on ARToolkit, please visit the following URL
  http://www.hitl.washington.edu/artoolkit/
  
  
----------------------------------------------------------------------
 NyARToolkit Features
----------------------------------------------------------------------

* Support multiple image inputs (BufferedImage, JMF output, binary input, 
  and so on)
* Enhanced reusability compared to ARToolkit
* High-speed capabilities are available for following calculation: 
  labeling, posture optimization, image processing, matrix operation, equation resolution (•û’öŽ®ŒvŽZ?). In total, it's about twice faster than ARToolkit.
* Support NyID-style ID marker
* A MarkerSystem class that makes easy multiple uses of AR/NyID markers.
* A simple sketching system for OpenGL
* API for changing image format to and from BufferedImage.
* Possible to use PNG image as markers, and to cut out parts of captured image.


---------------------------------------------------------------------
 NyARToolkit License
----------------------------------------------------------------------

NyARToolkit adaptes a commercial licensing and GPLv3 (or later) dual 
license scheme.
* The GNU General Public License (GPL) Ver.3 is intended for 
open source usage, where applications based on NyARToolkit should also 
be distributed under the GPL.
* Commercial licensing is intended for commercial useage, which means 
applications based on NyARTookit can be distributed without publishing 
the source. 
* If a GPLv3 license is approved, it will be available to use free of 
charge, regardless of commercial or non-commercial purposes.
* If a GPLv3 license is not approved, please consider purchasing the 
commercial license.

 GPLv3
 For more info on GPLv3, read LICENSE.text.
 
 Commercial License (Japan)
 Please contact M-Soft Inc.
 http://www.msoft.co.jp/pressrelease/press090928-1.html
 
 Commercial License (Other countries)
 Please contact ARToolWorks Inc.
 http://www.artoolworks.com/Home.html
 
 
----------------------------------------------------------------------
 External Libraries
----------------------------------------------------------------------

NyARToolkit uses the following external libraries. If newer version 
becomes available, please install the newer version.

 1. JMF JavaTM Media Framework 2.1.1e
	Required for running utils.jmf, sample.jogl
    URL: http://www.oracle.com/technetwork/java/javase/download-142937.html

 2. QuickTime 7.5
	Required for running utils.qt
	URL: http://www.apple.com/quicktime/qtjava/

 3. Jogl
	Required for running utils.jogl, sample.jogl
    URL: http://download.java.net/media/jogl/builds/archive/
    file   : jogl-1.1.1-rc8-xxxx-xxx.zip 
	
 4. java3d
	Required for running utils.java3d, sample.java3d
    URL: https://java3d.dev.java.net/binary-builds.html
    file:    java3d-1_5_1-xxxx-i586.exe
	
Depending on application and purpose, it's not necessary to install all 
the libraries above. Install only those needed.


----------------------------------------------------------------------
 Getting Started
----------------------------------------------------------------------

This section provides instruction for installing NyARToolkit development 
tools under Eclipse environment.

 1. Create a new workspace/project in Eclipse.
 2. Import lib, utils, and sample modules (directories) into the workspace/project
 3. Correct the errors found at the imported project. Common errors include 
    inconsistent character codes, or reference misses to external JAR files .
	The error of inconsistent character codes can be resolved by changing
	the project's encoding to UTF-8. For resolving reference misses to 
	external JAR libraries, please refer to the chapter of external library 
	(Where is the chapter?). 
 4. Connect webcam to PC.
 5. Run SimpleLiteMStandard.java within NyARToolkit.sample.jogl, and point 
    the webcam to the marker. If a cube appears, the installation of NyARToolkit
	is done!
	
 For detailed intruction of step 1 ~ step 4, please refer to 
 http://sixwish.jp/Nyartoolkit/ 
 
----------------------------------------------------------------------
 Project Outline
----------------------------------------------------------------------

The outline of the Eclipse project.

 * NyARToolkit
   The main body of NyARToolkit library. Basically it will work as normal 
   as long as J2ME API is provided. There are three source folders,
	1)src contains image processing, and numerical calculation classes.
	2)src.markersystem contains a MarkerSystem that is used to make the
	  manipulation of multiple markers easier.
	3)src.rpf contains classes that together form RealityPlatform system.
	
   There is no external libraries required by this module.
   
 * NyARToolkit.sample.java3d
   Sample application that outputs Java3D formats. There is one sample program.
   It depends on two external libraries, Java3D and JMF.
   
 * NyARToolkit.sample.jogl
   A sample that is packaged with some typical methods from OpenGL. 
	1)src contains sample applications that utilize MarkerSystem. It can 
	  further be grouped by those using OpenGL sketching and those not.
	2)src.old includes sample applications of previous formats.
	3)src.rpf includes sample applications that use RealtyPlatform.
	
   Usually, the samples under src will be enough. There are various kinds 
   of samples under src/sketch folder.
   
 * NyARToolkit.sandbox
   It buries unverified or incomplete programs. There are many bugs so 
   the quality is not assured. The sample programs under the test folder 
   will probably be useful if you are to test RealityPlatform.
   
 * NyARToolkit.utils.j2se
   It contains helper classes and test programs that are dependent on JavaSE.
   Classes in this module enable you to input BufferedImage directly into NyARToolkit.
   
 * NyARToolkit.utils.java3d
   It contains helper classes and test programs that are dependent on Java3D.
   It provides NyARToolkit with the ability to output Java3D format.
   
 * NyARToolkit.utils.jogl
   It contains helper classes and test programs that are dependent on Jogl.
   It provides NyARToolkit with the ability to output Jogl format.
 
 * NyARToolkit.utils.qt
   It contains helper classes and test programs that are dependent on QTJava.
   It supports image capturing function via QuickTime.
   
   
----------------------------------------------------------------------
 NyARToolkit Performance
----------------------------------------------------------------------

NyARToolkit replaced parts of the processing system of ARToolkit to enhance
processing speed. At algorithm level, the performance of detecting one marker
is four times faster, and it is two times faster when simply comparing speed
to ARToolkit.

When RPF(RealityPlatform, added in 3.0.0) module is used, it will likely 
to further increase speed up to 1.5 times faster.

RPF is still unstable, if you don't hava a specific reason, I recommend you
to use MarkerSystem module. 


----------------------------------------------------------------------
 FAQ
----------------------------------------------------------------------

 *Q1. I cannot save configuration changes in JMF under Windows 7.
	  =>Run JMFRegistry as an administrator, and you will be able to save
	    changes to JMF.
		
 *Q2. I cannot simply replace NyARToolkit versions older than 2.5.3 with
	  the latest version?
	  =>Some method parameters are changed. Refer to sample programs and
	    make necessary change to function calls.

 *Q3. What is RealityPlatform(RPF)? I don't know how to use it.
      =>RPF is a framework for marker-based AR application development.
	    Using RPF, you can detect unknown markers, and easily access to
		markers' environment properties. Besides, you can also create 
		jutting-out and/or partial-damage effects on markers.
	  
	  *Current version, 4.0.0, does not continue to maintain RPF. I recommend
	   you to use the MarkerSystem of latest version. 
	   
 *Q4. What is MarkerSystem?
      =>MarkerSystem is a framework designed to make the use of 
	    multiple markers as easy as possible. Coordinates transformation, 
		image acquisition,..etc can also be done without a pain in the neck
		via MarkerSystem. Moreover, the sketching system is improved with 
		reference to Processing's sketching system. So now coding a 
		NyARToolKit application is much more fun than before!
		
		
----------------------------------------------------------------------
 Known Bugs
----------------------------------------------------------------------
 Those known bugs are all related to RealityPlatform (RPF): 
 1. Posture feedback is not implemented.
 2. Because the noise processing function of contour extraction system 
    is based on least-squares method, it has greater delay.
 3. Due to the deficiencies of algorithms of the contour extraction system,
	drifting phenomenon of contour lines might occur.
 4. The performance of movement detection in two-dimensional system is low
 5. Edge extraction performs poorly When dealing with blurred edge.
 6. The performance of initial detection is low compared to older version 
    of NyARToolkit.


----------------------------------------------------------------------
 Contact Me
----------------------------------------------------------------------
 If you have any questions or comments, email me at wm(at)nyatla.jp.
 I might not be able to answer soon, but I will reply as soon as possible.  
 
 
----------------------------------------------------------------------
 Special Thanks
----------------------------------------------------------------------
 Hirokazu Kato, Ph. D.
 http://www.hitl.washington.edu/artoolkit/

 Prof. Mark Billinghurst
 http://www.hitlabnz.org/

 arc@dmz
 http://digitalmuseum.jp/
 