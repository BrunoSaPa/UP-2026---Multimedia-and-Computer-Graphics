import up.edu.cg.compression.Decompressor;
import up.edu.cg.compression.VectorQuantizer;
import up.edu.cg.core.Block;
import up.edu.cg.core.ImageData;
import up.edu.cg.io.ImageIOUtil;

void main() throws IOException {

    ImageData img = ImageIOUtil.load("input2.png");

    Block[] blocks = img.toBlocks(4);

    VectorQuantizer vq = new VectorQuantizer();
    vq.train(blocks, 256);

    byte[] indices = vq.encode(blocks);

    ImageData reconstructed =
            Decompressor.reconstruct(
                    img.getWidth(),
                    img.getHeight(),
                    4,
                    vq.getCodebook(),
                    indices
            );

    ImageIOUtil.save(reconstructed, "output2.jpg");
}
