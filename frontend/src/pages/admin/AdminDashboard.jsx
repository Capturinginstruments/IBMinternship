import React, { useState, useEffect } from 'react'
import {
  Shield, Users, BookOpen, TrendingUp, Bell, Plus,
  Trash2, Edit3, Loader2, AlertTriangle
} from 'lucide-react'
import { schemeService, notificationService } from '../../services/services'
import toast from 'react-hot-toast'

const StatCard = ({ icon: Icon, label, value, color }) => (
  <div className="glass-card p-5 flex items-center gap-4">
    <div className={`w-12 h-12 rounded-2xl ${color} flex items-center justify-center shrink-0`}>
      <Icon className="w-6 h-6 text-white" />
    </div>
    <div>
      <p className="text-sm text-gray-500 dark:text-gray-400">{label}</p>
      <p className="text-2xl font-bold text-gray-900 dark:text-white font-display">{value}</p>
    </div>
  </div>
)

export default function AdminDashboard() {
  const [tab, setTab] = useState('overview')
  const [schemes, setSchemes] = useState([])
  const [schemesLoading, setSchemesLoading] = useState(false)
  const [broadcastForm, setBroadcastForm] = useState({ title: '', message: '', type: 'INFO' })
  const [broadcasting, setBroadcasting] = useState(false)
  const [newScheme, setNewScheme] = useState({
    title: '', description: '', eligibility: '', benefits: '',
    documentsRequired: '', officialUrl: '', category: 'SUBSIDY',
    applicableStates: 'All India', applicableCrops: '', isActive: true
  })
  const [addingScheme, setAddingScheme] = useState(false)
  const [showSchemeForm, setShowSchemeForm] = useState(false)

  const loadSchemes = async () => {
    setSchemesLoading(true)
    try {
      const res = await schemeService.getAll(0, 50)
      setSchemes(res.data.data.content || [])
    } catch { toast.error('Failed to load schemes') }
    finally { setSchemesLoading(false) }
  }

  useEffect(() => { if (tab === 'schemes') loadSchemes() }, [tab])

  const handleBroadcast = async (e) => {
    e.preventDefault()
    if (!broadcastForm.title || !broadcastForm.message) return toast.error('Fill all fields')
    setBroadcasting(true)
    try {
      await notificationService.broadcast(broadcastForm)
      toast.success('Notification broadcast sent to all users!')
      setBroadcastForm({ title: '', message: '', type: 'INFO' })
    } catch { toast.error('Failed to broadcast') }
    finally { setBroadcasting(false) }
  }

  const handleAddScheme = async (e) => {
    e.preventDefault()
    if (!newScheme.title || !newScheme.description) return toast.error('Title and description required')
    setAddingScheme(true)
    try {
      await schemeService.create(newScheme)
      toast.success('Scheme added successfully!')
      setShowSchemeForm(false)
      setNewScheme({ title: '', description: '', eligibility: '', benefits: '',
        documentsRequired: '', officialUrl: '', category: 'SUBSIDY',
        applicableStates: 'All India', applicableCrops: '', isActive: true })
      loadSchemes()
    } catch { toast.error('Failed to add scheme') }
    finally { setAddingScheme(false) }
  }

  const handleDeleteScheme = async (id) => {
    if (!window.confirm('Deactivate this scheme?')) return
    try {
      await schemeService.delete(id)
      setSchemes(s => s.filter(x => x.id !== id))
      toast.success('Scheme deactivated')
    } catch { toast.error('Failed to deactivate') }
  }

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-amber-100 dark:bg-amber-900/40 flex items-center justify-center">
            <Shield className="w-6 h-6 text-amber-600" />
          </div>
          <div>
            <h1 className="page-title">Admin Dashboard</h1>
            <p className="page-subtitle">Manage schemes, notifications, and platform settings</p>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-5 mb-6">
        <StatCard icon={BookOpen} label="Total Schemes" value={schemes.length || '—'} color="bg-amber-500" />
        <StatCard icon={Users} label="Platform" value="Multi-user" color="bg-primary-600" />
        <StatCard icon={TrendingUp} label="Status" value="Live" color="bg-teal-500" />
        <StatCard icon={Bell} label="Broadcasts" value="Available" color="bg-purple-600" />
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        {[
          { key: 'overview', label: 'Overview' },
          { key: 'schemes', label: 'Manage Schemes' },
          { key: 'broadcast', label: 'Broadcast' },
        ].map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={tab === t.key ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-4">Platform Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 className="font-semibold text-gray-900 dark:text-white mb-3">API Integrations</h3>
              <div className="space-y-2">
                {[
                  { name: 'Google Gemini AI', status: 'Active', color: 'badge-green' },
                  { name: 'OpenWeatherMap', status: 'Active', color: 'badge-green' },
                  { name: 'HuggingFace Disease Model', status: 'Active', color: 'badge-green' },
                  { name: 'data.gov.in Market Prices', status: 'Active', color: 'badge-green' },
                  { name: 'AWS S3 Storage', status: 'Configured', color: 'badge-blue' },
                ].map(({ name, status, color }) => (
                  <div key={name} className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-gray-800/50">
                    <span className="text-sm text-gray-700 dark:text-gray-300">{name}</span>
                    <span className={`${color} text-xs`}>{status}</span>
                  </div>
                ))}
              </div>
            </div>
            <div>
              <h3 className="font-semibold text-gray-900 dark:text-white mb-3">Features</h3>
              <div className="space-y-2">
                {[
                  '✅ JWT Authentication + Role-based Access',
                  '✅ OTP Email Verification & Password Reset',
                  '✅ AI Crop Recommendation (Gemini)',
                  '✅ Weather + 5-Day Forecast',
                  '✅ Plant Disease Detection (HuggingFace)',
                  '✅ Live Market Prices (data.gov.in)',
                  '✅ Government Schemes Database',
                  '✅ Multilingual KisanAI Chatbot',
                  '✅ Notification System',
                  '✅ S3 Image Upload',
                ].map(f => (
                  <p key={f} className="text-sm text-gray-600 dark:text-gray-400 py-1.5 border-b border-gray-50 dark:border-gray-800">{f}</p>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'schemes' && (
        <div className="glass-card p-6">
          <div className="section-header mb-5">
            <h2 className="section-title">Government Schemes</h2>
            <button onClick={() => setShowSchemeForm(o => !o)} className="btn-primary flex items-center gap-2 text-sm">
              <Plus className="w-4 h-4" /> Add Scheme
            </button>
          </div>

          {showSchemeForm && (
            <form onSubmit={handleAddScheme} className="mb-6 p-5 rounded-2xl border-2 border-primary-200 dark:border-primary-900/50 bg-primary-50/30 dark:bg-primary-950/10">
              <h3 className="font-semibold text-gray-900 dark:text-white mb-4">New Government Scheme</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {[
                  { label: 'Title', key: 'title', placeholder: 'PM-KISAN Yojana' },
                  { label: 'Category', key: 'category', placeholder: '', isSelect: true,
                    options: ['SUBSIDY','LOAN','INSURANCE','TRAINING','EQUIPMENT','SEED','FERTILIZER'] },
                  { label: 'Official URL', key: 'officialUrl', placeholder: 'https://...' },
                  { label: 'Applicable States', key: 'applicableStates', placeholder: 'All India' },
                  { label: 'Applicable Crops', key: 'applicableCrops', placeholder: 'All crops' },
                ].map(f => (
                  <div key={f.key}>
                    <label className="label">{f.label}</label>
                    {f.isSelect ? (
                      <select className="select" value={newScheme[f.key]}
                        onChange={e => setNewScheme(s => ({ ...s, [f.key]: e.target.value }))}>
                        {f.options.map(o => <option key={o}>{o}</option>)}
                      </select>
                    ) : (
                      <input className="input" placeholder={f.placeholder}
                        value={newScheme[f.key]}
                        onChange={e => setNewScheme(s => ({ ...s, [f.key]: e.target.value }))} />
                    )}
                  </div>
                ))}
              </div>
              {['description','eligibility','benefits','documentsRequired'].map(k => (
                <div key={k} className="mt-3">
                  <label className="label capitalize">{k.replace(/([A-Z])/g, ' $1').trim()}</label>
                  <textarea rows={2} className="input resize-none"
                    value={newScheme[k] || ''}
                    onChange={e => setNewScheme(s => ({ ...s, [k]: e.target.value }))} />
                </div>
              ))}
              <div className="flex gap-2 mt-4">
                <button type="submit" disabled={addingScheme} className="btn-primary flex items-center gap-2">
                  {addingScheme ? <Loader2 className="w-4 h-4 animate-spin" /> : <Plus className="w-4 h-4" />}
                  Add Scheme
                </button>
                <button type="button" onClick={() => setShowSchemeForm(false)} className="btn-secondary">Cancel</button>
              </div>
            </form>
          )}

          {schemesLoading ? (
            <div className="space-y-3">
              {[...Array(4)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}
            </div>
          ) : (
            <div className="space-y-3">
              {schemes.map(s => (
                <div key={s.id} className="flex items-center gap-4 p-4 rounded-xl border border-gray-100 dark:border-gray-800">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="font-medium text-gray-900 dark:text-white text-sm truncate">{s.title}</h3>
                      <span className="badge-yellow text-xs">{s.category?.replace(/_/g, ' ')}</span>
                      {s.isActive ? <span className="badge-green text-xs">Active</span> : <span className="badge-red text-xs">Inactive</span>}
                    </div>
                  </div>
                  <button onClick={() => handleDeleteScheme(s.id)}
                    className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 rounded-lg transition-colors shrink-0">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === 'broadcast' && (
        <div className="glass-card p-6 max-w-2xl">
          <h2 className="section-title mb-6 flex items-center gap-2">
            <Bell className="w-5 h-5 text-purple-500" /> Broadcast Notification
          </h2>
          <div className="alert-warning mb-5 flex items-start gap-3 text-sm">
            <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
            This will send a notification to ALL registered users on the platform.
          </div>
          <form onSubmit={handleBroadcast} className="space-y-4">
            <div>
              <label className="label">Notification Title</label>
              <input className="input" placeholder="e.g. Heavy Rain Alert for Kharif Farmers"
                value={broadcastForm.title}
                onChange={e => setBroadcastForm(f => ({ ...f, title: e.target.value }))} />
            </div>
            <div>
              <label className="label">Message</label>
              <textarea rows={4} className="input resize-none"
                placeholder="Detailed notification message for farmers…"
                value={broadcastForm.message}
                onChange={e => setBroadcastForm(f => ({ ...f, message: e.target.value }))} />
            </div>
            <div>
              <label className="label">Type</label>
              <select className="select" value={broadcastForm.type}
                onChange={e => setBroadcastForm(f => ({ ...f, type: e.target.value }))}>
                {['INFO','WARNING','ALERT','PROMOTION','SYSTEM'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <button type="submit" disabled={broadcasting} className="btn-primary flex items-center gap-2">
              {broadcasting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Bell className="w-4 h-4" />}
              {broadcasting ? 'Sending…' : 'Send to All Users'}
            </button>
          </form>
        </div>
      )}
    </div>
  )
}
