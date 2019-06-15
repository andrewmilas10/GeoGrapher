/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geo;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 *
 * @author milas6582
 */
public class Point implements Serializable {
    double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;
    
    private ArrayList<Segment> segments = new ArrayList<>(); //segments which this point is an endpoint of
    private ArrayList<Circles> circles = new ArrayList<>();//circles which the point is on
    private ArrayList<Point> midPoints = new ArrayList<>();//points that are midpoints with this point and one other
    private ArrayList<Segment> bisectors = new ArrayList<>();//segments that are the angle bisector of this point with others
    private ArrayList<Segment> parallels = new ArrayList<>();//segments that are parallels determined by this point and another segment
    private ArrayList<Segment> perpendiculars = new ArrayList<>();//segments that are perpendiculars determined by this point and another segment
    private ArrayList<geoLabel> labels = new ArrayList<>();//labels defined by this point and others
    
    private ArrayList<Point> endPointsOf = new ArrayList<>();//points that this point is the midpoint of;
    private int timesAround = 0;
    private boolean disableUpdate = false;
    
    private ArrayList<Segment> segmentsOn = new ArrayList<>(); //segments which this point is on
    private double ratio = 0;//only if the point is on a segment. 
    //the scalar that the vector from the start endpoint to the point must be multiplied by to get the vector from the start endpoint to the the end endpoint
    private Point favoredPoint;
    
    private ArrayList<Circles> circlesOn = new ArrayList<>();//circles the point is on
    private double angle = 0;
    private boolean isClockwise;
    
    private fxCircle point;//the circle object representing the point itseld
    private double pointX, pointY, pointZ;
    private boolean visible;
    
    private String state = "drag";//whether the point can be drag, is selected, or is disabled
    private boolean canDrag = true;//whether a point can be dragged
    
    private java.awt.Color color = new java.awt.Color(74, 155, 232, 255);
    
    /**
    * Constructor. Sets up the coordinates and the circle that is displayed on screen.
    */
    public Point(double x, double y) {
        point = new fxCircle(); 
        point.setCenterX(0); 
        point.setCenterY(0);
        point.setTranslateX(x);
        point.setTranslateY(y);
        point.setRadius(8); 
        point.setFill(awtToFx(color));
        point.setStroke(Paint.valueOf("000000"));
    }
    
    /**
    * Stores info about point in instance fields to prepare for serialization.
    */
    public void setPointFields() {
        pointX = point.getTranslateX();
        pointY = point.getTranslateY();
        pointZ = point.getTranslateZ();
        visible = point.visibleProperty().getValue();
    }
    
    /**
    * Returns info to point using certain instance fields after serialization.
    */
    public void usePointFields() {
        point.setCenterX(0); 
        point.setCenterY(0);
        point.setTranslateX(pointX);
        point.setTranslateY(pointY);
        point.setTranslateZ(pointZ);
        point.setVisible(visible);
        point.setRadius(8); 
        point.setFill(awtToFx(color));
        point.setStroke(Paint.valueOf("000000"));
        if (awtToFx(color).equals((Paint)Color.WHITE)) {
            point.setStroke(awtToFx(color));
        }
        setState(state);
    }
    
