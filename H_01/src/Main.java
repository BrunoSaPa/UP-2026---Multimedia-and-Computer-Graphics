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

            case 1: //square
                System.out.print("Side length: ");
                double side = sc.nextDouble();
                perimeter = 4 * side;
                area = side * side;
                break;

            case 2: //rectangle
                System.out.print("Width: ");
                double width = sc.nextDouble();
                System.out.print("Height: ");
                double height = sc.nextDouble();
                perimeter = 2 * (width + height);
                area = width * height;
                break;

            case 3://triangle
                System.out.print("Side length: ");
                double tSide = sc.nextDouble();
                perimeter = 3 * tSide;
                area = (Math.sqrt(3) / 4) * tSide * tSide;
                break;

            case 4://circle
                System.out.print("Radius: ");
                double radius = sc.nextDouble();
                perimeter = 2 * Math.PI * radius;
                area = Math.PI * radius * radius;
                break;

            case 5://pentagon
                System.out.print("Side length: ");
                double pSide = sc.nextDouble();
                perimeter = 5 * pSide;
                area = (5 * pSide * pSide) / (4 * Math.tan(Math.PI / 5));
                break;

            case 6: //semi circle
                System.out.print("Radius: ");
                double r = sc.nextDouble();

                area = 0.5 * Math.PI * r * r;
                //this is an arc (half perimeter) plus the diameter
                perimeter = Math.PI * r + 2 * r;
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
