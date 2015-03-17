float cylinderBaseSize = 50;
float cylinderHeight = 50;
int cylinderResolution = 40;

PShape openCylinder = new PShape();

void setup() {
  size(400, 400, P3D);
  
  float angle;
  float[] x = new float[cylinderResolution + 1];
  float[] y = new float[cylinderResolution + 1];
  
  // get the x and y position on a circle for all the sides
  for(int i = 0; i < x.length; i++){
    angle = (TWO_PI / cylinderResolution) * i;
    x[i] = sin(angle) * cylinderBaseSize;
    y[i] = cos(angle) * cylinderBaseSize;
  }
  
  openCylinder = createShape();
  openCylinder.beginShape(QUAD_STRIP);
  //draw the border of the cylinder
  for(int i=0; i<x.length; i++){
    openCyinder.vertex(x[i], y[i], 0);
    openCyinder.vertex(x[i], y[i], cylinderHeight);
  }
  openCylinder.endShape();
}

void draw() {
  background(255);
  translate(mouseX, mouseY, 0);
  shape(openCylinder);
}

    
