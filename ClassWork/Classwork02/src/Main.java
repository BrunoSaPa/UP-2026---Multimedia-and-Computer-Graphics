import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//image.setRGB(x,y, Color.blue.getRGB());

class Point{
    private final int x;
    private final int y;

    public Point(int x, int y){
        this.y = y;
        this.x=x;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}

class Color{
    private final int r;
    private final int g;
    private final int b;

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    public int toRGB() {
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
}

class BarycentricCoordinates {
    private final double d1;
    private final double d2;
    private final double d3;

    public BarycentricCoordinates(double d1, double d2, double d3) {
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
    }

    public double getD1() { return d1; }
    public double getD2() { return d2; }
    public double getD3() { return d3; }

    public boolean isInsideTriangle() {
        return d1 >= 0 && d2 >= 0 && d3 >= 0;
    }
}


class ColoredVertex{
    private final Point point;
    private final Color color;

    public ColoredVertex(Point point, Color color) {
        this.point = point;
        this.color = color;
    }

    public Point getPoint() { return point; }
    public Color getColor() { return color; }
}

class Triangle{
    private final ColoredVertex v1;
    private final ColoredVertex v2;
    private final ColoredVertex v3;

    public Triangle(ColoredVertex v1, ColoredVertex v2, ColoredVertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public boolean containsPoint(int x, int y) {
        BarycentricCoordinates coords = calculateBarycentricCoordinates(x, y);
        return coords.isInsideTriangle();
    }

    public Color interpolateColor(int x, int y) {
        BarycentricCoordinates coords = calculateBarycentricCoordinates(x, y);

        int r = (int)(coords.getD1() * v2.getColor().getR() + coords.getD2() * v3.getColor().getR() + coords.getD3() * v1.getColor().getR());
        int g = (int)(coords.getD1() * v2.getColor().getG() + coords.getD2() * v3.getColor().getG() + coords.getD3() * v1.getColor().getG());
        int b = (int)(coords.getD1() * v2.getColor().getB() + coords.getD2() * v3.getColor().getB() + coords.getD3() * v1.getColor().getB());

        return new Color(r, g, b);
    }

    private BarycentricCoordinates calculateBarycentricCoordinates(int x, int y) {
        Point p1 = v1.getPoint();
        Point p2 = v2.getPoint();
        Point p3 = v3.getPoint();
        double areaMainTriangle = ((p3.getY() - p1.getY()) * (p2.getX() - p1.getX())) + ((p1.getX() - p3.getX()) * (p2.getY() - p1.getY()));

        double d1 = (((p3.getY() - p1.getY()) * (x - p1.getX())) + ((p1.getX() - p3.getX()) * (y - p1.getY()))) / areaMainTriangle;
        double d2 = (((p1.getY() - p2.getY()) * (x - p1.getX())) + ((p2.getX() - p1.getX()) * (y - p1.getY()))) / areaMainTriangle;
        double d3 = 1 - d1 - d2;

        return new BarycentricCoordinates(d1, d2, d3);
    }
}

class TriangleRenderer {
    private final int width;
    private final int height;
    private final BufferedImage image;

    public TriangleRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }

    public void render(Triangle triangle) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (triangle.containsPoint(x, y)) {
                    Color color = triangle.interpolateColor(x, y);
                    image.setRGB(x, y, color.toRGB());
                }
            }
        }
    }

    public void saveToFile(String filename) {
        File outputImage = new File(filename);
        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class Main{
    public static void main(String[] args) {
        int scalar = 2000;
        int height = 1*scalar;
        int width = 1*scalar;

        ColoredVertex blue = new ColoredVertex(new Point(width / 2, 0), new Color(0, 0, 255));
        ColoredVertex red = new ColoredVertex(new Point(0, height), new Color(255, 0, 0));
        ColoredVertex green = new ColoredVertex(new Point(width, height), new Color(0, 255, 0));
        Triangle triangle = new Triangle(blue, red, green);
        TriangleRenderer renderer = new TriangleRenderer(width, height);
        renderer.render(triangle);
        renderer.saveToFile("triangle2.jpg");
    }
}