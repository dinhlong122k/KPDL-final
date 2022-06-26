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

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);  //gán thuật toán RSA keypair

            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(1024, secureRandom); //sau khi gán keypair ta khởi tạo keypair
            //RSA hỗ trợ 1024, 2048,4096 cho key size

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic(); //lấy ra key public từ keypair
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded()); // chuyển từ public key sang string
            System.out.println("Public Key: " + publicKeyString);


            PrivateKey privateKey = keyPair.getPrivate(); // lay ra key private
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded()); // chuyen tu private key sang string
            System.out.println("Private key: " + privateKeyString);

            String encryptionString = Base64.getEncoder().encodeToString(RSAUtils.encrypt(text,publicKeyString)); //encode chuỗi cần mã hoá

            encryptString=encryptionString;

            textObject.setPrivate_key(privateKeyString);
            textObject.setHash_text(encryptionString);// set giá trị cho textObject lưu vào CSDL
            textObject.setText_hash(text);
            textRepo.insert(textObject); //lưu vào csdl

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
            InputStream ip=multipartFile.getInputStream(); //chuyển từ file multipartfile sang image
            Image img= ImageIO.read(ip);

            int imgWidth =img.getWidth(null);
            int imgHeight=img.getHeight(null);

            BufferedImage bufImg = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);//chuyển đổi từ ảnh sang dạng data để xử lý
            Graphics graphics=bufImg.getGraphics();
            graphics.drawImage(img,0,0,null); //định nghĩa vị trí bắt đầu của ảnh
            graphics.setFont(new Font("Arial", Font.BOLD, imgWidth/30)); //set font watermark
            graphics.setColor(new Color(255,0,0)); //set color watermark

            String watermark = "@COPYRIGHT BY GROUP 2";

            graphics.drawString(watermark, imgWidth / 2 , imgHeight -20); //vẽ text vào ảnh
            graphics.dispose();

            fileName=AmazoneS3Util.generateFileName(multipartFile.getOriginalFilename(),"thcs"); //sinh ra tên ảnh để upload
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ImageIO.write(bufImg,"png",baos); //chuyển ảnh đã được vẽ thành inpustream để upload
            InputStream is=new ByteArrayInputStream(baos.toByteArray());

            AmazoneS3Util.uploadFile("thcs_watermark",fileName,is);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        url = Contants.S3_BASE_URI + "thcs_watermark/" + fileName;
        return url;
    }
}
