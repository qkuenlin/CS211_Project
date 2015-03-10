float rotateY = 0;
float rotateZ = 0;
float rotateX = 0;
float mouse_y = 0;
float mouse_x = 0;
float rotateSpeed = 0.01;


void setup() {
  size(500,500,P3D);
  frameRate(60);
  noStroke();
  
}

void draw(){
  camera(width/2, height/2, 250, 250, 250, 0, 0, 1, 0);
  lightSpecular(204, 204, 204);
  directionalLight(50, 100, 125, 0, -1, 0);
  lightSpecular(100, 100, 100);
  directionalLight(50, 100, 125, 0, 1, 0);
  ambientLight(102, 102, 102);  
  background(200);
  translate(width/2, height/2, 0);
  rotateZ(rotateZ);
  rotateY(rotateY);
  rotateX(rotateX);
  smooth(4);
  shininess(20);
  specular(204,102,0);
  box(100,10,100);
  
  mouse_y = mouseY;
  mouse_x = mouseX;
  
}

void keyPressed() {
  if (key == CODED) {
    if(keyCode == LEFT){
      rotateY -= 10*rotateSpeed;
    }
    else if (keyCode == RIGHT) {
      rotateY += 10*rotateSpeed;
    }
  }
}


void mouseDragged(){
  rotateZ += rotateSpeed*(mouse_x-mouseX);
  if(rotateZ > PI/3) rotateZ = PI/3;
  else if (rotateZ < -PI/3)  rotateZ = -PI/3;
  
  rotateX += rotateSpeed*(mouse_y-mouseY);
  if(rotateX > PI/3) rotateX = PI/3;
   else if (rotateX < -PI/3)  rotateX = -PI/3;
   
}

void mouseWheel(MouseEvent event){
 float e = event.getCount();
 rotateSpeed *= pow(1.5,e);
}
