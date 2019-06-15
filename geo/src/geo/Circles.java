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
public class Circles implements Serializable {
    private ArrayList<Point> pointsOn = new ArrayList<>(); 
    private fxCircle circle;
    
    private Point center;
    private double circleZ;
    private boolean visible;
    private Point endPoint = null;
    private double radius;
    private java.awt.Color color = new java.awt.Color(0, 0, 0, 255);
    
    /**
    * Constructor. Sets up the center, radius, and the circle that is displayed on screen.
    */
    public Circles(Point newCenter, Point newEndPoint) {
        circle = new fxCircle(); 
        circle.setStrokeWidth(2);
        circle.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0));
        circle.setStroke(awtToFx(color));
         
        center = newCenter;
        endPoint = newEndPoint;
        radius = center.getVector().distanceTo(endPoint.getVector());
        
        center.addCircle(this);
        endPoint.addCircle(this);
        
        update();
    }
    
//    public Circles(Point newCenter, double newRadius) {  //TODO create circle with given radius
//        
//        circle = new Circle(); 
//        circle.setStrokeWidth(2);
//         
//        center = newCenter;
//        radius = newRadius;
//        
//        center.addCircle(this);
//        
//        update();
//    }
    
    /**
    * Stores info about circle in instance fields to prepare for serialization.
    */
    public void setCircleFields() {
        circleZ = circle.getTranslateZ();
        visible = circle.visibleProperty().getValue();
    }
    
    /**
    * Returns info to circle using certain instance fields after serialization.
    */
    public void useCircleFields() {
        circle.setTranslateZ(circleZ);
        circle.setVisible(visible);
        circle.toBack();
        circle.setStroke(awtToFx(color));
        circle.setStrokeWidth(2);
        circle.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0));
        update();
    }
    
    /**
    * Puts the line to the back of the canvas if it is white
    */
    public void toBackIfWhite() {
        if (awtToFx(color).equals(javafx.scene.paint.Color.WHITE)) {
            circle.toBack();
        }
    }
    
    
    /**
    * Updates the circle based on changes in the circle's center/endpoint.
    * Then updates all objects that are dependant on this point.
    */
    public void update() {
        if (endPoint != null) {
            radius = center.getVector().distanceTo(endPoint.getVector());
        }
        circle.setCenterX(center.getX());
        circle.setCenterY(center.getY());
        circle.setRadius(radius);
        for (Point point: pointsOn) {
            point.update();
        }
    }
    
    /**
    * Updates the visibility of this circle and all objects that are dependant on this circle.
    */
    public void setVisibility(boolean isVisible) {
        circle.setVisible(isVisible);
        for (Point point: pointsOn) {
            if (point.oneDependantInvisible()) {
                point.setVisibility(false);
            } else {
                point.setVisibility(true);
            }
        }
    }
    
    /**
    * Finds the angle that corresponds to the intersection of the circle and the given segment.
    * Picks the intersection that corresponds with the favored vector.
    */
    public double findIntersectSegmentAngle(Segment seg, Vector favoredV) {
        boolean isLeft = findVector(0).getX()-findVector(0).projectOnLine(seg.startV(), seg.endV()).getX()<.00001;
        boolean isBellow = findVector(0).getY()-findVector(0).projectOnLine(seg.startV(), seg.endV()).getY()<.000001; 
        for (int k=-1; k<360; k++) {
            boolean isLeft2 = findVector(k*Math.PI/180).getX()-findVector(k*Math.PI/180).projectOnLine(seg.startV(), seg.endV()).getX()<.00001;
            boolean isBellow2 = findVector(k*Math.PI/180).getY()-findVector(k*Math.PI/180).projectOnLine(seg.startV(), seg.endV()).getY()<.000001; 
            if (isBellow != isBellow2 || isLeft != isLeft2) {//found a possible intersection
                double oldAngle1 = k-1;
                double oldAngle2 = k;
                double newAngle = (oldAngle1+oldAngle2)/2; //average of previous angles
                int count = 0;//makes sure the loop doesn't go forever if there is no intersection
                while (findVector(newAngle*Math.PI/180).distanceToLine(seg.startV(), seg.endV())>.001 && count != 1000) {//while it's not good enough, get new average
                    boolean isLeft3 = findVector(newAngle*Math.PI/180).getX()<findVector(newAngle*Math.PI/180).projectOnLine(seg.startV(), seg.endV()).getX();
                    if (isLeft3 != isLeft) {
                        oldAngle2=newAngle;
                    } else {
                        oldAngle1 = newAngle;
                    }
                    newAngle = (oldAngle1+oldAngle2)/2;
                    count++;
                }
                
                //makes sure the intersection corresponds with the favored vector
                if ((seg.isLine()||findVector(newAngle*Math.PI/180).distanceToSegment(seg.startV(), seg.endV())<=.001)&&between0and1
                    (findVector(newAngle*Math.PI/180).findRatio(center.getVector().projectOnLine(seg.startV(), seg.endV()), favoredV))) {
                    return newAngle*Math.PI/180;
                }
            }
            isLeft = isLeft2;
            isBellow = isBellow2;
        }
        return -1;
    }
    
    /**
    * Return true if the given number is between 0 and 1.
    */
    public boolean between0and1(double num) {
        return 0<num&num<1;
    }
    
    /**
    * Finds the angle that corresponds to the intersection of the two circles.
    * Picks the intersection that is correctly clockwise or counterclockwise;
    */
    public double findIntersectCircleAngle(Circles c2, boolean isClockwise) {
        double angleCenter = center.getVector().angleTowards(c2.getCenter().getVector());
        boolean isLeft = findVector(0).getX()-findVector(0).projectOnCircle(c2).getX()<.00001;
        boolean isBellow = findVector(0).getY()-findVector(0).projectOnCircle(c2).getY()<.000001; 
        for (int k=1; k<360; k++) {
            boolean isLeft2 = findVector(k*Math.PI/180).getX()-findVector(k*Math.PI/180).projectOnCircle(c2).getX()<.00001;
            boolean isBellow2 = findVector(k*Math.PI/180).getY()-findVector(k*Math.PI/180).projectOnCircle(c2).getY()<.000001; 
            if (isBellow != isBellow2 || isLeft != isLeft2) { //found a possible intersection
                double oldAngle1 = k-1;
                double oldAngle2 = k;
                double newAngle = (oldAngle1+oldAngle2)/2;
                int count = 0;
                
                //while it's not good enough, get new average
                while (findVector(newAngle*Math.PI/180).distanceTo(findVector(newAngle*Math.PI/180).projectOnCircle(c2))>.001 && count != 30) {
                    boolean isLeft3 = findVector(newAngle*Math.PI/180).getX()<findVector(newAngle*Math.PI/180).projectOnCircle(c2).getX();
                    if (isLeft3 != isLeft) {
                        oldAngle2=newAngle;
                    } else {
                        oldAngle1 = newAngle;
                    }
                    newAngle = (oldAngle1+oldAngle2)/2;
                    count++;
                }
                
                //makes sure the intersection is correctly clockwise or counterclockwise.
                if (findVector(newAngle*Math.PI/180).distanceTo(findVector(newAngle*Math.PI/180).projectOnCircle(c2))<=.001) {
                    if ((newAngle*Math.PI/180>=angleCenter&&newAngle*Math.PI/180<angleCenter+Math.PI)||(newAngle*Math.PI/180<angleCenter-Math.PI)) {
                        if (isClockwise) {
                            return newAngle*Math.PI/180;
                        }
                    } else if (!isClockwise){
                        return newAngle*Math.PI/180;
                    }                    
                }
            }
            isLeft = isLeft2;
            isBellow = isBellow2;
        }
        return -1;
    }
        
    /**
    * Returns whether at least one object is invisible that the circle is dependant on.
    */
    public boolean oneDependantInvisible() {
        return (!center.getPoint().visibleProperty().getValue())||(!endPoint.getPoint().visibleProperty().getValue());
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
        
    public void setColor(Color col) {
        color = fxToAwt(col);
        circle.setStroke(col);
    }
    
    public Circle getCircle() {
        return circle;
    }
    
    public void addPointOn(Point point) {
        pointsOn.add(point);
    }
    
    public boolean isOver() {
        return circle.getStrokeWidth() == 4;
    }

    public double getRadius() {
        return radius;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Point getCenter() {
        return center;
    }

    public ArrayList<Point> getPointsOn() {
        return pointsOn;
    }
    
    public Vector findVector(double angle) {
        return (new Vector(radius*Math.cos(angle), radius*Math.sin(angle))).add(center.getVector());
    }
}
