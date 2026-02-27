package up.edu.cg.model;

public class ImageRegion {

    private final Point p1;
    private final Point p2;

    public ImageRegion(Point p1, Point p2) {

        int minX = Math.min(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxX = Math.max(p1.getX(), p2.getX());
        int maxY = Math.max(p1.getY(), p2.getY());

        this.p1 = new Point(minX, minY);
        this.p2 = new Point(maxX, maxY);
    }

    public int getX1() { return p1.getX(); }
    public int getY1() { return p1.getY(); }
    public int getX2() { return p2.getX(); }
    public int getY2() { return p2.getY(); }

    public int getWidth() {
        return p2.getX() - p1.getX();
    }

    public int getHeight() {
        return p2.getY() - p1.getY();
    }
}