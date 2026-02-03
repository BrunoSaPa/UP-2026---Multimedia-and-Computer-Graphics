package up.edu.cg.task4;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("chose convertion system");
        System.out.println("1 polar to cartesian");
        System.out.println("2 cartesian to polar");
        int choice = sc.nextInt();

        if (choice == 1) {
            System.out.print("radius: ");
            double r = sc.nextDouble();

            System.out.print("angle in degrees");
            double thetaDeg = sc.nextDouble();
            double thetaRad = Math.toRadians(thetaDeg);
            double x = r * Math.cos(thetaRad);
            double y = r * Math.sin(thetaRad);

            System.out.println("cartesian coords");
            System.out.println("x = " + x);
            System.out.println("y = " + y);

        } else if (choice == 2) {
            System.out.print("x: ");
            double x = sc.nextDouble();

            System.out.print("y: ");
            double y = sc.nextDouble();

            double r = Math.sqrt(x * x + y * y);
            double thetaRad = Math.atan2(y, x);
            double thetaDeg = Math.toDegrees(thetaRad);

            System.out.println("polar coordinates:");
            System.out.println("r = " + r);
            System.out.println("theta = " + thetaDeg + " degrees");

        } else {
            System.out.println("Invalid option.");
        }

        sc.close();
    }
}