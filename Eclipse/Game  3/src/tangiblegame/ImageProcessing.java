package tangiblegame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.*;

/*
 * Several filter are available:
 * 0- original image
 * 1- truncate intensity
 * 2- binary intensity
 * 3- hue filter
 * 4- saturation filter
 * 5- convolute
 * 6- gaussianBlur
 * 7- Sobel
 * 8- Sobel on hue filter
 * 9- Hough algorithm / edge detection
 * A- Quads with improved edge detection
 * 
 * Simply choose the filter you want by using the keys '1,2,3,4,5,6,7,8,9' 
 * Choose image using arrow keys Up and Down
 */

public class ImageProcessing{

	PApplet parent;

	static float[] tabSin;
	static float[] tabCos;
	static float discretizationStepsPhi = 0.03f;
	static float discretizationStepsR = 1.25f;
	
	int sat_threshold1;
	int sat_threshold2;
	int hue_threshold1;
	int hue_threshold2;
	int int_threshold;
	
	// dimensions of the accumulator
	int phiDim = (int) (Math.PI / discretizationStepsPhi);

	public ImageProcessing(PApplet parent, int st1, int st2, int ht1, int ht2, int bt ){
		this.parent = parent;
		
		this.sat_threshold1 = st1;
		this.sat_threshold2 = st2;
		this.hue_threshold1 = ht1;
		this.hue_threshold2 = ht2;
		this.int_threshold = bt;

		tabSin = new float[phiDim];
		tabCos = new float[phiDim];
		float ang = 0;
		float inverseR = 1.f / discretizationStepsR;
		for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
			// we can also pre-multiply by (1/discretizationStepsR) since we need it in the Hough loop
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}
	}
	
	public void setThreshold(int st1, int st2, int ht1, int ht2, int bt ){		
		this.sat_threshold1 = st1;
		this.sat_threshold2 = st2;
		this.hue_threshold1 = ht1;
		this.hue_threshold2 = ht2;
		this.int_threshold = bt;
	}

	public PImage improvedEdgeDetection(PImage img) {
		
		PImage result = new PImage(img.width, img.height, PApplet.RGB);

		result = binaryIntensity(
				gaussianBlur(saturation_hue(img, sat_threshold1, sat_threshold2, hue_threshold1, hue_threshold2)),
				int_threshold);
		
		ArrayList<Thread> th = new ArrayList<Thread>();
		int steps = img.height / 16;
		
		PImage output = new PImage(img.width, img.height, PApplet.RGB);
		
		for(int i=0; i<img.height; i += steps){
			th.add(new Thread(new sobelRunnable(i, Math.min(i+steps-1,img.height), result, output, parent)));
		}
		for(int i=0; i<th.size(); i++){
			th.get(i).start();
		}
		for(int i=0; i<th.size(); i++){
			try {
				th.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	
		return output;
	}
	
	


	public PVector getRotation(PImage img){
		//PImage img = cam.get();
		return getRotation(hough(improvedEdgeDetection(img)), img.width, img.height);
	}

	public PImage saturation_hue(PImage img, int satThreshold1, int satThreshold2, int hueThreshold1, int hueThreshold2 ){
		PImage result = new PImage(img.width, img.height, PApplet.RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (parent.saturation(c) > satThreshold1 || parent.saturation(c) < satThreshold2)
					c = 0;
				if (parent.hue(c) > hueThreshold1 || parent.hue(c) < hueThreshold2)
					c = 0;
				else
					c = 255;
				result.set(i, j, parent.color(c));
			}

		}

		return result;
	}
	
	public PImage hueFilter(PImage img, int threshold1, int threshold2) {
		PImage result = parent.createImage(img.width, img.height, PApplet.RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (parent.hue(c) > threshold1 || parent.hue(c) < threshold2)
					c = 0;
				else
					c = 255;
				result.set(i, j, parent.color(c));
			}

		}
		return result;
	}

	public PImage saturationFilter(PImage img, int threshold1, int threshold2) {

		PImage result = parent.createImage(img.width, img.height, PApplet.RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (parent.saturation(c) > threshold1 || parent.saturation(c) < threshold2)
					c = 0;
				result.set(i, j, parent.color(c));
			}

		}
		return result;
	}

	public PImage binaryIntensity(PImage img, int threshold) {
		PImage result = new PImage(img.width, img.height, PApplet.RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (parent.brightness(c) > threshold)
					c = 255;
				else
					c = 0;
				result.set(i, j, parent.color(c));
			}

		}
		return result;
	}

	public PImage gaussianBlur(PImage img) {
		float[][] kernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

		int weight = 99;

		PImage result = new PImage(img.width, img.height, PApplet.RGB);

		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
				int red = 0;
				int green = 0;
				int blue = 0;
				for (int i = 0; i <= 2; i++) {
					for (int j = 0; j <= 2; j++) {
						int clampedX = 0;
						if (x + i - 1 >= 0) {
							if (x + i - 1 >= img.width)
								clampedX = img.width - 1;
							else
								clampedX = x + i - 1;
						}
						int clampedY = 0;
						if (y + j - 1 >= 0) {
							if (y + j - 1 >= img.height)
								clampedY = img.height - 1;
							else
								clampedY = y + j - 1;
						}

						red += parent.red(img.pixels[clampedY * img.width + clampedX])
								* kernel[i][j] / weight;
						green += parent.green(img.pixels[clampedY * img.width
						                                 + clampedX])
						                                 * kernel[i][j] / weight;
						blue += parent.blue(img.pixels[clampedY * img.width + clampedX])
								* kernel[i][j] / weight;

					}
				}
				result.pixels[y * img.width + x] = parent.color(red, green, blue);
			}

		}
		return result;

	}



	public int[] houghAccumulator(PImage img, int rDim){

		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie. white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		
		for (int y = 0; y < img.height; y++) {
			for (int x = 0; x < img.width; x++) {
				// Are we on an edge?
				if (parent.brightness(img.pixels[y * img.width + x]) != 0) {
					for (int accPhi = 0; accPhi < phiDim; accPhi++) {

						//float p = accPhi * discretizationStepsPhi;
						float r = x*tabCos[accPhi] + y*tabSin[accPhi];

						//int rAcc = (int) (r / discretizationStepsR + (rDim - 1) * 0.5f);
						int rAcc = (int) ( r + (rDim -1 )*0.5f);
						accumulator[((accPhi+1)*(rDim+2) + rAcc)] += 1;
					}
				}
			}
		}

		return accumulator;
	}

	public ArrayList<PVector> hough(PImage edgeImg) {

		// our accumulator (with a 1 pixel margin around)
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		int[] accumulator = houghAccumulator(edgeImg, rDim);

		int minVotes = 75;

		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		// size of the region we search for a local maximum
		int neighbourhood = 10;

		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index int the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;

				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;

					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check if we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;

						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check if we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;

							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;

							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;

					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}

		Collections.sort(bestCandidates, new HoughComparator(accumulator));

		ArrayList<PVector> lines = new ArrayList<PVector>();

		for (int i = 0; i < bestCandidates.size(); i++) {
			// first, compute back the (r, phi) polar coordinates:
			int idx = bestCandidates.get(i);
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			lines.add(new PVector(r, phi));
		}

		return lines;	

	}

	public ArrayList<PVector> getIntersection(List<PVector> lines) {

		ArrayList<PVector> list = new ArrayList<PVector>();

		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);

			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);

				float d = tabCos[(int) (line2.y/discretizationStepsPhi)]*discretizationStepsR/1.f * tabSin[(int) (line1.y/discretizationStepsPhi)] *discretizationStepsR/1.f - tabCos[(int) (line1.y/discretizationStepsPhi)]*discretizationStepsR/1.f * tabSin[(int) (line2.y/discretizationStepsPhi)] *discretizationStepsR/1.f;
				float x = (line2.x * tabSin[(int) (line1.y/discretizationStepsPhi)] *discretizationStepsR/1.f - line1.x * tabSin[(int) (line2.y/discretizationStepsPhi)] *discretizationStepsR/1.f) / d;
				float y = (-line2.x * tabCos[(int) (line1.y/discretizationStepsPhi)]*discretizationStepsR/1.f + line1.x * tabCos[(int) (line2.y/discretizationStepsPhi)]*discretizationStepsR/1.f)
						/ d;

				list.add(new PVector(x, y));
			}
		}
		
		return list;

	}

	public PVector intersection(PVector l1, PVector l2) {
		List<PVector> list = new ArrayList<PVector>();
		list.add(l1);
		list.add(l2);
		return getIntersection(list).get(0);
	}

	public PVector getRotation(List<PVector> lines, int width, int height) {
	//	System.out.println("Lines Size: " + lines.size());
		List<PVector> smallLines = lines.subList(0, Math.min(lines.size(), 6));
		QuadGraph graph = new QuadGraph();
		graph.build(smallLines, width, height);
		List<int[]> quads = graph.findCycles();
		
		ArrayList<PVector> rotations = new ArrayList<PVector>(); 

		for (int[] quad : quads) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			// (intersection() is a simplified version of the
			// intersections() method you wrote last week, that simply
			// return the coordinates of the intersection between 2 lines)
			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);
			// Filter incorrect quads
			boolean isconvex =  QuadGraph.isConvex(c12, c23, c34, c41);
			boolean validArea = QuadGraph.validArea(c12, c23, c34, c41, (width*height*2),(width*height*2)/12);
			boolean nonFlatQuad = QuadGraph.nonFlatQuad(c12, c23, c34, c41);
			if (isconvex && validArea && nonFlatQuad) {
				//Display rotation                                 x
				List<PVector> quadVecList = Arrays.asList(c12, c23, c34, c41);
				sortCorners(quadVecList); //Sort clockwise
				
				TwoDThreeD tdtd = new TwoDThreeD(width,height);
				PVector rot = tdtd.get3DRotations(quadVecList);
				rotations.add(rot);
				//System.out.println(Math.toDegrees(rot.x)+" ; "+Math.toDegrees(rot.y)+" ; "+Math.toDegrees(rot.z));

			}
		}

		PVector rot = new PVector(0,0,0);
		for(PVector v: rotations){
			rot.x += 1.0*(v.x)/rotations.size();
			rot.y += 1.0*(v.y)/rotations.size();
			rot.z += 1.0*(v.z)/rotations.size();
		}

		//System.out.println(rot.x + " --- " + rot.y + " --- " + rot.z);

		return rot;
	}

	public static List<PVector> sortCorners(List<PVector> quad){
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x+b.x)/2,(a.y+b.y)/2);
		Collections.sort(quad,new CWComparator(center));
		//TODO:
		int best = 1;
		for (int i = 0; i < quad.size(); i++) {
			if (Math.sqrt(quad.get(i).x*quad.get(i).x+quad.get(i).y*quad.get(i).y)<Math.sqrt(quad.get(best).x*quad.get(best).x+quad.get(best).y*quad.get(best).y)) best=i;
			//System.out.println("Best current :"+Math.sqrt(quad.get(i).x*quad.get(i).x+quad.get(i).y*quad.get(i).y));
		}
		//System.out.println("Best Final:"+best);
		Collections.rotate(quad, quad.size()-best);
		return quad;
	}

	class HoughComparator implements Comparator<Integer> {
		int[] accumulator;

		public HoughComparator(int[] acc) {
			accumulator = acc;
		}

		public int compare(Integer o1, Integer o2) {
			if ((accumulator[o1] > accumulator[o2])
					|| (accumulator[o1] == accumulator[o2] && o1 < o2))
				return -1;
			return 1;
		}

	}

}

