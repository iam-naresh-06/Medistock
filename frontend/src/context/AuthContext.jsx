import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';

const TOKEN_EXPIRY_KEY = 'tokenExpiry';
const SESSION_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    const tokenExpiry = localStorage.getItem(TOKEN_EXPIRY_KEY);

    const isExpired = !tokenExpiry || Date.now() > parseInt(tokenExpiry, 10);

    if (token && username && role && !isExpired) {
      setUser({ token, username, email, role });
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else if (token && isExpired) {
      // Token expired — clear stale session silently
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('email');
      localStorage.removeItem('role');
      localStorage.removeItem(TOKEN_EXPIRY_KEY);
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';
      const response = await axios.post(`${apiBase}/auth/login`, {
        username,
        password,
      });
      const data = response.data;
      const expiryTime = Date.now() + SESSION_DURATION_MS;
      localStorage.setItem('token', data.token);
      localStorage.setItem('username', data.username);
      localStorage.setItem('email', data.email);
      localStorage.setItem('role', data.role);
      localStorage.setItem(TOKEN_EXPIRY_KEY, String(expiryTime));
      axios.defaults.headers.common['Authorization'] = `Bearer ${data.token}`;
      setUser(data);
      return { success: true, role: data.role };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data || 'Invalid username or password',
      };
    }
  };

  const registerUser = async (username, email, password, role, phoneNumber) => {
    try {
      await axios.post('http://localhost:8080/api/auth/register', {
        username,
        email,
        password,
        role,
        phoneNumber,
      });
      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data || 'Registration failed',
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register: registerUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
export default AuthContext;
