void setup() {
  size(1000, 1000, P2D);
}
void draw() {
  background(255, 255, 255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0);
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 300);
  
  //rotated around x  
  float[][] transform1 = rotateXMatrix(PI/8);
  input3DBox = transformBox(input3DBox, transform1);
  projectBox(eye, input3DBox).render();
  
  //rotated and translated
  float[][] transform2 = translationMatrix(200, 200, 0);
  input3DBox = transformBox(input3DBox, transform2);
  projectBox(eye, input3DBox).render();
  
  //rotated, translated, and scaled
  float[][] transform3 = scaleMatrix(2, 2, 2);
  input3DBox = transformBox(input3DBox, transform3);
  projectBox(eye, input3DBox).render();
}

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p){
  float normalize = 1 - (p.z/eye.z);
  float newX = (p.x-eye.x)/normalize;
  float newY = (p.y-eye.y)/normalize;
  My2DPoint newPoint = new My2DPoint(newX,newY);
  return newPoint;
}

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
  void render(){
  // Complete the code! use only line(x1, y1, x2, y2) built-in function.
  line(s[0].x,s[0].y,s[1].x,s[1].y);
  line(s[1].x,s[1].y,s[2].x,s[2].y);
  line(s[2].x,s[2].y,s[3].x,s[3].y);
  line(s[3].x,s[3].y,s[0].x,s[0].y);
  
  line(s[4].x,s[4].y,s[7].x,s[7].y);
  line(s[7].x,s[7].y,s[6].x,s[6].y);
  line(s[6].x,s[6].y,s[5].x,s[5].y);
  line(s[5].x,s[5].y,s[4].x,s[4].y);
  
  line(s[0].x,s[0].y,s[4].x,s[4].y);
  line(s[3].x,s[3].y,s[7].x,s[7].y);
  line(s[2].x,s[2].y,s[6].x,s[6].y);
  line(s[1].x,s[1].y,s[5].x,s[5].y);  
  }
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ){
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
    this.p = new My3DPoint[]{new My3DPoint(x,y+dimY,z+dimZ),
        new My3DPoint(x,y,z+dimZ),
        new My3DPoint(x+dimX,y,z+dimZ),
        new My3DPoint(x+dimX,y+dimY,z+dimZ),
        new My3DPoint(x,y+dimY,z),
        origin,
        new My3DPoint(x+dimX,y,z),
        new My3DPoint(x+dimX,y+dimY,z)
      };
  }
  My3DBox(My3DPoint[] p) {
  this.p = p;
  }
}

My2DBox projectBox (My3DPoint eye, My3DBox box) {
  My2DPoint[] projPointsArray = new My2DPoint[8];
  for (int i=0;i<8;i++){
    projPointsArray[i]= projectPoint(eye,box.p[i]);
  }  
  return new My2DBox(projPointsArray) ;
}


//Part II


float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z , 1};
  return result;
}



float[][] rotateXMatrix(float angle) {
  return(new float[][] {{1, 0 , 0 , 0},
  {0, cos(angle), sin(angle) , 0},
  {0, -sin(angle) , cos(angle) , 0},
  {0, 0 , 0 , 1}});
}
float[][] rotateYMatrix(float angle) {
// Complete the code!
  return(new float[][] {{cos(angle), 0 , sin(angle) , 0},
  {0, 1 , 0 , 0},
  {-sin(angle), 0 , cos(angle) , 0},
  {0, 0 , 0 , 1}});
}
float[][] rotateZMatrix(float angle) {
// Complete the code!
 return(new float[][] {{cos(angle), -sin(angle) , 0 , 0},
  { sin(angle), cos(angle),0 , 0},
  {0, 0 , 1 , 0},
  {0, 0 , 0 , 1}});
}
float[][] scaleMatrix(float x, float y, float z) {
// Complete the code!
 return(new float[][] {{x, 0 , 0 , 0},
  { 0, y ,0 , 0},
  {0, 0 , z , 0},
  {0, 0 , 0 , 1}});
}
float[][] translationMatrix(float x, float y, float z) {
// Complete the code!
return(new float[][] {{1, 0 , 0 , x},
  { 0, 1 ,0 , y},
  {0, 0 , 1 , z},
  {0, 0 , 0 , 1}});
}

float[] matrixProduct(float[][] a, float[] b) {
  //4x4 times 4x1
  float x0 = a[0][0]*b[0] + a[0][1]*b[1] + a[0][2]*b[2] + a[0][3]*b[3] ;
  float x1 = a[1][0]*b[0] + a[1][1]*b[1] + a[1][2]*b[2] + a[1][3]*b[3] ;
  float x2 = a[2][0]*b[0] + a[2][1]*b[1] + a[2][2]*b[2] + a[2][3]*b[3] ;
  float x3 = a[3][0]*b[0] + a[3][1]*b[1] + a[3][2]*b[2] + a[3][3]*b[3] ;
  return(new float[] {x0,x1,x2,x3});
}


My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  //Complete the code! You need to use the euclidian3DPoint()
  My3DPoint[] transformPointsArray = new My3DPoint[8];
  for (int i=0;i<8;i++){
    transformPointsArray[i]= euclidian3DPoint( matrixProduct(transformMatrix,homogeneous3DPoint(box.p[i])) );
  }  
  return new My3DBox(transformPointsArray) ;
}
