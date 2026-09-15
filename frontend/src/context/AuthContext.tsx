import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/authApi';
import type { User, LoginPayload, RegisterPayload } from '../types/auth';

interface AuthContextType {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<User>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // In-memory token storage only — never stored in localStorage/sessionStorage
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Silent session refresh using HttpOnly cookie
  const refreshSession = useCallback(async (): Promise<boolean> => {
    try {
      const response = await authApi.refresh();
      setAccessToken(response.accessToken);
      setUser(response.user);
      return true;
    } catch {
      setAccessToken(null);
      setUser(null);
      return false;
    }
  }, []);

  // Restore session on initial application load
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        await refreshSession();
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, [refreshSession]);

  const login = async (credentials: LoginPayload): Promise<void> => {
    const response = await authApi.login(credentials);
    setAccessToken(response.accessToken);
    setUser(response.user);
  };

  const register = async (payload: RegisterPayload): Promise<User> => {
    return await authApi.register(payload);
  };

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout();
    } finally {
      // Clear in-memory auth state regardless of backend response
      setAccessToken(null);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        isAuthenticated: !!user && !!accessToken,
        isLoading,
        login,
        register,
        logout,
        refreshSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

