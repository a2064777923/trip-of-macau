import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';
import { clearAdminAuth, getAdminToken } from './auth';
import { findSuspiciousTextIssue } from './textEncodingGuard';

interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export interface RequestResult<T = unknown> {
  success: boolean;
  data: T;
  message: string;
}

const RAW_API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim();
const API_BASE_URL = RAW_API_BASE_URL && RAW_API_BASE_URL.length > 0 ? RAW_API_BASE_URL : '/';
const APP_BASENAME = import.meta.env.BASE_URL?.replace(/\/$/, '') || '';


const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

instance.interceptors.request.use(
  (config) => {
    const token = getAdminToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAdminAuth();
      window.location.href = `${APP_BASENAME}/login`;
    }
    return Promise.reject({
      success: false,
      message: error.response?.data?.message || error.message || '网络错误',
      response: error.response,
    });
  },
);

async function unwrapResponse<T>(promise: Promise<any>): Promise<RequestResult<T>> {
  const response = await promise;
  const apiResponse = response.data as ApiResponse<T>;
  if (apiResponse && typeof apiResponse.code !== 'undefined') {
    if (apiResponse.code === 0 || apiResponse.code === 200) {
      return {
        success: true,
        data: apiResponse.data,
        message: apiResponse.message,
      };
    }
    return {
      success: false,
      data: null as T,
      message: apiResponse.message || '请求失败',
    };
  }

  return {
    success: true,
    data: response.data as T,
    message: '',
  };
}

function buildSuspiciousTextFailure<T>(path: string, reason: string, preview: string): Promise<RequestResult<T>> {
  return Promise.resolve({
    success: false,
    data: null as T,
    message: `检测到疑似乱码，请改用 UTF-8 重新填写后再提交。字段: ${path}，原因: ${reason}，内容片段: ${preview}`,
  });
}

const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrapResponse<T>(instance.get(url, config));
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const issue = findSuspiciousTextIssue(data);
    if (issue) {
      return buildSuspiciousTextFailure<T>(issue.path, issue.reason, issue.preview);
    }
    return unwrapResponse<T>(instance.post(url, data, config));
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const issue = findSuspiciousTextIssue(data);
    if (issue) {
      return buildSuspiciousTextFailure<T>(issue.path, issue.reason, issue.preview);
    }
    return unwrapResponse<T>(instance.put(url, data, config));
  },
  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const issue = findSuspiciousTextIssue(data);
    if (issue) {
      return buildSuspiciousTextFailure<T>(issue.path, issue.reason, issue.preview);
    }
    return unwrapResponse<T>(instance.patch(url, data, config));
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrapResponse<T>(instance.delete(url, config));
  },
};

export default request;

