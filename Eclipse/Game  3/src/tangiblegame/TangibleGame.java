package tangiblegame;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;
import processing.video.Capture;
import processing.video.Movie;

@SuppressWarnings("serial")
public class TangibleGame extends PApplet{

	static final String BASEPATH = "../assets/";

	public static float rotateY = 0;
	public static float rotateZ = 0;
	public static float rotateX = 0;
	public static float mouse_y = 0;
	public static float mouse_x = 0;
	public static float rotateSpeed = (float) 0.01;

	public static float boxThickness = 0;
	public static float boxLength = 600;

	public static float G = (float) (9.81/50);
	public static float normalForce = 1;
	public static float mu = (float) 0.03;
	public static float frictionMagnitude = normalForce * mu;

	public PShape[] cylindersShape;
	public static ArrayList<Tower> cylinders = new ArrayList<Tower>();
	public static int numberFrames = 90;
	public static PImage texture;

	public static boolean topView = false; //true = top view mode activated ; false= Top view not activated

	Ball ball = new Ball(10, this);

	PGraphics gameOverviewSurface;
	PGraphics barSurface;
	PGraphics scoreboardSurface;
	PGraphics barChartSurface;

	static float currentScore = 0;
	static float lastScore = 0;
	static float maxScore = 0;
	static ArrayList<Float> scoreList = new ArrayList<Float>();

	int barSurfaceHeight=100;
	int surfaceMargin = 5;
	int scrollHeight = 10;

	StopWatch sw = new StopWatch();
	HScrollbar hs;

	static boolean startMenu = true;

	private boolean loading;
	private boolean warmup = false;
	static float percent=0;

	static String playerName;

	public final static int TIMER_MAX = 2000;
	public static int timer = TIMER_MAX;
	PMatrix3D originalMartix;

	private PShape background;

	public static PImage logo;

	Movie video;
	Capture cam;
	boolean isVideo;
	ImageProcessing imgPro;
	int setup;

	int threshold1;
	int threshold2;
	HScrollbar thresholdBar1;
	HScrollbar thresholdBar2;

	int sat_threshold1;
	int sat_threshold2;
	int hue_threshold1;
	int hue_threshold2;
	int int_threshold;

	public void setup() {

		sw.start();
		size(800,800,P3D);  

		originalMartix = (PMatrix3D) getMatrix();

		frameRate(60);

		noStroke();
		sw.stop();      

		//charger la video: utiliser un absolute path
		System.out.println("Setup movie");
		video = new Movie(this,  BASEPATH+"testvideo.mov");

		imgPro = new ImageProcessing(this, 255, 100, 133, 38, 65);

		if(video.height !=0){
			isVideo = true;       
			setup = Integer.MAX_VALUE;
			System.out.println("Movie: video: name: " + video.filename + " height: " + video.height + " width: " + video.width);
			video.loop();
		}
		else{
			String[] cameras = Capture.list();
			if (cameras.length == 0) {
				println("There are no cameras available for capture.");
				exit();
			} else {
				println("Available cameras:");
				for (int i = 0; i < cameras.length; i++) {
					println(cameras[i]);
				}
				cam = new Capture(this, cameras[1]);
				cam.start();
			}

			setup = 0;
			isVideo = false;
		}

		thresholdBar1 = new HScrollbar(0, 550, 800, 20, this);
		thresholdBar2 = new HScrollbar(0, 580, 800, 20, this);		

		System.out.println("Create window : " + sw.getElapsedTime());

		loading = true;

		logo = loadImage(BASEPATH+"Logo.png");

		thread("load");

	}


	public void draw(){ 
		if(loading){
			loadingScreen();
		}
		else if(!isVideo && setup<Integer.MAX_VALUE){
			camSetup();
		}
		else {
			game();
		}


	}

	public void game(){
		background(200);


		noStroke();

		//Draw Game 

		pushMatrix();

		//Normal videoera if topView is false
		if(!topView){
			camera(0, -350, 850, ball.location.x, ball.location.y, ball.location.z, 0, 1, 0);
		} 

		//Light
		lightSpecular(200, 200, 200);
		directionalLight(50, 100, 125, 0, 1, 0);
		ambientLight(200, 200, 200);


		pushMatrix();       

		//Rotation of plane
		rotateZ(rotateZ);
		rotateY(rotateY);
		rotateX(rotateX);

		//videoera on top of pane if topView is true
		if(topView){
			camera(0, -850, 0, 0, 0, 0, 0, 0, 1);
		}

		//Artistic effect :)
		smooth(4);
		shininess(20);
		specular(204,102,0);

		//draw the plane
		/*fill(0,100,0,255);
        box(boxLength,boxThickness,boxLength); 
        fill(255);
		 */
		shape(background,0,1);


		//draw the cylinders
		pushMatrix();
		translate(0,-boxThickness/2, 0);
		for(int i=0; i<cylinders.size(); i++){
			cylinders.get(i).display();
		}
		popMatrix();

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
		pushMatrix();
		hint(DISABLE_DEPTH_TEST);

		resetMatrix();
		applyMatrix(originalMartix);
		ambientLight(120,120,120);
		fill(255);
		//Draw UI
		drawBarSurface();
		image(barSurface,0,height - barSurfaceHeight);
		drawGameOverviewSurface();
		image(gameOverviewSurface, surfaceMargin, height - gameOverviewSurface.height- surfaceMargin);
		drawScoreboardSurface();
		image(scoreboardSurface, 2*surfaceMargin+gameOverviewSurface.width, height - scoreboardSurface.height- surfaceMargin);
		drawBarChartSurface();
		image(barChartSurface, 3*surfaceMargin+gameOverviewSurface.width+scoreboardSurface.width, height - barChartSurface.height- surfaceMargin - scrollHeight);

		//TODO: Tangible: Affichage de la vidéo
		// Ne fonctionne pas... je ne sais pas pourquoi...
		PImage img;
		if(isVideo){
			if (video.available() == true) {
				video.read(); 
			}
			img = video.get();
		}
		else{
			if (cam.available() == true) {
				cam.read();
			}
			img = cam.get();
		}

		img.resize(img.width/4, img.height/4);
		image(img, 0, 0);

		//END TODO
		hs.update();
		hs.display();

		fill(255);

		hint(ENABLE_DEPTH_TEST);
		popMatrix();
		popMatrix();

	}


