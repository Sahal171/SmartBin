import React from 'react';
import { AlertCircle } from 'lucide-react';

export default function ErrorMessage({ message }) {
  if (!message) return null;

  return (
    <div className="toast toast-error">
      <AlertCircle size={20} style={{ flexShrink: 0 }} />
      <span>{message}</span>
    </div>
  );
}
