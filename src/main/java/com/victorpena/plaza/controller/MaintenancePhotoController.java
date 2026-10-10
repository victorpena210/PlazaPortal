package com.victorpena.plaza.controller;

import com.victorpena.plaza.service.MaintenancePhotoService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MaintenancePhotoController {
  private final MaintenancePhotoService photos;

  public MaintenancePhotoController(MaintenancePhotoService photos) {
    this.photos = photos;
  }

  @PostMapping("/tenant/maintenance/{id}/photos")
  public String upload(@PathVariable Long id, @RequestParam MultipartFile file)
      throws java.io.IOException {
    photos.upload(id, file);
    return "redirect:/tenant/dashboard";
  }

  @GetMapping("/maintenance-photos/{id}")
  @ResponseBody
  public ResponseEntity<Resource> photo(@PathVariable Long id) {
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header("X-Content-Type-Options", "nosniff")
        .body(new FileSystemResource(photos.readable(id)));
  }
}
