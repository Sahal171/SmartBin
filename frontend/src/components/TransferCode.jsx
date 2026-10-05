import React, { useState, useEffect } from 'react';
import { Copy, Check, Trash2, Clock, File, AlertTriangle } from 'lucide-react';
import fileApi from '../services/api';

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

export default function TransferCode({ uploadResult, onReset }) {
  const [copied, setCopied] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState(uploadResult?.remainingSeconds || 1800);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [isDeleted, setIsDeleted] = useState(false);

  // Live countdown timer
  useEffect(() => {
    if (remainingSeconds <= 0) return;
    const interval = setInterval(() => {
      setRemainingSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [remainingSeconds]);

  const formatCountdown = (totalSecs) => {
    if (totalSecs <= 0) return 'Expired';
    const mins = Math.floor(totalSecs / 60);
    const secs = totalSecs % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(uploadResult.transferCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    } catch (err) {
      // Fallback
      const textArea = document.createElement('textarea');
      textArea.value = uploadResult.transferCode;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to permanently delete this file now?')) {
      return;
    }

    setDeleting(true);
    setDeleteError('');
    try {
      await fileApi.deleteFile(uploadResult.transferCode);
      setIsDeleted(true);
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to delete file.');
      setDeleting(false);
    }
  };

  if (isDeleted) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
        <div style={{ color: 'var(--success)', marginBottom: '1rem' }}>
          <Check size={48} style={{ margin: '0 auto' }} />
        </div>
        <h3 style={{ fontSize: '1.5rem', marginBottom: '0.5rem', color: 'var(--gray-900)' }}>File Deleted</h3>
        <p style={{ color: 'var(--gray-600)', marginBottom: '1.5rem' }}>
          Your file has been permanently deleted and the transfer code has been invalidated.
        </p>
        <button onClick={onReset} className="btn btn-primary">
          Upload Another File
        </button>
      </div>
    );
  }

  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--gray-900)' }}>
          File Ready for Transfer
        </h2>
        <div className="countdown-badge">
          <Clock size={16} />
          <span>Expires in {formatCountdown(remainingSeconds)}</span>
        </div>
      </div>

      {deleteError && (
        <div className="toast toast-error" style={{ marginBottom: '1rem' }}>
          <AlertTriangle size={18} />
          <span>{deleteError}</span>
        </div>
      )}

      {remainingSeconds <= 0 && (
        <div className="toast toast-error" style={{ marginBottom: '1rem' }}>
          <AlertTriangle size={18} />
          <span>This file transfer has expired and is no longer retrievable.</span>
        </div>
      )}

      {/* Prominent Code Display */}
      <div className="code-box">
        <div className="code-title">Transfer Code</div>
        <div className="code-value">{uploadResult.transferCode}</div>
        <div className="code-instruction">
          Enter this code on another device to retrieve your file.
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <button
          onClick={handleCopy}
          className="btn btn-primary"
          style={{ flex: 1, minWidth: '160px' }}
        >
          {copied ? <Check size={18} /> : <Copy size={18} />}
          <span>{copied ? 'Copied to Clipboard!' : 'Copy Code'}</span>
        </button>

        <button
          onClick={handleDelete}
          disabled={deleting}
          className="btn btn-danger"
          style={{ minWidth: '140px' }}
        >
          <Trash2 size={18} />
          <span>{deleting ? 'Deleting...' : 'Delete File'}</span>
        </button>
      </div>

      {/* File Details Summary */}
      <ul className="details-list">
        <li className="details-item">
          <span className="details-label">File Name</span>
          <span className="details-value">{uploadResult.filename}</span>
        </li>
        <li className="details-item">
          <span className="details-label">File Size</span>
          <span className="details-value">{formatBytes(uploadResult.fileSize)}</span>
        </li>
        <li className="details-item">
          <span className="details-label">Content Type</span>
          <span className="details-value">{uploadResult.contentType}</span>
        </li>
        <li className="details-item">
          <span className="details-label">Uploaded At</span>
          <span className="details-value">{new Date(uploadResult.createdAt).toLocaleTimeString()}</span>
        </li>
      </ul>

      <button onClick={onReset} className="btn btn-secondary btn-full">
        Upload Another File
      </button>
    </div>
  );
}
