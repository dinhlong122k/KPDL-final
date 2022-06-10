package com.example.demo.AWS;

public class Contants {

    public static final String S3_BASE_URI;

    static {
        String bucketName=System.getenv("AWS_BUCKET_NAME"); //lay enviroiment AWS_BUCKET_NAME tu enviroiment cua maytinh
        String region=System.getenv("AWS_REGION"); //lay enviroiment ReGION cua AWS, khu vuc AWS dang chon la Singapore nen mac dinh la app.south...
        String partern="https://%s.s3.%s.amazonaws.com/";

        S3_BASE_URI=bucketName == null ? "" :String.format(partern,bucketName,region);;
    }
}
