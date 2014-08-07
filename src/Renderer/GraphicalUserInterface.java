package Renderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GraphicalUserInterface extends JFrame{
	public static final int imageWidth = 810;		
	public static final int imageHeight = 820;
	private JFrame frame;
	private JComponent drawing;
	private JLabel sliderOutput;
	private JLabel sliderOutput2;
	//Used for Rotation
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private Data data = new Data();
	//Polygons
	private static HashSet<PolyImage> polygons = new HashSet<PolyImage>();
	//Bounding box of Polygon
	private static Rectangle boundingBox;
	//Used for scaling
	private static float width;
	private static float height;

	//mouseclick - so doesn't rotate
	private boolean mouseDragged = false;

	public GraphicalUserInterface() {
		setUpInterface();
	}

	private void setUpInterface() {
		//WINDOW
		frame = new JFrame("3D Renderer");
		frame.setSize(imageWidth, imageHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//PANEL
		JPanel panel = new JPanel();
		JPanel bot = new JPanel();
		JPanel sliderPanel = new JPanel();
		frame.add(panel, BorderLayout.NORTH);
		frame.add(bot, BorderLayout.SOUTH);
		sliderPanel.setPreferredSize(new Dimension(200,100));

		//COMPONENTS
		JButton loadButton = new JButton("Load");
		JButton saveButton = new JButton("Save");
		JButton clearButton = new JButton("Clear");
		JButton quitButton = new JButton("Quit");
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0,100,50);
		JSlider intensity = new JSlider(JSlider.HORIZONTAL, 0,100,50);
		sliderOutput = new JLabel();
		sliderOutput2 = new JLabel(); 

		final JLabel sliderLabel = new JLabel("Ambient Level :");
		final JLabel sliderLabel2 = new JLabel("Intensity Level :");

		//DRAWING CANVAS
		drawing = new JComponent(){
			protected void paintComponent(Graphics g){
				if (polygons != null){			
					//Rotation
					for (PolyImage images: polygons){
						if (mouseDragged){
							images.rotateX((float)((endY - startY) * 0.01));	//Need to rotate y then x 
							images.rotateY((float)((startX - endX) * 0.01));
						}
					}
					//Translation
					GraphicalUserInterface.setBoundingBox();		//Rotation changes the bounds so need to recall
					for (PolyImage images: polygons){
						images.translate(-boundingBox.x + imageWidth/2 - boundingBox.width/2,
								-boundingBox.y + drawing.getHeight()/2 - boundingBox.height/2, 1.0f);
					}
					GraphicalUserInterface.setBoundingBox();
					//Scaling
					scale();
					setBoundingBox();

					//draw Image
					g.drawImage(Data.getImage(), 0, 0,null);
					mouseDragged = false;
				}
			}
		};

		drawing.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				endX = e.getX();
				endY = e.getY();
				mouseDragged = false;
			}
		});
		//MOUSE LISTENER
		drawing.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent arg0) {}
			@Override
			public void mouseDragged(MouseEvent e) {
				//Previous pressed Values
				startX = endX;
				startY = endY;
				//Current Dragged Values
				endX = e.getX();
				endY = e.getY();
				mouseDragged = true;
				drawing.repaint();
			}
		});

		//ADDING OF COMPONENTS
		panel.add(loadButton);
		panel.add(saveButton);
		panel.add(clearButton);
		sliderPanel.add(sliderLabel);
		sliderPanel.add(sliderOutput);
		sliderPanel.add(slider);
		sliderPanel.add(sliderLabel2);
		sliderPanel.add(sliderOutput2);
		sliderPanel.add(intensity);
		bot.add(sliderPanel);
		panel.add(quitButton);
		frame.add(drawing, BorderLayout.CENTER);

		//ACTION LISTENERS
		loadButton.addActionListener(new ActionListener() {			//LOAD
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadPolygons();
				GraphicalUserInterface.setBoundingBox();
				drawing.repaint();
			}});
		saveButton.addActionListener(new ActionListener() {			//SAVE
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		clearButton.addActionListener(new ActionListener() {		//CLEAR
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		quitButton.addActionListener(new ActionListener() {			//QUIT
			public void actionPerformed(ActionEvent e){ System.exit(0);}});

		slider.addChangeListener(new ChangeListener() {				//SLIDER
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					int adjust = (int)source.getValue();
					Data.setAmbientLight((float)((float)adjust/100));
					sliderOutput.setText(((float)((float)adjust) + ""));
					drawing.repaint();
				}
			}
		});
		intensity.addChangeListener(new ChangeListener() {			//INTENSITY
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()) {
					int adjust = (int)source.getValue();
					Data.setIntensity((float)((float)adjust/100));
					sliderOutput2.setText(((float)((float)adjust) + ""));
					drawing.repaint();
				}
			}
		});
		frame.setVisible(true);
	}

	public static void setBoundingBox(){
		if (polygons == null){ return; }
		float minX = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;

			for (PolyImage polys: polygons){
				for (Vector3D points: polys.getPoints()){
					minX = Math.min(minX, points.x);
					maxX = Math.max(maxX, points.x);
					minY = Math.min(minY, points.y);
					maxY = Math.max(maxY, points.y);
				}
			}
		GraphicalUserInterface.width = maxX - minX;
		GraphicalUserInterface.height = maxY - minY;
		boundingBox = new Rectangle((int)minX, (int)minY, (int)(maxX-minX), (int)(maxY-minY));
	}

	public void scale(){
		if (boundingBox.width > drawing.getWidth() || boundingBox.height > drawing.getHeight()){	//out of bounds
			float scaleFactor;
			if (boundingBox.width > boundingBox.height){
				scaleFactor = drawing.getWidth()/width - 0.1f;
			} else {
				scaleFactor = drawing.getHeight()/height - 0.1f;
			}

			for (PolyImage poly: polygons){
				poly.scale(scaleFactor, scaleFactor, scaleFactor);
				//poly.scale(0.1f, 0.1f, 0.1f);
			}
		} 
	}

	public static HashSet<PolyImage> getPolygons(){
		return polygons;
	}

	public void loadPolygons(){
		try {
		polygons = Data.readFiles();
		//Translation
		GraphicalUserInterface.setBoundingBox();		//Rotation changes the bounds so need to recall
		for (PolyImage images: polygons){
			images.translate(-boundingBox.x + imageWidth/2 - boundingBox.width/2,
					-boundingBox.y + drawing.getHeight()/2 - boundingBox.height/2, 1.0f);
		}
		GraphicalUserInterface.setBoundingBox();
		//Scaling
		scale();
		setBoundingBox();
		} catch (NullPointerException e){}
	}
}
