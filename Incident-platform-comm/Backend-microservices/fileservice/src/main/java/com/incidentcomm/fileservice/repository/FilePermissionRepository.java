package com.incidentcomm.fileservice.repository;

import com.incidentcomm.fileservice.model.FilePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FilePermissionRepository extends JpaRepository<FilePermission, Long> {

    // Find permissions for a specific file
    List<FilePermission> findByFileIdAndIsActive(Long fileId, boolean isActive);

    // Find user-specific permissions for a file
    Optional<FilePermission> findByFileIdAndUserIdAndIsActive(Long fileId, Long userId, boolean isActive);

    // Find role-based permissions for a file
    Optional<FilePermission> findByFileIdAndRoleAndIsActive(Long fileId, String role, boolean isActive);

    // Find all permissions for a user
    List<FilePermission> findByUserIdAndIsActive(Long userId, boolean isActive);

    // Find all permissions for a role
    List<FilePermission> findByRoleAndIsActive(String role, boolean isActive);

    // Deactivate all permissions for a file
    @Modifying
    @Transactional
    @Query("UPDATE FilePermission fp SET fp.isActive = false WHERE fp.fileId = :fileId")
    int deactivateAllPermissionsForFile(@Param("fileId") Long fileId);

    // Check if a user has specific permission
    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FilePermission fp " +
            "WHERE fp.fileId = :fileId AND fp.userId = :userId AND fp.isActive = true " +
            "AND (fp.canView = true OR fp.canEdit = true OR fp.canDelete = true OR fp.canDownload = true)")
    boolean hasAnyPermission(@Param("fileId") Long fileId, @Param("userId") Long userId);

    // Check if a user has view permission
    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FilePermission fp " +
            "WHERE fp.fileId = :fileId AND " +
            "((fp.userId = :userId AND fp.canView = true) OR " +
            "(fp.role IN :roles AND fp.canView = true)) AND " +
            "fp.isActive = true")
    boolean hasViewPermission(@Param("fileId") Long fileId,
                              @Param("userId") Long userId,
                              @Param("roles") List<String> roles);

    // Check if a user has download permission
    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FilePermission fp " +
            "WHERE fp.fileId = :fileId AND " +
            "((fp.userId = :userId AND fp.canDownload = true) OR " +
            "(fp.role IN :roles AND fp.canDownload = true)) AND " +
            "fp.isActive = true")
    boolean hasDownloadPermission(@Param("fileId") Long fileId,
                                  @Param("userId") Long userId,
                                  @Param("roles") List<String> roles);

    // Check if a user has edit permission
    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FilePermission fp " +
            "WHERE fp.fileId = :fileId AND " +
            "((fp.userId = :userId AND fp.canEdit = true) OR " +
            "(fp.role IN :roles AND fp.canEdit = true)) AND " +
            "fp.isActive = true")
    boolean hasEditPermission(@Param("fileId") Long fileId,
                              @Param("userId") Long userId,
                              @Param("roles") List<String> roles);

    // Check if a user has delete permission
    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FilePermission fp " +
            "WHERE fp.fileId = :fileId AND " +
            "((fp.userId = :userId AND fp.canDelete = true) OR " +
            "(fp.role IN :roles AND fp.canDelete = true)) AND " +
            "fp.isActive = true")
    boolean hasDeletePermission(@Param("fileId") Long fileId,
                                @Param("userId") Long userId,
                                @Param("roles") List<String> roles);

    // Delete expired permissions
    @Modifying
    @Transactional
    @Query("DELETE FROM FilePermission fp WHERE fp.expiresAt < :now")
    int deleteExpiredPermissions(@Param("now") LocalDateTime now);
}