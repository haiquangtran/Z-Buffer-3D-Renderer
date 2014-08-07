package Renderer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

import javax.swing.JFileChooser;

public class Data {
	private static float imageWidth;
	private static float imageHeight;
	private static BufferedImage image;
	//For Shading etc
	private static float ambientLight = 0.5f;
	private static float intensity = 0.5f;

	public static HashSet<PolyImage> readFiles(){
		HashSet<PolyImage> polygons = new HashSet<PolyImage>();
		JFileChooser chooser = new JFileChooser();						
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			//setValues = true;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
				Vector3D light = null;
				String line = reader.readLine();
				String[] first = line.split(" ");
				float x = Float.parseFloat(first[0]);
				float y = Float.parseFloat(first[1]);
				float z = Float.parseFloat(first[2]);
				light = new Vector3D(x, y ,z);

				while((line = reader.readLine()) != null){
					String[] data = line.split(" ");
					Vector3D[] points = new Vector3D[3];
					//Polygon Points stored in Float format
					points[0] = new Vector3D(Float.parseFloat(data[0]), 
							Float.parseFloat(data[1]), Float.parseFloat(data[2]));
					points[1] = new Vector3D(Float.parseFloat(data[3]),
							Float.parseFloat(data[4]), Float.parseFloat(data[5]));
					points[2] = new Vector3D(Float.parseFloat(data[6]),
							Float.parseFloat(data[7]), Float.parseFloat(data[8]));
					//Colour
					int r = (Integer.parseInt(data[9]) < 0? 0:Integer.parseInt(data[9]));
					int g = (Integer.parseInt(data[10]) < 0? 0:Integer.parseInt(data[10]));
					int b = (Integer.parseInt(data[11]) < 0? 0:Integer.parseInt(data[11]));
					//Make new Polygon Image 
					PolyImage polyImage = new PolyImage(light, points, new Color(r,g,b));
					//Add to Set
					polygons.add(polyImage);
				}
				reader.close();
				return polygons;
			} catch(Exception e){
				e.printStackTrace();
			}
		} 
		//So on cancel button it doesn't delete the image.
		if (!GraphicalUserInterface.getPolygons().isEmpty()){
			return GraphicalUserInterface.getPolygons();
		}
		return null;
	}

	public static void zBuffer(){
		//Finds the width and height of the image
		imageWidth = GraphicalUserInterface.imageWidth; 
		imageHeight =  GraphicalUserInterface.imageHeight;

		//Initialise
		Color[][] zBufferC = new Color[(int) imageWidth][(int) imageHeight];	 //of colour
		float[][] zBufferD = new float[(int) imageWidth][(int) imageHeight]; 	 //of infinity

		//Initialise the arrays
		for (int i =0; i < zBufferC.length; i++){
			for (int j =0; j < zBufferC[i].length; j++){
				zBufferD[i][j] = Float.POSITIVE_INFINITY;
				zBufferC[i][j] = Color.gray;		
			}
		}
		//for each polygon
		for (PolyImage polys: GraphicalUserInterface.getPolygons()){
			EdgeList[] edgeList = computeEdgeList(polys);		//compute edgeLists
			if (!polys.isHidden()){								//Remove hidden polygons
				for (int y = 0; y < edgeList.length-1; y++){
					int x = Math.round(edgeList[y].getLeftX());
					float z = edgeList[y].getLeftZ();
					//interpolating z and shaind as you go
					float mz = (edgeList[y].getRightZ() - edgeList[y].getLeftZ())
							/(edgeList[y].getRightX() - edgeList[y].getLeftX());
					//Insert shading into z buffer
					while (x <= Math.round(edgeList[y].getRightX())){
						//check if x is in bounds
						if (x > 0 && y > 0 && z < zBufferD[x][y]){
							zBufferD[x][y] = z;
							zBufferC[x][y] = polys.shading(ambientLight, intensity);	//Shading
						}
						x++;
						z += mz;
					}
				}
			}
		}
		//copy ZBufferC to image
		image = convertBitmapToImage(zBufferC);
	}

	/** Converts a 2D array of Colors to a BufferedImage.
    	Assumes that bitmap is indexed by column then row and has
		imageHeight rows and imageWidth columns.
		Note that image.setRGB requires x (col) and y (row) are given in that order.
	 */
	private static BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage((int)imageWidth, (int)imageHeight, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < imageWidth-1; x++) {
			for (int y = 0; y < imageHeight-1; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static EdgeList[] computeEdgeList(PolyImage poly){
		//Actions
		EdgeList[] edgeList = new EdgeList[GraphicalUserInterface.imageHeight];

		//initialize edgeList 
		for (int i = 0; i < edgeList.length; i++){
			edgeList[i] = new EdgeList();
			edgeList[i].setLeft(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);	//z = infinity
			edgeList[i].setRight(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		}

		Vector3D vertexA;
		Vector3D vertexB;
		//for each edge in polygon.. do
		for (int i = 0; i < poly.getPoints().length; i++){		//go through all edges
			Vector3D[] vertices = poly.getEdges(i);
			if (vertices[0].y < vertices[1].y){
				vertexA = vertices[0];		//smallest y value
				vertexB = vertices[1];		//other vertex
			} else {
				vertexA = vertices[1];		
				vertexB = vertices[0];
			}
			float mx = (vertexB.x - vertexA.x)/(vertexB.y - vertexA.y);	//run/rise? the gradient
			float mz = (vertexB.z - vertexA.z)/(vertexB.y - vertexA.y);	
			float x = vertexA.x;			//and any other values to interpolate	
			float z = vertexA.z;		
			int index = Math.abs(Math.round(vertexA.y));
			int maxIndex = Math.round(vertexB.y);
			while (index < maxIndex){
				//put x and z into edgelist[i] as left or right, depending on x
				if (x < edgeList[index].getLeftX()){
					edgeList[index].setLeft(x, z);
				} 
				if ( x > edgeList[index].getRightX()){
					edgeList[index].setRight(x, z);
				}
				index++;
				x += mx;
				z += mz;}
		}
		return edgeList;
	}

	public static BufferedImage getImage(){
		zBuffer();
		return image;
	}

	public static void setAmbientLight(float amount){
		ambientLight = amount;
	}

	public static void setIntensity(float amount){
		intensity = amount;
	}
}
