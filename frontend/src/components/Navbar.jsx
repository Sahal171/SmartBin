import React from 'react';
import { Send, Upload, DownloadCloud } from 'lucide-react';

export default function Navbar({ activePage, setActivePage }) {
  return (
    <nav className="navbar">
      <div className="container navbar-inner">
        <button
          onClick={() => setActivePage('home')}
          className="logo-link"
          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
        >
          <div className="logo-badge">
            <Send size={20} />
          </div>
          <span>SmartBin</span>
        </button>

        <div className="nav-links">
          <button
            onClick={() => setActivePage('upload')}
            className={`nav-btn ${activePage === 'upload' ? 'nav-btn-primary' : 'nav-btn-ghost'}`}
          >
            <Upload size={16} />
            <span>Upload</span>
          </button>

          <button
            onClick={() => setActivePage('retrieve')}
            className={`nav-btn ${activePage === 'retrieve' ? 'nav-btn-primary' : 'nav-btn-ghost'}`}
          >
            <DownloadCloud size={16} />
            <span>Retrieve</span>
          </button>
        </div>
      </div>
    </nav>
  );
}
