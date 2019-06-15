/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geo;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author milas6582
 */
public class FXMLDocumentController implements Initializable {
    private ArrayList<Point> points = new ArrayList<>(); //all points
    private ArrayList<Segment> segments = new ArrayList<>(); //all segments/lines
    private ArrayList<Circles> circles = new ArrayList<>(); //all circles
    private ArrayList<geoLabel> labels = new ArrayList<>(); //all labels
    private ArrayList<Point> selectedPoints = new ArrayList<>(); //points that are selected when calling a function
    private ArrayList<Segment> selectedLines = new ArrayList<>(); //line that are selected when calling a function 
    
    @FXML
    private Label infoLbl;
    
    @FXML
    private Button addPointBtn, moveBtn, addSegmentBtn, addCircleBtn, addLineBtn, distanceBtn, angleBtn,
        addMidpointBtn, addBisectorBtn, addParallelBtn, addPerpendicularBtn, deleteBtn, colorBtn;
    
    private final FileChooser fileChooser = new FileChooser();
    private File file = null;
    
    @FXML
    private ColorPicker colorPicker;
    private java.awt.Color color = new java.awt.Color(0, 0, 0, 255);
    
    private ArrayList<Button> buttons;
    
    @FXML private AnchorPane root, canvas;
    
    Vector mousePos = new Vector(0, 0);
    Vector v = new Vector(0, 0); //vector representing the origen
    
    public ArrayList<Double> zoom=new ArrayList<>(); //total factor that the screen was zoomed
    
    /**
    * Called when the addPoint button is pressed. Makes next click create a point.
    */
    @FXML
    private void handleAddPoint(ActionEvent event) {
        if (buildHandleBtnStart(addPointBtn, "Click a location to add a point.")) {
            canvas.setOnMouseClicked(addPoint);
        }
    }
    
    /**
    * Called when the addSegment button is pressed. Makes next clicks create a segment.
    */
    @FXML
    private void handleAddSegment() {
        if (buildHandleBtnStart(addSegmentBtn, "Click two points or locations to create a line segment between them.")) {
            canvas.setOnMouseClicked(addPointForSegment);
        }
    }
    
    /**
    * Called when the addLine button is pressed. Makes next clicks create a line.
    */
    @FXML
    private void handleAddLine() {
        if (buildHandleBtnStart(addLineBtn, "Click two points or locations to create a line between them.")) {
            canvas.setOnMouseClicked(addPointForLine);
        }
    }
    
    /**
    * Called when the addCirclebutton is pressed. Makes next clicks create a circle.
    */
    @FXML
    private void handleAddCircle() {
        if (buildHandleBtnStart(addCircleBtn, "Select an initial point as the center and a second point to be on the circle.")) {
            canvas.setOnMouseClicked(addPointForCircle);
        }
    }
    
    /**
    * Called when the addMidpoint button is pressed. Makes next clicks create a midpoint.
    */
    @FXML
    private void handleAddMidpoint() {
        if (buildHandleBtnStart(addMidpointBtn, "Select two points to create the midpoint between them.")) {
            canvas.setOnMouseClicked(addPointForMidpoint);
        }
    }
    
    /**
    * Called when the addBisector button is pressed. Makes next clicks create an angle bisector.
    */
    @FXML
    private void handleAddBisector() {
        if (buildHandleBtnStart(addBisectorBtn, "Select three points to create the angle bisector between them.")) {
            canvas.setOnMouseClicked(addPointForBisector);
        }
    }
    
    /**
    * Called when the addParallel button is pressed. Makes next clicks create a parallel line.
    */
    @FXML
    private void handleAddParallel() {
        if (buildHandleBtnStart(addParallelBtn, "Select a point and a line to create a line that is parallel to the chosen line and goes through the chosen point.")) {
            canvas.setOnMouseClicked(addPointForParallel);
        }
    }
    
    /**
    * Called when the addPerpendicular button is pressed. Makes next clicks create a perpendicular line.
    */
    @FXML
    private void handleAddPerpendicular() {
        if (buildHandleBtnStart(addPerpendicularBtn, "Select a point and a line to create a line that is perpendicular to the chosen line and goes through the chosen point.")) {
            canvas.setOnMouseClicked(addPointForPerpendicular);
        }   
    }
    
    /**
    * Called when the Move button is pressed. Allows the screen to be dragged.
    */
    @FXML
    private void handleMove() {
        if (buildHandleBtnStart(moveBtn, "Drag the screen around with your mouse.")) {
            canvas.setOnMousePressed(onScreenPressed);
            canvas.setOnMouseDragged(onScreenDragged);
        } else {
            canvas.setOnMousePressed(onPressed);
            canvas.setOnMouseDragged(onDragged);
        }
    }
    
