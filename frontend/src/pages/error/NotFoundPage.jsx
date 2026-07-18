import React from 'react'
import { Link } from 'react-router-dom'
import { AlertTriangle, Home, ArrowLeft } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex flex-col items-center justify-center p-6 transition-colors duration-300">
      <div className="glass-card max-w-md w-full text-center p-8 relative overflow-hidden animate-fade-in shadow-2xl">
        {/* Decorative background glow */}
        <div className="absolute -top-10 -right-10 w-32 h-32 bg-primary-500/10 rounded-full blur-2xl" />
        <div className="absolute -bottom-10 -left-10 w-32 h-32 bg-amber-500/10 rounded-full blur-2xl" />

        <div className="w-20 h-20 rounded-3xl bg-amber-50 dark:bg-amber-950/30 flex items-center justify-center mx-auto mb-6 border border-amber-200/50 dark:border-amber-900/30 animate-bounce">
          <AlertTriangle className="w-10 h-10 text-amber-600 dark:text-amber-500" />
        </div>

        <h1 className="text-6xl font-extrabold text-gray-900 dark:text-white font-display mb-3 tracking-tight">404</h1>
        <h2 className="text-xl font-bold text-gray-800 dark:text-gray-200 mb-4">Page Not Found</h2>
        
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-8 leading-relaxed">
          The agricultural coordinates you requested do not exist. It might have been moved, harvested, or deleted.
        </p>

        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <button onClick={() => window.history.back()}
            className="btn-secondary flex items-center justify-center gap-2 text-sm px-5 py-2.5">
            <ArrowLeft className="w-4 h-4" /> Go Back
          </button>
          <Link to="/dashboard"
            className="btn-primary flex items-center justify-center gap-2 text-sm px-5 py-2.5">
            <Home className="w-4 h-4" /> Home Dashboard
          </Link>
        </div>
      </div>
    </div>
  )
}
