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
    const [token, setToken] = useState<string | null>(localStorage.getItem('admin_token_v2'));
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Force logout for old sessions
        if (localStorage.getItem('admin_token')) {
            localStorage.removeItem('admin_token');
            localStorage.removeItem('isSuperAdmin');
            localStorage.removeItem('permissions');
            localStorage.removeItem('admin_id');
        }
        setLoading(false);
    }, [token]);

    const login = (newToken: string) => {
        localStorage.setItem('admin_token_v2', newToken);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem('admin_token_v2');
        localStorage.removeItem('isSuperAdmin');
        localStorage.removeItem('permissions');
        localStorage.removeItem('admin_id');
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
