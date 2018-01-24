package org.superbiz.moviefun.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client s3Client;
    private String s3BucketName;
    private String s3AccessKey;
    private String s3SecretKey;
    private String s3EndpointUrl;
    private boolean isInitialized = false;
    private final Tika tika = new Tika();

    public S3Store(String s3BucketName, String s3AccessKey, String s3SecretKey,
                   String s3EndpointUrl) {
        this.s3BucketName = s3BucketName;
        this.s3AccessKey = s3AccessKey;
        this.s3SecretKey = s3SecretKey;
        this.s3EndpointUrl = s3EndpointUrl;
    }

    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        s3Client = new AmazonS3Client(credentials);

        s3Client.setEndpoint(s3EndpointUrl);
        isInitialized = true;
    }

    public void put(Blob blob) throws IOException {
        if(!isInitialized) {
            init();
        }
        s3Client.putObject(s3BucketName, blob.name, blob.inputStream, new ObjectMetadata());
    }

    public Optional<Blob> get(String name) throws IOException {
        if(!isInitialized) {
            init();
        }
        if(s3Client.doesObjectExist(s3BucketName, name)) {
            S3Object object = s3Client.getObject(
                    new GetObjectRequest(s3BucketName, name));
            InputStream objectData = object.getObjectContent();

            byte[] bytes = IOUtils.toByteArray(objectData);

            return Optional.of(new Blob(
                    name,
                    new ByteArrayInputStream(bytes),
                    tika.detect(bytes)
            ));
        } else {
            return Optional.empty();
        }
    }

    public void deleteAll() {

    }
}
