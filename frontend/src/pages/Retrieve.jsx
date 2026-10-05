import React, { useState } from 'react';
import RetrieveForm from '../components/RetrieveForm';
import FileDetails from '../components/FileDetails';
import fileApi from '../services/api';

export default function Retrieve({ setActivePage }) {
  const [fileMetadata, setFileMetadata] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleRetrieve = async (code) => {
    setIsLoading(true);
    setError('');

    try {
      const data = await fileApi.retrieveFile(code);
      setFileMetadata(data);
    } catch (err) {
      if (err.response?.status === 429) {
        setError(err.response.data?.message || 'Too many retrieval attempts. Please wait a minute.');
      } else if (err.response?.status === 404) {
        setError('Invalid or expired transfer code.');
      } else {
        setError(err.response?.data?.message || 'Failed to retrieve file. Please verify the code.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setFileMetadata(null);
    setError('');
  };

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', width: '100%' }}>
      {!fileMetadata ? (
        <RetrieveForm
          onRetrieve={handleRetrieve}
          isLoading={isLoading}
          error={error}
        />
      ) : (
        <FileDetails
          fileMetadata={fileMetadata}
          onReset={handleReset}
        />
      )}
    </div>
  );
}