	public void loadingScreen(){
		background(0);
		stroke(255);
		noFill();
		image(logo, width/2 - logo.width/2, height/2 - logo.height/2);
		rect(width/2-150, height/2 + logo.height/2 + 20, 300, 10);
		fill(255);
		// The size of the rectangle is mapped to the percentage completed
		float w = map(percent, 0, 1, 0, 300);
		rect(width/2-150, height/2 + logo.height/2 + 20, w, 10);
		textSize(16);
		textAlign(CENTER);
		fill(255);
		text("Loading...", width/2, height/2 + logo.height/2+50);
		if(warmup){
			sw.start();
			Tower warmUp = new Tower(new PVector(10000,10000,0), this, cylindersShape);
			warmUp.activate();
			for(int i=0; i<numberFrames; i++){
				warmUp.display();
			}
			warmUp = null;
			percent = 1;
			sw.stop();
			System.out.println("Warm Up: " + sw.getElapsedTime());
			loading = false;
			warmup = false;
		}
	}

	public void camSetup(){

		if (cam.available() == true) {
			cam.read();
		}    	

		background(color(0, 0, 0));
		PImage img = cam.get();
		PImage toDisplay = img;
		String text = "";

		threshold1 = (int) (thresholdBar1.getPos() * 255);
		threshold2 = (int) (thresholdBar2.getPos() * 255);

		thresholdBar1.display();
		thresholdBar1.update();
		if (setup != 2) {
			thresholdBar2.display();
			thresholdBar2.update();
		}

		color(250);

		switch(setup){
		case 0:
			toDisplay = imgPro.saturationFilter(img, threshold1, threshold2);
			text = "Choose maximum and minimum saturation level --- Press \"ENTER\" key to validate";
			break;
		case 1:
			toDisplay = imgPro.saturation_hue(img, sat_threshold1, sat_threshold2, threshold1, threshold2);
			text = "Choose maximum and minimum hue level --- Press \"ENTER\" key to validate";
			break;
		case 2:
			toDisplay = imgPro.binaryIntensity(imgPro.gaussianBlur(imgPro.saturation_hue(img, sat_threshold1, sat_threshold2, hue_threshold1, hue_threshold2)),threshold1);
			text = "Choose intensity threshold level --- Press \"ENTER\" key to validate";
			break;
		}

		image(toDisplay, 0, 0);
		text(text, width/2, 530);




	}

	/*TODO: Tangible: Thread pour calculer la rotation de la plaque. 
        Le Thread fonctionne plus ou moins avec des images: le jeux "clignotte" de temps en temps
        Mais ne fonctionne pas avec la webvideo, ni la vidéo (du moins chez moi?)
	 */
	public void tangible(){ 
		PImage img;
		if(isVideo){
			img = video.get();
		}
		else img = cam.get();

		PVector rot = imgPro.getRotation(img);

		rotateZ=rot.z;
		rotateY=rot.y;
		rotateX=rot.x;

	}
	//END TODO

	public void load() throws IOException{
		sw.start();

		percent += 1/(10.0f+numberFrames);

		texture = loadImage(BASEPATH+"textures/Textures_all.png");

		percent += 1/(9.0f+numberFrames);

		barSurface = createGraphics(width, barSurfaceHeight, P2D);
		gameOverviewSurface = createGraphics(barSurfaceHeight-2*surfaceMargin, barSurfaceHeight-2*surfaceMargin, P2D);
		scoreboardSurface = createGraphics(barSurfaceHeight-2*surfaceMargin, barSurfaceHeight-2*surfaceMargin, P2D);
		barChartSurface = createGraphics(width-gameOverviewSurface.width-scoreboardSurface.width-4*surfaceMargin, barSurfaceHeight-2*surfaceMargin-scrollHeight, P2D);
		hs = new HScrollbar(3*surfaceMargin+gameOverviewSurface.width+scoreboardSurface.width,height - surfaceMargin - scrollHeight,width-gameOverviewSurface.width-scoreboardSurface.width-4*surfaceMargin, scrollHeight, this);

		percent += 1/(10.0f+numberFrames);

		sw.stop();
		System.out.println("Interface : " + sw.getElapsedTime());
		background = loadShape(BASEPATH+"tower.obj");
		//System.out.println("background ok");
		background.scale(1.75f);
		sw.start();
		cylindersShape = loadCylinder();
		sw.stop();
		System.out.println("Shapes Loading: " + sw.getElapsedTime());
		warmup = true;
	}

