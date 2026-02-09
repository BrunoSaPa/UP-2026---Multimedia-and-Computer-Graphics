package up.edu.cg.core;

public class Block {
    private final Pixel[] data;

    public Block(Pixel[] data) {
        this.data = data;
    }

    public Pixel[] getData() {
        return data;
    }

    public int length() {
        return data.length;
    }
}