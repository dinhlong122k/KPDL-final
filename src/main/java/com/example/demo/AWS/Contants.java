package com.example.demo.AWS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@PropertySource("classpath:application.properties")
public class Contants {


    private static final String BUCKET_NAME ="shop-udemy-course";
    private static final String REGION = "ap-southeast-1";

    public static final String S3_BASE_URI;


    static {
//        String bucketName=System.getenv("AWS_BUCKET_NAME"); //lay enviroiment AWS_BUCKET_NAME tu enviroiment cua maytinh
//        String region=System.getenv("AWS_REGION"); //lay enviroiment ReGION cua AWS, khu vuc AWS dang chon la Singapore nen mac dinh la app.south...
        String partern="https://%s.s3.%s.amazonaws.com/";
        S3_BASE_URI=BUCKET_NAME == null ? "" :String.format(partern,BUCKET_NAME,REGION);;
    }
}
