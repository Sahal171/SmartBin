import React, { useState, useRef } from 'react';
import { UploadCloud, File, X, ArrowUpRight } from 'lucide-react';
import fileApi from '../services/api';
import ErrorMessage from './ErrorMessage';

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

export default function FileUploader({ onUploadSuccess }) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelected(e.dataTransfer.files[0]);
    }
  };

  const handleFileInputChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelected(e.target.files[0]);
    }
  };

  const handleFileSelected = (file) => {
    setErrorMessage('');
    // 100MB limit check client-side
    if (file.size > 100 * 1024 * 1024) {
      setErrorMessage('File size exceeds the 100MB maximum limit.');
      return;
    }
    if (file.size === 0) {
      setErrorMessage('Selected file is empty.');
      return;
    }
    setSelectedFile(file);
  };

  const handleRemoveFile = () => {
    setSelectedFile(null);
    setUploadProgress(0);
    setErrorMessage('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    setErrorMessage('');
    setUploadProgress(0);

    try {
      const result = await fileApi.uploadFile(selectedFile, (percent) => {
        setUploadProgress(percent);
      });
      onUploadSuccess(result);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Upload failed. Please check your connection.';
      setErrorMessage(msg);
      setIsUploading(false);
    }
  };

  return (
    <div className="card">
      <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--gray-900)' }}>
        Upload a File
      </h2>
      <p style={{ color: 'var(--gray-500)', fontSize: '0.9375rem', marginBottom: '1.5rem' }}>
        Select or drag a file to generate a temporary transfer code. No sign-up required.
      </p>

      <ErrorMessage message={errorMessage} />

      {!selectedFile ? (
        <div
          className={`dropzone ${dragActive ? 'active' : ''}`}
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
        >
          <input
            ref={fileInputRef}
            type="file"
            onChange={handleFileInputChange}
            style={{ display: 'none' }}
          />
          <div className="dropzone-icon">
            <UploadCloud size={28} />
          </div>
          <div className="dropzone-title">Click to select or drag and drop</div>
          <div className="dropzone-subtitle">Any file format up to 100 MB</div>
        </div>
      ) : (
        <div>
          <div className="file-preview">
            <div className="file-preview-info">
              <File size={28} color="var(--primary)" />
              <div>
                <div className="file-name" title={selectedFile.name}>{selectedFile.name}</div>
                <div className="file-size">{formatBytes(selectedFile.size)}</div>
              </div>
            </div>
            {!isUploading && (
              <button
                onClick={handleRemoveFile}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--gray-400)', padding: '4px' }}
                title="Remove file"
              >
                <X size={20} />
              </button>
            )}
          </div>

          {isUploading && (
            <div className="progress-container">
              <div className="progress-header">
                <span>Uploading file...</span>
                <span>{uploadProgress}%</span>
              </div>
              <div className="progress-bar-bg">
                <div className="progress-bar-fill" style={{ width: `${uploadProgress}%` }}></div>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.25rem' }}>
            <button
              onClick={handleUpload}
              disabled={isUploading}
              className="btn btn-primary btn-full"
            >
              {isUploading ? (
                <>
                  <div className="spinner" style={{ width: '18px', height: '18px' }}></div>
                  <span>Uploading ({uploadProgress}%)...</span>
                </>
              ) : (
                <>
                  <ArrowUpRight size={18} />
                  <span>Start Upload & Generate Code</span>
                </>
              )}
            </button>

            {!isUploading && (
              <button onClick={handleRemoveFile} className="btn btn-secondary">
                Cancel
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
