import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
    isAuthenticated: boolean;
    token: string | null;
    login: (token: string) => void;
    logout: () => void;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('admin_token'));
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(false);
    }, [token]);

    const login = (newToken: string) => {
        localStorage.setItem('admin_token', newToken);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem('admin_token');
        setToken(null);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated: !!token, token, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};
