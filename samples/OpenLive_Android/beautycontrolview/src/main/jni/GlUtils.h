#ifndef FULIVENATIVEDEMO_GLUTILS_H
#define FULIVENATIVEDEMO_GLUTILS_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "android_util.h"

void checkGlError(char *op);

GLuint loadShader(int shaderType, const char *source);

void createProgram();

void drawFrame(int textureId, float texMatrix[]);

void releaseProgram();

#endif //FULIVENATIVEDEMO_GLUTILS_H
