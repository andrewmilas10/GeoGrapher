/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geo;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

/**
 *
 * @author milas6582
 */
public class geoLabel extends Label implements Serializable {
    private ArrayList<Point> points = new ArrayList<>(); 
    private double lblX, lblY, lblZ;
    private String txt;
    private boolean visible;
    private fxArc arc=new fxArc();
    
    //total factor that points were zoomed in, in an arrayList so it is pass by reference.
    private ArrayList<Double> zoom;
    
    /**
    * Constructor for label saying distance between points.
    */
    public geoLabel(Point newStart, Point newEnd, ArrayList<Double> zoom) {
        points.add(newStart);
        points.add(newEnd);
        this.zoom = zoom;
        setFont(new Font("System", 17));
        update();
    }
    
    /**
    * Constructor for label saying angle between points.
    */
    public geoLabel(Point start, Point center, Point end) {
        points.add(start);
        points.add(center);
        points.add(end);
        arc.setRadiusX(25);
        arc.setRadiusY(25);
        arc.setType(ArcType.ROUND);
        arc.setFill(Paint.valueOf("ffffff"));
        arc.setStroke(Paint.valueOf("000000"));
        setFont(new Font("System", 17));
        update();
    }
    
    public void setLabelFields() {
        lblX = getTranslateX();
        lblY = getTranslateY();
        lblZ = getTranslateZ();
        txt = getText();
        visible = visibleProperty().getValue();
    }
    
    public void useLabelFields() {
        setTranslateX(lblX);
        setTranslateY(lblY);
        setTranslateZ(lblZ);
        setText(txt);
        setVisible(visible);
        arc.setRadiusX(25);
        arc.setRadiusY(25);
        arc.setType(ArcType.ROUND);
        arc.setFill(Paint.valueOf("ffffff"));
        arc.setStroke(Paint.valueOf("000000"));
        setFont(new Font("System", 17));
        update();
    }
    
    /**
    * Updates the label based on changes in what the label is dependant on.
    */
    public void update() {
        if (points.size()==2) {//distance label
            setTranslateX((points.get(0).getX()+points.get(1).getX())/2);
            setTranslateY((points.get(0).getY()+points.get(1).getY())/2);
            setText(""+round(3, points.get(0).getVector().distanceTo(points.get(1).getVector())/10/zoom.get(0)));
            toFront();
        } else if (points.size()==3) {//angle label
            arc.setCenterX(points.get(1).getX());
            arc.setCenterY(points.get(1).getY());
            arc.setStartAngle(360-points.get(1).getVector().angleTowards(points.get(0).getVector())*180/Math.PI);
            double angle = ((points.get(1).getVector().angleTowards(points.get(2).getVector())-
                points.get(1).getVector().angleTowards(points.get(0).getVector()))*180/Math.PI+360)%360;
            if (angle<=180) {
                setText(""+round(3, angle));
                setTranslateX(points.get(0).getVector().rotate(points.get(1).getVector(), angle*Math.PI/360).getX()-5);
                setTranslateY(points.get(0).getVector().rotate(points.get(1).getVector(), angle*Math.PI/360).getY()-5);
                arc.setStartAngle(360-points.get(1).getVector().angleTowards(points.get(2).getVector())*180/Math.PI);
                arc.setLength(angle);
            } else {
                setText(""+round(3, 360-angle));
                setTranslateX(points.get(0).getVector().rotate(points.get(1).getVector(), (angle-360)*Math.PI/360).getX()-5);
                setTranslateY(points.get(0).getVector().rotate(points.get(1).getVector(), (angle-360)*Math.PI/360).getY()-5);
                arc.setStartAngle(360-points.get(1).getVector().angleTowards(points.get(0).getVector())*180/Math.PI);
                arc.setLength(360-angle);
            }
            toFront();
        }
    }
    
    /**
    * Rounds the given num the the given number of decimal places.
    */
    public double round(int place, double num) {
        return Math.round(num*Math.pow(10, place))/((double)Math.pow(10, place));
    }
    
    /**
    * Updates the label's visibility if it should be updated.
    */
    public void checkLblVisibility() {
        for (Point point: points) {
            if (!point.getPoint().visibleProperty().getValue()) {
                setVisible(false);
                arc.setVisible(false);
                return ;
            }
        }
        arc.setVisible(true);
        setVisible(true);
    }

    /**
    * Returns the arc made if the label is an angle label.
    */
    public Arc getArc() {
        return arc;
    }
}
