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

        double area = 0;
        double perimeter = 0;

        switch (option) {

            case 1:
                System.out.print("Side length: ");
                Square square = new Square(sc.nextDouble());
                area = square.getArea();
                perimeter = square.getPerimeter();
                break;

            case 2:
                System.out.print("Width: ");
                double w = sc.nextDouble();
                System.out.print("Height: ");
                double h = sc.nextDouble();
                Rectangle rectangle = new Rectangle(w, h);
                area = rectangle.getArea();
                perimeter = rectangle.getPerimeter();
                break;

            case 3:
                System.out.print("Side length: ");
                Triangle triangle = new Triangle(sc.nextDouble());
                area = triangle.getArea();
                perimeter = triangle.getPerimeter();
                break;

            case 4:
                System.out.print("Radius: ");
                Circle circle = new Circle(sc.nextDouble());
                area = circle.getArea();
                perimeter = circle.getPerimeter();
                break;

            case 5:
                System.out.print("Side length: ");
                Pentagon pentagon = new Pentagon(sc.nextDouble());
                area = pentagon.getArea();
                perimeter = pentagon.getPerimeter();
                break;

            case 6:
                System.out.print("Radius: ");
                SemiCircle semiCircle = new SemiCircle(sc.nextDouble());
                area = semiCircle.getArea();
                perimeter = semiCircle.getPerimeter();
                break;

            default:
                System.out.println("Invalid option");
                sc.close();
                return;
        }

        System.out.println("\nResults:");
        System.out.println("Perimeter: " + perimeter);
        System.out.println("Area: " + area);

        sc.close();
    }
}

//classes
class Square {
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

class Rectangle {
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

class Triangle { // equilateral
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

class Circle {
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

class Pentagon {
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

class SemiCircle {
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