    /**
    * Called when the Delete button is pressed. Deletes the clicked on object.
    */
    @FXML
    private void handleDelete() {
        if (buildHandleBtnStart(deleteBtn, "Select a point, segment, line, or circle to delete it.")) {
            canvas.setOnMouseClicked(findDelete);
        }
    }
    
    /**
    * Called when the changeColor button is pressed. Changes the color of the clicked on object.
    */
    @FXML
    private void handleChangeColor() {
        if (buildHandleBtnStart(colorBtn, "Select a point, segment, line, or circle to color it the below color.")) {
            canvas.setOnMouseClicked(changeColor);
        }
    }
    
    /**
    * Changes the used color when the color chooser changes values.
    */
    @FXML
    private void handlePickColor() {
        color = fxToAwt(colorPicker.getValue());
    }
    
    /**
    * Called when the Distance button is pressed. Creates a label saying the distance between two points.
    */
    @FXML
    private void handleFindDistance() {
        if (buildHandleBtnStart(distanceBtn, "Select two points to find the distance between them.")) {
            canvas.setOnMouseClicked(findDistance);
        }
    }
    
    /**
    * Called when the Angle button is pressed. Creates a label saying the angle between three points.
    */
    @FXML
    private void handleFindAngle() {
        if (buildHandleBtnStart(angleBtn, "Select three points to find the angle between.")) {
            canvas.setOnMouseClicked(findAngle);
        }
    }
    
    /**
    * Called when any button is pressed. Disables or enables points from being dragged, empties selected points/lines.
    * Returns true if the button was pressed for the first time.
    */
    private boolean buildHandleBtnStart(Button button, String txt) {
        if (addPointBtn.isDisabled()||moveBtn.isDisabled()) {
            canvas.setOnMouseClicked(null);
            infoLbl.setText("");
            enableButtons();
            for (Point point: points) {
                point.setState("drag");
                point.getPoint().setOnMouseClicked(null);
            } for (Segment seg: segments) {
                seg.setState("unselected");
                seg.getLine().setOnMouseClicked(null);
            }
        } else {
            infoLbl.setText(txt); 
            disableButtonsExcept(button);
            selectedPoints = new ArrayList<>();
            selectedLines = new ArrayList<>();
            for (Point point: points) {
                point.setState("disabled");
            }
            for (Segment seg: segments) {
                seg.setState("unselected");
            } 
            return true;
        }
        return false;
    }
    
    /**
    * Disables all points except the given parameter.
    */
    private void disableButtonsExcept(Button goodButton) {
        for (Button button: buttons) {
            if (!button.equals(goodButton)) {
                button.setDisable(true);
            }
        }
    }
    
    /**
    * Enables all buttons to be pressed.
    */
    private void enableButtons() {
        for (Button button: buttons) {
            button.setDisable(false);
        }
    }
    
