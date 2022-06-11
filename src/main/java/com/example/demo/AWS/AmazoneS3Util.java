package com.example.demo.AWS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AmazoneS3Util {

    // private static final String BUCKET_NAME ="shop-udemy-course";
    // private static final String REGION = "ap-southeast-1";

    private static final String BUCKET_NAME;
    private static final String REGION;
    private static final Logger logger= LoggerFactory.getLogger(AmazoneS3Util.class);

    static{
        BUCKET_NAME=System.getenv("AWS_BUCKET_NAME");
        REGION=System.getenv("AWS_REGION");
    }

    public static List<String> listObject(String folderName){
        S3Client client=S3Client.builder().build(); //khoi tao Server client ket noi voi AmazonS3 with access key, Secret key

        ListObjectsRequest listObjectsRequest=ListObjectsRequest.builder().bucket(BUCKET_NAME).prefix(folderName).build(); //request to the server

        ListObjectsResponse listObjectsResponse=client.listObjects(listObjectsRequest);

        List<S3Object> contents = listObjectsResponse.contents();

        ListIterator<S3Object> listIterator=contents.listIterator();

        List<String> listkeys=new ArrayList<>();

        while(listIterator.hasNext()){
            S3Object object=listIterator.next();
            listkeys.add(object.key());
        }

        return listkeys;
    }


    public static void uploadFile(String folderName, String fileName, InputStream inputStream){
        S3Client client=S3Client.builder().build();

        PutObjectRequest request=PutObjectRequest.builder().
                bucket(BUCKET_NAME).key(folderName + "/" +fileName).acl("public-read").build();
        try{
            int contentLength = inputStream.available();
            client.putObject(request, RequestBody.fromInputStream(inputStream,contentLength));
        }catch (IOException ex){
            logger.error("Could not upload file to Amazon S3",ex);
        }
    }

    public static void deleteFile(String fileName){
        S3Client client=S3Client.builder().build();

        DeleteObjectRequest request=DeleteObjectRequest.builder().bucket(BUCKET_NAME)
                .key(fileName).build();

        client.deleteObject(request);

    }

    public static void removeFolder(String folderName){
        S3Client client=S3Client.builder().build();
        ListObjectsRequest listObjectsRequest=ListObjectsRequest.builder().bucket(BUCKET_NAME).prefix(folderName+"/").build();
        ListObjectsResponse response=client.listObjects(listObjectsRequest);
        List<S3Object> contents=response.contents();

        ListIterator<S3Object> listIterator=contents.listIterator();

        while (listIterator.hasNext()){
            S3Object object=listIterator.next();
            DeleteObjectRequest deleteObjectRequest=DeleteObjectRequest.builder().bucket(BUCKET_NAME)
                    .key(object.key()).build();
            client.deleteObject(deleteObjectRequest);
        }

    }

    public static String generateFileName(String fileName,String type){

        //lay file type cua tep multipartFile
        String token= UUID.randomUUID().toString();
        return type + "." + new Date().getTime()+ "." +token.replace("-","") +"." +fileName;
    }
}
