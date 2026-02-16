

//<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
//
//  <polygon points="0,0 800,600 800,0" style="fill:red"></polygon>
//  <polygon points="0,0 0,600 800,600" style="fill:blue"></polygon>
//</svg>

import java.io.FileWriter;
import java.io.IOException;

//i did not fully understood if you wanted us to write the files like this, but i made my vectors in https://editsvgcode.com/
public class Main{
    static void main(String[] args) {

        //first
        String svgContent = """
            <svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
                <polygon points="0,0 800,600 800,0" style="fill:red"/>
                <polygon points="0,0 0,600 800,600" style="fill:blue"/>
            </svg>
            """;

        try (FileWriter writer = new FileWriter("output.svg")) {
            writer.write(svgContent);
            System.out.println("SVG file created.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //second file
        String svgContent2 = """
            <svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
            <rect width="800" height="600" fill="white"></rect>
            <line x1="150" y1="00" x2="150" y2="240" stroke-width="2" stroke="red"></line>
            <line x1="30" y1="120" x2="270" y2="120" stroke-width="2" stroke="red"></line>
            <line x1="70" y1="50" x2="230" y2="200" stroke-width="2" stroke="red"></line>
            <line x1="70" y1="190" x2="230" y2="50" stroke-width="2" stroke="red"></line>
            <path
              d="M 0,400
                C 50,400 50,320 100,320
                C 150,320 150,400 200,400
                C 250,400 250,320 300,320
                C 350,320 350,400 400,400
                C 450,400 450,320 500,320
                C 550,320 550,400 600,400
                C 650,400 650,320 700,320
                C 750,320 750,400 800,400"
              fill="green"
              stroke="green"
              stroke-width="2"
            />
            <rect width="800" height="200" fill="green" y="400" ></rect>

            <circle r="80" cx="150" cy="120" fill="yellow"></circle>
            </svg>
            """;

        try (FileWriter writer = new FileWriter("output2.svg")) {
            writer.write(svgContent2);
            System.out.println("SVG file created.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
