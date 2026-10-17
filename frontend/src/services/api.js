import axios from 'axios';

const API_BASE = `${import.meta.env.VITE_API_URL}/api/files`;

const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Accept': 'application/json',
  },
});

export const fileApi = {
  /**
   * Uploads a file with progress tracking
   */
  uploadFile: async (file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onUploadProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onUploadProgress(percentCompleted);
        }
      },
    });

    return response.data;
  },

  /**
   * Shares a text message or note
   */
  uploadText: async (text, title = '') => {
    const response = await apiClient.post('/text', {
      text,
      title: title.trim(),
    });
    return response.data;
  },

  /**
   * Retrieves file or text metadata using transfer code
   */
  retrieveFile: async (transferCode) => {
    const response = await apiClient.post('/retrieve', {
      transferCode: transferCode.trim().toUpperCase(),
    });
    return response.data;
  },

  /**
   * Gets file metadata by code
   */
  getMetadata: async (transferCode) => {
    const response = await apiClient.get(`/${transferCode.trim().toUpperCase()}`);
    return response.data;
  },

  /**
   * Downloads the file as a stream/blob and triggers browser save dialog
   */
  downloadFile: async (transferCode, fallbackFilename = 'downloaded_file') => {
    const code = transferCode.trim().toUpperCase();
    const response = await apiClient.get(`/${code}/download`, {
      responseType: 'blob',
    });

    // Extract filename from Content-Disposition header if available
    let filename = fallbackFilename;
    const disposition = response.headers['content-disposition'];
    if (disposition && disposition.includes('filename=')) {
      const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (match && match[1]) {
        filename = match[1].replace(/['"]/g, '');
        // Decode URI encoded filename if present
        try {
          filename = decodeURIComponent(filename);
        } catch (e) {
          // ignore
        }
      }
    }

    const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(blobUrl);

    return true;
  },

  /**
   * Manually deletes a file before its expiration
   */
  deleteFile: async (transferCode) => {
    const response = await apiClient.delete(`/${transferCode.trim().toUpperCase()}`);
    return response.data;
  },
};

export default fileApi;
