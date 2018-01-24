package org.superbiz.moviefun.albums;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.util.Blob;
import org.superbiz.moviefun.util.BlobStore;
import org.superbiz.moviefun.util.FileStore;
import org.superbiz.moviefun.util.S3Store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    @Value("${s3.endpointUrl}") String s3EndpointUrl;
    @Value("${s3.accessKey}") String s3AccessKey;
    @Value("${s3.secretKey}") String s3SecretKey;
    @Value("${s3.bucketName}") String s3BucketName;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }

    public BlobStore blobStore() {
        return new S3Store(s3BucketName, s3AccessKey, s3SecretKey, s3EndpointUrl);
//        return new FileStore();
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, albumId);

        return format("redirect:/albums/%s", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Blob blob = getFile(albumId);
        File targetFile;
        HttpHeaders headers;
        byte[] imageBytes;
        if(blob == null) {
            targetFile = new File("src/main/resources/default-cover.jpg");
            imageBytes = readAllBytes(targetFile.toPath());;
            headers = createImageHttpHeaders(targetFile.toPath(), imageBytes);
        } else {
            imageBytes = IOUtils.toByteArray(blob.inputStream);

            headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(blob.contentType));
            headers.setContentLength(imageBytes.length);

            return new HttpEntity<>(imageBytes, headers);
        }

        return new HttpEntity<>(imageBytes, headers);

    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, long albumId) throws IOException {
        BlobStore fileStore = blobStore();
        fileStore.put(
            new Blob(Long.toString(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType())
        );
    }

    private Blob getFile(@PathVariable long albumId) throws IOException {
        BlobStore fileStore = blobStore();
        return fileStore.get(Long.toString(albumId)).orElse(null);
    }

}
