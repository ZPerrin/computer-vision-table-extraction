import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class test {

    private final Path RESOURCES = Paths.get("src", "test", "resources");

    @Test
    public void test() throws IOException {

        File testPDF = RESOURCES.resolve("SBC-Template.pdf").toFile();
        File convertedImage = RESOURCES.resolve("pdf-to-jpg.jpg").toFile();
        File guassianImage = RESOURCES.resolve("guassian.jpg").toFile();
        File sobelImage = RESOURCES.resolve("sobel.jpg").toFile();

        BufferedImage image = convertToImage(testPDF, convertedImage);


        BufferedImage guassian = new Kernel(KernelType.GUASSIAN).filter(image);
        ImageIO.write(guassian, "jpg", guassianImage);
        BufferedImage sobel = new Kernel(KernelType.SOBEL_VERTICAL).filter(guassian);
        ImageIO.write(sobel, "jpg", sobelImage);
    }

    private BufferedImage convertToImage(File testPDF, File convertedImage
    ) throws IOException {
        PDDocument document = null;
        try {

            document = PDDocument.load(testPDF);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(1, 2, ImageType.GRAY);

            ImageIO.write(image, "jpg", convertedImage);

            return image;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return null;
    }


    public enum KernelType {
        SOBEL_HORIZONTAL,
        SOBEL_VERTICAL,
        GUASSIAN;
    }

    public class Kernel {

        private int[][] kernel = new int[3][3];
        private KernelType type = null;

        public Kernel(KernelType type) {

            this.type = type;
            if (KernelType.SOBEL_VERTICAL.equals(type)) {

                kernel[0][0] = -1;
                kernel[0][1] = 0;
                kernel[0][2] = 1;
                kernel[1][0] = -2;
                kernel[1][1] = 0;
                kernel[1][2] = 2;
                kernel[2][0] = -1;
                kernel[2][1] = 0;
                kernel[2][2] = 1;
            } else if (KernelType.SOBEL_HORIZONTAL.equals(type)) {
                kernel[0][0] = -1;
                kernel[0][1] = -2;
                kernel[0][2] = 1;
                kernel[1][0] = 0;
                kernel[1][1] = 0;
                kernel[1][2] = 0;
                kernel[2][0] = -1;
                kernel[2][1] = 2;
                kernel[2][2] = 1;
            } else if (KernelType.GUASSIAN.equals(type)) {
                kernel[0][0] = 1;
                kernel[0][1] = 2;
                kernel[0][2] = 1;
                kernel[1][0] = 2;
                kernel[1][1] = 4;
                kernel[1][2] = 2;
                kernel[2][0] = 1;
                kernel[2][1] = 2;
                kernel[2][2] = 1;
            }
        }

        public BufferedImage filter(BufferedImage original) {

            int width = original.getWidth();
            int height = original.getHeight();
            BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            Set<Integer> values = new HashSet<>();
            //if (KernelType.SOBEL_VERTICAL.equals(type) || KernelType.SOBEL_HORIZONTAL.equals(type)) {
            int max = 0;

            for (int x = 0; x < original.getWidth(); x++) {

                for (int y = 0; y < original.getHeight(); y++) {

                    int value = 0;
                    //            System.out.println("(" + x + ", " + y + ")");

                    // first column
                    if (x - 1 >= 0) {
                        value = value + kernel[1][0] * original.getRGB(x - 1, y);
                    }

                    if (x - 1 >= 0 && y - 1 >= 0) {
                        value = value + kernel[0][0] * original.getRGB(x - 1, y - 1);
                    }

                    if (x - 1 >= 0 && y + 1 < original.getHeight()) {
                        value = value + kernel[2][0] * original.getRGB(x - 1, y + 1);
                    }

                    // second column
                    if (y - 1 >= 0) {
                        value = value + kernel[0][1] * original.getRGB(x, y - 1);
                    }

                    value = value + kernel[1][1] * original.getRGB(x, y);

                    if (y + 1 < original.getHeight()) {
                        value = value + kernel[2][1] * original.getRGB(x, y + 1);
                    }

                    // third column
                    if (x + 1 < original.getWidth()) {
                        value = value + kernel[1][2] * original.getRGB(x + 1, y);
                    }

                    if (x + 1 < original.getWidth() && y - 1 >= 0) {
                        value = value + kernel[0][2] * original.getRGB(x + 1, y - 1);
                    }

                    if (x + 1 < original.getWidth() && y + 1 < original.getHeight()) {
                        value = value + kernel[2][2] * original.getRGB(x + 1, y + 1);
                    }

                    // average
                    value = value / 9;
                    if (value >= max) {
                        max = value;
                    }

                    if (KernelType.SOBEL_HORIZONTAL.equals(type) || KernelType.SOBEL_VERTICAL.equals(type)) {
                        if (value > 0) {
                            values.add(value);
                        }
                       // System.out.println(value & 0xFF);

                        filteredImage.setRGB(x, y, value);

                    } else {
                        filteredImage.setRGB(x, y, value);
                    }
                }
            }
            //}

            System.out.println(max);
            List<Integer> test = new ArrayList(values);
            Collections.sort(test);
            for (Integer i : test) {
                System.out.println(i);
            }
                return filteredImage;
        }
    }
}
