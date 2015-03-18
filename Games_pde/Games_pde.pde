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

PShape cylinder;
ArrayList<PVector> cylinders = new ArrayList<PVector>();

boolean topView = false; //true = top view mod ; false=Top view not activated

Ball ball = new Ball(10);

void setup() {
  size(1000,800,P3D);
  frameRate(60);
  noStroke();
  cylinder = loadShape("cylinder.obj");
}

void draw(){
  
  if(!topView){
    camera(0, -100, 400, 0, 0, 0, 0, 1, 0);
  }
  
  
  
  lightSpecular(204, 204, 204);
  directionalLight(50, 100, 125, 0, -1, 0);
  lightSpecular(100, 100, 100);
  directionalLight(50, 100, 125, 0, 1, 0);
  ambientLight(102, 102, 102);  
  background(200);
  rotateZ(rotateZ);
  rotateY(rotateY);
  rotateX(rotateX);
  if(topView){
    camera(0, -300, 0, 0, 0, 0, 0, 0, 1);
  }
  smooth(4);
  shininess(20);
  specular(204,102,0);
  box(boxLength,boxThickness,boxLength);  
  
  drawCylinders();
  
  mouse_y = mouseY;
  mouse_x = mouseX;
  
  if(!topView){
    ball.update();  
    ball.checkEdges();
    ball.checkCylinderCollision();
  }
  
  ball.display();
  
}

void drawCylinders() {
  pushMatrix();
  translate(0,-boxThickness/2, 0);
  for(int i=0; i<cylinders.size(); i++){
      pushMatrix();
      translate(cylinders.get(i).x, 0, cylinders.get(i).z);
      shape(cylinder,0,0,40,40); 
      popMatrix();
  }
  popMatrix();
  
}

void keyPressed() {
  if (key == CODED) {
    if(keyCode == LEFT){
      rotateY -= 10*rotateSpeed;
    }
    else if (keyCode == RIGHT) {
      rotateY += 10*rotateSpeed;
    }
    else if (keyCode == SHIFT) {
      topView = true;
    }
  }
}

void  keyReleased() {
  if (key == CODED) {
    if (keyCode == SHIFT) {
       topView = false;
    }
  }
}

void mouseClicked(){
 if(topView){ 
   
   cylinders.add(new PVector((mouseX-width*0.5)/2.5,0,(mouseY-height*0.5)/2.5)); 
 }
}

void mouseDragged(){
  if(!topView){
    rotateZ -= rotateSpeed*(mouse_x-mouseX);
    if(rotateZ > PI/3) rotateZ = PI/3;
    else if (rotateZ < -PI/3)  rotateZ = -PI/3;
  
    rotateX += rotateSpeed*(mouse_y-mouseY);
    if(rotateX > PI/3) rotateX = PI/3;
     else if (rotateX < -PI/3)  rotateX = -PI/3;
  }
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
  
  void checkCylinderCollision(){
    for(int i=0; i<cylinders.size(); i++){
      
     PVector n = new PVector(location.x-cylinders.get(i).x, location.z-cylinders.get(i).z);
     double distance = n.mag();
      
      if(distance-r <= 20){        
        
         n.normalize();
        
        location.x = n.x*(20+r)+cylinders.get(i).x;
        location.z = n.y*(20+r)+cylinders.get(i).z;       
       
        
        PVector velocityTemp = new PVector(velocity.x, velocity.z); 
        
        float vectorDot = 2*PVector.dot(velocityTemp,n);
        PVector tempV = new PVector(0,0);
        PVector.mult(n,vectorDot,tempV);     
        PVector.sub(velocityTemp, tempV, velocityTemp);
              
        velocity.x = velocityTemp.x;
        velocity.z = velocityTemp.y;
  
      }
    }
  }
  
    
}
