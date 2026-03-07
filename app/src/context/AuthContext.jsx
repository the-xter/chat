import {createContext, useContext, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../api/axios';
import {AUTH} from '../api/endpoints';

const AuthContext = createContext(null);

export function AuthProvider({children}) {
    const [user, setUser] = useState(() => {
        const username = localStorage.getItem('username');
        const token = localStorage.getItem('token');
        const roles = JSON.parse(localStorage.getItem('roles') || '[]');
        return token ? {username, token, roles} : null;
    });

    const navigate = useNavigate();

    const getRedirectPath = (roles) =>
        roles.includes('ADMIN') ? '/users' : '/chat';

    const login = async (username, password) => {
        const res = await api.post(AUTH.LOGIN, {username, password});
        const {token, username: name, roles} = res.data;
        localStorage.setItem('token', token);
        localStorage.setItem('username', name);
        localStorage.setItem('roles', JSON.stringify(roles));
        setUser({username: name, token, roles});
        navigate(getRedirectPath(roles));
    };

    const signup = async (username, email, password) => {
        const res = await api.post(AUTH.SIGNUP, {username, email, password});
        const {token, username: name, roles} = res.data;
        localStorage.setItem('token', token);
        localStorage.setItem('username', name);
        localStorage.setItem('roles', JSON.stringify(roles));
        setUser({username: name, token, roles});
        navigate(getRedirectPath(roles));
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('roles');
        setUser(null);
        navigate('/login');
    };

    return (
        <AuthContext.Provider value={{user, login, signup, logout}}>
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