	PShape[] loadCylinder() {
		PShape[] cubeframe = new PShape[numberFrames];
		cubeframe[0] = new AnimatedPShape(this, BASEPATH+"cylinder/tower.obj", texture);
		int prevTextureMode = this.g.textureMode;
		this.g.textureMode = NORMAL;
		PShapeOpenGL p3d = PShapeOpenGL.createShape3D((PGraphicsOpenGL)this.g, cubeframe[0]);
		this.g.textureMode = prevTextureMode;
		p3d.scale(0.5f);
		cubeframe[0] = p3d;
		if(cubeframe[0]==null){
			throw new IllegalArgumentException("loadShape() return null");
		}
		percent += (float) 1/(1.0f*numberFrames);
		ArrayList<Thread> th = new ArrayList<Thread>();
		int nTh = 8;
		for(int i=1; i <= nTh; i++){            
			th.add(new Thread(new loadCylinderPar(max(1,(i-1)*cubeframe.length/nTh), i*cubeframe.length/nTh, cubeframe, this, i, texture)));
		}
		for(int i=0; i<th.size(); i++){
			th.get(i).start();

		}for(int i=0; i<th.size(); i++){
			try {
				th.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return cubeframe;

	}

	//Drawing code of GameOverview UI
	private void drawGameOverviewSurface() {
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
			gameOverviewSurface.ellipse((TangibleGame.cylinders.get(i).getPosition().x+boxLength/2)*gameToSurfaceScale, (TangibleGame.cylinders.get(i).getPosition().z+boxLength/2)*gameToSurfaceScale, 20*gameToSurfaceScale*2,20*gameToSurfaceScale*2);
		}
		gameOverviewSurface.endDraw();
	}

	//Drawing code of Bar UI
	private void drawBarSurface(){
		barSurface.beginDraw();
		barSurface.background(225,221,202);
		barSurface.endDraw();
	}

	//Drawing code of Scoreboard UI
	private void drawScoreboardSurface(){
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

	public static void updateScore(float gain){
		lastScore = gain;
		if(gain>=0){
			if (gain>0.5) { //"If" to avoid cheating by letting the ball on contact of cylinder
				currentScore += gain;
				scoreList.add(currentScore);

			}

		}else{
			currentScore += gain;
			if (currentScore<0) currentScore = 0;
			else scoreList.add(currentScore);
		}
		if(currentScore > maxScore) maxScore = currentScore;

	}

	//Drawing code of BarChart UI
	private void drawBarChartSurface(){
		barChartSurface.beginDraw();
		barChartSurface.background(254,250,231);


		barChartSurface.noStroke();

		for(int i=0; i<scoreList.size(); i++){

			float rectWidth = max(9*hs.getPos()+1,(barChartSurface.width/scoreList.size()));

			float rectHeight = scoreList.get(i)/maxScore * (barChartSurface.height-surfaceMargin);


			barChartSurface.fill(0,0,200);
			barChartSurface.rect(barChartSurface.width-(scoreList.size()-i)*rectWidth,barChartSurface.height + surfaceMargin - rectHeight,rectWidth, rectHeight); 
		}



		barChartSurface.endDraw();
	}

	public void keyPressed() {
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
		else if(keyCode == 10 ) {
			switch(setup){
			case 0:
				sat_threshold1 = threshold1;
				sat_threshold2 = threshold2;
				setup = 1;
				break;
			case 1:
				hue_threshold1 = threshold1;
				hue_threshold2 = threshold2;
				setup = 2;
				break;
			case 2:
				int_threshold = threshold1;
				imgPro.setThreshold(sat_threshold1, sat_threshold2, hue_threshold1, hue_threshold2, int_threshold);
				setup = Integer.MAX_VALUE;
				break;
			}
		}
	}

	public void  keyReleased() {
		if (key == CODED) {
			if (keyCode == SHIFT) {
				topView = false;
			}
		}
	}

	public void mouseClicked(){
		//if topView is true, a cylinder is added where the mouse has been clicked
		if(topView){
			float x = (float) ((mouseX-width*0.5)*1.2);
			float y = (float) ((mouseY-height*0.5)*1.2);
			if( x < boxLength/2 && x > -boxLength/2 && y > -boxLength/2 && y < boxLength/2){
				cylinders.add(new Tower(new PVector(x,0,y), this, cylindersShape)); 
			}
		}
	}

	public void mouseDragged(){
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
	public void mouseWheel(MouseEvent event){
		float e = event.getCount();
		rotateSpeed *= pow((float) 1.5,e);
	}    

}