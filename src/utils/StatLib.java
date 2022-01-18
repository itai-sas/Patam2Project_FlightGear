package utils;


public class StatLib {

    // simple average
    public static float avg(float[] x){
        float sum =0;
        for(int i=0;i<x.length;i++)
            sum+= x[i];

        return (sum/x.length);
    }

    // returns the variance of X
    public static float var(float[] x){

        float mean= avg(x);
        float sqDiff = 0;
        for(int i=0;i<x.length;i++)
            sqDiff+= (x[i]-mean)*(x[i]-mean);
        return sqDiff / x.length;

    }

    // returns the covariance of X and Y
    public static float cov(float[] x, float[] y){
        float meanX= avg(x);
        float meanY = avg(y);
        float tmp =0;
        for(int i=0;i<x.length;i++)
            tmp+= (x[i]-meanX)*(y[i]-meanY);

        return tmp/x.length;
    }


    // returns the Pearson correlation coefficient of X and Y
    public static float pearson(float[] x, float[] y){
        return (float) (cov(x,y)/(Math.sqrt(var(x))*Math.sqrt(var(y))));
    }

    // performs a linear regression and returns the line equation
    public static Line linearReg(Point[] points){
        float[] arrayX = new float[points.length];
        float[] arrayY = new float[points.length];
        for(int i=0;i<points.length;i++) {
            arrayX[i] = points[i].x;
            arrayY[i] = points[i].y;
        }

        float a = cov(arrayX,arrayY)/var(arrayX);
        float b = avg(arrayY) - (a*avg(arrayX));
        return new Line(a,b);
    }

    // returns the deviation between point p and the line equation of the points
    public static float dev(Point p,Point[] points){
        Line examinedLine = linearReg(points);
        float LineYPoint = examinedLine.f(p.x);
        return Math.abs(LineYPoint-p.y);

    }

    // returns the deviation between point p and the line
    public static float dev(Point p,Line l){
        return Math.abs(l.f(p.x)-p.y);
    }

}
