package tangiblegame;
import processing.core.*;

import java.lang.Math;


public class Ball {
	PVector location;
	PVector velocity;
	PVector gravity;
	PVector friction;
	float  r;
	PApplet parent;

	Ball(int d, PApplet _parent) {
		parent = _parent;
		velocity = new PVector(0,0,0);
		gravity = new PVector((float) (Math.sin(TangibleGame.rotateZ) * TangibleGame.G), 0, -PApplet.sin(TangibleGame.rotateX) * TangibleGame.G);
		this.r=d/2;
		location = new PVector(0, -(TangibleGame.boxThickness+r/2), 0);


	}

	void reset(){
		velocity = new PVector(0,0,0);
		gravity = new PVector((float) (Math.sin(TangibleGame.rotateZ) * TangibleGame.G), 0, -PApplet.sin(TangibleGame.rotateX) * TangibleGame.G);
		location = new PVector(0, -(TangibleGame.boxThickness+r/2), 0);
	}

	//Function to upate the ball position and velocity according to physics
	void update(){
		PVector friction = velocity.get();

		gravity.set(PApplet.sin(TangibleGame.rotateZ) * TangibleGame.G, (float) 0, -PApplet.sin(TangibleGame.rotateX) * TangibleGame.G);
		velocity.add(gravity);

		friction.mult(-1);
		friction.normalize();
		friction.mult(TangibleGame.frictionMagnitude);

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
		if (location.x + r > TangibleGame.boxLength/2) {
			location.x = TangibleGame.boxLength/2 -r;
			velocity.x = velocity.x * -1;
			//Update score
			TangibleGame.updateScore(-velocity.mag());
		}
		else if (location.x -r < -TangibleGame.boxLength/2){  
			location.x = - TangibleGame.boxLength/2 + r;    
			velocity.x = velocity.x * -1;
			//Update score
			TangibleGame.updateScore(-velocity.mag());
		}

		if (location.z +r> TangibleGame.boxLength/2) {
			location.z = TangibleGame.boxLength/2 - r;
			velocity.z = velocity.z * -1;
			//Update score
			TangibleGame.updateScore(-velocity.mag());
		}
		else if (location.z -r<- TangibleGame.boxLength/2){
			location.z = - TangibleGame.boxLength/2 + r;
			velocity.z = velocity.z * -1;
			//Update score
			TangibleGame.updateScore(-velocity.mag());
		}
	}

	//Check if the ball collides with the cylinders.
	void checkCylinderCollision(){
		for(Tower tower : TangibleGame.cylinders){
			if(!tower.getState()){

				PVector n = new PVector(location.x- tower.getPosition().x, location.z- tower.getPosition().z);
				double distance = n.mag();


				double safeDistance = tower.getSafeDistance();


				if(distance-r <= safeDistance){
					n.normalize();

					location.x = n.x*(20+r) + tower.getPosition().x;
					location.z = n.y*(20+r) + tower.getPosition().z;       


					PVector velocityTemp = new PVector(velocity.x, velocity.z); 

					float vectorDot = 2*PVector.dot(velocityTemp,n);
					PVector tempV = new PVector(0,0);
					PVector.mult(n,vectorDot,tempV);     
					PVector.sub(velocityTemp, tempV, velocityTemp);

					velocity.x = velocityTemp.x;
					velocity.z = velocityTemp.y;

					//Update Score
					TangibleGame.updateScore(velocity.mag());
					tower.activate();

				}
			}
		}
	}
}
