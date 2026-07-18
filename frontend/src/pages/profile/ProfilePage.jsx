import React, { useState, useEffect } from 'react'
import {
  User, Edit3, Save, X, Camera, Loader2, Bell, BellOff,
  Check, Shield, Mail, Phone, MapPin, Leaf, Droplets
} from 'lucide-react'
import { profileService, notificationService } from '../../services/services'
import { useAuthStore } from '../../store/store'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore()
  const [profile, setProfile] = useState(null)
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [tab, setTab] = useState('profile') // 'profile' | 'notifications' | 'security'
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [form, setForm] = useState({
    firstName: '', lastName: '', phone: '',
    state: '', district: '', village: '', landAcres: '',
    soilType: '', primaryCrop: '', secondaryCrop: '', waterSource: ''
  })

  useEffect(() => {
    profileService.getProfile()
      .then(res => {
        setProfile(res.data.data)
        setForm({
          firstName: res.data.data.firstName || '',
          lastName: res.data.data.lastName || '',
          phone: res.data.data.phone || '',
          state: '', district: '', village: '',
          landAcres: '', soilType: '', primaryCrop: '',
          secondaryCrop: '', waterSource: ''
        })
      })
      .catch(() => toast.error('Failed to load profile'))
      .finally(() => setLoading(false))
  }, [])

  const loadNotifications = async () => {
    setTab('notifications')
    try {
      const res = await notificationService.getAll()
      setNotifications(res.data.data.content || [])
    } catch { toast.error('Failed to load notifications') }
  }

  const handleSave = async () => {
    setSaving(true)
    const fd = new FormData()
    fd.append('data', new Blob([JSON.stringify(form)], { type: 'application/json' }))
    if (imageFile) fd.append('profileImage', imageFile)
    try {
      const res = await profileService.updateProfile(fd)
      const updated = res.data.data
      setProfile(updated)
      updateUser(updated)
      setEditing(false)
      setImageFile(null)
      setImagePreview(null)
      toast.success('Profile updated successfully!')
    } catch { toast.error('Failed to update profile') }
    finally { setSaving(false) }
  }

  const handleMarkRead = async (id) => {
    try {
      await notificationService.markRead(id)
      setNotifications(n => n.map(x => x.id === id ? { ...x, isRead: true } : x))
    } catch {}
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllRead()
      setNotifications(n => n.map(x => ({ ...x, isRead: true })))
      toast.success('All notifications marked as read')
    } catch {}
  }

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }))

  if (loading) return (
    <div className="page-container">
      <div className="skeleton h-64 rounded-2xl mb-6" />
      <div className="skeleton h-96 rounded-2xl" />
    </div>
  )

  const roleColor = { FARMER: 'badge-green', ADMIN: 'badge-red', OFFICER: 'badge-blue' }

  return (
    <div className="page-container animate-fade-in">
      <h1 className="page-title mb-6">My Profile</h1>

      {/* Profile header card */}
      <div className="glass-card p-6 mb-6 flex flex-col sm:flex-row items-start sm:items-center gap-6">
        {/* Avatar */}
        <div className="relative">
          {imagePreview || profile?.profileImageUrl ? (
            <img src={imagePreview || profile.profileImageUrl} alt="avatar"
              className="w-24 h-24 rounded-2xl object-cover" />
          ) : (
            <div className="w-24 h-24 rounded-2xl gradient-green flex items-center justify-center text-white text-3xl font-bold font-display">
              {profile?.firstName?.[0]}{profile?.lastName?.[0]}
            </div>
          )}
          {editing && (
            <label className="absolute -bottom-2 -right-2 w-8 h-8 bg-primary-600 rounded-xl flex items-center justify-center cursor-pointer shadow-green hover:bg-primary-700 transition-colors">
              <Camera className="w-4 h-4 text-white" />
              <input type="file" accept="image/*" className="hidden"
                onChange={e => {
                  const f = e.target.files?.[0]
                  if (f) { setImageFile(f); setImagePreview(URL.createObjectURL(f)) }
                }} />
            </label>
          )}
        </div>

        <div className="flex-1">
          <div className="flex items-center gap-3 flex-wrap mb-1">
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white font-display">
              {profile?.firstName} {profile?.lastName}
            </h2>
            <span className={roleColor[profile?.role] || 'badge-gray'}>{profile?.role}</span>
            {profile?.isEmailVerified && (
              <span className="badge-green flex items-center gap-1">
                <Shield className="w-3 h-3" /> Verified
              </span>
            )}
          </div>
          <div className="flex flex-wrap gap-4 text-sm text-gray-500 dark:text-gray-400">
            <span className="flex items-center gap-1.5"><Mail className="w-4 h-4" /> {profile?.email}</span>
            {profile?.phone && <span className="flex items-center gap-1.5"><Phone className="w-4 h-4" /> {profile.phone}</span>}
          </div>
        </div>

        <div className="flex gap-2 shrink-0">
          {editing ? (
            <>
              <button onClick={() => { setEditing(false); setImageFile(null); setImagePreview(null) }} className="btn-secondary flex items-center gap-2 text-sm">
                <X className="w-4 h-4" /> Cancel
              </button>
              <button onClick={handleSave} disabled={saving} className="btn-primary flex items-center gap-2 text-sm">
                {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                {saving ? 'Saving…' : 'Save'}
              </button>
            </>
          ) : (
            <button onClick={() => setEditing(true)} className="btn-primary flex items-center gap-2 text-sm">
              <Edit3 className="w-4 h-4" /> Edit Profile
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        {[
          { key: 'profile', label: 'Profile Details' },
          { key: 'notifications', label: 'Notifications' },
        ].map(t => (
          <button key={t.key}
            onClick={() => t.key === 'notifications' ? loadNotifications() : setTab('profile')}
            className={tab === t.key ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'profile' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-6">Personal Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {[
              { label: 'First Name', key: 'firstName', type: 'text', placeholder: 'Ramesh' },
              { label: 'Last Name', key: 'lastName', type: 'text', placeholder: 'Kumar' },
              { label: 'Phone Number', key: 'phone', type: 'tel', placeholder: '9876543210' },
              { label: 'State', key: 'state', type: 'text', placeholder: 'Maharashtra' },
              { label: 'District', key: 'district', type: 'text', placeholder: 'Pune' },
              { label: 'Village', key: 'village', type: 'text', placeholder: 'Village name' },
              { label: 'Land Area (acres)', key: 'landAcres', type: 'number', placeholder: '5.5' },
              { label: 'Primary Crop', key: 'primaryCrop', type: 'text', placeholder: 'Wheat' },
              { label: 'Secondary Crop', key: 'secondaryCrop', type: 'text', placeholder: 'Soybean' },
              { label: 'Water Source', key: 'waterSource', type: 'text', placeholder: 'Borewell, Canal…' },
            ].map(f => (
              <div key={f.key}>
                <label className="label">{f.label}</label>
                {editing ? (
                  <input type={f.type} className="input" placeholder={f.placeholder}
                    value={form[f.key]} onChange={set(f.key)} />
                ) : (
                  <div className="px-4 py-3 rounded-xl bg-gray-50 dark:bg-gray-800/50 text-sm text-gray-900 dark:text-gray-100 min-h-[3rem]">
                    {form[f.key] || <span className="text-gray-400 italic">Not set</span>}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'notifications' && (
        <div className="glass-card p-6">
          <div className="section-header mb-6">
            <h2 className="section-title">Notifications</h2>
            <button onClick={handleMarkAllRead} className="btn-secondary text-sm px-3 py-1.5 flex items-center gap-1">
              <Check className="w-4 h-4" /> Mark all read
            </button>
          </div>
          {notifications.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <Bell className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No notifications yet</p>
            </div>
          ) : (
            <div className="space-y-3">
              {notifications.map(n => (
                <div key={n.id}
                  className={`flex items-start gap-4 p-4 rounded-xl border transition-all duration-200 ${
                    n.isRead
                      ? 'border-gray-100 dark:border-gray-800 bg-transparent'
                      : 'border-primary-200 dark:border-primary-900/50 bg-primary-50/50 dark:bg-primary-950/20'
                  }`}>
                  <div className={`w-2.5 h-2.5 rounded-full mt-1.5 shrink-0 ${n.isRead ? 'bg-gray-300' : 'bg-primary-500'}`} />
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900 dark:text-white text-sm">{n.title}</p>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{n.message}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      {format(new Date(n.createdAt), 'dd MMM yyyy HH:mm')}
                    </p>
                  </div>
                  {!n.isRead && (
                    <button onClick={() => handleMarkRead(n.id)}
                      className="text-xs text-primary-600 hover:underline shrink-0">Mark read</button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
