package up.edu.cg.task3;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.println("super simple aspect ratio calculator :)");

        System.out.print("enter width of the image in pixels");
        int width = scanner.nextInt();
        System.out.print("enter height of the image in pixels");
        int height = scanner.nextInt();

        int gcd = gcd(width, height);

        int aspectWidth = width / gcd;
        int aspectHeight = height / gcd;

        System.out.println("aspect ratio: " + aspectWidth + ":" + aspectHeight);

        scanner.close();
    }

    public static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}