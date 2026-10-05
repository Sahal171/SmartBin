import React, { useState } from 'react';
import FileUploader from '../components/FileUploader';
import TextUploader from '../components/TextUploader';
import TransferCode from '../components/TransferCode';
import { UploadCloud, FileText } from 'lucide-react';

export default function Upload({ setActivePage }) {
  const [activeTab, setActiveTab] = useState('file'); // 'file' or 'text'
  const [uploadResult, setUploadResult] = useState(null);

  const handleUploadSuccess = (result) => {
    setUploadResult(result);
  };

  const handleReset = () => {
    setUploadResult(null);
  };

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', width: '100%' }}>
      {!uploadResult ? (
        <div>
          {/* Tab Switcher */}
          <div style={{ display: 'flex', background: 'var(--gray-200)', padding: '4px', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', gap: '4px' }}>
            <button
              type="button"
              onClick={() => setActiveTab('file')}
              style={{
                flex: 1,
                padding: '0.625rem 1rem',
                border: 'none',
                borderRadius: 'var(--radius-sm)',
                fontWeight: 600,
                fontSize: '0.9375rem',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem',
                transition: 'all 0.15s ease',
                background: activeTab === 'file' ? '#ffffff' : 'transparent',
                color: activeTab === 'file' ? 'var(--primary)' : 'var(--gray-600)',
                boxShadow: activeTab === 'file' ? 'var(--shadow-sm)' : 'none',
              }}
            >
              <UploadCloud size={18} />
              <span>Share File</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('text')}
              style={{
                flex: 1,
                padding: '0.625rem 1rem',
                border: 'none',
                borderRadius: 'var(--radius-sm)',
                fontWeight: 600,
                fontSize: '0.9375rem',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem',
                transition: 'all 0.15s ease',
                background: activeTab === 'text' ? '#ffffff' : 'transparent',
                color: activeTab === 'text' ? 'var(--primary)' : 'var(--gray-600)',
                boxShadow: activeTab === 'text' ? 'var(--shadow-sm)' : 'none',
              }}
            >
              <FileText size={18} />
              <span>Share Text / Note</span>
            </button>
          </div>

          {activeTab === 'file' ? (
            <FileUploader onUploadSuccess={handleUploadSuccess} />
          ) : (
            <TextUploader onUploadSuccess={handleUploadSuccess} />
          )}
        </div>
      ) : (
        <TransferCode uploadResult={uploadResult} onReset={handleReset} />
      )}
    </div>
  );
}
