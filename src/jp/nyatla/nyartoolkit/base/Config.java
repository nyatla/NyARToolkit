package jp.nyatla.nyartoolkit.base;

public class Config {
	//Version系情報
//	public static final int AR_HEADER_VERSION_MAJOR=2; //#define AR_HEADER_VERSION_MAJOR		2
//	public static final int AR_HEADER_VERSION_MINOR=72;//#define AR_HEADER_VERSION_MINOR		72
//
//	public static final int AR_HEADER_VERSION_TINY=0;//#define AR_HEADER_VERSION_TINY		0
//
//	public static final int AR_HEADER_VERSION_BUILD=0;//#define AR_HEADER_VERSION_BUILD		0
//	
//	public static final String AR_HEADER_VERSION_STRING="2.72.0";//#define AR_HEADER_VERSION_STRING	"2.72.0"
//
//	public static final boolean AR_HAVE_HEADER_VERSION_2=true;//#define AR_HAVE_HEADER_VERSION_2
//	public static final boolean AR_HAVE_HEADER_VERSION_2_72=true;//#define AR_HAVE_HEADER_VERSION_2_72


	/*ビデオ入力系？*/
	/*------------------------------------------------------------*/

//	public static final int AR_DRAW_BY_GL_DRAW_PIXELS =0;//#define  AR_DRAW_BY_GL_DRAW_PIXELS    0
//	public static final int AR_DRAW_BY_TEXTURE_MAPPING=1;//#define  AR_DRAW_BY_TEXTURE_MAPPING   1
//	public static final int AR_DRAW_TEXTURE_FULL_IMAGE=0;//#define  AR_DRAW_TEXTURE_FULL_IMAGE   0
//	public static final int AR_DRAW_TEXTURE_HALF_IMAGE=1;//#define  AR_DRAW_TEXTURE_HALF_IMAGE   1
//	public static final int AR_IMAGE_PROC_IN_FULL=0;//#define  AR_IMAGE_PROC_IN_FULL        0
//	public static final int AR_IMAGE_PROC_IN_HALF=1;//#define  AR_IMAGE_PROC_IN_HALF        1
//	public static final int AR_FITTING_TO_IDEAL=0;//#define  AR_FITTING_TO_IDEAL          0
//	public static final int AR_FITTING_TO_INPUT=1;//#define  AR_FITTING_TO_INPUT          1

//	public static final int AR_TEMPLATE_MATCHING_COLOR=0;//#define  AR_TEMPLATE_MATCHING_COLOR   0
//	public static final int AR_TEMPLATE_MATCHING_BW=1;//#define  AR_TEMPLATE_MATCHING_BW      1
//	public static final int AR_MATCHING_WITHOUT_PCA=0;//#define  AR_MATCHING_WITHOUT_PCA      0
//	public static final int AR_MATCHING_WITH_PCA=1;//#define  AR_MATCHING_WITH_PCA         1
	
//	public static final int DEFAULT_TEMPLATE_MATCHING_MODE=AR_TEMPLATE_MATCHING_BW;//#define  DEFAULT_TEMPLATE_MATCHING_MODE     AR_TEMPLATE_MATCHING_COLOR
//	public static final int DEFAULT_MATCHING_PCA_MODE=AR_MATCHING_WITH_PCA;//#define  DEFAULT_MATCHING_PCA_MODE          AR_MATCHING_WITHOUT_PCA


	//#ifdef _WIN32
//	public static final int DEFAULT_IMAGE_PROC_MODE		=AR_IMAGE_PROC_IN_FULL;//#  define   DEFAULT_IMAGE_PROC_MODE     AR_IMAGE_PROC_IN_FULL
//	public static final int DEFAULT_FITTING_MODE			=AR_FITTING_TO_INPUT;//#  define   DEFAULT_FITTING_MODE        AR_FITTING_TO_INPUT
//	public static final int DEFAULT_DRAW_MODE				=AR_DRAW_BY_TEXTURE_MAPPING;//#  define   DEFAULT_DRAW_MODE           AR_DRAW_BY_TEXTURE_MAPPING
//	public static final int DEFAULT_DRAW_TEXTURE_IMAGE	=AR_DRAW_TEXTURE_FULL_IMAGE;//#  define   DEFAULT_DRAW_TEXTURE_IMAGE  AR_DRAW_TEXTURE_FULL_IMAGE
	//#endif

//	public static final int AR_PIX_SIZE_DEFAULT=
//		(AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_ABGR) || (AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_BGRA) || (AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_RGBA) || (AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_ARGB)?4:
//		((AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_BGR) || (AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_RGB)?3:
//		((AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_2vuy) || (AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_yuvs)?2:
//		((AR_DEFAULT_PIXEL_FORMAT == AR_PIXEL_FORMAT_MONO)?1:-1)));
//	public static final int AR_GET_TRANS_MAT_MAX_LOOP_COUNT=5;//#define   AR_GET_TRANS_MAT_MAX_LOOP_COUNT         5
//	public static final double AR_GET_TRANS_MAT_MAX_FIT_ERROR=1.0;//#define   AR_GET_TRANS_MAT_MAX_FIT_ERROR          1.0
//	public static final double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR=1.0;//#define   AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR     1.0

//	public static final int AR_AREA_MAX=100000;//#define   AR_AREA_MAX      100000
//	public static final int AR_AREA_MIN=70;//#define   AR_AREA_MIN          70


//	public static final int AR_SQUARE_MAX=30;//#define   AR_SQUARE_MAX        30
//	public static final int AR_CHAIN_MAX=10000;//#define   AR_CHAIN_MAX      10000
//	public static final int AR_PATT_NUM_MAX=50;//#define   AR_PATT_NUM_MAX      50 
//	public static final int AR_PATT_SIZE_X=16;//#define   AR_PATT_SIZE_X       16 
//	public static final int AR_PATT_SIZE_Y=16;//#define   AR_PATT_SIZE_Y       16 
//	public static final int AR_PATT_SAMPLE_NUM=64;//#define   AR_PATT_SAMPLE_NUM   64

//	public static final double AR_GL_CLIP_NEAR=50.0;//#define   AR_GL_CLIP_NEAR      50.0
//	public static final double AR_GL_CLIP_FAR=5000.0;//#define   AR_GL_CLIP_FAR     5000.0

//	public static final int AR_HMD_XSIZE=640;//#define   AR_HMD_XSIZE        640
//	public static final int AR_HMD_YSIZE=480;//#define   AR_HMD_YSIZE        480

//	public static final int AR_PARAM_NMIN=6;//#define   AR_PARAM_NMIN         6
//	public static final int AR_PARAM_NMAX=1000;//#define   AR_PARAM_NMAX      1000
//	public static final double AR_PARAM_C34=100.0;//#define   AR_PARAM_C34        100.0
}
