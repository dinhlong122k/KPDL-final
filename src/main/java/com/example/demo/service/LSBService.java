package com.example.demo.service;

import com.example.demo.AWS.AmazoneS3Util;
import com.example.demo.AWS.Contants;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class LSBService {

//    public BufferedImage readImage(MultipartFile multipartFile){
//        BufferedImage buff =null;
//        try{
//            buff = ImageIO.read(multipartFile.getInputStream());
//
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//        return buff;
//    }
//
//    public int[] bit_Msg(String msg){
//        int j=0;
//        int[] b_msg=new int[msg.length()*8];
//        for(int i=0;i<msg.length();i++){
//            int x=msg.charAt(i);
//            String x_s=Integer.toBinaryString(x);
//            while(x_s.length()!=8){
//                x_s='0'+x_s;
//            }
//            System.out.println("dec value for "+x +" is "+x_s);
//
//            for(int i1=0;i1<8;i1++) {
//                b_msg[j] = Integer.parseInt(String.valueOf(x_s.charAt(i1)));
//                j++;
//            };
//        }
//
//        return b_msg;
//    }
//
//    public void hideTheMessage (int[] bits, BufferedImage theImage) throws Exception {
//        File file=new File("C:\\result.jpg");
//        BufferedImage sten_img = null;
//        int bit_l = bits.length / 8;
//        int[] bl_msg = new int[8];
//        System.out.println("bit lent " + bit_l);
//        String bl_s = Integer.toBinaryString(bit_l);
//        while (bl_s.length() != 8) {
//            bl_s = '0' + bl_s;
//        }
//        for (int i1 = 0; i1 < 8; i1++) {
//            bl_msg[i1] = Integer.parseInt(String.valueOf(bl_s.charAt(i1)));
//        }
//        ;
//        int j = 0;
//        int b = 0;
//        int currentBitEntry = 8;
//
//        for (int x = 0; x < theImage.getWidth(); x++) {
//            for (int y = 0; y < theImage.getHeight(); y++) {
//                if (x == 0 && y < 8) {
//                    int currentPixel = theImage.getRGB(x, y);
//                    int ori = currentPixel;
//                    int red = currentPixel >> 16;
//                    red = red & 255;
//                    int green = currentPixel >> 8;
//                    green = green & 255;
//                    int blue = currentPixel;
//                    blue = blue & 255;
//                    String x_s = Integer.toBinaryString(blue);
//                    String sten_s = x_s.substring(0, x_s.length() - 1);
//                    sten_s = sten_s + Integer.toString(bl_msg[b]);
//
//                    //j++;
//                    int temp = Integer.parseInt(sten_s, 2);
//                    int s_pixel = Integer.parseInt(sten_s, 2);
//                    int a = 255;
//                    int rgb = (a << 24) | (red << 16) | (green << 8) | s_pixel;
//                    theImage.setRGB(x, y, rgb);
//                    //System.out.println("original "+ori+" after "+theImage.getRGB(x, y));
//                    ImageIO.write(theImage, "png", file);
//                    b++;
//
//                } else if (currentBitEntry < bits.length + 8) {
//
//                    int currentPixel = theImage.getRGB(x, y);
//                    int ori = currentPixel;
//                    int red = currentPixel >> 16;
//                    red = red & 255;
//                    int green = currentPixel >> 8;
//                    green = green & 255;
//                    int blue = currentPixel;
//                    blue = blue & 255;
//                    String x_s = Integer.toBinaryString(blue);
//                    String sten_s = x_s.substring(0, x_s.length() - 1);
//                    sten_s = sten_s + Integer.toString(bits[j]);
//                    j++;
//                    int temp = Integer.parseInt(sten_s, 2);
//                    int s_pixel = Integer.parseInt(sten_s, 2);
//
//                    int a = 255;
//                    int rgb = (a << 24) | (red << 16) | (green << 8) | s_pixel;
//                    theImage.setRGB(x, y, rgb);
//                    ImageIO.write(theImage, "png",file );
//
//                    currentBitEntry++;
//                }
//            }
//        }
//
//    }




    // -------------------------------- LSB 2 ENCRYPT----------------------------------//
    BufferedImage sourceImage= null;
    BufferedImage embeddedImage=null;

    public String embedMessage(String message, MultipartFile multipartFile) throws IOException {
        String name="";
        String url="";
        try{
            sourceImage= ImageIO.read(multipartFile.getInputStream());
            embeddedImage= sourceImage.getSubimage(0,0,sourceImage.getWidth(),sourceImage.getHeight());

            embeddedMessage(embeddedImage,message);
            name =save(embeddedImage,multipartFile);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        url = Contants.S3_BASE_URI + "thcs_lsb/" + name;
        return url;
    }

    private void embeddedMessage(BufferedImage img, String message){
        int messageLength = message.length();

        int imageWidth = img.getWidth(), imageHeight = img.getHeight(),
                imageSize =imageWidth * imageHeight;

        embeddedInteger(img, messageLength, 0 ,0);

        byte b[] = message.getBytes();

        for(int i=0;i<b.length; i++){
            embeddedByte(img, b[i], i* 8 +32,0 );
        }
    }

    private void embeddedByte(BufferedImage img, byte b, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight(),
                startX = start/maxY, startY = start - startX*maxY, count=0;
        for(int i=startX; i<maxX && count<8; i++) {
            for(int j=startY; j<maxY && count<8; j++) {
                int rgb = img.getRGB(i, j), bit = getBitValue(b, count);
                rgb = setBitValue(rgb, storageBit, bit);
                img.setRGB(i, j, rgb);
                count++;
            }
        }
    }

    private void embeddedInteger(BufferedImage img, int n, int start, int storageBit){
        int maxX = img.getWidth(), maxY = img.getHeight(),
                startX =start/maxY, startY =start- startX* maxY, count =0;

        for(int i=startX; i< maxX && count<32; i++){
            for(int j=startY; j<maxY && count<32 ;j++){
                int rgb =img.getRGB(i,j), bit =getBitValue(n, count);
                rgb =setBitValue(rgb, storageBit, bit);
                img.setRGB(i, j, rgb);
                count++;
            }
        }
    }

    private int getBitValue(int n, int location) {
        int v = n & (int) Math.round(Math.pow(2, location));
        return v==0?0:1;
    }

    private int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location), bv = getBitValue(n, location);
        if(bv == bit)
            return n;
        if(bv == 0 && bit == 1)
            n |= toggle;
        else if(bv == 1 && bit == 0)
            n ^= toggle;
        return n;
    }

    private String save(BufferedImage img, MultipartFile multipartFile) throws IOException {
        String fileName ="";
        try{
            fileName= AmazoneS3Util.generateFileName(multipartFile.getOriginalFilename(),"thcs");
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ImageIO.write(img,"png",baos);
            InputStream is=new ByteArrayInputStream(baos.toByteArray());

            AmazoneS3Util.uploadFile("thcs_lsb",fileName,is);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return fileName;
    }


    //---------------------------- LSB 2 DECRYPT -------------------------------------

    public String decodeMessage(MultipartFile multipartFile) throws IOException {
        String messageEmbedded =null;
        try{
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            int len = extractInteger(image, 0, 0);
            byte b[] = new byte[len];
            for(int i=0; i<len; i++)
                b[i] = extractByte(image, i*8+32, 0);
            messageEmbedded= new String(b);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return messageEmbedded;
    }

    private int extractInteger(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight(),
                startX = start/maxY, startY = start - startX*maxY, count=0;
        int length = 0;
        for(int i=startX; i<maxX && count<32; i++) {
            for(int j=startY; j<maxY && count<32; j++) {
                int rgb = img.getRGB(i, j), bit = getBitValue(rgb, storageBit);
                length = setBitValue(length, count, bit);
                count++;
            }
        }
        return length;
    }

    private byte extractByte(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight(),
                startX = start/maxY, startY = start - startX*maxY, count=0;
        byte b = 0;
        for(int i=startX; i<maxX && count<8; i++) {
            for(int j=startY; j<maxY && count<8; j++) {
                int rgb = img.getRGB(i, j), bit = getBitValue(rgb, storageBit);
                b = (byte)setBitValue(b, count, bit);
                count++;
            }
        }
        return b;
    }

}
