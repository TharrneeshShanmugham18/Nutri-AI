import axios from 'axios';
import type { AuthResponse, LoginPayload, RegisterPayload, User } from '../types/auth';

const apiClient = axios.create({
  baseURL: '/api/v1/auth',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authApi = {
  login: async (credentials: LoginPayload): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/login', credentials);
    return response.data;
  },

  register: async (payload: RegisterPayload): Promise<User> => {
    const response = await apiClient.post<User>('/register', payload);
    return response.data;
  },

  refresh: async (): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/refresh');
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/logout');
  },

  getCurrentUser: async (accessToken: string): Promise<User> => {
    const response = await apiClient.get<User>('/me', {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
    return response.data;
  },
};

