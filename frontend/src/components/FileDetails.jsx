import React, { useState, useEffect } from 'react';
import { DownloadCloud, Clock, CheckCircle, ArrowLeft, AlertCircle, Copy, Check, FileText } from 'lucide-react';
import fileApi from '../services/api';

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

export default function FileDetails({ fileMetadata, onReset }) {
  const [downloading, setDownloading] = useState(false);
  const [downloadSuccess, setDownloadSuccess] = useState(false);
  const [copiedText, setCopiedText] = useState(false);
  const [error, setError] = useState('');
  const [remainingSeconds, setRemainingSeconds] = useState(fileMetadata?.remainingSeconds || 0);

  const isText = fileMetadata.transferType === 'TEXT';

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
    return `${mins}m ${secs < 10 ? '0' : ''}${secs}s`;
  };

  const handleCopyText = async () => {
    if (!fileMetadata.textContent) return;
    try {
      await navigator.clipboard.writeText(fileMetadata.textContent);
      setCopiedText(true);
      setTimeout(() => setCopiedText(false), 2500);
    } catch (err) {
      const ta = document.createElement('textarea');
      ta.value = fileMetadata.textContent;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
      setCopiedText(true);
      setTimeout(() => setCopiedText(false), 2500);
    }
  };

  const handleDownload = async () => {
    setDownloading(true);
    setError('');
    try {
      await fileApi.downloadFile(fileMetadata.transferCode, fileMetadata.filename);
      setDownloadSuccess(true);
      setTimeout(() => setDownloadSuccess(false), 4000);
    } catch (err) {
      setError(err.response?.data?.message || 'Download failed. The file may have expired.');
    } finally {
      setDownloading(false);
    }
  };

  const isExpired = remainingSeconds <= 0;

  return (
    <div className="card" style={{ maxWidth: '620px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.5rem' }}>
        <button
          onClick={onReset}
          className="btn btn-secondary"
          style={{ padding: '0.375rem 0.75rem', minHeight: 'auto', fontSize: '0.875rem' }}
        >
          <ArrowLeft size={16} />
          <span>Back</span>
        </button>

        <div className="countdown-badge">
          <Clock size={16} />
          <span>{formatCountdown(remainingSeconds)}</span>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--gray-900)', wordBreak: 'break-all' }}>
          {fileMetadata.filename}
        </h2>
        <p style={{ color: 'var(--gray-500)', fontSize: '0.9375rem', marginTop: '0.25rem' }}>
          {isText ? 'Text note retrieved successfully' : 'Ready for download'}
        </p>
      </div>

      {error && (
        <div className="toast toast-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {downloadSuccess && (
        <div className="toast toast-success">
          <CheckCircle size={18} />
          <span>Download started successfully!</span>
        </div>
      )}

      {/* Render text box if transferType is TEXT */}
      {isText && fileMetadata.textContent && (
        <div style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--gray-500)' }}>
              MESSAGE CONTENT
            </span>
            <button
              onClick={handleCopyText}
              className="btn btn-secondary"
              style={{ padding: '0.25rem 0.625rem', minHeight: 'auto', fontSize: '0.8125rem', gap: '0.375rem' }}
            >
              {copiedText ? <Check size={14} color="var(--success)" /> : <Copy size={14} />}
              <span>{copiedText ? 'Copied!' : 'Copy Text'}</span>
            </button>
          </div>
          <pre
            style={{
              background: '#0f172a',
              color: '#f8fafc',
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              maxHeight: '260px',
              overflowY: 'auto',
              fontFamily: 'monospace',
              fontSize: '0.9375rem',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word',
              lineHeight: 1.5,
              border: '1px solid #1e293b'
            }}
          >
            {fileMetadata.textContent}
          </pre>
        </div>
      )}

      <ul className="details-list">
        <li className="details-item">
          <span className="details-label">Transfer Code</span>
          <span className="details-value" style={{ fontFamily: 'monospace', letterSpacing: '0.05em' }}>
            {fileMetadata.transferCode}
          </span>
        </li>
        <li className="details-item">
          <span className="details-label">Transfer Type</span>
          <span className="details-value">{isText ? 'Text Note' : 'File Upload'}</span>
        </li>
        <li className="details-item">
          <span className="details-label">{isText ? 'Size' : 'File Size'}</span>
          <span className="details-value">{formatBytes(fileMetadata.fileSize)}</span>
        </li>
        <li className="details-item">
          <span className="details-label">Uploaded At</span>
          <span className="details-value">{new Date(fileMetadata.createdAt).toLocaleTimeString()}</span>
        </li>
      </ul>

      {isText ? (
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <button
            onClick={handleCopyText}
            className="btn btn-primary"
            style={{ flex: 1, minWidth: '160px' }}
          >
            {copiedText ? <Check size={18} /> : <Copy size={18} />}
            <span>{copiedText ? 'Copied to Clipboard!' : 'Copy Text'}</span>
          </button>
          <button
            onClick={handleDownload}
            disabled={downloading || isExpired}
            className="btn btn-secondary"
            style={{ minWidth: '150px' }}
          >
            <DownloadCloud size={18} />
            <span>Download .txt</span>
          </button>
        </div>
      ) : (
        <button
          onClick={handleDownload}
          disabled={downloading || isExpired}
          className="btn btn-primary btn-full btn-lg"
        >
          {downloading ? (
            <>
              <div className="spinner" style={{ width: '20px', height: '20px' }}></div>
              <span>Downloading File...</span>
            </>
          ) : (
            <>
              <DownloadCloud size={20} />
              <span>{isExpired ? 'File Expired' : 'Download File'}</span>
            </>
          )}
        </button>
      )}

      <div style={{ textAlign: 'center', marginTop: '1.25rem' }}>
        <button
          onClick={onReset}
          style={{ background: 'none', border: 'none', color: 'var(--gray-500)', cursor: 'pointer', fontSize: '0.875rem' }}
        >
          Retrieve another item
        </button>
      </div>
    </div>
  );
}
