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
import javafx.scene.shape.Line;

/**
 *
 * @author milas6582
 */
public class Segment implements Serializable {
    protected ArrayList<Point> pointsOn = new ArrayList<>(); 
    protected fxLine line;
    private boolean visible;
    public double lineZ;
    protected String state = "unselected";
    
    protected Point start;
    protected Point end;
    
    //points that define the line if it is angle bisector
    protected ArrayList<Point> bisectorPoints = new ArrayList<>();
    
    //point that define this segment if it is a parallel line
    protected Point parallelPoint;
    //segments that define this segment if it is a parallel line
    protected Segment parallelSegment;
    protected ArrayList<Segment> parallels = new ArrayList<>();//segments that are parallels determined by this segment and another point
    
    //point that define this segment if it is a perpendicular line
    protected Point perpendicularPoint;
    //segments that define this segment if it is a perpendicular line
    protected Segment perpendicularSegment;
    protected ArrayList<Segment> perpendiculars = new ArrayList<>();//segments that are perpendiculars determined by this segment and another point
    
    protected double angle = 0;
    
    private java.awt.Color color = new java.awt.Color(0, 0, 0, 255);
    
    /**
    * Constructor. Sets up the endpoints and the line that is displayed on screen.
    */
    public Segment(Point newStart, Point newEnd) {
        line = new fxLine(); 
        line.setStrokeWidth(2);
        start = newStart;
        end = newEnd;
        start.addSegment(this);
        end.addSegment(this);
        update();
    }
    
    /**
    * Stores info about line in instance fields to prepare for serialization.
    */
    public void setSegmentFields() {
        lineZ = line.getTranslateZ();
        visible = line.visibleProperty().getValue();
    }
    
    /**
    * Returns info to line using certain instance fields after serialization.
    */
    public void useSegmentFields() {
        line.setStrokeWidth(2);
        line.setTranslateZ(lineZ);
        line.setVisible(visible);
        line.toBack();
        line.setStroke(awtToFx(color));
        setState(state);
        update();
    }
    
    /**
    * Puts the line to the back of the canvas if it is white
    */
    public void toBackIfWhite() {
        if (awtToFx(color).equals(javafx.scene.paint.Color.WHITE)) {
            line.toBack();
        }
    }
    
    /**
    * Updates the segment based on changes in the endpoints.
    * Then updates all objects that are dependant on this segment.
    */
    public void update() {
        line.setStartX(start.getX());
        line.setStartY(start.getY());
        line.setEndX(end.getX());
        line.setEndY(end.getY());
        
        for (Point point: pointsOn) {
            point.update();
        }
        for (Segment seg: parallels) {
            seg.update();
        }
        for (Segment seg: perpendiculars) {
            seg.update();
        }
    }
    
    /**
    * Updates the visibility of this segment and all objects that are dependant on it.
    */
    public void setVisibility(boolean isVisible) {
        line.setVisible(isVisible);
        for (Point point: pointsOn) {
            point.checkPointVisiblity();
        }
        for (Segment seg: parallels) {
            seg.checkSegVisibility();
        }
        for (Segment seg: perpendiculars) {
            seg.checkSegVisibility();
        }
    }
    
    /**
    * Updates the segment's visibility if it should be updated.
    */
    public void checkSegVisibility() {
        if (oneEndpointInvisible()) {
            setVisibility(false);
        } else {
            setVisibility(true);
        }
    }
    
    /**
    * Returns the endpoint closer to the given vector
    */
    public Point getPointCloserTo(Vector v) {
        if (start.getVector().distanceTo(v)<end.getVector().distanceTo(v)) {
            return start;
        }
        return end;
    }
    
    /**
    * Return the point on the side of the screen closer to the given point.
    */
    public Vector getSegmentEndCloserTo(Point point) {
        if (getPointCloserTo(new Vector(line.getStartX(), line.getStartY())).equals(point)) {
            return new Vector(line.getStartX(), line.getStartY());
        }
        return new Vector(line.getEndX(), line.getEndY());
    }
    
    /**
    * Sets the segment's state and color.
    */
    public void setState(String newState) {
        if (newState.equals("selected")) {
            line.setStroke(Paint.valueOf("d3c719"));
        } else if (newState.equals("unselected")) {
            line.setStroke(awtToFx(color));
        }
        state = newState;
    }

    /**
    * Returns whether at least one object is invisible that the point is dependant on.
    */
    public boolean oneEndpointInvisible() {
        if (start.getPoint().visibleProperty().getValue()&&end.getPoint().visibleProperty().getValue()) {
            if (bisectorPoints.size()!=0) {
                for (Point point: bisectorPoints) {
                    if (!point.getPoint().visibleProperty().getValue()) {
                        return true;
                    }
                }
            }
            if (parallelPoint != null) {
                if (!parallelPoint.getPoint().visibleProperty().getValue()||!parallelSegment.getLine().visibleProperty().getValue()) {
                    return true;
                }
            }
            if (perpendicularPoint != null) {
                if (!perpendicularPoint.getPoint().visibleProperty().getValue()||!perpendicularSegment.getLine().visibleProperty().getValue()) {
                    return true;
                }
            }
            return false;
        }
        return true;
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
    
    public Line getLine() {
        return line;
    }
    
    public boolean isLine() {
        return false;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public void addBisectorPoints(ArrayList<Point> points) {
        bisectorPoints = points;
    }

    public void addParallel(Segment parallel) {
        parallels.add(parallel);
    }

    public void setParallelSegment(Segment parallelSegment) {
        this.parallelSegment = parallelSegment;
    }

    public void setParallelPoint(Point parallelPoint) {
        this.parallelPoint = parallelPoint;
    }
    
    public void addPerpendicular(Segment perpendicular) {
        perpendiculars.add(perpendicular);
    }

    public void setPerpendicularSegment(Segment perpendicularSegment) {
        this.perpendicularSegment = perpendicularSegment;
    }

    public void setPerpendicularPoint(Point perpendicularPoint) {
        this.perpendicularPoint = perpendicularPoint;
    }
    
    public void addPointOn(Point point) {
        pointsOn.add(point);
    }
    
    public boolean isOver() {
        return line.getStrokeWidth() == 4;
    }
    
    public String getState() {
        return state;
    }
    
    public void setColor(Color col) {
        color = fxToAwt(col);
        line.setStroke(col);
    }
    
    public Vector startV() {
        return start.getVector();
    }

    public Point getEnd() {
        return end;
    }

    public Point getStart() {
        return start;
    }
    
    public Vector endV() {
        return end.getVector();
    }

    public ArrayList<Point> getBisectorPoints() {
        return bisectorPoints;
    }

    public Point getPerpendicularPoint() {
        return perpendicularPoint;
    }

    public Segment getPerpendicularSegment() {
        return perpendicularSegment;
    }

    public Segment getParallelSegment() {
        return parallelSegment;
    }

    public ArrayList<Segment> getPerpendiculars() {
        return perpendiculars;
    }

    public ArrayList<Segment> getParallels() {
        return parallels;
    }

    public ArrayList<Point> getPointsOn() {
        return pointsOn;
    }
}
