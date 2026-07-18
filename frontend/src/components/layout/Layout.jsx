import React, { useState, useEffect } from 'react'
import { Outlet, NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Sprout, Cloud, Bug, TrendingUp,
  BookOpen, MessageSquare, User, Settings, Bell,
  Sun, Moon, Menu, X, LogOut, Shield, ChevronRight
} from 'lucide-react'
import { useAuthStore, useThemeStore, useNotificationStore } from '../../store/store'
import { authService, notificationService } from '../../services/services'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/crops',     icon: Sprout,          label: 'Crop Advice' },
  { to: '/weather',   icon: Cloud,           label: 'Weather' },
  { to: '/disease',   icon: Bug,             label: 'Disease Detection' },
  { to: '/market',    icon: TrendingUp,      label: 'Market Prices' },
  { to: '/schemes',   icon: BookOpen,        label: 'Govt Schemes' },
  { to: '/chat',      icon: MessageSquare,   label: 'AI Chatbot' },
  { to: '/profile',   icon: User,            label: 'My Profile' },
]

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { user, logout, isAdmin } = useAuthStore()
  const { isDark, toggle } = useThemeStore()
  const { unreadCount, setUnreadCount } = useNotificationStore()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    notificationService.getUnreadCount()
      .then(res => setUnreadCount(res.data.data.count))
      .catch(() => {})
  }, [location.pathname])

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    try { await authService.logout(refreshToken) } catch {}
    logout()
    navigate('/login')
    toast.success('Logged out successfully')
  }

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-950 overflow-hidden">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-20 bg-black/50 lg:hidden"
             onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={clsx(
        'fixed inset-y-0 left-0 z-30 w-64 transform transition-transform duration-300 ease-in-out',
        'bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 flex flex-col',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        'lg:relative lg:translate-x-0 lg:flex'
      )}>
        {/* Logo */}
        <div className="flex items-center gap-3 p-6 border-b border-gray-100 dark:border-gray-800">
          <div className="w-10 h-10 rounded-xl gradient-green flex items-center justify-center shadow-green">
            <Sprout className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-gray-900 dark:text-white font-display text-sm leading-tight">
              AI Farmer
            </h1>
            <p className="text-xs text-primary-600 dark:text-primary-400 font-medium">Assistant</p>
          </div>
          <button className="ml-auto lg:hidden btn-icon" onClick={() => setSidebarOpen(false)}>
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto p-4 space-y-1 no-scrollbar">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink key={to} to={to} onClick={() => setSidebarOpen(false)}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 group',
                isActive
                  ? 'bg-primary-50 dark:bg-primary-950/50 text-primary-700 dark:text-primary-300'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
              )}>
              {({ isActive }) => (
                <>
                  <Icon className={clsx('w-5 h-5 shrink-0 transition-colors',
                    isActive ? 'text-primary-600 dark:text-primary-400' : 'group-hover:text-gray-700 dark:group-hover:text-gray-300')} />
                  <span>{label}</span>
                  {isActive && <ChevronRight className="w-4 h-4 ml-auto text-primary-400" />}
                </>
              )}
            </NavLink>
          ))}

          {isAdmin() && (
            <NavLink to="/admin" onClick={() => setSidebarOpen(false)}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 mt-4 border',
                isActive
                  ? 'bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border-amber-200 dark:border-amber-800'
                  : 'text-amber-600 dark:text-amber-400 border-amber-100 dark:border-amber-900/50 hover:bg-amber-50 dark:hover:bg-amber-950/20'
              )}>
              <Shield className="w-5 h-5 shrink-0" />
              <span>Admin Panel</span>
            </NavLink>
          )}
        </nav>

        {/* User info at bottom */}
        <div className="p-4 border-t border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 dark:bg-gray-800">
            <div className="w-9 h-9 rounded-full gradient-green flex items-center justify-center text-white font-bold text-sm shrink-0">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-900 dark:text-white truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user?.role}</p>
            </div>
            <button onClick={handleLogout}
              className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 rounded-lg transition-colors">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="h-16 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 flex items-center px-4 gap-4 shrink-0">
          <button className="lg:hidden btn-icon" onClick={() => setSidebarOpen(true)}>
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex-1" />

          {/* Theme toggle */}
          <button onClick={toggle} className="btn-icon text-gray-500 dark:text-gray-400">
            {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
          </button>

          {/* Notifications */}
          <button className="btn-icon relative text-gray-500 dark:text-gray-400"
            onClick={() => navigate('/profile')}>
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-bold">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {/* Avatar */}
          <NavLink to="/profile" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full gradient-green flex items-center justify-center text-white font-bold text-xs">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
          </NavLink>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
