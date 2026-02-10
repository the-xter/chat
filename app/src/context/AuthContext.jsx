import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const username = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    return token ? { username, token } : null;
  });

  const navigate = useNavigate();

  const login = async (username, password) => {
    const res = await api.post('/auth/login', { username, password });
    const { token, username: name } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('username', name);
    setUser({ username: name, token });
    navigate('/users');
  };

  const signup = async (username, email, password) => {
    const res = await api.post('/auth/signup', { username, email, password });
    const { token, username: name } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('username', name);
    setUser({ username: name, token });
    navigate('/users');
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
