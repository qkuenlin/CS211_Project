Mover mover;
PVector GRAVITY = new PVector(0, 1);
float d = 48;

void setup() {
  size(800,200);
   mover = new Mover();
   frameRate(60);
}

void draw() {
  background(255);
   
  mover.update();
  mover.checkEdges();
  mover.display();
}

class Mover {
  PVector location;
  PVector velocity;
  
  Mover() {
     location = new PVector(width/2, height/2);
     velocity = new PVector(1,2);
  }
  
  void update(){
    velocity.add(GRAVITY);
    location.add(velocity);
    
  }
  
  void display(){
    stroke(0);
    strokeWeight(2);
    fill(127);
    
    ellipse(location.x, location.y, d, d);
  }
  
  void checkEdges(){
    if (location.x + d/2> width) {
      location.x = width - d/2;
      velocity.x = velocity.x * -1;
    }
    else if (location.x -d/2<0){  
      location.x = d/2;    
       velocity.x = velocity.x * -1;
    }
    
    if (location.y +d/2> height) {
      location.y = height - d/2;
       velocity.y = velocity.y * -1;
    }
    else if (location.y -d/2<0){
      location.y = d/2;
       velocity.y = velocity.y * -1;
    }
  }
}

