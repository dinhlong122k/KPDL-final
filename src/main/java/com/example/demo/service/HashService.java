package com.example.demo.service;


import com.example.demo.AWS.AmazoneS3Util;
import com.example.demo.AWS.Contants;
import com.example.demo.Common.Text;
import com.example.demo.Utils.RSAUtils;
import com.example.demo.repository.TextRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.*;
import java.util.Base64;

@Service
@Transactional
public class HashService {

//   @Autowired
//   private TextHashRepository repo;

   @Autowired
   private TextRepository textRepo;
    public static String algorithm = "RSA";

    @Transactional
    public String HashText(String text) throws Exception {
        String encryptString = "";
        try {
            Text textObject =new Text();

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);

            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(512, secureRandom);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            System.out.println("Public Key: " + publicKeyString);


            PrivateKey privateKey = keyPair.getPrivate();
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            System.out.println("Private key: " + privateKeyString);

            String encryptionString = Base64.getEncoder().encodeToString(RSAUtils.encrypt(text,publicKeyString));

            encryptString=encryptionString;

            textObject.setPrivate_key(privateKeyString);
            textObject.setHash_text(encryptionString);
            textObject.setText_hash(text);
            textRepo.insert(textObject);

        } catch (Exception ex) {
            System.out.println("Loi");
        }

        return encryptString;
    }


    @Transactional
    public String getTextDecrypt(String hash_text) throws Exception{
        String decrypt = "";
        try{
            decrypt=textRepo.getText(hash_text);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return decrypt;
    }

    @Transactional
    public String getTextDecryptPrivate_key(String hash_text) throws Exception{
        String decrypt = "";
        try{
            Text text=textRepo.getPrivate_Key(hash_text);
            decrypt=RSAUtils.decrypt(text.getHash_text(),text.getPrivate_key());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return decrypt;
    }


    @Transactional
    public String addWaterMarkImage(MultipartFile multipartFile) throws IOException {
        String url="";
        String fileName="";
        try{
            InputStream ip=multipartFile.getInputStream();
            Image img= ImageIO.read(ip);

            int imgWidth =img.getWidth(null);
            int imgHeight=img.getHeight(null);

            BufferedImage bufImg = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);
            Graphics graphics=bufImg.getGraphics();
            graphics.drawImage(img,0,0,null);
            graphics.setFont(new Font("Arial", Font.BOLD, imgWidth/30));
            graphics.setColor(new Color(255,0,0));

            String watermark = "@COPYRIGHT BY GROUP 2";

            graphics.drawString(watermark, imgWidth / 2 , imgHeight -20);
            graphics.dispose();

            fileName=AmazoneS3Util.generateFileName(multipartFile.getOriginalFilename(),"thcs");
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ImageIO.write(bufImg,"png",baos);
            InputStream is=new ByteArrayInputStream(baos.toByteArray());

            AmazoneS3Util.uploadFile("thcs_watermark",fileName,is);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        url = Contants.S3_BASE_URI + "thcs_watermark/" + fileName;
        return url;
    }

    @Transactional
    public void addWatermarkPDF(MultipartFile multipartFile) throws IOException{
        try{
//           Document document=new Document();
//
//            String path = ":/sample.pdf";
//
//           PdfWriter pdfWriter=PdfWriter.getInstance(document, new FileOutputStream(path));
//
//           pdfWriter.setPageEvent(new MyPdfPageEventHelper());


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
