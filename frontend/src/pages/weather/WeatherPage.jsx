import React, { useState, useEffect } from 'react'
import {
  Cloud, MapPin, Thermometer, Droplets, Wind, Eye, Gauge,
  Loader2, RefreshCw, Sunrise, Sunset, Info
} from 'lucide-react'
import { weatherService } from '../../services/services'
import toast from 'react-hot-toast'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Area, AreaChart
} from 'recharts'

const WEATHER_ICONS = {
  '01': '☀️', '02': '⛅', '03': '🌥️', '04': '☁️',
  '09': '🌧️', '10': '🌦️', '11': '⛈️', '13': '❄️', '50': '🌫️'
}
const getIcon = (code) => {
  if (!code) return '🌤️'
  const prefix = code.substring(0, 2)
  return WEATHER_ICONS[prefix] || '🌤️'
}

const WindDir = ({ deg }) => {
  const dirs = ['N','NE','E','SE','S','SW','W','NW']
  const d = Math.round(deg / 45) % 8
  return <span className="font-medium">{dirs[d]}</span>
}

export default function WeatherPage() {
  const [weather, setWeather] = useState(null)
  const [loading, setLoading] = useState(false)
  const [city, setCity] = useState('')
  const [cityInput, setCityInput] = useState('')

  const fetchByGPS = () => {
    if (!navigator.geolocation) return toast.error('Geolocation not supported')
    setLoading(true)
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const res = await weatherService.getByCoords(pos.coords.latitude, pos.coords.longitude)
          setWeather(res.data.data)
          setCity(res.data.data.city)
        } catch { toast.error('Failed to fetch weather') }
        finally { setLoading(false) }
      },
      () => { toast.error('Location access denied'); setLoading(false) }
    )
  }

  const fetchByCity = async (e) => {
    e.preventDefault()
    if (!cityInput.trim()) return
    setLoading(true)
    try {
      const res = await weatherService.getByCity(cityInput.trim())
      setWeather(res.data.data)
      setCity(res.data.data.city)
      setCityInput('')
    } catch { toast.error('City not found. Try a different city name.') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetchByGPS() }, [])

  const forecastChartData = weather?.forecast?.map(f => ({
    day: f.dayName, max: Math.round(f.tempMax), min: Math.round(f.tempMin),
    rain: Math.round(f.rainProbability), humidity: f.humidity
  })) || []

  const sunriseTime = weather?.sunrise
    ? new Date(weather.sunrise * 1000).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })
    : '--'
  const sunsetTime = weather?.sunset
    ? new Date(weather.sunset * 1000).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })
    : '--'

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-sky-100 dark:bg-sky-900/40 flex items-center justify-center">
            <Cloud className="w-6 h-6 text-sky-600" />
          </div>
          <div>
            <h1 className="page-title">Weather & Farming Advice</h1>
            <p className="page-subtitle">Real-time weather with AI-powered farming recommendations</p>
          </div>
        </div>
      </div>

      {/* Location controls */}
      <div className="glass-card p-5 mb-6 flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <button onClick={fetchByGPS} disabled={loading}
          className="btn-primary flex items-center gap-2 justify-center shrink-0">
          {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <MapPin className="w-4 h-4" />}
          Use My Location
        </button>
        <span className="text-gray-400 hidden sm:block">or</span>
        <form onSubmit={fetchByCity} className="flex gap-2 flex-1">
          <input className="input flex-1" placeholder="Enter city name (e.g. Pune, Indore, Jaipur)"
            value={cityInput} onChange={e => setCityInput(e.target.value)} />
          <button type="submit" disabled={loading} className="btn-secondary shrink-0">
            <RefreshCw className="w-4 h-4" />
          </button>
        </form>
      </div>

      {loading && !weather && (
        <div className="glass-card p-16 flex flex-col items-center gap-4">
          <Loader2 className="w-10 h-10 animate-spin text-sky-500" />
          <p className="text-gray-500 dark:text-gray-400">Fetching weather data…</p>
        </div>
      )}

      {weather && (
        <div className="space-y-6 animate-slide-up">
          {/* Current weather hero */}
          <div className="rounded-3xl overflow-hidden" style={{
            background: 'linear-gradient(135deg, #0369a1 0%, #0ea5e9 50%, #38bdf8 100%)'
          }}>
            <div className="p-8 text-white">
              <div className="flex items-start justify-between flex-wrap gap-6">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <MapPin className="w-4 h-4 text-sky-200" />
                    <span className="text-sky-200">{weather.city}, {weather.country}</span>
                  </div>
                  <div className="text-8xl font-bold font-display leading-none">
                    {Math.round(weather.temperature)}°
                  </div>
                  <p className="text-sky-100 text-xl capitalize mt-2">{weather.description}</p>
                  <p className="text-sky-200 mt-1">Feels like {Math.round(weather.feelsLike)}°C</p>
                </div>
                <div className="text-right">
                  <div className="text-7xl">{getIcon(weather.iconCode)}</div>
                  <p className="text-sky-200 mt-2">H: {Math.round(weather.tempMax)}° / L: {Math.round(weather.tempMin)}°</p>
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-8 pt-6 border-t border-white/20">
                {[
                  { icon: Droplets, label: 'Humidity', value: `${weather.humidity}%` },
                  { icon: Wind, label: 'Wind', value: `${weather.windSpeed} m/s` },
                  { icon: Gauge, label: 'Pressure', value: `${weather.pressure} hPa` },
                  { icon: Cloud, label: 'Cloudiness', value: `${weather.cloudiness}%` },
                ].map(({ icon: Icon, label, value }) => (
                  <div key={label} className="text-center">
                    <Icon className="w-5 h-5 text-sky-200 mx-auto mb-1" />
                    <p className="text-sky-200 text-xs">{label}</p>
                    <p className="text-white font-bold">{value}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Sunrise / Sunset */}
          <div className="grid grid-cols-2 gap-4">
            <div className="glass-card p-5 flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
                <Sunrise className="w-6 h-6 text-amber-500" />
              </div>
              <div>
                <p className="text-sm text-gray-500 dark:text-gray-400">Sunrise</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{sunriseTime}</p>
              </div>
            </div>
            <div className="glass-card p-5 flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-orange-100 dark:bg-orange-900/30 flex items-center justify-center">
                <Sunset className="w-6 h-6 text-orange-500" />
              </div>
              <div>
                <p className="text-sm text-gray-500 dark:text-gray-400">Sunset</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{sunsetTime}</p>
              </div>
            </div>
          </div>

          {/* 5-day forecast */}
          {weather.forecast?.length > 0 && (
            <div className="glass-card p-6">
              <h3 className="section-title mb-5">5-Day Forecast</h3>
              <div className="grid grid-cols-5 gap-3 mb-6">
                {weather.forecast.map((day) => (
                  <div key={day.date} className="text-center p-3 rounded-2xl bg-gray-50 dark:bg-gray-800/50">
                    <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">{day.dayName}</p>
                    <div className="text-3xl my-2">{getIcon(day.iconCode)}</div>
                    <p className="text-sm font-bold text-gray-900 dark:text-white">{Math.round(day.tempMax)}°</p>
                    <p className="text-xs text-gray-400">{Math.round(day.tempMin)}°</p>
                    {day.rainProbability > 10 && (
                      <div className="flex items-center justify-center gap-0.5 mt-1">
                        <Droplets className="w-3 h-3 text-sky-400" />
                        <span className="text-xs text-sky-500">{Math.round(day.rainProbability)}%</span>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* Temperature chart */}
              <ResponsiveContainer width="100%" height={180}>
                <AreaChart data={forecastChartData}>
                  <defs>
                    <linearGradient id="tempGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis dataKey="day" tick={{ fontSize: 12, fill: '#9ca3af' }} />
                  <YAxis tick={{ fontSize: 11, fill: '#9ca3af' }} unit="°" />
                  <Tooltip formatter={(v, n) => [`${v}°C`, n === 'max' ? 'High' : 'Low']} />
                  <Area type="monotone" dataKey="max" name="max" stroke="#0ea5e9" fill="url(#tempGrad)" strokeWidth={2} />
                  <Line type="monotone" dataKey="min" name="min" stroke="#38bdf8" strokeWidth={2} dot={false} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* AI Farming Advice */}
          {weather.geminiAdvice && (
            <div className="glass-card p-6">
              <h3 className="section-title flex items-center gap-2 mb-4">
                <Info className="w-5 h-5 text-primary-500" /> AI Farming Advice
              </h3>
              <div className="alert-success text-sm leading-relaxed whitespace-pre-line">
                {weather.geminiAdvice}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
