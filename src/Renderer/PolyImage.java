package Renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

public class PolyImage {
	private Vector3D light;
	private Vector3D[] points;
	private Color reflectivity;
	private Vector3D normal;
	//Remove hidden polygons
	private boolean hidden = false;

	//You will need to compute a unit normal of each polygon in order to render it (but do this after rotating, translating, and scaling it).

	private Transform transform;

	public PolyImage(Vector3D lightSource, Vector3D[] points, Color reflectivity){
		this.light = lightSource;
		this.points = points;
		this.reflectivity = reflectivity;
		computeNormal();
	}

	public Vector3D getLightSource() {
		return light;
	}

	public Vector3D[] getPoints(){
		return points;
	}

	public Vector3D[] getEdges(int index){
		if (index == 0){
			return new Vector3D[]{points[0], points[1]};
		} else if (index == 1){
			return new Vector3D[]{points[1], points[2]};
		} else if (index == 2){
			return new Vector3D[]{points[0], points[2]};
		}
		return null;
	}

	public Color getReflectivity() {
		return reflectivity;
	}

	public void rotateX(float radians){
		transform = Transform.newXRotation(radians);
		for (int i = 0; i < points.length; i++){
			points[i] = transform.multiply(points[i]);
		}
		light = transform.multiply(light);
		computeNormal();
	}

	public void rotateY(float radians){
		transform = Transform.newYRotation(radians);
		for (int i = 0; i < points.length; i++){
			points[i] = transform.multiply(points[i]);
		}
		light = transform.multiply(light);
		computeNormal();
	}

	public void computeNormal(){
		//Convert to unit vectors and use cross product to produce the normal vector
		normal = (points[1].minus(points[0])).unitVector().crossProduct((points[2].minus(points[1])).unitVector()); //Anti-clockwise
		if (normal.z > 0){	//if z is positive then vector is facing away from viewer.
			hidden = true;
		} else {
			hidden = false;
		}
	}

	public Vector3D getNormalVector(){
		return normal;
	}

	public boolean isHidden(){
		return hidden;
	}

	public void rotateZ(float radians){
		transform = Transform.newZRotation(radians);
		for (int i = 0; i < points.length; i++){
			points[i] = transform.multiply(points[i]);
		}
		light = transform.multiply(light);
		computeNormal();
	}

	public Color shading(float ambientLevel, float intensity){
		float cost = ((normal.unitVector()).dotProduct(light.unitVector()));
		int r = (int) ((ambientLevel + intensity * cost) * reflectivity.getRed());
		int g = (int) ((ambientLevel + intensity * cost) * reflectivity.getGreen());
		int b = (int) ((ambientLevel + intensity * cost) * reflectivity.getBlue());
		if (r < 0){
			r = 0;
		} else if (r > 255){
			r = 255;
		}
		if (g < 0){
			g = 0;
		} else if (g > 255){
			g = 255;
		}
		if (b < 0){
			b = 0;
		} else if (b > 255){
			b = 255;
		}
		Color shading = new Color(r,g,b);
		return shading;
	}

	public void scale(float sx, float sy, float sz){
		transform = Transform.newScale(sx, sy, sz);
		for (int i = 0; i < points.length; i++){
			points[i] = transform.multiply(points[i]);
		}
		computeNormal();
	}


	public void translate(float tx, float ty, float tz){
		//GraphicalUserInterface.setBoundingBox();	//because rotation changes all the values so need to call this again
		transform = Transform.newTranslation(tx, ty, tz);
		for (int i = 0; i < points.length; i++){
			points[i] = transform.multiply(points[i]);
		}
		//GraphicalUserInterface.setBoundingBox();
		computeNormal();
	}
}
