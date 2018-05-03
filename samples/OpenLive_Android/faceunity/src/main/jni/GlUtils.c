
#include "GlUtils.h"

// Simple vertex shader, used for all programs.
static const char *VERTEX_SHADER =
        "uniform mat4 uMVPMatrix;\n"
                "uniform mat4 uTexMatrix;\n"
                "attribute vec4 aPosition;\n"
                "attribute vec4 aTextureCoord;\n"
                "varying vec2 vTextureCoord;\n"
                "void main() {\n"
                "    gl_Position = uMVPMatrix * aPosition;\n"
                "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
                "}\n";

// Simple fragment shader for use with "normal" 2D textures.
static const char *FRAGMENT_SHADER_2D =
        "precision mediump float;\n"
                "varying vec2 vTextureCoord;\n"
                "uniform sampler2D sTexture;\n"
                "void main() {\n"
                "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
                "}\n";

static const float FULL_RECTANGLE_COORDS[] = {
        -1.0f, -1.0f,   // 0 bottom left
        1.0f, -1.0f,   // 1 bottom right
        -1.0f, 1.0f,   // 2 top left
        1.0f, 1.0f,   // 3 top right
};

static const float FULL_RECTANGLE_TEX_COORDS[] = {
        0.0f, 0.0f,     // 0 bottom left
        1.0f, 0.0f,     // 1 bottom right
        0.0f, 1.0f,     // 2 top left
        1.0f, 1.0f      // 3 top right
};

static const float IDENTITY_MATRIX[] = {
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
};

GLuint mProgramHandle;
int muMVPMatrixLoc;
int muTexMatrixLoc;
int maPositionLoc;
int maTextureCoordLoc;

void checkGlError(char *op) {
    int error = glGetError();
    if (error != GL_NO_ERROR) {
        LOGE("%s : glError 0x%d", op, error);
    }
}

void checkLocation(int location, char *label) {
    if (location < 0) {
        LOGE("Unable to locate '%s' in program", label);
    }
}

GLuint loadShader(int shaderType, const char *source) {
    GLuint shader = glCreateShader(shaderType);
    checkGlError("glCreateShader type=" + shaderType);
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    return shader;
}

void createProgram() {

    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_2D);

    mProgramHandle = glCreateProgram();
    checkGlError("glCreateProgram");
    if (mProgramHandle == 0) {
        LOGE("Could not create program");
    }
    glAttachShader(mProgramHandle, vertexShader);
    glAttachShader(mProgramHandle, pixelShader);
    checkGlError("glAttachShader");

    glLinkProgram(mProgramHandle);

    maPositionLoc = glGetAttribLocation(mProgramHandle, "aPosition");
    checkLocation(maPositionLoc, "aPosition");
    maTextureCoordLoc = glGetAttribLocation(mProgramHandle, "aTextureCoord");
    checkLocation(maTextureCoordLoc, "aTextureCoord");
    muMVPMatrixLoc = glGetUniformLocation(mProgramHandle, "uMVPMatrix");
    checkLocation(muMVPMatrixLoc, "uMVPMatrix");
    muTexMatrixLoc = glGetUniformLocation(mProgramHandle, "uTexMatrix");
    checkLocation(muTexMatrixLoc, "uTexMatrix");

}

void drawFrame(int textureId, float texMatrix[]) {
    checkGlError("draw start");

    // Select the program.
    glUseProgram(mProgramHandle);
    checkGlError("glUseProgram");

    // Set the texture.
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    checkGlError("glBindTexture");

    // Copy the model / view / projection matrix over.
    glUniformMatrix4fv(muMVPMatrixLoc, 1, GL_FALSE, IDENTITY_MATRIX);
    checkGlError("glUniformMatrix4fv MVPMatrix");

    // Copy the texture transformation matrix over.
    glUniformMatrix4fv(muTexMatrixLoc, 1, GL_FALSE, texMatrix);
    checkGlError("glUniformMatrix4fv texMatrix");

    // Enable the "aPosition" vertex attribute.
    glEnableVertexAttribArray(maPositionLoc);
    checkGlError("glEnableVertexAttribArray");

    // Connect vertexBuffer to "aPosition".
    glVertexAttribPointer(maPositionLoc, 2,
                          GL_FLOAT, GL_FALSE, 8, FULL_RECTANGLE_COORDS);
    checkGlError("glVertexAttribPointer");

    // Enable the "aTextureCoord" vertex attribute.
    glEnableVertexAttribArray(maTextureCoordLoc);
    checkGlError("glEnableVertexAttribArray");

    // Connect texBuffer to "aTextureCoord".
    glVertexAttribPointer(maTextureCoordLoc, 2,
                          GL_FLOAT, GL_FALSE, 8, FULL_RECTANGLE_TEX_COORDS);
    checkGlError("glVertexAttribPointer");

    // Draw the rect.
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    checkGlError("glDrawArrays");

    // Done -- disable vertex array, texture, and program.
    glDisableVertexAttribArray(maPositionLoc);
    glDisableVertexAttribArray(maTextureCoordLoc);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);

    checkGlError("draw end");
}


void releaseProgram() {
    glDeleteProgram(mProgramHandle);
    mProgramHandle = -1;
}