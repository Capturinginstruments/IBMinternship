import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Sprout, Loader2, User, Mail, Phone, Lock } from 'lucide-react'
import { authService } from '../../services/services'
import { useAuthStore } from '../../store/store'
import toast from 'react-hot-toast'

const ROLES = [
  { value: 'FARMER',  label: '🌾 Farmer', desc: 'Access all farming tools' },
  { value: 'OFFICER', label: '🏛️ Agricultural Officer', desc: 'Manage and assist farmers' },
]

const Field = ({ id, label, icon: Icon, error, ...props }) => (
  <div>
    <label className="label" htmlFor={id}>{label}</label>
    <div className="relative">
      {Icon && <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />}
      <input id={id} className={`input ${Icon ? 'pl-10' : ''} ${error ? 'input-error' : ''}`} {...props} />
    </div>
    {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
  </div>
)

export default function SignupPage() {
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phone: '', password: '', role: 'FARMER'
  })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()

  const validate = () => {
    const e = {}
    if (!form.firstName || form.firstName.length < 2) e.firstName = 'Minimum 2 characters'
    if (!form.lastName || form.lastName.length < 2) e.lastName = 'Minimum 2 characters'
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Valid email required'
    if (form.phone && !/^[6-9]\d{9}$/.test(form.phone)) e.phone = 'Valid 10-digit Indian number'
    if (!form.password || form.password.length < 8) e.password = 'Minimum 8 characters'
    else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.password))
      e.password = 'Must have uppercase, lowercase, and a digit'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      const res = await authService.signup(form)
      const { accessToken, refreshToken, user } = res.data.data
      setAuth(user, accessToken, refreshToken)
      toast.success(`Welcome to AI Farmer Assistant, ${user.firstName}! 🌾`)
      navigate('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message || 
                  (typeof err.response?.data === 'string' && err.response.data.length < 200 && err.response.data) ||
                  err.message || 
                  'Registration failed. Please try again.'
      toast.error(msg)
      setErrors({ form: msg })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-gray-50 dark:bg-gray-950">
      <div className="w-full max-w-lg animate-fade-in">
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-10 h-10 rounded-xl gradient-green flex items-center justify-center shadow-green">
            <Sprout className="w-6 h-6 text-white" />
          </div>
          <span className="font-bold text-xl text-gray-900 dark:text-white font-display">
            AI Farmer Assistant
          </span>
        </div>

        <div className="glass-card p-8">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white font-display mb-1">
            Create your account
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mb-6">
            Join thousands of empowered Indian farmers
          </p>

          {errors.form && <div className="alert-danger mb-5 text-sm">{errors.form}</div>}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Role selector */}
            <div>
              <label className="label">I am a</label>
              <div className="grid grid-cols-2 gap-3">
                {ROLES.map(r => (
                  <button key={r.value} type="button"
                    onClick={() => setForm({ ...form, role: r.value })}
                    className={`p-3 rounded-xl border-2 text-left transition-all duration-200 ${
                      form.role === r.value
                        ? 'border-primary-500 bg-primary-50 dark:bg-primary-950/40'
                        : 'border-gray-200 dark:border-gray-700 hover:border-primary-300'
                    }`}>
                    <div className="font-medium text-sm text-gray-900 dark:text-white">{r.label}</div>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{r.desc}</div>
                  </button>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Field id="firstName" label="First Name" icon={User} placeholder="Ramesh"
                error={errors.firstName} value={form.firstName}
                onChange={e => setForm({ ...form, firstName: e.target.value })} />
              <Field id="lastName" label="Last Name" placeholder="Kumar"
                error={errors.lastName} value={form.lastName}
                onChange={e => setForm({ ...form, lastName: e.target.value })} />
            </div>

            <Field id="email" label="Email Address" icon={Mail} type="email"
              placeholder="you@example.com" error={errors.email} value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })} />

            <Field id="phone" label="Mobile Number (Optional)" icon={Phone} type="tel"
              placeholder="9876543210" error={errors.phone} value={form.phone}
              onChange={e => setForm({ ...form, phone: e.target.value })} />

            <div>
              <label className="label" htmlFor="password">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input id="password" type={showPw ? 'text' : 'password'} placeholder="Min 8 chars"
                  className={`input pl-10 pr-12 ${errors.password ? 'input-error' : ''}`}
                  value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
                <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPw(!showPw)}>
                  {showPw ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-3 mt-2">
              {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : null}
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-gray-500 dark:text-gray-400 mt-6 text-sm">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 dark:text-primary-400 font-semibold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
