package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaintenancePhotoService {
  private final Path root;
  private final MaintenancePhotoRepository photos;
  private final MaintenanceRequestRepository requests;
  private final AccessService access;
  private final NotificationService mail;

  public MaintenancePhotoService(
      @Value("${app.upload-dir:./data/uploads}") String directory,
      MaintenancePhotoRepository photos,
      MaintenanceRequestRepository requests,
      AccessService access,
      NotificationService mail) {
    this.root = Path.of(directory).toAbsolutePath().normalize();
    this.photos = photos;
    this.requests = requests;
    this.access = access;
    this.mail = mail;
  }

  @Transactional
  public void upload(Long requestId, MultipartFile file) throws IOException {
    var r = requests.lockById(requestId).orElseThrow(AccessService::missing);
    if (!r.getUser().getId().equals(access.user().getId())) throw AccessService.missing();
    if (file.isEmpty()
        || file.getSize() > 5 * 1024 * 1024
        || photos.countByRequestId(requestId) >= 5)
      throw new IllegalArgumentException("Add up to five photos, each 5 MB or smaller.");
    byte[] input = file.getBytes();
    java.awt.image.BufferedImage image;
    try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new IllegalArgumentException("Choose a JPEG or PNG image.");
      var reader = readers.next();
      try {
        reader.setInput(stream);
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        if (!Set.of("jpeg", "jpg", "png").contains(format)
            || ((long) reader.getWidth(0)) * reader.getHeight(0) > 20000000)
          throw new IllegalArgumentException(
              "Choose a JPEG or PNG photo no larger than 20 megapixels.");
        image = reader.read(0);
      } finally {
        reader.dispose();
      }
    }
    if (image == null) throw new IllegalArgumentException("The photo could not be read.");
    var rgb =
        new java.awt.image.BufferedImage(
            image.getWidth(), image.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
    var graphics = rgb.createGraphics();
    graphics.setColor(java.awt.Color.WHITE);
    graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    var photo = new MaintenancePhoto();
    photo.setRequest(r);
    photo.setStorageKey(UUID.randomUUID() + ".jpg");
    photo.setOriginalName("maintenance-photo.jpg");
    Files.createDirectories(root);
    Path target = root.resolve(photo.getStorageKey());
    try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
      if (!ImageIO.write(rgb, "jpg", out)) throw new IOException("Unable to save photo.");
    }
    org.springframework.transaction.support.TransactionSynchronizationManager
        .registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
              @Override
              public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED)
                  try {
                    Files.deleteIfExists(target);
                  } catch (IOException ignored) {
                  }
              }
            });
    photos.save(photo);
    mail.owners(
        r.getOffice().getProperty().getCompany(),
        "Maintenance photo added",
        "Request " + requestId + " has a new photo. " + mail.url("/admin/maintenance"));
  }

  public Path readable(Long id) {
    var p = photos.findById(id).orElseThrow(AccessService::missing);
    var r = p.getRequest();
    if (!r.getUser().getId().equals(access.user().getId()))
      access.member(r.getOffice().getProperty().getCompany().getId());
    var path = root.resolve(p.getStorageKey()).normalize();
    if (!path.startsWith(root)) throw AccessService.missing();
    return path;
  }
}
