import React from 'react';

export default function LoadingSpinner({ text = 'Loading...' }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '2rem', gap: '0.75rem' }}>
      <div className="spinner spinner-primary" style={{ width: '32px', height: '32px', borderWidth: '3px' }}></div>
      {text && <span style={{ fontSize: '0.9375rem', color: 'var(--gray-500)', fontWeight: 500 }}>{text}</span>}
    </div>
  );
}
