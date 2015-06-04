package tangiblegame;
import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

public class loadCylinderPar implements Runnable {
	
	int from;
	int to;
	PShape[] frame;
	PApplet parent;
	int index;
	PImage texture;
	
	loadCylinderPar(int from, int to, PShape[] frame, PApplet parent, int index, PImage texture){
		this.from = from;
		this.to = to;
		this.frame = frame;
		this.parent = parent;
		this.index = index;
		this.texture = texture;
	} 
	

	@Override
	public void run() {
		StopWatch sw = new StopWatch();
		sw.start();
		for(int i = from; i < to; i++){
			String name = "../assets/cylinder/destruction_tower_000";
			if(i<10) name += "00"+i+".obj";
			else if (i<100) name += "0" + i+".obj";
			else name += i + ".obj";
			//System.out.println("Thread " + index + ": loading frame " + i + "/" + to + " - reste : " + (to-i));
			frame[i] = new AnimatedPShape(parent, name, texture);
			int prevTextureMode = parent.g.textureMode;
			parent.g.textureMode = PConstants.NORMAL;
			PShapeOpenGL p3d = PShapeOpenGL.createShape3D((PGraphicsOpenGL)parent.g, frame[i]);
			parent.g.textureMode = prevTextureMode;
			p3d.scale(0.5f);
			frame[i] = p3d;
			if(frame[i]==null){
				throw new IllegalArgumentException("loadShape() return null");
			}
			TangibleGame.percent += (float) 1/(7.0f+frame.length);;
		}
		sw.stop();
		//System.out.println("Thread " + index + " done in " + sw.getElapsedTime() + "ns");
		
	}

}