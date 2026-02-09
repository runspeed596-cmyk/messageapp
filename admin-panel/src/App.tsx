import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import AdminLayout from './layouts/AdminLayout';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import HomePage from './pages/HomePage';
import Banners from './pages/Banners';
import Universities from './pages/Universities';
import Entertainment from './pages/Entertainment';
import Discounts from './pages/Discounts';
import Competitions from './pages/Competitions';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return <>{children}</>;
};

function AppContent() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />

      <Route path="/" element={
        <ProtectedRoute>
          <AdminLayout><Dashboard /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/home" element={
        <ProtectedRoute>
          <AdminLayout><HomePage /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/users" element={
        <ProtectedRoute>
          <AdminLayout><Users /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/banners" element={
        <ProtectedRoute>
          <AdminLayout><Banners /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/universities" element={
        <ProtectedRoute>
          <AdminLayout><Universities /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/entertainment" element={
        <ProtectedRoute>
          <AdminLayout><Entertainment /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/discounts" element={
        <ProtectedRoute>
          <AdminLayout><Discounts /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/competitions" element={
        <ProtectedRoute>
          <AdminLayout><Competitions /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter basename="/admin">
        <AppContent />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
