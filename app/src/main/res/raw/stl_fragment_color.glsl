precision mediump float;

uniform vec4 aColor;

varying vec4 ambient;
varying vec4 diffuse;
varying vec4 specular;

void main() {
	vec4 finalColor = aColor;

	gl_FragColor = finalColor*ambient + finalColor*specular + finalColor*diffuse;
}