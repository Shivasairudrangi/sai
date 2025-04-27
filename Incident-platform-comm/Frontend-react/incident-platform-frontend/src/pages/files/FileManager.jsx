import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import fileService from '../../services/fileService';
import { useAuth } from '../../utils/AuthContext';

function FileManager() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [tags, setTags] = useState([]);
  const [selectedTag, setSelectedTag] = useState('');
  const [uploading, setUploading] = useState(false);
  const { currentUser } = useAuth();

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        setLoading(true);
        const filesData = await fileService.getFiles();
        setFiles(filesData);
        
        // Fetch tags
        const tagsData = await fileService.getTags();
        setTags(tagsData);
      } catch (err) {
        console.error('Error fetching files:', err);
        setError('Failed to load files. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchFiles();
  }, []);

  const handleFileUpload = async (e) => {
    const uploadFiles = e.target.files;
    if (!uploadFiles.length) return;

    try {
      setUploading(true);
      
      for (let i = 0; i < uploadFiles.length; i++) {
        const file = uploadFiles[i];
        const formData = new FormData();
        formData.append('file', file);
        formData.append('userId', currentUser.id);
        
        // Add selected tag if any
        if (selectedTag) {
          formData.append('tags', selectedTag);
        }
        
        await fileService.uploadFile(formData);
      }
      
      // Refresh file list
      const updatedFiles = await fileService.getFiles();
      setFiles(updatedFiles);
      
      // Reset file input
      e.target.value = null;
    } catch (err) {
      console.error('Error uploading files:', err);
      alert('Failed to upload one or more files. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  const handleFileSearch = async () => {
    if (!searchQuery.trim()) {
      // If search is empty, just get all files
      const filesData = await fileService.getFiles();
      setFiles(filesData);
      return;
    }
    
    try {
      setLoading(true);
      const searchResults = await fileService.searchFiles(searchQuery);
      setFiles(searchResults);
    } catch (err) {
      console.error('Error searching files:', err);
      setError('Failed to search files. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleFileDelete = async (fileId) => {
    if (!confirm('Are you sure you want to delete this file?')) return;
    
    try {
      await fileService.deleteFile(fileId);
      
      // Update file list
      setFiles(files.filter(file => file.id !== fileId));
    } catch (err) {
      console.error('Error deleting file:', err);
      alert('Failed to delete file. Please try again.');
    }
  };

  const handleFileDownload = async (fileId, fileName) => {
    try {
      await fileService.downloadFile(fileId);
    } catch (err) {
      console.error('Error downloading file:', err);
      alert('Failed to download file. Please try again.');
    }
  };

  const toggleFileSelection = (fileId) => {
    if (selectedFiles.includes(fileId)) {
      setSelectedFiles(selectedFiles.filter(id => id !== fileId));
    } else {
      setSelectedFiles([...selectedFiles, fileId]);
    }
  };

  const handleBulkDelete = async () => {
    if (!selectedFiles.length) return;
    
    if (!confirm(`Are you sure you want to delete ${selectedFiles.length} selected files?`)) return;
    
    try {
      for (const fileId of selectedFiles) {
        await fileService.deleteFile(fileId);
      }
      
      // Update file list
      setFiles(files.filter(file => !selectedFiles.includes(file.id)));
      
      // Clear selection
      setSelectedFiles([]);
    } catch (err) {
      console.error('Error deleting files:', err);
      alert('Failed to delete some files. Please try again.');
    }
  };

  return (
    <div className="file-manager-container">
      <header className="page-header">
        <h1>File Manager</h1>
        <div className="file-upload-wrapper">
          <div className="tag-selector">
            <select 
              value={selectedTag} 
              onChange={e => setSelectedTag(e.target.value)}
              className="tag-select"
            >
              <option value="">No tag</option>
              {tags.map(tag => (
                <option key={tag} value={tag}>{tag}</option>
              ))}
            </select>
          </div>
          <label className="file-upload-btn">
            Upload Files
            <input 
              type="file"
              multiple
              onChange={handleFileUpload}
              disabled={uploading}
              style={{ display: 'none' }}
            />
          </label>
        </div>
      </header>
      
      <div className="file-tools">
        <div className="file-search">
          <input
            type="text"
            placeholder="Search files..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            onKeyPress={e => e.key === 'Enter' && handleFileSearch()}
          />
          <button onClick={handleFileSearch} className="btn btn-secondary">
            Search
          </button>
        </div>
        
        {selectedFiles.length > 0 && (
          <div className="bulk-actions">
            <span>{selectedFiles.length} selected</span>
            <button onClick={handleBulkDelete} className="btn btn-danger">
              Delete Selected
            </button>
          </div>
        )}
      </div>

      {loading ? (
        <div className="loading-indicator">Loading files...</div>
      ) : error ? (
        <div className="error-message">{error}</div>
      ) : (
        <>
          {files.length === 0 ? (
            <div className="empty-state">
              <p>No files found. Upload files to get started.</p>
            </div>
          ) : (
            <div className="files-table-container">
              <table className="files-table">
                <thead>
                  <tr>
                    <th width="40">
                      <input 
                        type="checkbox" 
                        onChange={() => {
                          if (selectedFiles.length === files.length) {
                            setSelectedFiles([]);
                          } else {
                            setSelectedFiles(files.map(file => file.id));
                          }
                        }}
                        checked={selectedFiles.length === files.length && files.length > 0}
                      />
                    </th>
                    <th>Name</th>
                    <th>Size</th>
                    <th>Type</th>
                    <th>Uploaded By</th>
                    <th>Upload Date</th>
                    <th>Tags</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {files.map(file => (
                    <tr key={file.id}>
                      <td>
                        <input 
                          type="checkbox"
                          checked={selectedFiles.includes(file.id)}
                          onChange={() => toggleFileSelection(file.id)}
                        />
                      </td>
                      <td>{file.name}</td>
                      <td>{formatFileSize(file.size)}</td>
                      <td>{file.fileType}</td>
                      <td>{file.uploaderName}</td>
                      <td>{new Date(file.uploadDate).toLocaleDateString()}</td>
                      <td>
                        <div className="file-tags">
                          {file.tags.map(tag => (
                            <span key={tag} className="tag">{tag}</span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <div className="file-actions">
                          <button 
                            onClick={() => handleFileDownload(file.id, file.name)}
                            className="btn btn-sm btn-secondary"
                          >
                            Download
                          </button>
                          {currentUser.id === file.uploaderId && (
                            <button 
                              onClick={() => handleFileDelete(file.id)}
                              className="btn btn-sm btn-danger"
                            >
                              Delete
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}

// Helper function to format file size
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

export default FileManager; 