    /**
    * Updates the point based on changes in what the point is dependant on.
    * Then updates all objects that are dependant on this point.
    */
    public void update() {
        toFrontIfNotWhite();
        
        if (circlesOn.size() == 1 && segmentsOn.size() == 0) {//one circle, 0 segments
            setCoords(circlesOn.get(0).findVector(angle));
        } else if (segmentsOn.size() == 1 && circlesOn.size() == 0){//zero circles, 1 segment
            setCoords(getVector().findPoint(segmentsOn.get(0).startV(), segmentsOn.get(0).endV(), ratio)); 
        }  else if (circlesOn.size() >= 2) {//on multiple circles
            angle = circlesOn.get(0).findIntersectCircleAngle(circlesOn.get(1), isClockwise);
            if (angle != -1 && !oneDependantInvisible()) {
                setCoords(circlesOn.get(0).findVector(angle));
                setVisibility(true);
            } else {
                setVisibility(false);
            }
        } else if (segmentsOn.size() >= 2 && circlesOn.size() == 0) {//on multiple segments
            Vector coords = getVector().findIntersection(segmentsOn.get(0), segmentsOn.get(1));
            if (coords != null && !oneDependantInvisible()) {
                setCoords(coords);
                setVisibility(true);
            } else {
                setVisibility(false);
            }
        } else if (segmentsOn.size() >= 1 && circlesOn.size() == 1) {//on a segment and a circle
            angle = circlesOn.get(0).findIntersectSegmentAngle(segmentsOn.get(0), getFavoredVector());
            if (angle != -1 && !oneDependantInvisible()) {
                setCoords(circlesOn.get(0).findVector(angle));
                setVisibility(true);
            } else {
                setVisibility(false);
            }
        } else if (endPointsOf.size() != 0) {//it is the midpoint with two other points
            if (!oneDependantInvisible()) {
                setCoords(new Vector((endPointsOf.get(0).getX()+endPointsOf.get(1).getX())/2,
                (endPointsOf.get(0).getY()+endPointsOf.get(1).getY())/2));
                setVisibility(true);
            } else {
                setVisibility(false);
            }
        }
        
        //updates all objects dependant on this point.
        for (Segment seg: segments) {
            seg.update();
        }
        for (Circles circle: circles) {
            circle.update();
        }
        for (Point point: midPoints) {
            point.update();
        }
        for (Segment seg: bisectors) {
            seg.update();
        }  
        for (Segment seg: parallels) {
            seg.update();
        }  
        for (Segment seg: perpendiculars) {
            seg.update();
        }
        for (geoLabel lbl: labels) {
            lbl.update();
        } 
    }
    
    /**
    * Puts the point to the front of the canvas unless it is white, in which case
    * it is sent to the back.
    */
    public void toFrontIfNotWhite() {
        point.toFront();
        if (awtToFx(color).equals(javafx.scene.paint.Color.WHITE)) {
            point.toBack();
        }
    }
    
