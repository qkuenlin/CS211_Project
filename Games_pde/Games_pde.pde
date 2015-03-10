float rotateY = 0;
float rotateZ = 0;
float rotateX = 0;
float mouse_y = 0;
float mouse_x = 0;
float rotateSpeed = 0.01;

float boxThickness = 8;
float boxLength = 250;

float G = 9.81/60;
float normalForce = 1;
float mu = 0.03;
float frictionMagnitude = normalForce * mu;

Ball ball = new Ball(10);


void setup() {
  size(1000,800,P3D);
  frameRate(60);
  noStroke();
  
}

void draw(){
  camera(width/2, height/2, 400, 500, 350, 0, 0, 1, 0);
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
  box(boxLength,boxThickness,boxLength);
  
  mouse_y = mouseY;
  mouse_x = mouseX;
  
  ball.update();
  ball.checkEdges();
  ball.display();
  
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
  rotateZ -= rotateSpeed*(mouse_x-mouseX);
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

class Ball {
  PVector location;
  PVector velocity;
  PVector gravity;
  PVector friction;
  float  r;
  
  Ball(float r) {
     velocity = new PVector(0,0,0);
     gravity = new PVector(sin(rotateZ) * G, 0, -sin(rotateX) * G);
     this.r=r;
     location = new PVector(0, -(boxThickness+r/2), 0);
     
     
  }
  
  void update(){
    PVector friction = velocity.get();
    
    gravity.set(sin(rotateZ) * G, 0, -sin(rotateX) * G);
    velocity.add(gravity);
    
    friction.mult(-1);
    friction.normalize();
    friction.mult(frictionMagnitude);
    
    velocity.add(friction);
    location.add(velocity);
    
  }
  
  void display(){ 
    shininess(30);
    specular(204,204,0);
   
    pushMatrix();
    translate(location.x, location.y, location.z); 
    sphere(r);
    popMatrix();
  }
  
  void checkEdges(){
    if (location.x + r > boxLength/2) {
      location.x =boxLength/2 -r;
      velocity.x = velocity.x * -1;
    }
    else if (location.x -r < -boxLength/2){  
      location.x = -boxLength/2 + r;    
       velocity.x = velocity.x * -1;
    }
    
    if (location.z +r>boxLength/2) {
      location.z = boxLength/2 - r;
       velocity.z = velocity.z * -1;
    }
    else if (location.z -r<-boxLength/2){
      location.z = -boxLength/2 + r;
       velocity.z = velocity.z * -1;
    }
  }
}
