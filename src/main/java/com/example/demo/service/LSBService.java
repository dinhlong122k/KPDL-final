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

    // -------------------------------- LSB 2 ENCRYPT----------------------------------//
    BufferedImage sourceImage= null;
    BufferedImage embeddedImage=null;

    public String embedMessage(String message, MultipartFile multipartFile) throws IOException {
        String name="";
        String url="";
        try{

            //chuyển hình ảnh thành giá trị bit
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

        //lấy giá trị thông tin hình ảnh
        int imageWidth = img.getWidth(), imageHeight = img.getHeight(),
                imageSize =imageWidth * imageHeight;

        //chuyen hinh anh sang dang bit
        embeddedInteger(img, messageLength, 0 ,0);


        //chuyen chuỗi cần giấu tin sang dạng bytes
        byte b[] = message.getBytes();

        for(int i=0;i<b.length; i++){

            //lay ra gia tri của chuỗi từ vị trí i*8 +32(vì 32 kí tự đầu dành cho độ dài của chuỗi)
            embeddedByte(img, b[i], i* 8 +32,0 );
        }
    }


    //thay byte của message vào ảnh
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


    //đưa độ dài của chuỗi vào trong bức ảnh
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

    //kiem tra neu như giá trị =0 thì trả về bit là 0, ngược lại trả về bit là 1
    private int getBitValue(int n, int location) {
        int v = n & (int) Math.round(Math.pow(2, location));  //bitwise AND (convert n- độ dài chuỗi sang bit và math_round) sau đó so sánh để gán giá trị là 0 hoặc 1
        return v==0?0:1;
    }

    private int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location), bv = getBitValue(n, location);
        if(bv == bit)
            return n;
        if(bv == 0 && bit == 1)
            n |= toggle; //set n là 1
        else if(bv == 1 && bit == 0)
            n ^= toggle; //set n là 0
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

    public String decodeMessage(BufferedImage image) throws IOException{
        String messageEmbedded =null;
        try{
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


    //lấy ra độ dài của chuỗi
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


    //lấy ra các byte chuỗi
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
