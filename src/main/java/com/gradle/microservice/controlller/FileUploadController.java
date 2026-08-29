package com.gradle.microservice.controlller;

import com.gradle.microservice.model.FileUploadModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/fileupload")
public class FileUploadController {

    private ConcurrentHashMap<String, FileUploadModel> fileData = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<FileUploadModel> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        FileUploadModel fileModel = new FileUploadModel(file.getOriginalFilename(), file.getBytes());
        fileData.put(fileModel.getFileName(), fileModel);
        return ResponseEntity.ok(fileModel);
    }


    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        FileUploadModel fileModel = fileData.get(fileName);

        if (fileModel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.getFileName() + "\"")
                .body(fileModel.getData());
    }

    @GetMapping("/all")
    public ResponseEntity<Object> listFiles() {
        return ResponseEntity.ok(fileData.keySet());
    }
}
