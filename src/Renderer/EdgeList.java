package Renderer;

public class EdgeList {
	float[] edgelist;
	
	public EdgeList(){
		edgelist = new float[4];
	}
	
	public void setLeft(float x, float z){
		edgelist[0] = x;
		edgelist[1] = z;
	}
	
	public void setRight(float x, float z){
		edgelist[2] = x;
		edgelist[3] = z;
	}
	
	public float getLeftX(){
		return edgelist[0];
	}
	
	public float getRightX(){
		return edgelist[2];
	}

	public float getLeftZ(){
		return edgelist[1];
	}
	
	public float getRightZ(){
		return edgelist[3];
	}
}
