package tangiblegame;
import processing.core.*;


public class Tower {
	private PApplet parent;
	private PShape[] shapes;
	private PVector position;
	private boolean state; //false = animation inactive; true animation active
	private int fadeCountDown;
	private int frame;
	private float angle;

	public Tower(PVector _position, PApplet _parent, PShape[] _shapes){
		parent = _parent;
		position = _position;
		shapes = _shapes;
		state = false;
		fadeCountDown = 30;
		frame = 0;
		angle = (float) (Math.random() * 2 * Math.PI);
	}
	
	public int getSafeDistance(){
		return 20;
	}

	public PVector getPosition(){
		return this.position;
	}

	public void display(){
		parent.pushMatrix();
		parent.translate(position.x, 0, position.z);
		parent.rotateY(angle);
		if(!getState()){
			parent.shape(shapes[0],0,0); 
		}
		else if (fadeCountDown > 0 && frame == shapes.length-1){
			if(!TangibleGame.topView){
				fadeCountDown -= 1;
				if(fadeCountDown % 4 == 0 || (fadeCountDown + 1)%4 == 0){
					parent.fill(255, 100);
					parent.shape(shapes[shapes.length-1], 0, 0);
					parent.fill(255,255);
				}
			}
			else parent.shape(shapes[shapes.length-1], 0, 0);
		}
		else if(fadeCountDown <= 0 && frame == shapes.length-1){
			TangibleGame.cylinders.remove(this);
		}
		else {
			frame = Math.min(frame, shapes.length-1);
			parent.shape(shapes[frame],0,0);
			if(!TangibleGame.topView){
				frame +=1;
			}
		}
		parent.popMatrix();
	}
	
	public void activate(){		
		state = true;
	}

	public boolean getState() {
		return state;
	}

}
