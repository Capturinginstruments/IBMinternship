import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Sprout, Loader2, Mail, Lock } from 'lucide-react'
import { authService } from '../../services/services'
import { useAuthStore } from '../../store/store'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()

  const validate = () => {
    const e = {}
    if (!form.email) e.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Enter a valid email'
    if (!form.password) e.password = 'Password is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      const res = await authService.login(form)
      const { accessToken, refreshToken, user } = res.data.data
      setAuth(user, accessToken, refreshToken)
      toast.success(`Welcome back, ${user.firstName}! 🌾`)
      navigate('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid email or password'
      toast.error(msg)
      setErrors({ form: msg })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left — brand panel */}
      <div className="hidden lg:flex lg:w-1/2 gradient-hero flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-72 h-72 rounded-full bg-white blur-3xl" />
          <div className="absolute bottom-20 right-20 w-96 h-96 rounded-full bg-primary-300 blur-3xl" />
        </div>
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-16">
            <div className="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center">
              <Sprout className="w-7 h-7 text-white" />
            </div>
            <span className="text-white font-bold text-xl font-display">AI Farmer Assistant</span>
          </div>
          <h2 className="text-4xl font-bold text-white font-display leading-tight mb-4">
            Empowering India's<br />Farmers with AI
          </h2>
          <p className="text-primary-200 text-lg leading-relaxed">
            Get crop recommendations, detect plant diseases, check market prices,
            and access government schemes — all powered by cutting-edge AI.
          </p>
        </div>
        <div className="relative z-10 grid grid-cols-2 gap-4">
          {[
            { v: '50K+', l: 'Farmers Served' },
            { v: '95%', l: 'Disease Accuracy' },
            { v: '₹2Cr+', l: 'Savings Generated' },
            { v: '20+', l: 'Govt Schemes' },
          ].map(({ v, l }) => (
            <div key={l} className="bg-white/10 backdrop-blur-sm rounded-2xl p-4 border border-white/20">
              <div className="text-2xl font-bold text-white font-display">{v}</div>
              <div className="text-primary-200 text-sm">{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Right — login form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-gray-50 dark:bg-gray-950">
        <div className="w-full max-w-md animate-fade-in">
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <Sprout className="w-7 h-7 text-primary-600" />
            <span className="font-bold text-xl text-primary-700 dark:text-primary-400 font-display">
              AI Farmer Assistant
            </span>
          </div>

          <h1 className="text-3xl font-bold text-gray-900 dark:text-white font-display mb-2">
            Welcome back
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mb-8">
            Sign in to your farmer account
          </p>

          {errors.form && (
            <div className="alert-danger mb-6 flex items-center gap-2 text-sm">
              <span>{errors.form}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="label" htmlFor="email">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  id="email" type="email" placeholder="you@example.com"
                  className={`input pl-10 ${errors.email ? 'input-error' : ''}`}
                  value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
                />
              </div>
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="label mb-0" htmlFor="password">Password</label>
                <Link to="/forgot-password" className="text-sm text-primary-600 dark:text-primary-400 hover:underline font-medium">
                  Forgot password?
                </Link>
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  id="password" type={showPw ? 'text' : 'password'} placeholder="••••••••"
                  className={`input pl-10 pr-12 ${errors.password ? 'input-error' : ''}`}
                  value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}
                />
                <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPw(!showPw)}>
                  {showPw ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-3 text-base">
              {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : null}
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-gray-500 dark:text-gray-400 mt-8">
            Don't have an account?{' '}
            <Link to="/signup" className="text-primary-600 dark:text-primary-400 font-semibold hover:underline">
              Create account
            </Link>
          </p>

          <div className="mt-8 p-4 rounded-xl bg-primary-50 dark:bg-primary-950/30 border border-primary-100 dark:border-primary-900 text-sm">
            <p className="font-semibold text-primary-800 dark:text-primary-300 mb-1">Demo Account</p>
            <p className="text-primary-600 dark:text-primary-400">Email: admin@farmerassist.in</p>
            <p className="text-primary-600 dark:text-primary-400">Password: Admin@123</p>
          </div>
        </div>
      </div>
    </div>
  )
}
