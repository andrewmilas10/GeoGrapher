/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author milas6582
 */
public class Lines extends Segment implements Serializable {
    
    /**
    * Constructor. Sets up the endpoints and the line that is displayed on screen.
    */
    public Lines(Point newStart, Point newEnd) {
        super(newStart, newEnd);
        update();
    }
    
    /**
    * Returns true whether the segment is a full line.
    */
    public boolean isLine() {
        return true;
    }
    
    /**
    * Updates the segment based on changes in the endpoints.
    * Then updates all objects that are dependant on this segment.
    */
    public void update() {
        if (bisectorPoints.size() != 0) {//the line is an angle bisector
            double newAngle =(bisectorPoints.get(1).getVector().angleTowards(bisectorPoints.get(2).getVector()) -
                bisectorPoints.get(1).getVector().angleTowards(bisectorPoints.get(0).getVector())+2*Math.PI)%(2*Math.PI)/2;
            if ((angle<=Math.PI&&angle>Math.PI/2&&newAngle<Math.PI/4)||(angle<Math.PI/2&&angle>=0&&newAngle>3*Math.PI/4)) {
                for (Point point: pointsOn) {
                    point.reverseRatio();
                }
            }
            angle = newAngle;
            Vector image = bisectorPoints.get(0).getVector().rotate(bisectorPoints.get(1).getVector(), angle);
            end = new Point(image.getX(), image.getY());
        }
        if (parallelPoint != null) {//the line is a parallel line
            Vector image = parallelPoint.getVector().add(parallelSegment.endV().sub(parallelSegment.startV()));
            end = new Point(image.getX(), image.getY());
        }
        if (perpendicularPoint != null) {//the line is a perpendicular line
            Vector image;
            if (perpendicularSegment.isLine()) {
                image = perpendicularPoint.getVector().projectOnLine(perpendicularSegment.startV(), perpendicularSegment.endV());
            } else {
                image = perpendicularPoint.getVector().projectOnSegmentStrict(perpendicularSegment.startV(), perpendicularSegment.endV());
            }
            if (image != null) {
                if (image.equalTo(perpendicularSegment.startV())) {
                    image = perpendicularSegment.endV().rotate(image, Math.PI/2);
                } else {
                    image = perpendicularSegment.startV().rotate(image, Math.PI/2);
                }
                end = new Point(image.getX(), image.getY());
                setVisibility(true);
            } else {
                setVisibility(false);
            }
        }
        
        //sets the line's endpoints to be on the screen's boundaries.
        ArrayList<Vector> vectors = (new Vector(0, 0)).getBoundaryIntersections(start.getVector(), end.getVector());
        if (vectors == null) {
            line.setStartX(-1);
            line.setStartY(-1);
            line.setEndX(-1);
            line.setEndY(-1);
        } else {
            line.setStartX(vectors.get(0).getX());
            line.setStartY(vectors.get(0).getY());
            line.setEndX(vectors.get(1).getX());
            line.setEndY(vectors.get(1).getY()); 
        }
        //updates objects dependant on this line.
        for (Point point: pointsOn) {
            if (!point.getDisableUpdate()) {
                point.update();
            }
        }
        for (Segment seg: parallels) {
            seg.update();
        }
        for (Segment seg: perpendiculars) {
            seg.update();
        }
    }
}
