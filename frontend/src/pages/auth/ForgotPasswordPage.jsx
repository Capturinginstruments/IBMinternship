import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { Sprout, Mail, KeyRound, Lock, Loader2, CheckCircle } from 'lucide-react'
import { authService } from '../../services/services'
import toast from 'react-hot-toast'

const STEPS = { EMAIL: 1, OTP: 2, PASSWORD: 3, DONE: 4 }

export default function ForgotPasswordPage() {
  const [step, setStep] = useState(STEPS.EMAIL)
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSendOtp = async (e) => {
    e.preventDefault()
    if (!email) return toast.error('Enter your email')
    setLoading(true)
    try {
      await authService.forgotPassword(email)
      setStep(STEPS.OTP)
      toast.success('OTP sent! Check your email inbox.')
    } catch (err) {
      toast.error('Failed to send OTP. Please try again.')
    } finally { setLoading(false) }
  }

  const handleResetPassword = async (e) => {
    e.preventDefault()
    if (!otp || !password) return toast.error('Fill all fields')
    if (password.length < 8) return toast.error('Password must be at least 8 characters')
    setLoading(true)
    try {
      await authService.resetPassword({ email, otp, newPassword: password })
      setStep(STEPS.DONE)
      toast.success('Password reset successful!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid OTP or it has expired')
    } finally { setLoading(false) }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-gray-50 dark:bg-gray-950">
      <div className="w-full max-w-md animate-fade-in">
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-10 h-10 rounded-xl gradient-green flex items-center justify-center">
            <Sprout className="w-6 h-6 text-white" />
          </div>
          <span className="font-bold text-xl text-gray-900 dark:text-white font-display">AI Farmer Assistant</span>
        </div>

        <div className="glass-card p-8">
          {step === STEPS.DONE ? (
            <div className="text-center">
              <CheckCircle className="w-16 h-16 text-primary-500 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white font-display mb-2">Password Reset!</h2>
              <p className="text-gray-500 dark:text-gray-400 mb-6">You can now login with your new password.</p>
              <Link to="/login" className="btn-primary">Go to Login</Link>
            </div>
          ) : step === STEPS.EMAIL ? (
            <>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white font-display mb-2">Forgot Password</h1>
              <p className="text-gray-500 dark:text-gray-400 mb-6">Enter your email to receive a reset OTP</p>
              <form onSubmit={handleSendOtp} className="space-y-4">
                <div>
                  <label className="label">Email Address</label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input type="email" placeholder="you@example.com" value={email}
                      onChange={e => setEmail(e.target.value)} className="input pl-10" />
                  </div>
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-3">
                  {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Mail className="w-5 h-5" />}
                  {loading ? 'Sending OTP...' : 'Send OTP'}
                </button>
              </form>
            </>
          ) : (
            <>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white font-display mb-2">Reset Password</h1>
              <p className="text-gray-500 dark:text-gray-400 mb-6">Enter the OTP sent to <strong>{email}</strong></p>
              <form onSubmit={handleResetPassword} className="space-y-4">
                <div>
                  <label className="label">OTP Code</label>
                  <div className="relative">
                    <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input type="text" placeholder="6-digit OTP" value={otp} maxLength={6}
                      onChange={e => setOtp(e.target.value.replace(/\D/g, ''))} className="input pl-10" />
                  </div>
                </div>
                <div>
                  <label className="label">New Password</label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input type="password" placeholder="Min 8 characters" value={password}
                      onChange={e => setPassword(e.target.value)} className="input pl-10" />
                  </div>
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-3">
                  {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Lock className="w-5 h-5" />}
                  {loading ? 'Resetting...' : 'Reset Password'}
                </button>
              </form>
              <button className="text-sm text-primary-600 mt-3 hover:underline"
                onClick={() => setStep(STEPS.EMAIL)}>← Change email</button>
            </>
          )}

          <p className="text-center text-gray-500 dark:text-gray-400 mt-6 text-sm">
            Remember your password?{' '}
            <Link to="/login" className="text-primary-600 dark:text-primary-400 font-semibold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
