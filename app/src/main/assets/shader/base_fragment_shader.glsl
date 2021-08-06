
#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 aCoord;

uniform sampler2D vTexture;


void main() {

    gl_FragColor = texture2D(vTexture,aCoord);
}
