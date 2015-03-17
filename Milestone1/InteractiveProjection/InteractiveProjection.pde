float rotationX = 0;
float rotationY = 0;
float scaleRate = 1;
float mouse_y = 0;
float mouse_x = 0;

void setup() {
  size(300, 300, P2D);
  frameRate(60);
}

void draw(){
  background(255,255,255);
  translate(width/2, height/2, 0);
  My3DPoint eye = new My3DPoint(-100, -100, -5000);
  My3DPoint origin = new My3DPoint(0,0,0);
  My3DBox input3DBox = new My3DBox(origin, 100, 100, 100);
  

  float[][] scale = scaleMatrix(scaleRate, scaleRate, scaleRate);
  float[][] rotateX = rotateXMatrix(rotationX);
  float[][] rotateY = rotateYMatrix(rotationY);
  input3DBox = transformBox(input3DBox, scale);
  input3DBox = transformBox(input3DBox, rotateX);
  input3DBox = transformBox(input3DBox, rotateY);
  projectBox(eye, input3DBox).render();
  
  mouse_y = mouseY;
  mouse_x = mouseX;

}


void mouseDragged(){
  scaleRate += 0.01*(mouse_y-mouseY);
}

void keyPressed(){
  if (key == CODED){
    if (keyCode == UP){
      rotationX += 0.1;
    }
    else if(keyCode == DOWN){
      rotationX -= 0.1;
    }
    if(keyCode == LEFT){
      rotationY -= 0.1;
    }
    else if(keyCode == RIGHT){
      rotationY += 0.1;
    }
  }
}

class My2DPoint{
  float x;
  float y;
  My2DPoint(float x, float y){
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z){
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
 void render(){
   
  line(s[0].x, s[0].y ,s[1].x, s[1].y); 
  line(s[0].x, s[0].y ,s[4].x, s[4].y);
  line(s[0].x, s[0].y ,s[3].x, s[3].y);
  line(s[1].x, s[1].y ,s[2].x, s[2].y);
  line(s[1].x, s[1].y ,s[5].x, s[5].y);
  line(s[2].x, s[2].y ,s[3].x, s[3].y); 
  line(s[2].x, s[2].y ,s[6].x, s[6].y);
  line(s[3].x, s[3].y ,s[7].x, s[7].y);
  line(s[4].x, s[4].y ,s[5].x, s[5].y);
  line(s[4].x, s[4].y ,s[7].x, s[7].y);
  line(s[5].x, s[5].y ,s[6].x, s[6].y);
  line(s[6].x, s[6].y ,s[7].x, s[7].y);   
  };
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ){
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
    this.p = new My3DPoint[]{new My3DPoint(x, y+dimY, z+dimZ),
                            new My3DPoint(x, y, z+dimZ),
                            new My3DPoint(x+dimX, y, z+dimZ),
                            new My3DPoint(x+dimX, y+dimY, z+dimZ),
                            new My3DPoint(x, y+dimY, z),
                            origin,
                            new My3DPoint(x+dimX, y, z),
                            new My3DPoint(x+dimX, y+dimY, z),
    };
  }
  My3DBox(My3DPoint[]p) {
  this.p = p;  
  }
}
    

My2DPoint projectPoint(My3DPoint eye, My3DPoint p){
  float Xp = (p.x - eye.x)/(-p.z/eye.z+1);
  float Yp = (p.y - eye.y)/(-p.z/eye.z+1);
  return new My2DPoint(Xp, Yp);
}

My2DBox projectBox(My3DPoint eye, My3DBox box){
  My2DPoint[] s = new My2DPoint[8];
  for(int i=0; i<8; i++){
    s[i] = projectPoint(eye, box.p[i]);
  };
  return new My2DBox(s);
}

float[] homogeneous3DPoint(My3DPoint p) {
  float[] result = {p.x, p.y, p.z, 1};
  return result;
}

float[][] rotateXMatrix(float angle){
  return(new float[][] {{1,0,0,0},
                        {0, cos(angle), sin(angle), 0},
                        {0, -sin(angle), cos(angle), 0},
                        {0, 0, 0, 1}});
}

float[][] rotateYMatrix(float angle){
  return(new float[][] {{cos(angle),0,sin(angle),0},
                        {0, 1, 0, 0},
                        {-sin(angle),0, cos(angle), 0},
                        {0, 0, 0, 1}});
}

float[][] rotateZMatrix(float angle){
  return(new float[][] {{cos(angle),-sin(angle),0,0},
                        {sin(angle),cos(angle),0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 0, 1}});
}

float[][] translationMatrix(float x, float y, float z){
  return(new float[][] {{1,0,0,x},
                        {0,1,0,y},
                        {0,0,1,z},
                        {0,0,0,1}});
}

float[][] scaleMatrix(float x, float y, float z){
  return(new float[][] {{x,0,0,0},
                        {0,y,0,0},
                        {0,0,z,0},
                        {0,0,0,1}});
}

float[] matrixProduct(float[][] a, float[] b){
  float[] result = new float[b.length];
  for(int i=0; i< a.length; i++){
    float acc = 0;
    for(int j=0; j<b.length;j++){
       acc += a[i][j]*b[j];  
    }
    result[i] = acc;
  }
  return result;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix){
  My3DPoint[] transform3DBoxPoints = new My3DPoint[8];
  for(int i=0; i<8; i++){
    float[] vector = homogeneous3DPoint(box.p[i]);    
    transform3DBoxPoints[i] = euclidian3DPoint(matrixProduct(transformMatrix, vector));        
  }
  return new My3DBox(transform3DBoxPoints);
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}
