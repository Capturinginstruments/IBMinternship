import React, { useEffect, useState } from 'react'
import {
  Sprout, Cloud, Bug, TrendingUp, BookOpen, MessageSquare,
  Bell, ChevronRight, Loader2, Thermometer, Droplets, Wind,
  ArrowUpRight, AlertTriangle, CheckCircle, Star
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { profileService } from '../../services/services'
import { useAuthStore } from '../../store/store'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

const StatCard = ({ icon: Icon, label, value, color, to }) => (
  <Link to={to} className="glass-card-hover p-5 flex items-center gap-4 group">
    <div className={`w-12 h-12 rounded-2xl ${color} flex items-center justify-center shrink-0`}>
      <Icon className="w-6 h-6 text-white" />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">{label}</p>
      <p className="text-xl font-bold text-gray-900 dark:text-white font-display truncate">{value}</p>
    </div>
    <ChevronRight className="w-5 h-5 text-gray-300 dark:text-gray-600 group-hover:text-primary-500 transition-colors shrink-0" />
  </Link>
)

const QuickAction = ({ icon: Icon, label, to, color }) => (
  <Link to={to} className={`glass-card-hover p-4 flex flex-col items-center gap-2 text-center`}>
    <div className={`w-12 h-12 rounded-2xl ${color} flex items-center justify-center`}>
      <Icon className="w-6 h-6 text-white" />
    </div>
    <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{label}</span>
  </Link>
)

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)
  const { user } = useAuthStore()

  useEffect(() => {
    profileService.getDashboard()
      .then(res => setDashboard(res.data.data))
      .catch(() => toast.error('Could not load dashboard'))
      .finally(() => setLoading(false))
  }, [])

  const greeting = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Good Morning'
    if (h < 17) return 'Good Afternoon'
    return 'Good Evening'
  }

  if (loading) return (
    <div className="page-container">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        {[...Array(4)].map((_, i) => <div key={i} className="shimmer-card" />)}
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {[...Array(3)].map((_, i) => <div key={i} className="skeleton h-64 rounded-2xl" />)}
      </div>
    </div>
  )

  return (
    <div className="page-container animate-fade-in">
      {/* Header */}
      <div className="page-header">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div>
            <h1 className="page-title">
              {greeting()}, {user?.firstName}! 🌾
            </h1>
            <p className="page-subtitle">
              {format(new Date(), 'EEEE, MMMM d, yyyy')} · Here's your farm overview
            </p>
          </div>
          <Link to="/crops" className="btn-primary flex items-center gap-2">
            <Sprout className="w-4 h-4" />
            Get Crop Advice
          </Link>
        </div>
      </div>

      {/* Quick Stats Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <StatCard icon={Bug} label="Disease Alerts" color="bg-red-500"
          value={dashboard?.unresolvedDiseaseAlerts > 0 ? `${dashboard.unresolvedDiseaseAlerts} Active` : 'All Clear ✓'}
          to="/disease" />
        <StatCard icon={Bell} label="Notifications" color="bg-blue-500"
          value={dashboard?.unreadNotifications > 0 ? `${dashboard.unreadNotifications} Unread` : 'All Read ✓'}
          to="/profile" />
        <StatCard icon={Sprout} label="Last Crop Advice" color="bg-primary-600"
          value={dashboard?.latestCropRecommendation?.recommendedCrop || 'None yet'}
          to="/crops" />
        <StatCard icon={TrendingUp} label="Market Price" color="bg-amber-500"
          value={dashboard?.marketSummary
            ? `₹${dashboard.marketSummary.modalPrice?.toLocaleString()} ${dashboard.marketSummary.priceUnit || '/q'}`
            : 'Check prices'}
          to="/market" />
      </div>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Weather card */}
        <div className="glass-card p-6 col-span-1">
          <div className="section-header">
            <h2 className="section-title flex items-center gap-2">
              <Cloud className="w-5 h-5 text-sky-500" /> Weather
            </h2>
            <Link to="/weather" className="text-sm text-primary-600 dark:text-primary-400 hover:underline font-medium flex items-center gap-1">
              Details <ArrowUpRight className="w-3 h-3" />
            </Link>
          </div>
          {dashboard?.weather ? (
            <div className="text-center py-4">
              <div className="text-6xl mb-3">
                {dashboard.weather.iconCode?.includes('01') ? '☀️'
                  : dashboard.weather.iconCode?.includes('02') ? '⛅'
                  : dashboard.weather.iconCode?.includes('09') || dashboard.weather.iconCode?.includes('10') ? '🌧️'
                  : dashboard.weather.iconCode?.includes('11') ? '⛈️'
                  : '🌤️'}
              </div>
              <p className="text-4xl font-bold text-gray-900 dark:text-white font-display">
                {Math.round(dashboard.weather.temperature)}°C
              </p>
              <p className="text-gray-500 dark:text-gray-400 capitalize mt-1">{dashboard.weather.description}</p>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mt-1">{dashboard.weather.city}</p>
              <div className="flex items-center justify-center gap-6 mt-4 pt-4 border-t border-gray-100 dark:border-gray-800">
                <div className="flex items-center gap-1 text-sm text-gray-500">
                  <Droplets className="w-4 h-4 text-blue-400" /> {dashboard.weather.humidity}%
                </div>
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <Cloud className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500 dark:text-gray-400 text-sm">Set your location in profile<br/>to see weather</p>
              <Link to="/weather" className="btn-outline mt-4 text-sm px-4 py-2">Get Weather</Link>
            </div>
          )}
        </div>

        {/* Latest crop recommendation */}
        <div className="glass-card p-6 col-span-1">
          <div className="section-header">
            <h2 className="section-title flex items-center gap-2">
              <Sprout className="w-5 h-5 text-primary-500" /> Crop Advice
            </h2>
            <Link to="/crops" className="text-sm text-primary-600 dark:text-primary-400 hover:underline font-medium flex items-center gap-1">
              New <ArrowUpRight className="w-3 h-3" />
            </Link>
          </div>
          {dashboard?.latestCropRecommendation ? (
            <div>
              <div className="text-5xl mb-4 text-center">🌾</div>
              <div className="text-center mb-4">
                <h3 className="text-2xl font-bold text-gray-900 dark:text-white font-display">
                  {dashboard.latestCropRecommendation.recommendedCrop}
                </h3>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                  {dashboard.latestCropRecommendation.season} Season
                </p>
              </div>
              <div className="confidence-bar mb-2">
                <div className="h-full rounded-full bg-gradient-to-r from-primary-500 to-primary-600 transition-all duration-1000"
                  style={{ width: `${dashboard.latestCropRecommendation.confidenceScore || 0}%` }} />
              </div>
              <p className="text-xs text-gray-500 dark:text-gray-400 text-right">
                {Math.round(dashboard.latestCropRecommendation.confidenceScore || 0)}% confidence
              </p>
              <p className="text-xs text-gray-400 mt-3 text-center">
                Recommended on {dashboard.latestCropRecommendation.createdAt
                  ? format(new Date(dashboard.latestCropRecommendation.createdAt), 'dd MMM yyyy')
                  : '—'}
              </p>
            </div>
          ) : (
            <div className="text-center py-8">
              <Sprout className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500 dark:text-gray-400 text-sm mb-4">No recommendations yet</p>
              <Link to="/crops" className="btn-primary text-sm px-4 py-2">Get Recommendation</Link>
            </div>
          )}
        </div>

        {/* Featured Schemes */}
        <div className="glass-card p-6 col-span-1">
          <div className="section-header">
            <h2 className="section-title flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-amber-500" /> Schemes
            </h2>
            <Link to="/schemes" className="text-sm text-primary-600 dark:text-primary-400 hover:underline font-medium flex items-center gap-1">
              All <ArrowUpRight className="w-3 h-3" />
            </Link>
          </div>
          {dashboard?.featuredSchemes?.length > 0 ? (
            <div className="space-y-3">
              {dashboard.featuredSchemes.map((s) => (
                <Link key={s.id} to="/schemes"
                  className="flex items-start gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                  <div className="w-9 h-9 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
                    <Star className="w-5 h-5 text-amber-500" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 dark:text-white leading-snug line-clamp-2">{s.title}</p>
                    <span className="badge-yellow mt-1 text-xs">{s.category?.replace(/_/g, ' ')}</span>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <BookOpen className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500 dark:text-gray-400 text-sm">No schemes available</p>
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="mb-8">
        <h2 className="section-title mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          <QuickAction icon={Sprout}       label="Crop Advice"      to="/crops"    color="bg-primary-600" />
          <QuickAction icon={Cloud}        label="Weather"          to="/weather"  color="bg-sky-500" />
          <QuickAction icon={Bug}          label="Disease Check"    to="/disease"  color="bg-red-500" />
          <QuickAction icon={TrendingUp}   label="Market Prices"    to="/market"   color="bg-amber-500" />
          <QuickAction icon={BookOpen}     label="Govt Schemes"     to="/schemes"  color="bg-purple-600" />
          <QuickAction icon={MessageSquare} label="Ask KisanAI"    to="/chat"     color="bg-teal-600" />
        </div>
      </div>

      {/* Recent Chat Sessions */}
      {dashboard?.recentChats?.length > 0 && (
        <div className="glass-card p-6">
          <div className="section-header">
            <h2 className="section-title flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-teal-500" /> Recent Chats
            </h2>
            <Link to="/chat" className="text-sm text-primary-600 dark:text-primary-400 hover:underline font-medium">
              Open Chat
            </Link>
          </div>
          <div className="space-y-3">
            {dashboard.recentChats.map((chat) => (
              <Link key={chat.sessionId} to={`/chat?session=${chat.sessionId}`}
                className="flex items-center gap-4 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                <div className="w-10 h-10 rounded-xl bg-teal-100 dark:bg-teal-900/30 flex items-center justify-center shrink-0">
                  <MessageSquare className="w-5 h-5 text-teal-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-gray-900 dark:text-gray-100 truncate">{chat.lastMessage}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{chat.sessionId.slice(0, 8)}...</p>
                </div>
                <ChevronRight className="w-4 h-4 text-gray-300 shrink-0" />
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
