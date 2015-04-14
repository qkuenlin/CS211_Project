import processing.core.*;

import java.lang.Math;


public class Ball {
	PVector location;
	  PVector velocity;
	  PVector gravity;
	  PVector friction;
	  float  r;
	  PApplet parent;
	  
	  Ball(int r, PApplet _parent) {
		 parent = _parent;
	     velocity = new PVector(0,0,0);
	     gravity = new PVector((float) (Math.sin(MainApp.rotateZ) * MainApp.G), 0, -PApplet.sin(MainApp.rotateX) * MainApp.G);
	     this.r=r;
	     location = new PVector(0, -(MainApp.boxThickness+r/2), 0);
	     
	     
	  }
	  
	  //Function to upate the ball position and velocity according to physics
	  void update(){
	    PVector friction = velocity.get();
	    
	    gravity.set(PApplet.sin(MainApp.rotateZ) * MainApp.G, (float) 0, -PApplet.sin(MainApp.rotateX) * MainApp.G);
	    velocity.add(gravity);
	    
	    friction.mult(-1);
	    friction.normalize();
	    friction.mult(MainApp.frictionMagnitude);
	    
	    velocity.add(friction);
	    location.add(velocity);
	    
	  }
	  
	  //diplay the ball
	  void display(){ 
	    parent.shininess(30);
	    parent.specular(204,204,0);
	   
	    parent.pushMatrix();
	    parent.translate(location.x, location.y, location.z); 
	    parent.sphere(r);
	    parent.popMatrix();
	    
	  }
	  
	  //Check if the ball touch the border of the plane. In this case the ball bounces on it by inverting the speed.
	  void checkEdges(){
	    if (location.x + r > MainApp.boxLength/2) {
	      location.x = MainApp.boxLength/2 -r;
	      velocity.x = velocity.x * -1;
	      //Update score
	      MainApp.updateScore(-velocity.mag());
	    }
	    else if (location.x -r < -MainApp.boxLength/2){  
	      location.x = - MainApp.boxLength/2 + r;    
	       velocity.x = velocity.x * -1;
	       //Update score
	       MainApp.updateScore(-velocity.mag());
	    }
	    
	    if (location.z +r> MainApp.boxLength/2) {
	      location.z = MainApp.boxLength/2 - r;
	       velocity.z = velocity.z * -1;
	       //Update score
	       MainApp.updateScore(-velocity.mag());
	    }
	    else if (location.z -r<- MainApp.boxLength/2){
	      location.z = - MainApp.boxLength/2 + r;
	       velocity.z = velocity.z * -1;
	       //Update score
	       MainApp.updateScore(-velocity.mag());
	    }
	  }
	  
	  //Check if the ball collides with the cylinders.
	  void checkCylinderCollision(){
	    for(int i=0; i< MainApp.cylinders.size(); i++){
	      
	     PVector n = new PVector(location.x- MainApp.cylinders.get(i).x, location.z- MainApp.cylinders.get(i).z);
	     double distance = n.mag();
	      
	      if(distance-r <= 20){        
	        
	         n.normalize();
	        
	        location.x = n.x*(20+r)+ MainApp.cylinders.get(i).x;
	        location.z = n.y*(20+r)+ MainApp.cylinders.get(i).z;       
	       
	        
	        PVector velocityTemp = new PVector(velocity.x, velocity.z); 
	        
	        float vectorDot = 2*PVector.dot(velocityTemp,n);
	        PVector tempV = new PVector(0,0);
	        PVector.mult(n,vectorDot,tempV);     
	        PVector.sub(velocityTemp, tempV, velocityTemp);
	              
	        velocity.x = velocityTemp.x;
	        velocity.z = velocityTemp.y;
	        
	        //Update Score
	        MainApp.updateScore(velocity.mag());
	  
	      }
	    }
	  }
}