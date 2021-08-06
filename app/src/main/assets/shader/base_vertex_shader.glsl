
uniform mat4 u_matrix;
attribute vec4 vPosition;
attribute vec2 vCoord;

varying vec2 aCoord;

void main(){

    gl_Position =  vPosition;
    aCoord =   vCoord;
}