    /**
    * Adds a point when the screen is clicked.
    */
    EventHandler<MouseEvent> addPoint = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            addPointMain(new Point(t.getX(), t.getY()));
        }
    };
    
    /**
    * Returns the point that the mouse is hovering over.
    * If the mouse isn't over a point, it will create a new one an return that.
    */
    public Point getPointOver(MouseEvent t) {
        if (!overPoint()) {
            Point point = new Point(t.getX(), t.getY());
            addPointMain(point);
            return point;
        }else {
            for (Point point: points) {
                if (point.isOver()) {
                    return point;
                }
            }
        }
        return null;
    }
    
    /**
    * Returns the point or segment that the mouse is hovering over.
    * The first element in the returned arrayList is 0 if a point is returned, 1 if a segment is returned.
    * The second is the index in points/segments that represents the desired point/segment.
    */
    public int[] getPointOrSegmentOver(MouseEvent t) {
        int[] toReturn = new int[2];
        toReturn[0]=0;
        if (!overPoint()&&!overSeg()&&selectedPoints.size()==0) {
            Point point = new Point(t.getX(), t.getY());
            addPointMain(point);
            toReturn[1]=points.size()-1;
            return toReturn;
        } else if (overPoint()&&selectedPoints.size()<=1) {
            for (int i =0; i<points.size(); i++) {
                if (points.get(i).isOver()) {
                    toReturn[1]=i;
                    return toReturn;
                }
            }
        } else if (!overPoint()&&overSeg()&&selectedLines.size()<=1) {
            for (int i =0; i<segments.size(); i++) {
                if (segments.get(i).isOver()) {
                    toReturn[0] = 1;
                    toReturn[1]=i;
                    return toReturn;
                }
            }
        }
        return null;
    }
    
    /**
    * Selects a point and calls the main function for adding segments.
    */
    EventHandler<MouseEvent> addPointForSegment = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                segmentSelectorMain(point);
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for adding lines.
    */
    EventHandler<MouseEvent> addPointForLine = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                lineSelectorMain(point);
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for adding circles.
    */
    EventHandler<MouseEvent> addPointForCircle = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                circleSelectorMain(point);
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for adding midpoints.
    */
    EventHandler<MouseEvent> addPointForMidpoint = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                midpointSelectorMain(point);
            }
        }
    };    
    
    /**
    * Selects a point and calls the main function for adding angle bisectors.
    */
    EventHandler<MouseEvent> addPointForBisector = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                bisectorSelectorMain(point);
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for adding parallel lines.
    */
    EventHandler<MouseEvent> addPointForParallel =  
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            int[] info = getPointOrSegmentOver(t);
            if (info != null) {
                if (info[0]==0) {//selects a point
                    parallelSelectorPointMain(points.get(info[1]));
                } else {//selects a segment
                    parallelSelectorLineMain(segments.get(info[1]));
                }
            }
        }
    }; 
    
    /**
    * Selects a point and calls the main function for adding perpendicular lines.
    */
    EventHandler<MouseEvent> addPointForPerpendicular = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            int[] info = getPointOrSegmentOver(t);
            if (info != null) {
                if (info[0]==0) {//selects a point
                    perpendicularSelectorPointMain(points.get(info[1]));
                } else {//selects a segment
                    perpendicularSelectorLineMain(segments.get(info[1]));
                }
            }
        }
    };
    
    /**
    * Finds the object the mouse is hovering over and deletes it
    */
    EventHandler<MouseEvent> findDelete = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            if (overPoint()) {//deletes a point
                for (int i =0; i<points.size(); i++) {
                    if (points.get(i).isOver()) {
                        deletePoint(points.get(i));
                        break;
                    }
                }
            } else if (overSeg()) {//deletes a segment
                for (int i =0; i<segments.size(); i++) {
                    if (segments.get(i).isOver()) {
                        deleteSegment(segments.get(i));
                        break;
                    }
                }
            } else if (overCircle()) {//deletes a circle
                for (int i =0; i<circles.size(); i++) {
                    if (circles.get(i).isOver()) {
                        deleteCircle(circles.get(i));
                        break;
                    }
                }
            }
        }
    };
    
    /**
    * Finds the object the mouse is hovering over and changes its color
    */
    EventHandler<MouseEvent> changeColor = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Color col2 = awtToFx(color);
            if (overPoint()) {
                for (int i =0; i<points.size(); i++) {
                    if (points.get(i).isOver()) {
                        points.get(i).setColor(col2);
                        points.get(i).getPoint().toFront();
                        
                        //if the point is white, remove the border and send it to the back of the canvas
                        if (col2.equals((Paint)Color.WHITE)) {
                            points.get(i).getPoint().setStroke(col2);
                            points.get(i).getPoint().toBack();
                        } else {
                            points.get(i).getPoint().setStroke(Color.BLACK);
                        }
                        break;
                    }
                }
            } else if (overSeg()) {
                for (int i =0; i<segments.size(); i++) {
                    if (segments.get(i).isOver()) {
                        segments.get(i).setColor(col2);
                        if (col2.equals((Paint)Color.WHITE)) {
                            segments.get(i).getLine().toBack();
                        }
                        break;
                    }
                }
            } else if (overCircle()) {
                for (int i =0; i<circles.size(); i++) {
                    if (circles.get(i).isOver()) {
                        circles.get(i).setColor(col2);
                        if (col2.equals((Paint)Color.WHITE)) {
                            circles.get(i).getCircle().toBack();
                        }
                        break;
                    }
                }
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for finding the distance between two points.
    */
    EventHandler<MouseEvent> findDistance = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                distanceMain(point);
            }
        }
    };
    
    /**
    * Selects a point and calls the main function for finding the angel between three points.
    */
    EventHandler<MouseEvent> findAngle = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            Point point = getPointOver(t);
            if (point != null) {
                angleMain(point);
            }
        }
    };
    
    /**
    * Deletes the given point and all other objects dependant on it.
    */
    public void deletePoint(Point point) {
        for (Segment seg: point.getSegments()) {
            deleteSegment(seg);
        }
        for (Circles circle: point.getCircles()) {
            deleteCircle(circle);
        }
        for (Point p: point.getMidPoints()) {
            deletePoint(p);
        }
        for (Segment seg: point.getBisectors()) {
            deleteSegment(seg);
        }  
        for (Segment seg: point.getParallels()) {
            deleteSegment(seg);
        }  
        for (Segment seg: point.getPerpendiculars()) {
            deleteSegment(seg);
        }
        for (geoLabel lbl: point.getLabels()) {
            deleteLabel(lbl);
        }
        points.remove(point);
        canvas.getChildren().remove(point.getPoint());
    }
    
    /**
    * Deletes the given segment and all other objects dependant on it.
    */
    public void deleteSegment(Segment seg) {
        for (Point p: seg.getPointsOn()) {
            deletePoint(p);
        }
        for (Segment segment: seg.getParallels()) {
            deleteSegment(segment);
        }
        for (Segment segment: seg.getPerpendiculars()) {
            deleteSegment(segment);
        }
        segments.remove(seg);
        canvas.getChildren().remove(seg.getLine());
    }
    
    /**
    * Deletes the given circle and all other objects dependant on it.
    */
    public void deleteCircle(Circles circle) {
        for (Point p: circle.getPointsOn()) {
            deletePoint(p);
        }
        circles.remove(circle);
        canvas.getChildren().remove(circle.getCircle());
    }
    
    /**
    * Deletes the given Label and all other objects dependant on it.
    */
    public void deleteLabel(geoLabel lbl) {
        labels.remove(lbl);
        canvas.getChildren().remove(lbl.getArc());
        canvas.getChildren().remove(lbl);
    }
    
    /**
    * Returns whether the mouse is hovering over a point.
    */
    public boolean overPoint() {
        for (Point point: points) {
            if (point.isOver()) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Returns whether the mouse is hovering over a segment.
    */
    public boolean overSeg() {
        for (Segment seg: segments) {
            if (seg.isOver()) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Returns whether the mouse is hovering over a circle.
    */
    public boolean overCircle() {
        for (Circles circle: circles) {
            if (circle.isOver()) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Adds a point and makes sure it remains connected to objects its dependant on
    */
    public void addPointMain(Point point) {
        points.add(point);
        canvas.getChildren().add(point.getPoint());
        point.setState("disabled");
        for (Circles circle: circles) {//if its on a circle
            if (circle.isOver()) { 
                point.addCircleOn(circle);
                circle.addPointOn(point);
                circle.getCircle().setStrokeWidth(2);
            }
        }
        for (Segment segment: segments) {//if its on a segment
            if (segment.isOver()) { 
                point.addSegmentOn(segment);
                segment.addPointOn(point);
                segment.getLine().setStrokeWidth(2);
            }
        }
        
        //decides which interection point is favored if the point is on two circles
        if (point.getCirclesOn().size()>=2) {
            double angleMouse = point.getCirclesOn().get(0).getCenter().getVector().angleTowards(mousePos);
            double angleCenter = point.getCirclesOn().get(0).getCenter().getVector().angleTowards(point.getCirclesOn().get(1).getCenter().getVector());
            if ((angleMouse>=angleCenter&&angleMouse<angleCenter+Math.PI)||(angleMouse<angleCenter-Math.PI)) {
                point.setIsClockwise(true);
            } else {
                point.setIsClockwise(false);
            }
        }
        
        //decides which interection point is favored if the point is on a circle and a segment
        else if (point.getSegmentsOn().size()==1&&point.getCirclesOn().size()>=1) {
            Vector start = new Vector(point.getSegmentsOn().get(0).getLine().getStartX(),
                            point.getSegmentsOn().get(0).getLine().getStartY());
            Vector end = new Vector(point.getSegmentsOn().get(0).getLine().getEndX(),
                            point.getSegmentsOn().get(0).getLine().getEndY());
            double ratio = mousePos.projectOnLine(start, end).findRatio(point.getCirclesOn().get(0).
                            getCenter().getVector().projectOnLine(start, end), start);
            if (0<ratio&&ratio<1) {
                point.setFavoredPoint(point.getSegmentsOn().get(0).getPointCloserTo(start));
            } else {
                point.setFavoredPoint(point.getSegmentsOn().get(0).getPointCloserTo(end));
            }
        }
        
        point.update();
    }
    
    /**
    * Adds the given point to selectedPoints if it isn't already there and the 
    * size of selectedPoints is less than what is given to be the maximum.
    */
    public void editSelectedPoints(Point point, int max) {
        if (point.getState().equals("selected")) {//its there, so remove it
            point.setState("disabled");
            selectedPoints.remove(point);
        } else if (point.getState().equals("disabled") && selectedPoints.size()<max) {//add it
            point.setState("selected");
            selectedPoints.add(point);
        }
    }
    
    /**
    * Adds the given segment to selectedSegments if it isn't already there and the 
    * size of selectedSoints is less than what is given to be the maximum.
    */
    public void editSelectedSegments(Segment seg, int max) {
        if (seg.getState().equals("selected")) {//remove it
            seg.setState("unselected");
            selectedLines.remove(seg);
        } else if (seg.getState().equals("unselected")&& selectedLines.size()<max) {//add it
            seg.setState("selected");
            selectedLines.add(seg);
        }
    }
    
    /**
    * Creates the segment once two points are selected.
    */
    public void segmentSelectorMain(Point point) {
        editSelectedPoints(point, 2);
        if (selectedPoints.size() == 2) {
            segments.add(new Segment(selectedPoints.get(0), selectedPoints.get(1))); 
            canvas.getChildren().add(segments.get(segments.size()-1).getLine());
            selectedPoints.get(0).getPoint().toFront();
            selectedPoints.get(1).getPoint().toFront();
            handleAddSegment();
        }
    }
    
    /**
    * Creates the line once two points are selected.
    */
    public void lineSelectorMain(Point point) {
        editSelectedPoints(point, 2);
        if (selectedPoints.size() == 2) {
            segments.add(new Lines(selectedPoints.get(0), selectedPoints.get(1))); 
            canvas.getChildren().add(segments.get(segments.size()-1).getLine());
            selectedPoints.get(0).getPoint().toFront();
            selectedPoints.get(1).getPoint().toFront();
            handleAddLine();
        }
    }
    
    /**
    * Creates the circle once two points are selected.
    */
    public void circleSelectorMain(Point point) {
        editSelectedPoints(point, 2);
        if (selectedPoints.size() == 2) {
            circles.add(new Circles(selectedPoints.get(0), selectedPoints.get(1))); 
            canvas.getChildren().add(circles.get(circles.size()-1).getCircle());
            selectedPoints.get(0).getPoint().toFront();
            selectedPoints.get(1).getPoint().toFront();
            handleAddCircle();
        }
    }
    
    /**
    * Creates the midpoint once two points are selected.
    */
    public void midpointSelectorMain(Point point) {
        editSelectedPoints(point, 2);
        if (selectedPoints.size() == 2) {
            Point midPoint = new Point((selectedPoints.get(0).getX()+selectedPoints.get(1).getX())/2,
                (selectedPoints.get(0).getY()+selectedPoints.get(1).getY())/2);
            midPoint.addEndPointsOf(selectedPoints.get(0), selectedPoints.get(1));
            selectedPoints.get(0).addMidPoint(midPoint);
            selectedPoints.get(1).addMidPoint(midPoint);
            addPointMain(midPoint);
            handleAddMidpoint();
        }
    } 
    
    /**
    * Creates the angle bisector once three points are selected.
    */
    public void bisectorSelectorMain(Point point) {
        editSelectedPoints(point, 3);
        if (selectedPoints.size() == 3) {
            double angle = (selectedPoints.get(1).getVector().angleTowards(selectedPoints.get(2).getVector()) -
                    selectedPoints.get(1).getVector().angleTowards(selectedPoints.get(0).getVector())+2*Math.PI)%(2*Math.PI)/2;
            Vector image = selectedPoints.get(0).getVector().rotate(selectedPoints.get(1).getVector(), angle);
            segments.add(new Lines(selectedPoints.get(1), new Point(image.getX(), image.getY()))); 
            segments.get(segments.size()-1).addBisectorPoints(selectedPoints);
            segments.get(segments.size()-1).update();
            canvas.getChildren().add(segments.get(segments.size()-1).getLine());
            for (int i = 0; i<3; i++) {
                selectedPoints.get(i).getPoint().toFront();
                selectedPoints.get(i).addBisector(segments.get(segments.size()-1));
            }
            handleAddBisector();
        }
    }
    
    /**
    * Selects a point to prepare for creating a parallel line
    */
    public void parallelSelectorPointMain(Point point) {
        editSelectedPoints(point, 1);
        if (selectedPoints.size() == 1&&selectedLines.size()==1) {
            parallelSelectorMain();
        }
    }
    
    /**
    * Selects a segment to prepare for creating a parallel line
    */
    public void parallelSelectorLineMain(Segment seg) {
        editSelectedSegments(seg, 1);
        if (selectedPoints.size() == 1&&selectedLines.size()==1) {
            parallelSelectorMain();
        }
    }
    
    /**
    * Creates the parallel line once a point and a segment are selected.
    */
    public void parallelSelectorMain() {
        Vector image = selectedPoints.get(0).getVector().add(selectedLines.get(0).endV().sub(selectedLines.get(0).startV()));
        Segment parallel = new Lines(selectedPoints.get(0), new Point(image.getX(), image.getY()));
        segments.add(parallel); 
        parallel.setParallelPoint(selectedPoints.get(0));
        parallel.setParallelSegment(selectedLines.get(0));
        parallel.update();
        canvas.getChildren().add(parallel.getLine());
        selectedPoints.get(0).addParallel(parallel);
        selectedPoints.get(0).getPoint().toFront();
        selectedLines.get(0).addParallel(parallel);
        handleAddParallel();
    }
    
    /**
    * Selects a point to prepare for creating a perpendicular line
    */
    public void perpendicularSelectorPointMain(Point point) {
        editSelectedPoints(point, 1);
        if (selectedPoints.size() == 1&&selectedLines.size()==1) {
            perpendicularSelectorMain();
        }
    }
    
    /**
    * Selects a segment to prepare for creating a perpendicular line
    */
    public void perpendicularSelectorLineMain(Segment seg) {
        editSelectedSegments(seg, 1);
        if (selectedPoints.size() == 1&&selectedLines.size()==1) {
            perpendicularSelectorMain();
        }
    }
    
    /**
    * Creates the perpendicular line once a point and a segment are selected.
    */
    public void perpendicularSelectorMain() {
        Segment perpendicular = new Lines(selectedPoints.get(0), new Point(0, 0));
        segments.add(perpendicular); 
        perpendicular.setPerpendicularPoint(selectedPoints.get(0));
        perpendicular.setPerpendicularSegment(selectedLines.get(0));
        perpendicular.update();
        canvas.getChildren().add(perpendicular.getLine());
        selectedPoints.get(0).addPerpendicular(perpendicular);
        selectedPoints.get(0).getPoint().toFront();
        selectedLines.get(0).addPerpendicular(perpendicular);
        handleAddPerpendicular();
    }
    
    /**
    * Creates the label saying the distance between two points once they are selected.
    */
    public void distanceMain(Point point) {
        editSelectedPoints(point, 2);
        if (selectedPoints.size() == 2) {
            geoLabel lbl = new geoLabel(selectedPoints.get(0), selectedPoints.get(1), zoom);
            labels.add(lbl); 
            canvas.getChildren().add(lbl);
            selectedPoints.get(0).addLbl(lbl);
            selectedPoints.get(1).addLbl(lbl);
            handleFindDistance();
        }
    }   
    
    /**
    * Creates the label saying the angle between three points once they are selected.
    */
    public void angleMain(Point point) {
        editSelectedPoints(point, 3);
        if (selectedPoints.size() == 3) {
            geoLabel lbl = new geoLabel(selectedPoints.get(0), selectedPoints.get(1), selectedPoints.get(2));
            labels.add(lbl); 
            canvas.getChildren().add(lbl);
            canvas.getChildren().add(lbl.getArc());
            selectedPoints.get(0).addLbl(lbl);
            selectedPoints.get(1).addLbl(lbl);
            selectedPoints.get(2).addLbl(lbl);
            handleFindAngle();
        }
    } 
    
    /**
    * Zooms the canvas in or out with respect to the mouse's position.
    */
    @FXML
    public void scroll(ScrollEvent t) {
        double zoomFactor = 1.05;
        if (t.getDeltaY() < 0){
            zoomFactor = 2.0 - zoomFactor;
        }
        zoom.set(0, zoom.get(0)*zoomFactor);

        for (Point point: points) {//disables all points on bisectors/perpendicular lines from zooming
            if (isOnBisectorOrPerpendicular(point)) {
                point.setDisableUpdate(true);
            }
        }

        for (Point point: points) {//zooms in all of the points
            if (point.getTotalOn()==0) {
                point.setCoords(point.getVector().sub(mousePos).scale(zoomFactor).add(mousePos));
                point.update();
            } else if (isOnBisectorOrPerpendicular(point)) {//manually zoom in all points on bisector/perpendicular lines
                point.setCoords(point.getVector().sub(mousePos).scale(zoomFactor).add(mousePos));
            }
            
        }
        
        //sets the ratio for all points on bisectors/parallel lines and enables them to update.
        for (Point point: points) {
            if (isOnBisectorOrPerpendicular(point)) {
                point.setRatio(point.getVector().findRatio(point.getSegmentsOn().get(0).startV(), point.getSegmentsOn().get(0).endV()));
                point.setDisableUpdate(false);
            }
        }
    }
    
    /**
    * Returns whether a given point is one an angle bisector or perpendicular line.
    */
    public boolean isOnBisectorOrPerpendicular(Point point) {
        return (point.getSegmentsOn().size()==1&&point.getCirclesOn().size()==0&&point.getSegmentsOn().get(0).getBisectorPoints().size()!=0)||
                (point.getSegmentsOn().size()==1&&point.getCirclesOn().size()==0&&point.getSegmentsOn().get(0).getPerpendicularPoint()!=null);
    }
    
    /**
    * Checks which objects should enlarge because the mouse is hovering over them.
    */
    @FXML
    public void onMouseMove(MouseEvent t) {
        mousePos = new Vector(t.getX(), t.getY());
        boolean overPoint = false;
        for (Point point: points) {//checks points
            if (mousePos.distanceTo(point.getVector())<=8) {
                point.getPoint().setRadius(9);
                overPoint = true;
            } else {
                point.getPoint().setRadius(8);
            }
        }
        
        for (Segment segment: segments) {//checks segments
            if (overPoint || mousePos.distanceToLine(segment.startV(), segment.endV())>3
                ||(!segment.isLine()&&mousePos.distanceToSegment(segment.startV(), segment.endV())>3)) {
                segment.getLine().setStrokeWidth(2);
            } else {
                segment.getLine().setStrokeWidth(4);
            }
        }
        
        for (Circles circle: circles) {//checks circles
            if (overPoint || (mousePos.distanceTo(circle.getCenter().getVector())>circle.getRadius()+.5
                    ||mousePos.distanceTo(circle.getCenter().getVector())<circle.getRadius()-2.5)) {
                circle.getCircle().setStrokeWidth(2);
            } else {
                circle.getCircle().setStrokeWidth(4);
            }
        }
    }
    
    double orgSceneX, orgSceneY;
    double orgGetX, orgGetY;
    /**
    * Gets ready for the screen to be dragged by storing the mouse's coordinates
    */
    EventHandler<MouseEvent> onScreenPressed = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            for (Point point: points) {
                if (point.getTotalOn()==0) {
                    point.setOrgCoords();
                }
            }
        }
    };
    
    /**
    * Drags the screen around by dragging around all "free" points on it.
    */
    EventHandler<MouseEvent> onScreenDragged = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            for (Point point: points) {
                if (point.getTotalOn()==0) {
                    point.setCoords(new Vector(point.getOrgX() + t.getSceneX() - orgSceneX, point.getOrgY() + t.getSceneY() - orgSceneY));
                    point.update();
                }
            }
        }  
    };
    
    /**
    * Gets ready for a point to be dragged by storing the its coordinates
    */
    EventHandler<MouseEvent> onPressed = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            for (Point point: points) {
                if (point.getState().equals("drag")&&point.isOver()) {
                    orgSceneX = t.getSceneX();
                    orgSceneY = t.getSceneY();
                    point.setOrgTranslateX(point.getX());
                    point.setOrgTranslateY(point.getY());
                }
            }
        }
    };
     
    /**
    * Drags the point around, making sure its stays on the objects it need to be on.
    */
    EventHandler<MouseEvent> onDragged = 
        new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            for (Point point: points) {
                if (point.getState().equals("drag")&&point.isOver()) {
                    double newX = point.getOrgTranslateX() + t.getSceneX() - orgSceneX;
                    double newY = point.getOrgTranslateY() + t.getSceneY() - orgSceneY;
                    if (point.getTotalOn() == 0) {
                        point.setCoords(new Vector(newX, newY));
                    } else if(point.getCirclesOn().size()==1&&point.getSegmentsOn().size()==0) {//its on a circle, update angle
                        point.setAngle(point.getCirclesOn().get(0).getCenter().getVector().angleTowards(new Vector(newX, newY)));
                    } else if (point.getSegmentsOn().size() == 1 && point.getCirclesOn().size()==0){//its on a segment, update ratio
                        Vector v;
                        if (!point.getSegmentsOn().get(0).isLine()) {
                            v = (new Vector(newX, newY)).projectOnSegment(point.getSegmentsOn().get(0).startV(), point.getSegmentsOn().get(0).endV());
                        } else {
                            v = (new Vector(newX, newY)).projectOnLine(point.getSegmentsOn().get(0).startV(), point.getSegmentsOn().get(0).endV());
                        }
                        
                        point.setRatio(v.findRatio(point.getSegmentsOn().get(0).startV(), point.getSegmentsOn().get(0).endV()));
                    }

                    point.update();
                }
                
            }
            
        }
    };
    
    /**
    * Returns the javafx color corresponding to the given java.awt color
    */
    public Color awtToFx(java.awt.Color col) {
        return Color.rgb(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()/255); 
    }
    
    /**
    * Returns the java.awt color corresponding to the given javafx color
    */
    public java.awt.Color fxToAwt(Color col) {
        return new java.awt.Color((float) col.getRed(),(float) col.getGreen(),(float) col.getBlue(),(float) col.getOpacity());
    }
    
    /**
    * Saves the file that is being worked on currently by serializing the state of all objects into the
    * file. If this is a new file, the user will be able to save its name in a spot in the directory.
    */
    @FXML
    public void updateFile() {
        try {       
            if (file == null) { //new file
                file = fileChooser.showSaveDialog((Stage) root.getScene().getWindow());
            }
            if (file != null) {
                
                //storing state of javafx objects into instance fields
                for (Point point: points) {
                point.setPointFields();
                }
                for (Segment seg: segments) {
                    seg.setSegmentFields();
                }
                for (Circles circle: circles) {
                    circle.setCircleFields();
                }
                for (geoLabel lbl: labels) {
                    lbl.setLabelFields();
                }
                
                //adding serailizized objects to the text file
                FileOutputStream file2 = new FileOutputStream(file.getAbsolutePath());
                ObjectOutputStream out = new ObjectOutputStream(file2);
                out.writeObject(points);
                out.writeObject(segments);
                out.writeObject(circles);
                out.writeObject(labels);
                out.writeObject(selectedPoints);
                out.writeObject(selectedLines);
                out.writeObject(color);
                out.writeObject(mousePos);
                out.writeObject(zoom);
                file2.close();
            }
        } catch (IOException e) {}
    }
    
    /**
    * Opens a file that the user chooses by deserializing its text into different objects.
    */
    @FXML
    public void readFile() {
        try {
            file = fileChooser.showOpenDialog((Stage) root.getScene().getWindow()); //choose file
            if (file != null) {
                //deserializes the file
                FileInputStream file2 = new FileInputStream(file.getAbsolutePath());
                ObjectInputStream in = new ObjectInputStream(file2);
                points = (ArrayList<Point>) in.readObject();
                segments = (ArrayList<Segment>) in.readObject();
                circles = (ArrayList<Circles>) in.readObject();
                labels = (ArrayList<geoLabel>) in.readObject();
                selectedPoints = (ArrayList<Point>) in.readObject();
                selectedLines = (ArrayList<Segment>) in.readObject();
                color = (java.awt.Color) in.readObject();
                mousePos = (Vector) in.readObject();
                zoom = (ArrayList<Double>) in.readObject();
                file2.close();

                //updates javafx objects that weren't deserialized by using the info
                //in certain instance fields that were deserialized.
                colorPicker.setValue(awtToFx(color));
                canvas.getChildren().clear();
                for (Point point: points) {
                    point.usePointFields();
                    canvas.getChildren().add(point.getPoint());
                }
                for (Segment seg: segments) {
                    canvas.getChildren().add(seg.getLine());
                    seg.useSegmentFields();
                }
                for (Circles circle: circles) {
                    canvas.getChildren().add(circle.getCircle());
                    circle.useCircleFields();
                }
                for (geoLabel lbl: labels) {
                    canvas.getChildren().add(lbl);
                    canvas.getChildren().add(lbl.getArc());
                    lbl.useLabelFields();
                }
                for (Point point: points) {
                    point.toFrontIfNotWhite();
                }
                for (Segment seg: segments) {
                    seg.toBackIfWhite();
                }
                for (Circles circle: circles) {
                    circle.toBackIfWhite();
                }
            }
        } catch (IOException | ClassNotFoundException e) {}
    }    
    
    /**
    * Initializes all necessary instance fields.
    */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Rectangle clip = new Rectangle(826, 703);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        canvas.setClip(clip);
        canvas.setOnMousePressed(onPressed);
        canvas.setOnMouseDragged(onDragged);
        colorPicker.setValue(Color.valueOf("000000"));
        zoom.add(1.0);
        
        buttons = new ArrayList<>(Arrays.asList(addPointBtn, moveBtn, addSegmentBtn, addCircleBtn, distanceBtn, angleBtn, 
            addLineBtn, addMidpointBtn, addBisectorBtn, addParallelBtn, addPerpendicularBtn, deleteBtn, colorBtn));
    }    
}
