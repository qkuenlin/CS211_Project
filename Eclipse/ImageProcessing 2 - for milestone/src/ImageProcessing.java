import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.*;


@SuppressWarnings("serial")
public class ImageProcessing extends PApplet {
	PImage img1;
	PImage img2;
	PImage img3;
	PImage img4;
	PImage img;
	
	double imgWidth;
	double imgHeight;
	
	static float[] tabSin;
    static float[] tabCos;
	static float discretizationStepsPhi = 0.03f;
	static float discretizationStepsR = 1.25f;
	// dimensions of the accumulator
	int phiDim = (int) (Math.PI / discretizationStepsPhi);

	public void setup() {
		size(1200, 300);
		frameRate(30);
		img1 = loadImage("../assets/lego_board/board1.jpg");
		img2 = loadImage("../assets/lego_board/board2.jpg");
		img3 = loadImage("../assets/lego_board/board3.jpg");
		img4 = loadImage("../assets/lego_board/board4.jpg");		
		
		img = img1; //Change to img1, img2, img3 or img4 to have the others lego board images;
		
		// pre-compute the sin and cos values
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

	public void draw() {
		background(color(0, 0, 0));	
		
		imgWidth = width/3;
		imgHeight = (imgWidth / img.width) * img.height;
		
		PImage toDisplay = createImage(img1.width, img1.height, RGB);
		
		
		toDisplay.copy(img, 0, 0, img.width, img.height, 0, 0, toDisplay.width, toDisplay.height);
		toDisplay.resize((int) imgWidth, (int)imgHeight);

		// sobel = createImage(img1.width, img1.height, RGB);		
		PImage sobel = improvedEdgeDetection(toDisplay);
		
		PImage hough = showHough(sobel);		
		
		//toDisplay.resize((int) imgWidth, (int) imgHeight);
		hough.resize((int) imgWidth, (int) imgHeight);
		//sobel.resize((int) imgWidth, (int) imgHeight);
		
		image(toDisplay, 0, 0);
		getIntersection(hough(sobel));
		
		image(hough, toDisplay.width, 0);
		image(sobel, (hough.width + toDisplay.width), 0);
		
	}

	public PImage improvedEdgeDetection(PImage img) {

		PImage result = createImage(width, height, RGB);

		result = sobel(binaryIntensity(
				gaussianBlur(hueFilter(saturationFilter(img, 255, 61), 140, 96)),
				230));

		return result;
	}

	public PImage binaryIntensity(PImage img, int threshold) {
		PImage result = createImage(width, height, RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (brightness(c) > threshold)
					c = 255;
				else
					c = 0;
				result.set(i, j, color(c));
			}

		}
		return result;
	}

	public PImage hueFilter(PImage img, int threshold1, int threshold2) {
		PImage result = createImage(width, height, RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (hue(c) > threshold1 || hue(c) < threshold2)
					c = 0;
				else
					c = 255;
				result.set(i, j, color(c));
			}

		}
		return result;
	}

	public PImage saturationFilter(PImage img, int threshold1, int threshold2) {
		PImage result = createImage(width, height, RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (saturation(c) > threshold1 || saturation(c) < threshold2)
					c = 0;
				result.set(i, j, color(c));
			}

		}
		return result;
	}

	public PImage gaussianBlur(PImage img) {
		float[][] kernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

		int weight = 99;

		PImage result = createImage(img.width, img.height, RGB);

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

						red += red(img.pixels[clampedY * img.width + clampedX])
								* kernel[i][j] / weight;
						green += green(img.pixels[clampedY * img.width
						                          + clampedX])
						                          * kernel[i][j] / weight;
						blue += blue(img.pixels[clampedY * img.width + clampedX])
								* kernel[i][j] / weight;

					}
				}
				result.pixels[y * img.width + x] = color(red, green, blue);
			}

		}

		return result;

	}

	public PImage sobel(PImage img) {

		int[][] hkernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		int[][] vkernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };

		PImage result = createImage(img.width, img.height, RGB);

		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}

		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
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

		for (int y = 2; y < img.height - 2; y++) {
			for (int x = 2; x < img.width - 2; x++) {
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) {
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}

		return result;
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
			displayLine(r, phi, edgeImg);
		}

		return lines;

	}
	
	public int[] houghAccumulator(PImage img, int rDim){

		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie. white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < img.height; y++) {
			for (int x = 0; x < img.width; x++) {
				// Are we on an edge?
				if (brightness(img.pixels[y * img.width + x]) != 0) {
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
	
	public PImage showHough(PImage img){
		
		int rDim = (int) (((img.width + img.height) * 2 + 1) / discretizationStepsR);
		int[] accumulator = houghAccumulator(img, rDim);
		
		PImage retImg = createImage(rDim+2,  phiDim+2,  ALPHA);
		
		for(int i = 0; i < retImg.width*retImg.height; i++){
			retImg.pixels[i] = color(min(255, 5*accumulator[i]));
		}	
		
		return retImg;
		
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

				fill(255, 128, 0);
				ellipse(x, y, 5, 5);

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

	void getQuads(List<PVector> lines, int width, int height) {
		QuadGraph graph = new QuadGraph();
		graph.build(lines, width, height);
		List<int[]> quads = graph.findCycles();

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
			boolean isconvex = QuadGraph.isConvex(c12, c23, c34, c41);
			boolean validArea = QuadGraph.validArea(c12, c23, c34, c41, (float) (imgWidth*imgHeight*2), (float)((imgWidth*imgWidth)/5.0));
			boolean nonFlatQuad = QuadGraph.nonFlatQuad(c12, c23, c34, c41);
			if (isconvex && validArea && nonFlatQuad) {
				// Choose a random, semi-transparent colour
				Random random = new Random();
				fill(color(min(255, random.nextInt(300)),
						min(255, random.nextInt(300)),
						min(255, random.nextInt(300)), 50));
				//fill(random.nextInt(300));

				quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);
			}
		}
	}

	public void displayLine(float r, float phi, PImage edgeImg) {
		// cartesian equation of a line: y= ax+b;
		// in polar: y= (-cos(phi)/sin(phi))x + r/sin(phi))
		// => y=0: x= r/cos(phi)
		// => x=0: y= r/sin(pi)

		// compute the intersection of this line with the 4 borders of the image
		
		int AccPhi = (int) (phi / discretizationStepsPhi);
		double sinPhi = (tabSin[AccPhi]*discretizationStepsR/1.f);
		double cosPhi = (tabCos[AccPhi]*discretizationStepsR/1.f);
		int x0 = 0;
		int y0 = (int) (r / sinPhi);
		int x1 = (int) (r / cosPhi);
		int y1 = 0;
		int x2 = edgeImg.width;
		int y2 = (int) (-cosPhi / sinPhi * x2 + r / sinPhi);
		int y3 = edgeImg.width;
		int x3 = (int) (-(y3 - r / sinPhi) * (sinPhi / cosPhi));

		// finally plot the line
		stroke(204, 102, 0);
		if (y0 > 0) {
			if (x1 > 0)
				line(x0, y0, x1, y1);
			else if (y2 > 0)
				line(x0, y0, x2, y2);
			else
				line(x0, y0, x3, y3);
		} else {
			if (x1 > 0) {
				if (y2 > 0)
					line(x1, y1, x2, y2);
				else
					line(x1, y1, x3, y3);
			} else
				line(x2, y2, x3, y3);
		}
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