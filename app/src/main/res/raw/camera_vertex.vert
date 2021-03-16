attribute vec4 a_Position;
attribute vec4 a_TextureCoordinates;

uniform mat4 u_Matrix;

varying vec2 v_TextureCoordinates;

void main(){
    gl_Position = a_Position;
    v_TextureCoordinates = (u_Matrix*a_TextureCoordinates).xy;
}