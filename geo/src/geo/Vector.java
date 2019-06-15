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
public class Vector implements Serializable {
    private double x;
    private double y;
    
    /**
    * Constructor. Sets up the vector's coordinates.
    */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
    * Returns whether the vector is equal (ignoring rounding issues) with another.
    */
    public boolean equalTo(Vector v2) {
        return (Math.abs(v2.getX()-x)<.0001&&Math.abs(v2.getY()-y)<.0001);
    }
    
    /**
    * Returns the vector found by adding this one to another.
    */
    public Vector add(Vector v2) {
        return new Vector(x+v2.getX(), y+v2.getY());
    }
    
    /**
    * Returns the vector found by subtracting another vector from this one.
    */
    public Vector sub(Vector v2) {
        return new Vector(x-v2.getX(), y-v2.getY());
    }
    
    /**
    * Returns the vector found by scaling this vector by a constant.
    */
    public Vector scale(double k) {
        return new Vector(x*k, y*k);
    }
    
    /**
    * Returns the dot product of this vector with another one.
    */
    public double dot(Vector v2) {
        return x*v2.getX()+y*v2.getY();
    }
    
    /**
    * Returns the vector found by projecting this one to another.
    */
    public Vector projectOn(Vector v2) {
        return v2.scale(dot(v2)/v2.dot(v2));
    }
    
    /**
    * Returns the vector found by projecting this one to the line between two given vectors.
    */
    public Vector projectOnLine(Vector v2, Vector v3) {
        return (sub(v2).projectOn(v3.sub(v2))).add(v2);
    }
    
    /**
    * Returns the vector found by projecting this one to the segment between two given vectors.
    * Places the projection on the endpoint if it isn't on the segment.
    */
    public Vector projectOnSegment(Vector v2, Vector v3) {
        Vector proj = projectOnLine(v2, v3);
        double ratio = proj.findRatio(v2, v3);
        if (ratio <0) {
            ratio = 0;
        } else if (ratio >1) {
            ratio = 1;
        }
        return findPoint(v2, v3, ratio);
    }
    
    /**
    * Returns the vector found by projecting this one to the segment between two given vectors.
    * Returns null if it isn't on the segment.
    */
    public Vector projectOnSegmentStrict(Vector v2, Vector v3) {
        Vector proj = projectOnLine(v2, v3);
        if(proj.findRatio(v2, v3)<0||proj.findRatio(v2, v3)>1) {
            return null;
        }
        return proj;
    }
    
    /**
    * Returns an arrayList of two vectors that are the intersections of the line between the
    * two vectors and the boundaries of the screen. Return null if there isn't an intersection.
    */
    public ArrayList<Vector> getBoundaryIntersections(Vector v2, Vector v3) {
        ArrayList<Vector> vectors = new ArrayList<>();
        if ((v3.sub(v2).getY()==0)) {
            vectors.add(new Vector(0, v2.getY()));
            vectors.add(new Vector(826, v2.getY()));
            return vectors;
        } else if ((v3.sub(v2).getX()==0)) {
            vectors.add(new Vector(v2.getX(), 0));
            vectors.add(new Vector(v2.getX(), 703));
            return vectors;
        }
        double yInt = v2.getY()+(-1*v2.getX())/(v3.getX()-v2.getX())*(v3.getY()-v2.getY());
        double xInt = v2.getX()+(-1*v2.getY())/(v3.getY()-v2.getY())*(v3.getX()-v2.getX());
        if (yInt<0&&xInt<0) {
            return null;
        } else if (yInt>0&&xInt>0) {
            vectors.add(new Vector(0, yInt));
            vectors.add(new Vector(xInt, 0));
        } else if (yInt<0&&xInt>0) {
            vectors.add(new Vector(xInt, 0));
            double y2 = v2.getY()+(826-1*v2.getX())/(v3.getX()-v2.getX())*(v3.getY()-v2.getY());
            vectors.add(new Vector(826, y2));            
        } else if (yInt>0&&xInt<0) {
            vectors.add(new Vector(0, yInt));
            double x2 = v2.getX()+(703-1*v2.getY())/(v3.getY()-v2.getY())*(v3.getX()-v2.getX());
            vectors.add(new Vector(x2, 703));
        } else {
            return null;
        }
        return vectors;
    }
    
