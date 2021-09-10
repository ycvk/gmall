package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    //  获取文件上传对应的地址
    @Value("${minio.endpointUrl}")
    public String endpointUrl;

    @Value("${minio.accessKey}")
    public String accessKey;

    @Value("${minio.secreKey}")
    public String secreKey;

    @Value("${minio.bucketName}")
    public String bucketName;


    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//  准备获取到上传的文件路径！
        String url = "";

        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        // MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(endpointUrl)
                        .credentials(accessKey, secreKey)
                        .build();
        // 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (isExist) {
            System.out.println("Bucket already exists.");
        } else {
            // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
        //  定义一个文件的名称 : 文件上传的时候，名称不能重复！
        String fileName = System.currentTimeMillis() + UUID.randomUUID().toString();
        // 使用putObject上传一个文件到存储桶中。
        //  minioClient.putObject("asiatrip","asiaphotos.zip", "/home/user/Photos/asiaphotos.zip");
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
        //  System.out.println("/home/user/Photos/asiaphotos.zip is successfully uploaded as asiaphotos.zip to `asiatrip` bucket.");
        //  文件上传之后的路径： http://39.99.159.121:9000/gmall/xxxxxx
        url = endpointUrl + "/" + bucketName + "/" + fileName;

        System.out.println("url:\t" + url);
        //  将文件上传之后的路径返回给页面！


        return Result.ok(url);
    }

}