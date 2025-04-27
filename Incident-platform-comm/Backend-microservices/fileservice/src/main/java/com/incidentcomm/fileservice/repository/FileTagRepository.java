package com.incidentcomm.fileservice.repository;

import com.incidentcomm.fileservice.model.FileTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileTagRepository extends JpaRepository<FileTag, Long> {

    // Find tags for a specific file
    List<FileTag> findByFileId(Long fileId);

    // Find a specific tag for a file
    Optional<FileTag> findByFileIdAndTagName(Long fileId, String tagName);

    // Find all tags created by a user
    List<FileTag> findByCreatedBy(Long createdBy);

    // Delete all tags for a file
    void deleteByFileId(Long fileId);

    // Delete a specific tag for a file
    void deleteByFileIdAndTagName(Long fileId, String tagName);

    // Get all unique tag names
    @Query("SELECT DISTINCT ft.tagName FROM FileTag ft ORDER BY ft.tagName")
    List<String> findAllUniqueTagNames();

    // Count files by tag name
    @Query("SELECT ft.tagName, COUNT(DISTINCT ft.fileId) " +
            "FROM FileTag ft GROUP BY ft.tagName ORDER BY COUNT(DISTINCT ft.fileId) DESC")
    List<Object[]> countFilesByTag();

    // Find files with multiple tags (all tags must match)
    @Query("SELECT ft.fileId FROM FileTag ft " +
            "WHERE ft.tagName IN :tagNames " +
            "GROUP BY ft.fileId " +
            "HAVING COUNT(DISTINCT ft.tagName) = :tagCount")
    List<Long> findFileIdsWithAllTags(List<String> tagNames, long tagCount);
}