package com.example.demo.api;

import com.example.demo.Common.Response;
import com.example.demo.service.HashService;
import com.example.demo.service.LSBService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class Rest {

    @Autowired
    private HashService service;

    @Autowired
    private LSBService lsbService;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json",value = "/encryptText")
    public ResponseEntity<Response> HashText(@RequestBody Response text) throws Exception{
        Response response=new Response();
        try{
            response.setText(service.HashText(text.getText()));

            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
//
//    @RequestMapping(value = "decryptText",method = RequestMethod.POST, produces = "application/json")
//    public ResponseEntity<Response> getDecryptText(@RequestBody String hashText) throws Exception{
//        Response response=new Response();
//
//        try{
//            String text_decrypted=service.getTextDecrypt(hashText);
//            response.setText(text_decrypted);
//            return new ResponseEntity<>(response,HttpStatus.OK);
//        }catch (Exception e){
//            e.printStackTrace();
//            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
//        }
//    }

    @RequestMapping(value = "decryptTextPV",method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Response> getDecryptTextWithPrivate_Key(@RequestBody Response hashText) throws Exception{
        Response response=new Response();

        try{
            String text_decrypted=service.getTextDecryptPrivate_key(hashText.getText());
            response.setText(text_decrypted);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/wtfile",method = RequestMethod.POST,produces = "application/json")
    public Response addWatermarkFile(@RequestParam("file") MultipartFile multiPartFile) throws IOException {
        Response response=new Response();

        try{
            String ext1= FilenameUtils.getExtension(multiPartFile.getOriginalFilename());
//            AmazoneS3Util.uploadFile("thcs",multiPartFile.getOriginalFilename(),multiPartFile.getInputStream());
            if(ext1.equals("jpg") || ext1.equals("png") || ext1.equals("jpeg")){
                String url= service.addWaterMarkImage(multiPartFile);
                response.setText(url);
            }

            else if(ext1.equals("pdf")){
                service.addWatermarkPDF(multiPartFile);
            }else{
                response.setText("Not correct extension");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/encryptLsb",method = RequestMethod.POST,produces = "application/json",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response EncryptMessageImage(HttpServletRequest request) throws Exception{
        Response response=new Response();
        try{
//            int[] bits=lsbService.bit_Msg("body.getText()");
//            System.out.println("msg in file: " + "body.getText()");
//            BufferedImage buff= lsbService.readImage(multipartFile);
//            lsbService.hideTheMessage(bits,buff);




            // ----------------- LSB 2---------------------------//
            String text =request.getParameter("text");
            MultipartHttpServletRequest mulrequest= (MultipartHttpServletRequest) request;
            MultipartFile multipartFile= mulrequest.getFile("file");
            response.setText(lsbService.embedMessage(text,multipartFile));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return  response;
    }

    @RequestMapping(value = "/decryptLsb",method = RequestMethod.POST,produces = "application/json")
    public Response DecryptMessageImageEmbedded(@RequestParam("file") MultipartFile multipartFile) throws Exception{
        Response response=new Response();
        try{
//            int[] bits=lsbService.bit_Msg("body.getText()");
//            System.out.println("msg in file: " + "body.getText()");
//            BufferedImage buff= lsbService.readImage(multipartFile);
//            lsbService.hideTheMessage(bits,buff);

            // ----------------- LSB 2---------------------------//
            String text =lsbService.decodeMessage(multipartFile);
            response.setText(text);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return  response;
    }
}
