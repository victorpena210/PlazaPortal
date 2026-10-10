package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenancePhotoRepository extends JpaRepository<MaintenancePhoto, Long> {
  List<MaintenancePhoto> findByRequestIdOrderById(Long requestId);

  long countByRequestId(Long requestId);
}
