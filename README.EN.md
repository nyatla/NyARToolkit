

# NyARToolkit for Java

Copyright (C)2008-2016 Ryo Iizuka

http://nyatla.jp/nyartoolkit/  
airmail(at)ebony.plala.or.jp  
wm(at)nyatla.jp  



## NyARToolkit for Java

* NyARToolkit is a class library for Augmented Reality (AR) application based on the ARToolkit5.2.3.
* Compatible with J2SE7.0 and above.
* Composed of basic features of ARToolkit, NyARToolkit's orginal features and framework.
* Consisting of three modules: an independent "lib" module, an environment-dependent "utils" module, and a "sample" module that can package sample applications.
* The lib module only work under J2ME API environment.
* The util module provides controls for camera, 3D system, and helper class for interfacing external modules.
* The sample module is a collection of AR applications, with a minimal amout of features.
 
For more info on ARToolkit, please visit the following URL  
http://www.hitl.washington.edu/artoolkit/

## NyARToolkit Features
* Support multiple image inputs (BufferedImage, JMF output, binary input, and so on)
* Enhanced reusability compared to ARToolkit
* High-speed capabilities are available for following calculation: labeling, posture optimization, image processing, matrix operation, equation resolution
* Support AR-Marker/NyID-style ID marker/NFT marker.
* A MarkerSystem class that makes easy multiple uses of AR/NyID/NFT markers.
* A simple sketching system for OpenGL
* API for changing image format to and from BufferedImage.
* Possible to use PNG image as markers, and to cut out parts of captured image.



## External Libraries

NyARToolkit uses the following external libraries. If newer version becomes available, please install the newer version.

1. Webcam Capture  
General Webcam capture library. This is included.  
http://webcam-capture.sarxos.pl/

2. Jogl2  
Required for running utils.jogl, sample.jogl. This is partially included.  
URL:http://jogamp.org/deployment/jogamp-current/archive/  
file:jogamp-all-platforms.7z

Following libraries are optional.

1. JMF JavaTM Media Framework 2.1.1e  
Required for running utils.jmf, sample.jogl  
URL: http://www.oracle.com/technetwork/java/javase/download-142937.html

2. QuickTime 7.5  
Required for running utils.qt  
URL: http://www.apple.com/quicktime/qtjava/

3. java3d  
Required for running utils.java3d, sample.java3d  
URL: https://java3d.dev.java.net/binary-builds.html  
file:java3d-1_5_1-xxxx-i586.exe

4. Jogl1  
Required for running utils.jogl, sample.jogl  
URL: http://download.java.net/media/jogl/builds/archive/  
file: jogl-1.1.1-rc8-xxxx-xxx.zip 


 Getting Started
----------------------------------------------------------------------

This section provides instruction for installing NyARToolkit development 
tools under Eclipse environment.

1. Create a new workspace/project in Eclipse.
2. Import lib, utils, and sample modules (directories) into the workspace/project
3. Correct the errors found at the imported project. Common errors include inconsistent character codes, or reference misses to external JAR files .  
	The error of inconsistent character codes can be resolved by changing the project's encoding to UTF-8.  
	For resolving reference misses to external JAR libraries, please refer to the chapter of external library.
4. Connect webcam to PC.
5. Run [WebcamCapture.java](https://github.com/nyatla/NyARToolkit/blob/master/sample/jogl/src/jp/nyatla/nyartoolkit/jogl/sample/sketch/webcamcapture/WebCamSample.java) within NyARToolkit.sample.jogl, and point the webcam to the marker. If a cube appears, the installation of NyARToolkit is done!
	
For detailed intruction of step 1 ~ step 4, please refer to  
http://sixwish.jp/Nyartoolkit/ 


## Project Outline

The outline of the Eclipse project.

* NyARToolkit  
The main body of NyARToolkit library. Basically it will work as normal as long as J2ME API is provided. There are three source folders,  
	1. src contains image processing, and numerical calculation classes.
	2. src.markersystem contains a MarkerSystem that is used to make the manipulation of multiple markers easier.
	3. src.rpf contains classes that together form RealityPlatform system.  
There is no external libraries required by this module.
* NyARToolkit.sample.java3d  
Sample application that outputs Java3D formats. There is one sample program.
It depends on two external libraries, Java3D and JMF.
* NyARToolkit.sample.jogl  
A sample that is packaged with some typical methods from OpenGL. There are various kinds of samples under src/sketch folder.
	1. src contains sample applications that utilize MarkerSystem. It can further be grouped by those using OpenGL sketching and those not.
	2. src.old includes sample applications of previous formats.
	3. src.rpf includes sample applications that use RealtyPlatform.
* NyARToolkit.sandbox  
It buries unverified or incomplete programs. There are many bugs so the quality is not assured. The sample programs under the test folder will probably be useful if you are to test RealityPlatform.
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

## License
NyARToolkit adaptes a LGPLv3 dual license.

* GNU Lesser General Public License (LGPL) Ver.3 is intended for open source usage, where applications based on NyARToolkit should also 
be distributed under the LGPL.
* Commercial licensing is intended for non-LGPL useage, which means 
applications based on NyARTookit can be distributed without LGPL. 

If a LGPLv3 license is approved, it will be available to use free of charge, regardless of commercial or non-commercial purposes.
If a LGPLv3 license is not approved, please consider purchasing the commercial license.

Commercial License
Please contact ARToolWorks Inc.
http://www.artoolworks.com/Home.html
