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

    public static class Pair {

        int x;
        int y;

        Pair(int iX, int iY) {
            x = iX;
            y = iY;
        }
    }

    public static int[] convertToIntArray(byte[] input)
    {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++)
        {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }



    public static String OffsetImage(BufferedImage img ,File path , int offsetx, int offsety) {
        BufferedImage bi = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, resizeWidth, resizeHeight);
        int offsetSizeX = offsetx;
        int offsetSizeY = offsety;


        g.drawImage(img, resizeWidth / 2 - offsetSizeX, resizeHeight / 2 - offsetSizeY, null);
        g.dispose();


        //Saves image to view changes made by normalizer
        String pathString = path.getAbsolutePath();
        File savePath = new File(pathString.substring(0,pathString.length()-4) + offsetx + offsety +  ".png");
                try {
               ImageIO.write(bi, "png", savePath);
                            } catch (IOException e) {
               System.out.println("IO Exception");
                            }


        String data = "[";
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int pixel = bi.getRGB(x,y);
                if(pixel == -1){
                    pixel = 255;
                } else {
                    pixel = 0;
                }
                data = data.concat(pixel + ",");
            }
        }

        // Potentially faster way to convert however data sizes don't match
//        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//
//
//        int [] rgb = convertToIntArray(pixels);
//        for(int i : rgb) {
//         data = data.concat(i + ",");
//        }



        data = data.substring(0,data.length()-1);
        data = data.concat("],");


        return data;
    }


    static String normalize(BufferedImage img,File path) {

        int width = img.getWidth();
        int height = img.getHeight();
        int firstx = 1000;
        int firsty = 1000;
        int secondX = 0;
        int secondY = 0;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x,y);
                if (pixel != white && (pixel>>24) != 0x00) {
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


        //System.out.println("First X " + firstx + "FirstY " + firsty);
        //System.out.println("Second X " + secondX + "Second Y " + secondY);

//        BufferedImage bi = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_BYTE_BINARY);
//
//        Graphics2D g = bi.createGraphics();
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, resizeWidth, resizeHeight);
//        int offsetSizeX = (secondX - firstx) / 2;
//        int offsetSizeY = (secondY - firsty) / 2;
//
//
//        g.drawImage(img, resizeWidth / 2 - offsetSizeX, resizeHeight / 2 - offsetSizeY, null);
//        g.dispose();

        // Saves image to view changes made by normalizer
        //        try {
        //       ImageIO.write(bi, "png", path);
        //                    } catch (IOException e) {
        //       System.out.println("IO Exception");
       //                    }



//        String data = "[";
//        for (int y = 0; y < bi.getHeight(); y++) {
//            for (int x = 0; x < bi.getWidth(); x++) {
//                int pixel = bi.getRGB(x,y);
//                if(pixel == -1){
//                    pixel = 255;
//                } else {
//                    pixel = 0;
//                }
//                data = data.concat(pixel + ",");
//                }
//            }



        // Potentially faster way to convert however data sizes don't match
//        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//
//
//        int [] rgb = convertToIntArray(pixels);
//        for(int i : rgb) {
//         data = data.concat(i + ",");
//        }

        int offsetSizeX = (secondX - firstx) / 2;
        int offsetSizeY = (secondY - firsty) / 2;

         String data = OffsetImage(img,path,offsetSizeX,offsetSizeY);


         data = data.concat(OffsetImage(img,path,offsetSizeX-10,offsetSizeY));
        data = data.concat(OffsetImage(img,path,offsetSizeX+10,offsetSizeY));
        data = data.concat(OffsetImage(img,path,offsetSizeX-10,offsetSizeY+10));
        data = data.concat(OffsetImage(img,path,offsetSizeX+10,offsetSizeY-10));
        data = data.concat(OffsetImage(img,path,offsetSizeX,offsetSizeY+10));
        data = data.concat(OffsetImage(img,path,offsetSizeX,offsetSizeY-10));

        return data;

    }


    public static void main(String[] args) {

        File path = new File("C:\\Users\\Administrator\\IdeaProjects\\ImageNormalizer\\src\\Images");


        File[] files = path.listFiles();
        // Each directory corresponds to label index as each directory contains diff data
       int labelIndex = 0;
        for (File f : files) {
            if (f.isDirectory()) {
               String stringfilePath = f.getAbsolutePath().concat( "\\" + "Data" + ".js");
                File characterData = new File(stringfilePath);
                try {
                    characterData.createNewFile();
                    Path filePath= Paths.get(stringfilePath);
                    String imageData = "Const in = [";
                    String fileEnd = "\n export const DATA = {\n" +
                            "  inputs: in,\n" +
                            "}; ";

                    for (File sf : f.listFiles()) {
                       if( sf.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                           BufferedImage image = ImageIO.read(sf);
                           imageData = imageData.concat(normalize(image,sf));
                       }
                    }
                       //Remove last comma
                    imageData = imageData.substring(0,imageData.length()-1);
                    // Make sure array is closed
                     imageData = imageData.concat("];" + fileEnd);
                    //System.out.println(imageData);


                    try {
                    byte[] data = imageData.getBytes(StandardCharsets.UTF_8);
                        Files.write(filePath, data);
                    } catch (IOException e) {
                        System.out.println("Data can't be written to file" + stringfilePath);
                    }
                } catch (IOException e) {
                    System.out.println("File can't be created");
                }
                System.out.println(stringfilePath);

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
//                }
            }
        }
    }

}

