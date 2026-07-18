import React, { Suspense, lazy, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { useAuthStore, useThemeStore } from './store/store'

// Layout
import Layout from './components/layout/Layout'
import Spinner from './components/common/Spinner'

// Auth pages (eager loaded)
import LoginPage from './pages/auth/LoginPage'
import SignupPage from './pages/auth/SignupPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'

// Lazy loaded feature pages
const DashboardPage     = lazy(() => import('./pages/dashboard/DashboardPage'))
const CropPage          = lazy(() => import('./pages/crop/CropPage'))
const WeatherPage       = lazy(() => import('./pages/weather/WeatherPage'))
const DiseasePage       = lazy(() => import('./pages/disease/DiseasePage'))
const MarketPage        = lazy(() => import('./pages/market/MarketPage'))
const SchemesPage       = lazy(() => import('./pages/schemes/SchemesPage'))
const ChatbotPage       = lazy(() => import('./pages/chatbot/ChatbotPage'))
const ProfilePage       = lazy(() => import('./pages/profile/ProfilePage'))
const AdminDashboard    = lazy(() => import('./pages/admin/AdminDashboard'))
const NotFoundPage      = lazy(() => import('./pages/error/NotFoundPage'))

const ProtectedRoute = ({ children, adminOnly }) => {
  const { isAuthenticated, user } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (adminOnly && user?.role !== 'ADMIN') return <Navigate to="/dashboard" replace />
  return children
}

const PublicRoute = ({ children }) => {
  const { isAuthenticated } = useAuthStore()
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : children
}

const PageLoader = () => (
  <div className="flex items-center justify-center h-96">
    <Spinner size="lg" />
  </div>
)

export default function App() {
  const { init } = useThemeStore()
  useEffect(() => { init() }, [])

  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: 'var(--toast-bg, #1f2937)',
            color: '#f9fafb',
            borderRadius: '12px',
            padding: '12px 16px',
            boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
          },
          success: { iconTheme: { primary: '#22c55e', secondary: '#fff' } },
          error:   { iconTheme: { primary: '#ef4444', secondary: '#fff' } },
        }}
      />
      <Routes>
        {/* Public auth routes */}
        <Route path="/login"           element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/signup"          element={<PublicRoute><SignupPage /></PublicRoute>} />
        <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />

        {/* Protected routes with sidebar layout */}
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={
            <Suspense fallback={<PageLoader />}><DashboardPage /></Suspense>} />
          <Route path="crops"    element={
            <Suspense fallback={<PageLoader />}><CropPage /></Suspense>} />
          <Route path="weather"  element={
            <Suspense fallback={<PageLoader />}><WeatherPage /></Suspense>} />
          <Route path="disease"  element={
            <Suspense fallback={<PageLoader />}><DiseasePage /></Suspense>} />
          <Route path="market"   element={
            <Suspense fallback={<PageLoader />}><MarketPage /></Suspense>} />
          <Route path="schemes"  element={
            <Suspense fallback={<PageLoader />}><SchemesPage /></Suspense>} />
          <Route path="chat"     element={
            <Suspense fallback={<PageLoader />}><ChatbotPage /></Suspense>} />
          <Route path="profile"  element={
            <Suspense fallback={<PageLoader />}><ProfilePage /></Suspense>} />
          <Route path="admin"    element={
            <ProtectedRoute adminOnly>
              <Suspense fallback={<PageLoader />}><AdminDashboard /></Suspense>
            </ProtectedRoute>} />
        </Route>

        <Route path="*" element={<Suspense fallback={<PageLoader />}><NotFoundPage /></Suspense>} />
      </Routes>
    </BrowserRouter>
  )
}