class sobelRunnable implements Runnable {
	int from;
	int to;
	PImage img;
	PImage result;
	PApplet parent;

	public sobelRunnable(int from, int to, PImage img, PImage output, PApplet parent){
		this.from = from;
		this.to = Math.min(to,img.height);
		this.img = img;
		this.result = output;
		this.parent = parent;
	}


	@Override
	public void run() {

		int[][] hkernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		int[][] vkernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };

		for (int i = from; i < to; i++) {
			result.pixels[i] = parent.color(0);
		}

		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int y = from; y < to; y++) {
			for (int x = 0; x < img.width; x++) {
				int sum_h = 0;
				int sum_v = 0;
				for (int i = 0; i <= 2; i++) {
					for (int j = 0; j <= 2; j++) {
						int clampedX = 0;
						if (x + i - 1 >= 0) {
							if (x + i - 1 >= img.width)
								clampedX = img.width - 1;
							else
								clampedX = x + i - 1;
						}
						int clampedY = 0;
						if (y + j - 1 >= 0) {
							if (y + j - 1 >= img.height)
								clampedY = img.height - 1;
							else
								clampedY = y + j - 1;
						}

						sum_h += img.pixels[clampedY * img.width + clampedX]
								* vkernel[i][j];
						sum_v += img.pixels[clampedY * img.width + clampedX]
								* hkernel[i][j];
					}
				}
				float r = (float) Math.sqrt(Math.pow(sum_h, 2)
						+ Math.pow(sum_v, 2));
				if (max < r)
					max = r;
				buffer[y * img.width + x] = r;
			}
		}
		
		int upperbound = Math.min(to, img.height - 2);
		int downbound = Math.max(2, from);

		for (int y = downbound; y < upperbound; y++) {
			for (int x = 2; x < img.width - 2; x++) {
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) {
					result.pixels[y * img.width + x] = parent.color(255);
				} else {
					result.pixels[y * img.width + x] = parent.color(0);
				}
			}
		}

	}

}
