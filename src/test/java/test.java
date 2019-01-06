import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class test {

    private final Path RESOURCES = Paths.get("src", "test", "resources");

    @Test
    public void test() throws IOException {

        File testPDF = RESOURCES.resolve("SBC-Template.pdf").toFile();
        File convertedImage = RESOURCES.resolve("pdf-to-jpg.jpg").toFile();
        File guassianImage = RESOURCES.resolve("guassian.jpg").toFile();
        File sobelImage = RESOURCES.resolve("sobel.jpg").toFile();

        // pdf to jpg
        BufferedImage image = convertToImage(testPDF, convertedImage, 1);

        // guassian blur
        BufferedImage guassian = guassianConvolution(image);
        ImageIO.write(guassian, "jpg", guassianImage);

        // sobel edge detection
        BufferedImage sobel = sobelConvolution(guassian);
        ImageIO.write(sobel, "jpg", sobelImage);
    }

    private BufferedImage convertToImage(File testPDF, File convertedImage, int page
    ) throws IOException {
        PDDocument document = null;
        try {

            document = PDDocument.load(testPDF);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(page, 3, ImageType.GRAY);

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

    public BufferedImage guassianConvolution(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                int guassValue = convolveGuassianPixel(image, x, y);
                filteredImage.setRGB(x, y, guassValue);
            }
        }

        return filteredImage;
    }

    public int convolveGuassianPixel(BufferedImage image, int x, int y) {

        int[][] guassian = Kernel.GUASSIAN_3x3.getKernel();

        int width = image.getWidth(),
                height = image.getHeight(),
                kernelWidth = guassian.length,
                kernelHeight = guassian[0].length;

        int gValue = 0;
        for (int i = 0; i < kernelWidth; i++) {

            int iX = x + 1 - i;
            for (int j = 0; j < kernelHeight; j++) {

                int iY = y + 1 - j;
                gValue += (iX >= 0 && iX < width) && (iY >= 0 && iY < height) ? image.getRGB(iX, iY) * guassian[(kernelWidth - 1) - i][(kernelHeight -1) - j] : 0;
            }
        }

        int d = (kernelWidth * kernelWidth);
        gValue = gValue / d;

        return gValue;
    }

    public BufferedImage sobelConvolution(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                int sobelValue = convolveSobelPixel(image, x, y);
                if (getGreyScale(sobelValue) > 0) {
                    filteredImage.setRGB(x, y, Color.white.getRGB()); // just set every edge to white
                }
            }
        }

        return filteredImage;
    }

    public int convolveSobelPixel(BufferedImage image, int x, int y) {

        int[][] sobelH = Kernel.SOBEL_HORIZONTAL_3x3.getKernel(),
                sobelV = Kernel.SOBEL_VERTICAL_3x3.getKernel();

        int width = image.getWidth(),
                height = image.getHeight(),
                kernelWidth = sobelH.length,
                kernelHeight = sobelH[0].length;

        int gx = 0;
        int gy = 0;
        for (int i = 0; i < kernelWidth; i++) {

            int iX = x + 1 - i;
            for (int j = 0; j < kernelHeight; j++) {

                int iY = y + 1 - j;
                gx += (iX >= 0 && iX < width) && (iY >= 0 && iY < height) ? image.getRGB(iX, iY) * sobelH[(kernelWidth - 1) - i][(kernelHeight -1) - j] : 0;
                gy += (iX >= 0 && iX < width) && (iY >= 0 && iY < height) ? image.getRGB(iX, iY) * sobelV[(kernelWidth - 1) - i][(kernelHeight -1) - j] : 0;
            }
        }

        int d = (kernelWidth * kernelWidth);
        gx = gx / d;
        gy = gy / d;

        return (int) Math.sqrt((gx * gx) + (gy * gy));
    }

    public enum Kernel {

        SOBEL_HORIZONTAL_3x3,
        SOBEL_VERTICAL_3x3,
        GUASSIAN_3x3;

        // note, rows here are columns in the matrix
        private int[][] SOBELV_3X3 = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        private int[][] SOBELH_3X3 = {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };
        private int[][] GUASS_3X3 = {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };

        public int[][] getKernel() {

            if (SOBEL_VERTICAL_3x3.equals(this)){
                return SOBELV_3X3;
            } else if (SOBEL_HORIZONTAL_3x3.equals(this)) {
                return SOBELH_3X3;
            } else if (GUASSIAN_3x3.equals(this)) {
                return GUASS_3X3;
            }
            return null;
        }
    }

    private int getGreyScale(int rgb) {

        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        // from https://en.wikipedia.org/wiki/Grayscale - calculating luminance
        int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

        return gray;
    }
}
