import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import AdminLayout from './layouts/AdminLayout';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Banners from './pages/Banners';
import Universities from './pages/Universities';
import Entertainment from './pages/Entertainment';
import Discounts from './pages/Discounts';
import Competitions from './pages/Competitions';
import UserProfiles from './pages/UserProfiles';
import OfficialChannelsGroups from './pages/OfficialChannelsGroups';
import Advertisements from './pages/Advertisements';
import WorldOfScienceSettings from './pages/WorldOfScienceSettings';

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

      <Route path="/user-profiles" element={
        <ProtectedRoute>
          <AdminLayout><UserProfiles /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/official-channels-groups" element={
        <ProtectedRoute>
          <AdminLayout><OfficialChannelsGroups /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/advertisements" element={
        <ProtectedRoute>
          <AdminLayout><Advertisements /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="/world-of-science-settings" element={
        <ProtectedRoute>
          <AdminLayout><WorldOfScienceSettings /></AdminLayout>
        </ProtectedRoute>
      } />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter basename="/ca978112ca">
        <AppContent />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
