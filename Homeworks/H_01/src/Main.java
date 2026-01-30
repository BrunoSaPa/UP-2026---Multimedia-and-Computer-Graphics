import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Select a shape:");
        System.out.println("1 Square");
        System.out.println("2 Rectangle");
        System.out.println("3 Triangle");
        System.out.println("4 Circle");
        System.out.println("5 Pentagon");
        System.out.println("6 semi circle");
        System.out.print("Option: ");

        int option = sc.nextInt();

        Shape shape = null;

        switch (option) {

            case 1:
                System.out.print("Side length: ");
                shape = new Square(sc.nextDouble());
                break;

            case 2:
                System.out.print("Width: ");
                double w = sc.nextDouble();
                System.out.print("Height: ");
                double h = sc.nextDouble();
                shape = new Rectangle(w, h);
                break;

            case 3:
                System.out.print("Side length: ");
                shape = new Triangle(sc.nextDouble());
                break;

            case 4:
                System.out.print("Radius: ");
                shape = new Circle(sc.nextDouble());
                break;

            case 5:
                System.out.print("Side length: ");
                shape = new Pentagon(sc.nextDouble());
                break;

            case 6:
                System.out.print("Radius: ");
                shape = new SemiCircle(sc.nextDouble());
                break;

            default:
                System.out.println("Invalid option");
                sc.close();
                return;
        }

        System.out.println("\nResults:");
        System.out.println("Perimeter: " + shape.getPerimeter());
        System.out.println("Area: " + shape.getArea());

        sc.close();
    }
}

//base class
abstract class Shape {
    abstract double getArea();
    abstract double getPerimeter();
}

//shapes
class Square extends Shape {
    double side;

    Square(double side) {
        this.side = side;
    }

    double getArea() {
        return side * side;
    }

    double getPerimeter() {
        return 4 * side;
    }
}

class Rectangle extends Shape {
    double width, height;

    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    double getArea() {
        return width * height;
    }

    double getPerimeter() {
        return 2 * (width + height);
    }
}

class Triangle extends Shape { // equilateral
    double side;

    Triangle(double side) {
        this.side = side;
    }

    double getArea() {
        return (Math.sqrt(3) / 4) * side * side;
    }

    double getPerimeter() {
        return 3 * side;
    }
}

class Circle extends Shape {
    double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    double getArea() {
        return Math.PI * radius * radius;
    }

    double getPerimeter() {
        return 2 * Math.PI * radius;
    }
}

class Pentagon extends Shape {
    double side;

    Pentagon(double side) {
        this.side = side;
    }

    double getArea() {
        return (5 * side * side) / (4 * Math.tan(Math.PI / 5));
    }

    double getPerimeter() {
        return 5 * side;
    }
}

class SemiCircle extends Shape {
    double radius;

    SemiCircle(double radius) {
        this.radius = radius;
    }

    double getArea() {
        return 0.5 * Math.PI * radius * radius;
    }

    double getPerimeter() {
        return Math.PI * radius + 2 * radius;
    }
}
