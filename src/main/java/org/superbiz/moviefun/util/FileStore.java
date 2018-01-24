package org.superbiz.moviefun.util;

import org.apache.tika.Tika;

import java.io.*;
import java.util.Optional;

import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        byte[] buffer = new byte[blob.inputStream.available()];
        blob.inputStream.read(buffer);

        File targetFile = new File(format("src/main/resources/%s", blob.name.toString()));
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File targetFile = new File(format("src/main/resources/%s", name));
        if(targetFile.exists()) {
            String contentType = new Tika().detect(targetFile);

            return Optional.of(new Blob(name, new FileInputStream(targetFile), contentType));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

}