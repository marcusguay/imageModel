import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;


public class main {
    static int resizeWidth = 150;
    static int resizeHeight = 150;
    static Color black = new Color(0, 0, 0);
    static int rgb = black.getRGB();
    static Color whiteC = new Color(255, 255, 255);
    static int white = whiteC.getRGB();

    public static class DataLabelPair {

        String data;
        String labels;

        DataLabelPair(String newData, String newLabel) {
            data = newData;
            labels = newLabel;
        }
    }

    public static int[] convertToIntArray(byte[] input) {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }


    public static DataLabelPair OffsetImage(BufferedImage img, File path, int offsetx, int offsety,int labelIndex) {
        BufferedImage bi = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, resizeWidth, resizeHeight);
        int offsetSizeX = offsetx;
        int offsetSizeY = offsety;


        g.drawImage(img, resizeWidth / 2 - offsetSizeX, resizeHeight / 2 - offsetSizeY, null);
        g.dispose();


        //Saves image to view changes made by normalizer
//        String pathString = path.getAbsolutePath();
//        File savePath = new File(pathString.substring(0, pathString.length() - 4) + offsetx + offsety + ".png");
//        try {
//            ImageIO.write(bi, "png", savePath);
//        } catch (IOException e) {
//            System.out.println("IO Exception");
//        }


        String data = "[";
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int pixel = bi.getRGB(x, y);
                if (pixel == -1) {
                    pixel = 255;
                } else {
                    pixel = 0;
                }
                data = data.concat(pixel + ",");
            }
        }

        // Potentially faster way to convert however data sizes don't seem to match.
//        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//
//
//        int [] rgb = convertToIntArray(pixels);
//        for(int i : rgb) {
//         data = data.concat(i + ",");
//        }


        data = data.substring(0, data.length() - 1);
        data = data.concat("],");

         String label = "";
         label = label + labelIndex + ",";

        DataLabelPair pair = new DataLabelPair(data,label);

        return pair;
    }


    static DataLabelPair normalize(BufferedImage img, File path, int labelIndex) {

        int width = img.getWidth();
        int height = img.getHeight();
        int firstx = 1000;
        int firsty = 1000;
        int secondX = 0;
        int secondY = 0;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                if (pixel != white && (pixel >> 24) != 0x00) {
                    img.setRGB(x, y, rgb);
                    if (x < firstx) {
                        firstx = x;
                    }
                    if (x > secondX) {
                        secondX = x;
                    }

                    if (y < firsty) {
                        firsty = y;
                    }
                    if (y > secondY) {
                        secondY = y;
                    }

                }
            }
        }


        int offsetSizeX = (secondX - firstx) / 2;
        int offsetSizeY = (secondY - firsty) / 2;


        DataLabelPair pair = OffsetImage(img, path, offsetSizeX, offsetSizeY,labelIndex);
        DataLabelPair pair1 = OffsetImage(img, path, offsetSizeX - 25, offsetSizeY,labelIndex);
        DataLabelPair pair2 = OffsetImage(img, path, offsetSizeX + 25, offsetSizeY,labelIndex);
        DataLabelPair pair3 = OffsetImage(img, path, offsetSizeX - 25, offsetSizeY + 25,labelIndex);
        DataLabelPair pair4 = OffsetImage(img, path, offsetSizeX + 25, offsetSizeY - 25,labelIndex);
        DataLabelPair pair5 = OffsetImage(img, path, offsetSizeX, offsetSizeY + 25,labelIndex);
        DataLabelPair pair6 = OffsetImage(img, path, offsetSizeX, offsetSizeY - 25,labelIndex);

        String data = pair.data;
        data = data.concat(pair1.data);
        data = data.concat(pair2.data);
        data = data.concat(pair3.data);
        data = data.concat(pair4.data);
        data = data.concat(pair5.data);
        data = data.concat(pair6.data);

        String label =  pair.labels;
        label = label.concat(pair1.labels);
        label = label.concat(pair2.labels);
        label = label.concat(pair3.labels);
        label = label.concat(pair4.labels);
        label = label.concat(pair5.labels);
        label = label.concat(pair6.labels);


        DataLabelPair dataLabelPair = new DataLabelPair(data,label);
        return dataLabelPair;

    }


    public static void main(String[] args) {

        File path = new File("C:\\Users\\Administrator\\IdeaProjects\\ImageNormalizer\\src\\Images");


        File[] files = path.listFiles();
        // Each directory corresponds to label index as each directory contains diff data
        String stringfilePath = path.getAbsolutePath().concat("\\" + "Data" + ".js");
        File characterData = new File(stringfilePath);
        int labelIndex = 0;

        try {
            Path filePath = Paths.get(stringfilePath);
            characterData.createNewFile();
            String imageData = "Const in = [";
            String labelData = "Const out = [";
            String correspondingChar = "Const chars = [";
            String fileEnd = "\n export const DATA = {\n" +
                    "  inputs: in,\n" +
                    "characters: chars, \n" +
                      " outputs: out,\n" +
                    "}; ";

            for (File f : files) {
                if (f.isDirectory()) {
                  correspondingChar = correspondingChar.concat(" \"" + f.getName() + "\" "+ ",");
                    for (File sf : f.listFiles()) {
                        if (sf.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                            BufferedImage image = ImageIO.read(sf);
                            DataLabelPair datapair = normalize(image, sf, labelIndex);
                            imageData = imageData.concat(datapair.data);
                            labelData = labelData.concat(datapair.labels);
                        }
                    }
                    labelIndex++;
                }
            }




                    try {
                        //Remove last comma
                        imageData = imageData.substring(0, imageData.length() - 1);
                        // Make sure array is closed
                        imageData = imageData.concat("];");
                        //System.out.println(imageData);

                        //Remove last comma
                        labelData= labelData.substring(0, labelData.length() - 1);
                        // Make sure array is closed
                        labelData = labelData.concat("];");


                        correspondingChar= correspondingChar.substring(0, correspondingChar.length() - 1);
                        // Make sure array is closed
                        correspondingChar = correspondingChar.concat("];");
                        //System.out.println(imageData);
                        String allCombined = imageData + correspondingChar +labelData + fileEnd;
                        byte[] data = allCombined.getBytes(StandardCharsets.UTF_8);
                        Files.write(filePath, data);
                    } catch (IOException e) {
                        System.out.println("Data can't be written to file" + filePath);
                    }





        } catch (IOException e) {
            System.out.println("File can't be created");
        }

        System.out.println(stringfilePath);
    }


}








               //  File file = new File(();

//                for (File sf : f.listFiles()) {
//
//                    try {
//                       BufferedImage image = ImageIO.read(sf);
//                       File outputFile = sf;
//                        ImageIO.write((main.normalize(image)), "png", outputFile);
//                    } catch (IOException e) {
//                       System.out.println("IO Exception");
//                    }
//

