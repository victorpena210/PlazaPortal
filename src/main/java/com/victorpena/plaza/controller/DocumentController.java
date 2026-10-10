package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.DocumentService;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {
  private final DocumentService documents;

  public DocumentController(DocumentService documents) {
    this.documents = documents;
  }

  @GetMapping("/documents/{id}")
  public ResponseEntity<Resource> download(@PathVariable Long id) {
    var d = documents.readable(id);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(d.getOriginalName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .header("X-Content-Type-Options", "nosniff")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .contentType(MediaType.APPLICATION_PDF)
        .body(new FileSystemResource(documents.path(d)));
  }
}
