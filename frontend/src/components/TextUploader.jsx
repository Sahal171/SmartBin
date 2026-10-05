import React, { useState } from 'react';
import { Send, FileText, ArrowUpRight } from 'lucide-react';
import fileApi from '../services/api';
import ErrorMessage from './ErrorMessage';

export default function TextUploader({ onUploadSuccess }) {
  const [text, setText] = useState('');
  const [title, setTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!text.trim()) {
      setErrorMessage('Please enter the text message you wish to share.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const result = await fileApi.uploadText(text, title);
      onUploadSuccess(result);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to share message.';
      setErrorMessage(msg);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="card">
      <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--gray-900)' }}>
        Share Text or Note
      </h2>
      <p style={{ color: 'var(--gray-500)', fontSize: '0.9375rem', marginBottom: '1.5rem' }}>
        Paste code snippets, passwords, URLs, or notes to access on another device.
      </p>

      <ErrorMessage message={errorMessage} />

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 600, color: 'var(--gray-700)', marginBottom: '0.375rem' }}>
            Note Title (Optional)
          </label>
          <input
            type="text"
            className="code-input"
            style={{
              width: '100%',
              fontSize: '0.9375rem',
              textAlign: 'left',
              fontFamily: 'inherit',
              letterSpacing: 'normal',
              textTransform: 'none',
              padding: '0.75rem 1rem',
              borderWidth: '1px'
            }}
            placeholder="e.g. WiFi Password, Lab Setup Commands, Link"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            disabled={isSubmitting}
            maxLength={100}
          />
        </div>

        <div style={{ marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.375rem' }}>
            <label style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--gray-700)' }}>
              Message / Content
            </label>
            <span style={{ fontSize: '0.75rem', color: 'var(--gray-400)' }}>
              {text.length.toLocaleString()} characters
            </span>
          </div>
          <textarea
            rows={8}
            className="code-input"
            style={{
              width: '100%',
              fontSize: '0.9375rem',
              textAlign: 'left',
              fontFamily: 'monospace',
              letterSpacing: 'normal',
              textTransform: 'none',
              padding: '0.875rem 1rem',
              lineHeight: 1.5,
              resize: 'vertical',
              borderWidth: '1px'
            }}
            placeholder="Paste your text, code, or secret here..."
            value={text}
            onChange={(e) => setText(e.target.value)}
            disabled={isSubmitting}
            autoFocus
          />
        </div>

        <button
          type="submit"
          disabled={!text.trim() || isSubmitting}
          className="btn btn-primary btn-full btn-lg"
        >
          {isSubmitting ? (
            <>
              <div className="spinner" style={{ width: '20px', height: '20px' }}></div>
              <span>Generating Code...</span>
            </>
          ) : (
            <>
              <ArrowUpRight size={20} />
              <span>Share Message & Get Code</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
}