    /**
    * Returns whether at least one object is invisible that the point is dependant on.
    */
    public boolean oneDependantInvisible() {
        for (Segment segment: segmentsOn) {
            if (!segment.getLine().visibleProperty().getValue()) {
                return true;
            }
        }
        for (Circles circle: circlesOn) {
            if (!circle.getCircle().visibleProperty().getValue()) {
                return true;
            }
        }
        for (Point points: endPointsOf) {
            if (!points.getPoint().visibleProperty().getValue()) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Updates the visibility of this point and all objects that are dependant on this point.
    */
    public void setVisibility(boolean isVisible) {
        point.setVisible(isVisible);
        for (Segment segment: segments) {
            segment.checkSegVisibility();
        } 
        for (Circles circle: circles) {
            if (circle.oneDependantInvisible()) {
                circle.setVisibility(false);
            } else {
                circle.setVisibility(true);
            }
        }
        for (Point point: midPoints) {
            point.checkPointVisiblity();
        }
        for (Segment seg: bisectors) {
            seg.checkSegVisibility();
        }  
        for (Segment seg: parallels) {
            seg.checkSegVisibility();
        }  
        for (Segment seg: perpendiculars) {
            seg.checkSegVisibility();
        }
        for (geoLabel lbl: labels) {
            lbl.checkLblVisibility();
        } 
    }
    
    /**
    * Updates the point's visibility if it should be updated.
    */
    public void checkPointVisiblity() {
        if (oneDependantInvisible()) {
            setVisibility(false);
        } else {
            setVisibility(true);
        }
    }
    
    /**
    * Sets the point's coordinates based on the given vector.
    */
    public void setCoords(Vector v1) {
        point.setTranslateX(v1.getX());
        point.setTranslateY(v1.getY());
    }

    /**
    * Sets the point's state and color.
    */
    public void setState(String newState) {
        if (newState.equals("selected")) {
            point.setFill(Paint.valueOf("fcdd2f"));
        } else if (newState.equals("drag") && canDrag) {
            point.setFill(awtToFx(color));
        } else {
            newState = "disabled";
            if (awtToFx(color).equals(Paint.valueOf("4a9be8"))) {
                point.setFill(Paint.valueOf("595855"));
            }else {
                point.setFill(awtToFx(color));
            }
        }
        state = newState;
    }
    
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
    
    //Basic Mutators and Accessors Below---------------------------------------------------------------------------
    
    public boolean isOver() {
        return point.getRadius() == 9;
    }
    
    public void addSegmentOn(Segment line) {
        segmentsOn.add(line);
        if (getTotalOn()==1) {
            ratio = getVector().findRatio(segmentsOn.get(0).startV(), segmentsOn.get(0).endV());
        } else{
            canDrag = false;
        }
    }
    
    public void addCircleOn(Circles circle) {
        circlesOn.add(circle);
        if (getTotalOn()==1) {
            angle = circle.getCenter().getVector().angleTowards(getVector());
        } else {
            canDrag = false;
        }
    }

    public void addEndPointsOf(Point p1, Point p2) {
        endPointsOf.add(p1);
        endPointsOf.add(p2);
        canDrag = false;
    }
    
    public void addMidPoint(Point p1) {
        midPoints.add(p1);
    }
    
    public void addLbl(geoLabel lbl) {
        labels.add(lbl);
    }
    
    public void addBisector(Segment seg) {
        bisectors.add(seg);
    }
    
    public void addParallel(Segment parallel) {
        parallels.add(parallel);
    }
    
    public void addPerpendicular(Segment perpendicular) {
        perpendiculars.add(perpendicular);
    }
    
    public double getTotalOn() {
        return segmentsOn.size()+circlesOn.size();
    }
    
    public void setColor(Color col) {
        color=fxToAwt(col);
        point.setFill(col);
    }
    
    public ArrayList<Segment> getSegmentsOn() {
        return segmentsOn;
    }
    
    public ArrayList<Circles> getCirclesOn() {
        return circlesOn;
    }
    
    public double getX() {
        return point.getTranslateX();
    }
    
    public Vector getVector() {
        return new Vector(getX(), getY());
    }
    
    public Circle getPoint() {
        return point;
    }

    public String getState() {
        return state;
    }
    

    public void setOrgCoords() {
        this.orgTranslateX = getX();
        this.orgTranslateY = getY();
    }

    public double getOrgY() {
        return orgTranslateY;
    }

    public double getOrgX() {
        return orgTranslateX;
    }
    
    public double getY() {
        return point.getTranslateY();
    }
    
    public void reverseRatio() {
        ratio*=-1;
    }
    
    public boolean getDisableUpdate() {
        return disableUpdate;
    }

    public void setDisableUpdate(boolean disableUpdate) {
        this.disableUpdate = disableUpdate;
    }
    
    public void setIsClockwise(boolean isClockwise) {
        this.isClockwise = isClockwise;
    }

    public void setFavoredPoint(Point favoredPoint) {
        this.favoredPoint = favoredPoint;
    }
    
    public Vector getFavoredVector() {
        return segmentsOn.get(0).getSegmentEndCloserTo(favoredPoint);
    }

    public ArrayList<Segment> getSegments() {
        return segments;
    }

    public ArrayList<Segment> getBisectors() {
        return bisectors;
    }

    public ArrayList<Point> getMidPoints() {
        return midPoints;
    }

    public ArrayList<Segment> getPerpendiculars() {
        return perpendiculars;
    }

    public ArrayList<Segment> getParallels() {
        return parallels;
    }

    public ArrayList<geoLabel> getLabels() {
        return labels;
    }

    public void addSegment(Segment line) {
        segments.add(line);
    }

    public ArrayList<Circles> getCircles() {
        return circles;
    }

    public void addCircle(Circles circle) {
        circles.add(circle);
    }

    public void setOrgTranslateY(double orgTranslateY) {
        this.orgTranslateY = orgTranslateY;
    }

    public void setOrgTranslateX(double orgTranslateX) {
        this.orgTranslateX = orgTranslateX;
    }

    public double getOrgTranslateY() {
        return orgTranslateY;
    }

    public double getOrgTranslateX() {
        return orgTranslateX;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }
}
