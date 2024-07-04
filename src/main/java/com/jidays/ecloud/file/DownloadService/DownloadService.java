package com.jidays.ecloud.file.DownloadService;

import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.file.FileService.FileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DownloadService {
    @Autowired
    FileSystemService fileSystemService;
    private final Map<String, EFile> tmp_tokens = new ConcurrentHashMap<>();

    public String createToken(EFile file) {
        String token = java.util.UUID.randomUUID().toString();
        tmp_tokens.put(token, file);
        return token;
    }

    public EFile useToken(String token) {
        EFile file = tmp_tokens.get(token);
        if (file != null) {
            tmp_tokens.remove(token);
        }
        return file;
    }

    public InputStreamResource getFileResource(EFile file) {
        if (file == null) {
            return null;
        }
        try {
            return fileSystemService.getFileResource(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
