import React from 'react';
import { Upload, DownloadCloud, ShieldCheck, Zap, Clock, Smartphone } from 'lucide-react';

export default function Home({ setActivePage }) {
  return (
    <div>
      <section className="hero">
        <div className="hero-tag">
          <Zap size={14} />
          <span>No Accounts • No Setup • Ephemeral</span>
        </div>
        <h1 className="hero-title">
          Transfer files between devices <span className="hero-gradient">without logging in</span>.
        </h1>
        <p className="hero-subtitle">
          SmartBin lets you instantly beam documents, images, and archives from your phone to any computer using a simple 6-character code. Auto-expires in 30 minutes.
        </p>

        <div className="hero-cta">
          <button
            onClick={() => setActivePage('upload')}
            className="btn btn-primary btn-lg"
          >
            <Upload size={20} />
            <span>Upload File</span>
          </button>

          <button
            onClick={() => setActivePage('retrieve')}
            className="btn btn-secondary btn-lg"
          >
            <DownloadCloud size={20} />
            <span>Retrieve File</span>
          </button>
        </div>
      </section>

      {/* How It Works */}
      <div className="steps-grid">
        <div className="step-card">
          <div className="step-num">1</div>
          <h3 className="step-title">Upload from Device</h3>
          <p className="step-desc">
            Select or drag any file from your phone or desktop. Files up to 100MB are encrypted in transit.
          </p>
        </div>

        <div className="step-card">
          <div className="step-num">2</div>
          <h3 className="step-title">Get a 6-Character Code</h3>
          <p className="step-desc">
            Receive a cryptographically secure, unambiguous code (e.g. <code>7K4P92</code>). No login or email needed.
          </p>
        </div>

        <div className="step-card">
          <div className="step-num">3</div>
          <h3 className="step-title">Instant Retrieval</h3>
          <p className="step-desc">
            Type the code on the target computer or lab workstation to stream the file immediately.
          </p>
        </div>
      </div>

      {/* Features Banner */}
      <div className="card" style={{ background: 'var(--gray-900)', color: 'white', border: 'none' }}>
        <h3 style={{ fontSize: '1.25rem', marginBottom: '1.25rem', textAlign: 'center' }}>
          Engineered for Frictionless Security
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'flex-start' }}>
            <Clock size={20} color="#818cf8" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>Auto-Expiring</div>
              <div style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
                Files and metadata are automatically purged after 30 minutes.
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'flex-start' }}>
            <ShieldCheck size={20} color="#34d399" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>Brute-Force Protected</div>
              <div style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
                Rate limiting blocks automated guessing attempts on transfer codes.
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'flex-start' }}>
            <Smartphone size={20} color="#f472b6" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>Mobile First</div>
              <div style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
                Clean touch targets and camera/gallery file picker integration.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
