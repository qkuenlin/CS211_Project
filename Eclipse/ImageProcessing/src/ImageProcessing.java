import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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

@SuppressWarnings("serial")
public class ImageProcessing extends PApplet {
	PImage img1;
	PImage img2;
	PImage img3;
	PImage img4;
	PImage todisplay;
	int threshold1;
	int threshold2;
	int type;
	int imgNum;
	HScrollBar thresholdBar1;
	HScrollBar thresholdBar2;
	
	static float[] tabSin;
    static float[] tabCos;
	static float discretizationStepsPhi = 0.06f;
	static float discretizationStepsR = 2.5f;
	// dimensions of the accumulator
	int phiDim = (int) (Math.PI / discretizationStepsPhi);

	Capture cam;

	public void setup() {
		type = 0;
		imgNum = 1;
		size(800, 600);
		frameRate(30);
		thresholdBar1 = new HScrollBar(this, 0, 550, 800, 20);
		thresholdBar2 = new HScrollBar(this, 0, 580, 800, 20);
		img1 = loadImage("../assets/lego_board/board1.jpg");
		img2 = loadImage("../assets/lego_board/board2.jpg");
		img3 = loadImage("../assets/lego_board/board3.jpg");
		img4 = loadImage("../assets/lego_board/board4.jpg");

		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
			cam = new Capture(this, cameras[0]);
			cam.start();
		}
		
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

		threshold1 = (int) (thresholdBar1.getPos() * 255);
		threshold2 = (int) (thresholdBar2.getPos() * 255);

		int display = 0;

		if (cam.available() == true) {
			cam.read();
		}

		PImage img = createImage(img1.width, img2.height, RGB);

		switch (imgNum) {
		case 1:
			img = img1;
			break;
		case 2:
			img = img2;
			break;
		case 3:
			img = img3;
			break;
		case 4:
			img = img4;
			break;
		case 5:
			img = cam.get();
			break;
		}

		switch (type) {
		case 0:
			todisplay = img;
			display = 0;
			break;
		case 1:
			todisplay = truncateIntensity(img, threshold2);
			display = 1;
			break;
		case 2:
			todisplay = binaryIntensity(img, threshold2);
			display = 1;
			break;
		case 3:
			todisplay = hueFilter(img, threshold1, threshold2);
			display = 2;
			break;
		case 4:
			todisplay = saturationFilter(img, threshold1, threshold2);
			display = 2;
			break;
		case 5:
			todisplay = convolute(img);
			display = 0;
			break;
		case 6:
			todisplay = gaussianBlur(img);
			display = 0;
			break;
		case 7:
			todisplay = sobel(img);
			display = 0;
			break;
		case 8:
			todisplay = edgeDetection(img);
			display = 0;
			break;
		case 9:
			todisplay = img;
			display = 0;
			break;
		case 10:
			todisplay = improvedEdgeDetection(img);
			display = 0;
			break;
		}

		image(todisplay, 0, 0);

		if (type == 9) {
			getIntersection(hough(edgeDetection(img)));
		}
		if (type == 10) {
			StopWatch sw = new StopWatch();
			sw.start();
			getQuads(hough(improvedEdgeDetection(img)), img.width, img.height);
			sw.stop();
			System.out.println("time elapsed: " + sw.getElapsedTime() + " --- fps: " + (1000/sw.getElapsedTime()));
		}

		if (display == 2) {
			thresholdBar1.display();
			thresholdBar1.update();
		}
		if (display != 0) {
			thresholdBar2.display();
			thresholdBar2.update();
		}
	}

	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == UP) {
				imgNum = min(5, imgNum + 1);
			}
			if (keyCode == DOWN) {
				imgNum = max(1, imgNum - 1);

			}
		}
		if (key == '0') {
			type = 0;
		}
		if (key == '1') {
			type = 1;
		}
		if (key == '2') {
			type = 2;
		}
		if (key == '3') {
			type = 3;
		}
		if (key == '4') {
			type = 4;
		}
		if (key == '5') {
			type = 5;
		}
		if (key == '6') {
			type = 6;
		}
		if (key == '7') {
			type = 7;
		}
		if (key == '8') {
			type = 8;
		}
		if (key == '9') {
			type = 9;
		}
		if (key == 'A') {
			type = 10;
		}
	}

	public PImage edgeDetection(PImage img) {

		PImage result = createImage(width, height, RGB);

		result = sobel(hueFilter(
				saturationFilter(truncateIntensity(img, 191), 255, 61), 140, 96));

		return result;
	}

	public PImage improvedEdgeDetection(PImage img) {

		PImage result = createImage(width, height, RGB);

		result = sobel(binaryIntensity(
				gaussianBlur(hueFilter(saturationFilter(img, 255, 61), 140, 96)),
				230));

		return result;
	}

	public PImage truncateIntensity(PImage img, int threshold) {
		PImage result = createImage(width, height, RGB);
		for (int i = 0; i < img.width; i++) {
			for (int j = 0; j < img.height; j++) {
				int c = img.get(i, j);
				if (brightness(c) > threshold)
					c = threshold;
				result.set(i, j, color(c));
			}

		}
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

	public PImage convolute(PImage img) {
		float[][] kernel = { { 0, 1, 0 }, { 1, 0, 1 }, { 0, 1, 0 } };

		float weight = 1.f;

		PImage result = createImage(img.width, img.height, RGB);

		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
				int sum = 0;
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

						sum += img.pixels[clampedY * img.width + clampedX]
								* kernel[i][j] / weight;

					}
				}
				result.pixels[y * img.width + x] = sum;
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

		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie. white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
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
		int minVotes = 150;

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
				ellipse(x, y, 10, 10);

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
			boolean validArea = QuadGraph.validArea(c12, c23, c34, c41, width*height*10, (float)((width*height)/64.0));
			boolean nonFlatQuad = QuadGraph.nonFlatQuad(c12, c23, c34, c41);
			System.out.println("width :"+width);
			System.out.println("height :"+height);
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