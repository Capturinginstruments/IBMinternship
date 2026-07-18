import React, { useState, useCallback } from 'react'
import { useDropzone } from 'react-dropzone'
import {
  Bug, Upload, ImageIcon, Loader2, CheckCircle, AlertTriangle,
  FlaskConical, Shield, History, X, Eye
} from 'lucide-react'
import { diseaseService } from '../../services/services'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

const ConfidenceBadge = ({ score }) => {
  if (score >= 80) return <span className="badge-green">High Confidence {Math.round(score)}%</span>
  if (score >= 50) return <span className="badge-yellow">Medium Confidence {Math.round(score)}%</span>
  return <span className="badge-red">Low Confidence {Math.round(score)}%</span>
}

export default function DiseasePage() {
  const [image, setImage] = useState(null)
  const [preview, setPreview] = useState(null)
  const [cropType, setCropType] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [history, setHistory] = useState([])
  const [tab, setTab] = useState('detect')
  const [histLoading, setHistLoading] = useState(false)

  const onDrop = useCallback((accepted) => {
    if (!accepted.length) return toast.error('Invalid file type')
    const file = accepted[0]
    setImage(file)
    setPreview(URL.createObjectURL(file))
    setResult(null)
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'image/jpeg': [], 'image/png': [], 'image/webp': [] },
    maxSize: 10 * 1024 * 1024,
    multiple: false
  })

  const handleDetect = async () => {
    if (!image) return toast.error('Please upload a crop image')
    setLoading(true)
    const fd = new FormData()
    fd.append('image', image)
    if (cropType) fd.append('cropType', cropType)
    try {
      const res = await diseaseService.detect(fd)
      setResult(res.data.data)
      toast.success('Analysis complete!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Detection failed. Please try again.')
    } finally { setLoading(false) }
  }

  const loadHistory = async () => {
    setTab('history')
    setHistLoading(true)
    try {
      const res = await diseaseService.getHistory()
      setHistory(res.data.data.content || [])
    } catch { toast.error('Failed to load history') }
    finally { setHistLoading(false) }
  }

  const handleResolve = async (id) => {
    try {
      await diseaseService.markResolved(id)
      setHistory(h => h.map(r => r.id === id ? { ...r, isResolved: true } : r))
      toast.success('Marked as resolved')
    } catch { toast.error('Failed to update') }
  }

  const clearImage = () => { setImage(null); setPreview(null); setResult(null) }

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-red-100 dark:bg-red-900/40 flex items-center justify-center">
            <Bug className="w-6 h-6 text-red-600" />
          </div>
          <div>
            <h1 className="page-title">Plant Disease Detection</h1>
            <p className="page-subtitle">Upload a crop photo for instant AI-powered disease analysis</p>
          </div>
        </div>
        <div className="flex gap-2 mt-4">
          <button onClick={() => setTab('detect')}
            className={tab === 'detect' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            Detect Disease
          </button>
          <button onClick={loadHistory}
            className={tab === 'history' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            <History className="w-4 h-4 inline mr-1" /> History
          </button>
        </div>
      </div>

      {tab === 'detect' && (
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
          {/* Upload area */}
          <div className="space-y-5">
            <div className="glass-card p-6">
              {preview ? (
                <div className="relative">
                  <img src={preview} alt="Crop" className="w-full h-72 object-cover rounded-2xl" />
                  <button onClick={clearImage}
                    className="absolute top-3 right-3 w-8 h-8 bg-black/50 rounded-full flex items-center justify-center text-white hover:bg-red-500 transition-colors">
                    <X className="w-4 h-4" />
                  </button>
                  <div className="absolute bottom-3 left-3">
                    <span className="px-3 py-1 bg-black/60 text-white text-xs rounded-full backdrop-blur-sm">
                      {image?.name}
                    </span>
                  </div>
                </div>
              ) : (
                <div {...getRootProps()} className={`border-2 border-dashed rounded-2xl p-12 text-center cursor-pointer transition-all duration-200 ${
                  isDragActive
                    ? 'border-primary-400 bg-primary-50 dark:bg-primary-950/30'
                    : 'border-gray-300 dark:border-gray-700 hover:border-primary-400 hover:bg-gray-50 dark:hover:bg-gray-800/30'
                }`}>
                  <input {...getInputProps()} />
                  <Upload className={`w-10 h-10 mx-auto mb-3 ${isDragActive ? 'text-primary-500' : 'text-gray-400'}`} />
                  <p className="font-semibold text-gray-700 dark:text-gray-300 mb-1">
                    {isDragActive ? 'Drop the image here' : 'Drag & drop your crop photo'}
                  </p>
                  <p className="text-sm text-gray-400">or click to browse · JPEG, PNG, WebP · Max 10MB</p>
                </div>
              )}

              <div className="mt-4">
                <label className="label">Crop Type (Optional)</label>
                <input className="input" placeholder="e.g. Tomato, Wheat, Rice, Cotton"
                  value={cropType} onChange={e => setCropType(e.target.value)} />
              </div>

              <button onClick={handleDetect} disabled={!image || loading}
                className="btn-primary w-full flex items-center justify-center gap-2 py-3 mt-4">
                {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Bug className="w-5 h-5" />}
                {loading ? 'Analyzing Image…' : 'Detect Disease'}
              </button>
            </div>

            <div className="glass-card p-5">
              <h3 className="font-semibold text-gray-900 dark:text-white mb-3 text-sm">📸 Tips for Best Results</h3>
              <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
                <li className="flex items-start gap-2"><CheckCircle className="w-4 h-4 text-primary-500 mt-0.5 shrink-0" /> Take a close-up photo of the affected area</li>
                <li className="flex items-start gap-2"><CheckCircle className="w-4 h-4 text-primary-500 mt-0.5 shrink-0" /> Ensure good natural lighting (avoid flash)</li>
                <li className="flex items-start gap-2"><CheckCircle className="w-4 h-4 text-primary-500 mt-0.5 shrink-0" /> Include the whole leaf, stem, or fruit</li>
                <li className="flex items-start gap-2"><CheckCircle className="w-4 h-4 text-primary-500 mt-0.5 shrink-0" /> Avoid blurry or dark images</li>
              </ul>
            </div>
          </div>

          {/* Result */}
          <div>
            {loading && (
              <div className="glass-card p-12 flex flex-col items-center gap-4 text-center">
                <div className="w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/40 flex items-center justify-center">
                  <Bug className="w-8 h-8 text-red-500 animate-bounce-slow" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-white">Analyzing your crop image…</p>
                <p className="text-gray-500 dark:text-gray-400 text-sm">
                  Running HuggingFace plant disease model + Gemini AI analysis
                </p>
                <div className="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-2 mt-2">
                  <div className="h-full rounded-full bg-red-500 animate-pulse" style={{ width: '60%' }} />
                </div>
              </div>
            )}

            {result && !loading && (
              <div className="space-y-5 animate-slide-up">
                {/* Disease header */}
                <div className={`glass-card overflow-hidden ${result.diseaseName?.toLowerCase().includes('healthy') ? 'border-2 border-primary-400' : 'border-2 border-red-400'}`}>
                  <div className={`p-5 ${result.diseaseName?.toLowerCase().includes('healthy') ? 'bg-primary-50 dark:bg-primary-950/40' : 'bg-red-50 dark:bg-red-950/30'}`}>
                    <div className="flex items-center justify-between flex-wrap gap-3">
                      <div className="flex items-center gap-3">
                        {result.diseaseName?.toLowerCase().includes('healthy')
                          ? <CheckCircle className="w-8 h-8 text-primary-500" />
                          : <AlertTriangle className="w-8 h-8 text-red-500" />}
                        <div>
                          <h2 className="text-xl font-bold text-gray-900 dark:text-white font-display">
                            {result.diseaseName}
                          </h2>
                          <p className="text-sm text-gray-500 dark:text-gray-400">
                            {result.cropType || 'Crop'} · Detected by AI
                          </p>
                        </div>
                      </div>
                      <ConfidenceBadge score={result.confidenceScore || 0} />
                    </div>
                  </div>

                  {result.imageUrl && (
                    <div className="px-5 pb-4">
                      <img src={result.imageUrl} alt="Analyzed" className="w-full h-40 object-cover rounded-xl mt-3" />
                    </div>
                  )}
                </div>

                {/* Details */}
                {[
                  { icon: Bug, label: 'Explanation', value: result.geminiExplanation, color: 'text-gray-600' },
                  { icon: Shield, label: 'Treatment', value: result.treatment, color: 'text-green-600' },
                  { icon: FlaskConical, label: 'Medicines / Pesticides', value: result.medicine, color: 'text-blue-600' },
                  { icon: CheckCircle, label: 'Prevention', value: result.prevention, color: 'text-amber-600' },
                ].filter(d => d.value).map(({ icon: Icon, label, value, color }) => (
                  <div key={label} className="glass-card p-5">
                    <h3 className="font-semibold text-gray-900 dark:text-white mb-2 flex items-center gap-2">
                      <Icon className={`w-5 h-5 ${color}`} /> {label}
                    </h3>
                    <p className="text-sm text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-line">{value}</p>
                  </div>
                ))}
              </div>
            )}

            {!result && !loading && (
              <div className="glass-card p-12 flex flex-col items-center gap-4 text-center">
                <div className="w-20 h-20 rounded-3xl bg-red-50 dark:bg-red-950/30 flex items-center justify-center">
                  <ImageIcon className="w-10 h-10 text-red-300" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white font-display">Upload a Crop Photo</h3>
                <p className="text-gray-500 dark:text-gray-400 max-w-xs">
                  Upload a clear photo of the affected plant part and click "Detect Disease" for instant AI analysis.
                </p>
              </div>
            )}
          </div>
        </div>
      )}

      {tab === 'history' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-6">Detection History</h2>
          {histLoading ? (
            <div className="space-y-3">
              {[...Array(3)].map((_, i) => <div key={i} className="skeleton h-24 rounded-xl" />)}
            </div>
          ) : history.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <Bug className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No disease detections yet</p>
            </div>
          ) : (
            <div className="space-y-4">
              {history.map((rec) => (
                <div key={rec.id} className="flex items-center gap-4 p-4 rounded-xl border border-gray-100 dark:border-gray-800">
                  {rec.imageUrl
                    ? <img src={rec.imageUrl} alt="crop" className="w-16 h-16 rounded-xl object-cover shrink-0" />
                    : <div className="w-16 h-16 rounded-xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center shrink-0">
                        <Bug className="w-6 h-6 text-gray-400" />
                      </div>
                  }
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="font-semibold text-gray-900 dark:text-white text-sm">{rec.diseaseName}</h3>
                      <ConfidenceBadge score={rec.confidenceScore || 0} />
                      {rec.isResolved && <span className="badge-green">Resolved</span>}
                    </div>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                      {rec.cropType || 'Unknown crop'} · {format(new Date(rec.createdAt), 'dd MMM yyyy HH:mm')}
                    </p>
                  </div>
                  {!rec.isResolved && (
                    <button onClick={() => handleResolve(rec.id)}
                      className="btn-secondary text-xs px-3 py-1.5 shrink-0">
                      Mark Resolved
                    </button>
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