    /**
    * Returns the distance of the vector to the line between the given vectors.
    */
    public double distanceToLine(Vector v2, Vector v3) {
        return (sub(projectOnLine(v2, v3))).getMagnitude();
    }
    
    /**
    * Returns the distance of the vector to the segment between the given vectors.
    */
    public double distanceToSegment(Vector v2, Vector v3) {
        return (sub(projectOnSegment(v2, v3))).getMagnitude();
    }
    
    /**
    * Returns the ratio of the start vector to this vector over the start vector to the end vector.
    */
    public double findRatio(Vector start, Vector end) {
        if (end.sub(start).getX()!=0) {
            return sub(start).getX()/end.sub(start).getX();
        }
        return sub(start).getY()/end.sub(start).getY();
    }
    
    /**
    * Returns the vector on the line between the two vector with the given vector.
    */
    public Vector findPoint(Vector start, Vector end, double ratio) {
        return end.sub(start).scale(ratio).add(start);
    }
    
    /**
    * Returns the vector that is the intersection of the two segments.
    * Returns null it the segments don't intersect.
    */
    public Vector findIntersection(Segment s1, Segment s2) {
        double a=s1.startV().getX(),b=s1.endV().getX()-a,c=s2.startV().getX(),d=s2.endV().getX()-c;
        double e=s1.startV().getY(),f=s1.endV().getY()-e,g=s2.startV().getY(),h=s2.endV().getY()-g;
        
        if (d*f-h*b == 0) {
            return null;
        }
        double k = ((a-c)*f+(g-e)*b)/(d*f-h*b);
        double t;
        if (b != 0) {
            t = (c+d*k-a)/b;
        } else if (f!=0) {
            t = (g+h*k-e)/f;
        } else {
            return null;
        }
        
        if ((!s2.isLine()&&(k<0||k>1))||(!s1.isLine()&&(t<0||t>1))) {
            return null;
        }
        return (new Vector(a+b*t, e+f*t));
    }
    
    /**
    * Returns the magnitude of the vector.
    */
    public double getMagnitude() {
        return Math.sqrt(x*x+y*y);
    }

    /**
    * Returns the y-coordinate of the given vector.
    */
    public double getY() {
        return y;
    }

    /**
    * Returns the x-coordinate of the given vector
    */
    public double getX() {
        return x;
    }
    
    /**
    * Returns distance between this vector to the given vector.
    */
    public double distanceTo(Vector v2) {
        return sub(v2).getMagnitude();
    }
    
    /**
    * Return slope of the vector. Return null if the slope is undefined.
    */
    public Double getSlope() {
        if (x==0) {
            return null;
        }
        return y/x;
    }
    
    /**
    * Returns projection of the vector on the given circle.
    */
    public Vector projectOnCircle(Circles c) {
        return c.findVector(c.getCenter().getVector().angleTowards(this));
    }
    
    /**
    * Returns the angle between the given vector, this one, and the x-axis.
    */
    public double angleTowards(Vector v2) {
        if (sub(v2).getSlope() == null) {
            if (v2.getY()>=y) {
                return Math.PI/2;
            }
            return 3*Math.PI/2;
        }
        double arctan = Math.atan(sub(v2).getSlope());
        if (v2.getX()<x) {
            return ((arctan+Math.PI*3)%(2*Math.PI));
        }
        return (arctan+2*Math.PI)%(2*Math.PI);
    }
    
    /**
    * returns the vector found by rotating this one around the given center by the given angle.
    */
    public Vector rotate(Vector center, double angle) {
        ComplexNumber c = new ComplexNumber(center.getX(), center.getY());
        ComplexNumber num = ComplexNumber.subtract(new ComplexNumber(getX(), getY()), c);
        return ComplexNumber.add(ComplexNumber.multiply(ComplexNumber.divide(num, new ComplexNumber(num.mod()/50, 0)), 
            new ComplexNumber(Math.cos(angle), Math.sin(angle))), c).toVector();
    } 
}
