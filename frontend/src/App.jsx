import React, { useState } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Upload from './pages/Upload';
import Retrieve from './pages/Retrieve';

export default function App() {
  const [activePage, setActivePage] = useState('home');

  return (
    <>
      <Navbar activePage={activePage} setActivePage={setActivePage} />

      <main className="main-content">
        <div className="container">
          {activePage === 'home' && <Home setActivePage={setActivePage} />}
          {activePage === 'upload' && <Upload setActivePage={setActivePage} />}
          {activePage === 'retrieve' && <Retrieve setActivePage={setActivePage} />}
        </div>
      </main>

      <footer className="footer">
        <div className="container">
          <p>SmartBin • Ephemeral File Transfer • Files expire automatically after 30 minutes</p>
        </div>
      </footer>
    </>
  );
}
