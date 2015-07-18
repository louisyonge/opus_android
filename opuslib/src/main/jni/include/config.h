#ifndef CONFIG_H
#define CONFIG_H


/* Comment out the next line for floating-point code */
//define FIXED_POINT           1
#define FLOATING_POINT        	1
#define OPUS_BUILD            	1
#define OUTSIDE_SPEEX         	1
#define OPUSTOOLS             	1
#define USE_ALLOCA            	1
#define _BUILD_SPEEX          	1
#define SPX_RESAMPLE_EXPORT
#define RANDOM_PREFIX         	opus__
#define PACKAGE_NAME           	"opus-tools"
#define PACKAGE_VERSION        	"1.1"
#define PACKAGE_BUGREPORT 		"opus@xiph.org"
#define PACKAGE_STRING 			"opus 1.1"
#define PACKAGE_TARNAME 		"opus"

/* Comment out the next line for Android */
#define ANDROID_V

#endif /* CONFIG_H */
