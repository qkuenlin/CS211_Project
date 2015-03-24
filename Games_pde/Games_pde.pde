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
//Cylinder radius = 20;


boolean topView = false; //true = top view mode activated ; false= Top view not activated

Ball ball = new Ball(10);


PGraphics gameOverviewSurface;
PGraphics barSurface;
PGraphics scoreboardSurface;
float currentScore = 0;
float lastScore = 0;

int barSurfaceHeight=100;
int surfaceMargin = 5;

void setup() {
  size(500,500,P3D);
  frameRate(60);
  noStroke();
  cylinder = loadShape("cylinder.obj");
  
  barSurface = createGraphics(width, barSurfaceHeight, P2D);
  gameOverviewSurface = createGraphics(barSurfaceHeight-2*surfaceMargin, barSurfaceHeight-2*surfaceMargin, P2D);
  scoreboardSurface = createGraphics(barSurfaceHeight-2*surfaceMargin, barSurfaceHeight-2*surfaceMargin, P2D);
}

void draw(){
  background(200);
  
  //Draw UI
  drawBarSurface();
  image(barSurface,0,height - barSurfaceHeight);
  drawGameOverviewSurface();
  image(gameOverviewSurface, surfaceMargin, height - gameOverviewSurface.height- surfaceMargin);
  drawScoreboardSurface();
  image(scoreboardSurface, 2*surfaceMargin+gameOverviewSurface.width, height - scoreboardSurface.height- surfaceMargin);
 
  
  //Draw Game
  pushMatrix();
  //Normal Camera if topView is false
  if(!topView){
    camera(0, -100, 400, 0, 0, 0, 0, 1, 0);
  } 
  
  //Light
  lightSpecular(100, 100, 100);
  directionalLight(50, 100, 125, 0, 1, 0);
  ambientLight(102, 102, 102);
  

  
  //Rotation of plane
  rotateZ(rotateZ);
  rotateY(rotateY);
  rotateX(rotateX);
  
  //Camera on top of pane if topView is true
  if(topView){
    camera(0, -300, 0, 0, 0, 0, 0, 0, 1);
  }
  
  //Artistic effect :)
  smooth(4);
  shininess(20);
  specular(204,102,0);
  
  //draw the plane
  box(boxLength,boxThickness,boxLength); 
  
  //draw the cylinders
  drawCylinders();
  
  //get latest mouse coordinates
  mouse_y = mouseY;
  mouse_x = mouseX;
  
  //If topView is false, game continues. otherwise it freezes.
  if(!topView){
    ball.update();  
    ball.checkEdges();
    ball.checkCylinderCollision();
  }
  
  //draw ball
  ball.display();
  
  popMatrix();
  
}

//Drawing code of GameOverview UI
void drawGameOverviewSurface() {
    float gameToSurfaceScale = gameOverviewSurface.height/boxLength;
    gameOverviewSurface.beginDraw();
    gameOverviewSurface.background(0,0,155);
    gameOverviewSurface.noStroke();
    //Draw Ball
    gameOverviewSurface.fill(255,0,0); 
    gameOverviewSurface.ellipse((ball.location.x+boxLength/2)*gameToSurfaceScale, (ball.location.z+boxLength/2)*gameToSurfaceScale, ball.r*gameToSurfaceScale*2, ball.r*gameToSurfaceScale*2);
    //Draw Cylinders
    gameOverviewSurface.fill(255); 
    for(int i=0; i<cylinders.size(); i++){
      gameOverviewSurface.ellipse((cylinders.get(i).x+boxLength/2)*gameToSurfaceScale, (cylinders.get(i).z+boxLength/2)*gameToSurfaceScale, 20*gameToSurfaceScale*2,20*gameToSurfaceScale*2);
    }
    gameOverviewSurface.endDraw();
}

//Drawing code of Bar UI
void drawBarSurface(){
  barSurface.beginDraw();
  barSurface.background(245,241,222);
  barSurface.endDraw();
}

//Drawing code of Scoreboard UI
void drawScoreboardSurface(){
  scoreboardSurface.beginDraw();
  scoreboardSurface.background(0,0);
  scoreboardSurface.stroke(255);
  scoreboardSurface.fill(0,0);
  scoreboardSurface.rect(0,0,scoreboardSurface.width-1, scoreboardSurface.height-1);
  scoreboardSurface.fill(0);
  scoreboardSurface.noStroke();
  String s = "Total score:\n" + String.format("%.3f",currentScore) ;
  scoreboardSurface.text(s, surfaceMargin, 0, 70, 80); 
  s = "Velocity:\n" + String.format("%.3f", ball.velocity.mag()) ;
  scoreboardSurface.text(s, surfaceMargin, (scoreboardSurface.width/3), 70, 80); 
  s = "Last Score:\n" + String.format("%.3f", lastScore) ;
  scoreboardSurface.text(s, surfaceMargin, (2*scoreboardSurface.width/3), 70, 80); 
  scoreboardSurface.endDraw();
}

void updateScore(float gain){
  lastScore = gain;
  if(gain>=0){
      if (gain>0.5) currentScore += gain; //"If" to avoid cheating by letting the ball on contact of cylinder
  }else{
    currentScore += gain;
    if (currentScore<0) currentScore = 0;
  }
}


//Function to draw the cylinders
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
  //if topView is true, a cylinder is added where the mouse has been clicked
 if(topView){
   float x = (mouseX-width*0.5)/2.4;
   float y = (mouseY-height*0.5)/2.4;
   if( x < boxLength/2 && x > -boxLength/2 && y > -boxLength/2 && y < boxLength/2){
   cylinders.add(new PVector(x,0,y)); 
   }
 }
}

void mouseDragged(){
  //if top view is false, the plan can be rotated. Otherwise, the plane stay still
  if(!topView){
    rotateZ -= rotateSpeed*(mouse_x-mouseX);
    if(rotateZ > PI/3) rotateZ = PI/3;
    else if (rotateZ < -PI/3)  rotateZ = -PI/3;
  
    rotateX += rotateSpeed*(mouse_y-mouseY);
    if(rotateX > PI/3) rotateX = PI/3;
     else if (rotateX < -PI/3)  rotateX = -PI/3;
  }
}

// Adjust the speed of rotation
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
  
  //Function to upate the ball position and velocity according to physics
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
  
  //diplay the ball
  void display(){ 
    shininess(30);
    specular(204,204,0);
   
    pushMatrix();
    translate(location.x, location.y, location.z); 
    sphere(r);
    popMatrix();
    
  }
  
  //Check if the ball touch the border of the plane. In this case the ball bounces on it by inverting the speed.
  void checkEdges(){
    if (location.x + r > boxLength/2) {
      location.x =boxLength/2 -r;
      velocity.x = velocity.x * -1;
      //Update score
      updateScore(-velocity.mag());
    }
    else if (location.x -r < -boxLength/2){  
      location.x = -boxLength/2 + r;    
       velocity.x = velocity.x * -1;
       //Update score
       updateScore(-velocity.mag());
    }
    
    if (location.z +r>boxLength/2) {
      location.z = boxLength/2 - r;
       velocity.z = velocity.z * -1;
       //Update score
       updateScore(-velocity.mag());
    }
    else if (location.z -r<-boxLength/2){
      location.z = -boxLength/2 + r;
       velocity.z = velocity.z * -1;
       //Update score
       updateScore(-velocity.mag());
    }
  }
  
  //Check if the ball collides with the cylinders.
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
        
        //Update Score
        updateScore(velocity.mag());
  
      }
    }
  }
  
    
}
