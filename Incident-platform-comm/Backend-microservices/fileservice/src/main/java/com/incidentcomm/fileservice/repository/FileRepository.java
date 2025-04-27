package com.incidentcomm.fileservice.repository;

import com.incidentcomm.fileservice.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    // Find files by uploader
    List<File> findByUploadedByAndIsDeletedOrderByUploadedAtDesc(Long uploadedBy, boolean isDeleted);

    // Find files by incident
    List<File> findByIncidentIdAndIsDeletedOrderByUploadedAtDesc(Long incidentId, boolean isDeleted);

    // Find files by message
    List<File> findByMessageIdAndIsDeletedOrderByUploadedAtDesc(Long messageId, boolean isDeleted);

    // Find public files
    List<File> findByIsPublicAndIsDeletedOrderByUploadedAtDesc(boolean isPublic, boolean isDeleted);

    // Find files by file type
    List<File> findByFileTypeContainingAndIsDeletedOrderByUploadedAtDesc(String fileType, boolean isDeleted);

    // Find non-deleted file by ID
    Optional<File> findByIdAndIsDeleted(Long id, boolean isDeleted);

    // Search files by name
    @Query("SELECT f FROM File f WHERE " +
            "(LOWER(f.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "f.isDeleted = false")
    Page<File> searchFiles(@Param("keyword") String keyword, Pageable pageable);

    // Find files by multiple incident IDs
    List<File> findByIncidentIdInAndIsDeletedOrderByUploadedAtDesc(List<Long> incidentIds, boolean isDeleted);

    // Find files by tag (via JOIN with FileTag)
    @Query("SELECT DISTINCT f FROM File f JOIN FileTag ft ON f.id = ft.fileId " +
            "WHERE ft.tagName = :tagName AND f.isDeleted = false " +
            "ORDER BY f.uploadedAt DESC")
    List<File> findByTagName(@Param("tagName") String tagName);

    // Find files accessible to a specific user
    @Query("SELECT DISTINCT f FROM File f LEFT JOIN FilePermission fp ON f.id = fp.fileId " +
            "WHERE f.isDeleted = false AND " +
            "((f.isPublic = true) OR " +
            "(f.uploadedBy = :userId) OR " +
            "(fp.userId = :userId AND fp.canView = true AND fp.isActive = true) OR " +
            "(fp.role IN :userRoles AND fp.canView = true AND fp.isActive = true))")
    Page<File> findAccessibleFiles(@Param("userId") Long userId,
                                   @Param("userRoles") List<String> userRoles,
                                   Pageable pageable);

    // Find files for a user, including those shared with the user
    @Query("SELECT DISTINCT f FROM File f LEFT JOIN FilePermission fp ON f.id = fp.fileId " +
            "WHERE f.isDeleted = false AND " +
            "((f.uploadedBy = :userId) OR " +
            "(fp.userId = :userId AND fp.canView = true AND fp.isActive = true))")
    Page<File> findUserFiles(@Param("userId") Long userId, Pageable pageable);
}