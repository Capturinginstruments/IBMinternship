import React, { useState } from 'react'
import {
  Sprout, Loader2, ChevronDown, History, Info,
  Droplets, DollarSign, FlaskConical, Leaf, BarChart3
} from 'lucide-react'
import { cropService } from '../../services/services'
import toast from 'react-hot-toast'
import { RadarChart, PolarGrid, PolarAngleAxis, Radar, ResponsiveContainer, Tooltip } from 'recharts'
import { format } from 'date-fns'

const STATES = ['Maharashtra','Punjab','Haryana','Uttar Pradesh','Rajasthan','Madhya Pradesh',
  'Gujarat','Karnataka','Andhra Pradesh','Telangana','Tamil Nadu','Kerala','West Bengal',
  'Bihar','Odisha','Assam','Himachal Pradesh','Uttarakhand']

const SEASONS = ['SUMMER','WINTER','RAINY']
const SOILS   = ['Alluvial','Black','Red','Laterite','Sandy','Clay','Loamy','Silty']

const defaultForm = {
  state:'Maharashtra', district:'Pune', season:'RAINY', soilType:'Black',
  nitrogen:80, phosphorus:40, potassium:40, temperature:28, humidity:65, rainfall:700, phLevel:6.5
}

const InputField = ({ label, id, unit, min, max, step = 1, value, onChange }) => (
  <div>
    <label className="label" htmlFor={id}>
      {label} {unit && <span className="text-gray-400 font-normal">({unit})</span>}
    </label>
    <input id={id} type="number" min={min} max={max} step={step}
      className="input" value={value} onChange={e => onChange(Number(e.target.value))} />
  </div>
)

export default function CropPage() {
  const [form, setForm] = useState(defaultForm)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [history, setHistory] = useState([])
  const [tab, setTab] = useState('form') // 'form' | 'history'
  const [histLoading, setHistLoading] = useState(false)

  const set = (key) => (val) => setForm(f => ({ ...f, [key]: val }))
  const setStr = (key) => (e) => setForm(f => ({ ...f, [key]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setResult(null)
    try {
      const res = await cropService.recommend(form)
      setResult(res.data.data)
      toast.success('Crop recommendation ready! 🌾')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to get recommendation')
    } finally { setLoading(false) }
  }

  const loadHistory = async () => {
    setTab('history')
    setHistLoading(true)
    try {
      const res = await cropService.getHistory()
      setHistory(res.data.data.content || [])
    } catch { toast.error('Failed to load history') }
    finally { setHistLoading(false) }
  }

  const radarData = result ? [
    { subject: 'Nitrogen', value: Math.min((form.nitrogen / 200) * 100, 100) },
    { subject: 'Phosphorus', value: Math.min((form.phosphorus / 200) * 100, 100) },
    { subject: 'Potassium', value: Math.min((form.potassium / 200) * 100, 100) },
    { subject: 'Humidity', value: form.humidity },
    { subject: 'Rainfall', value: Math.min((form.rainfall / 3000) * 100, 100) },
    { subject: 'pH', value: (form.phLevel / 14) * 100 },
  ] : []

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-primary-100 dark:bg-primary-900/40 flex items-center justify-center">
            <Sprout className="w-6 h-6 text-primary-600 dark:text-primary-400" />
          </div>
          <div>
            <h1 className="page-title">AI Crop Recommendation</h1>
            <p className="page-subtitle">Enter your soil and climate data for the best crop suggestion</p>
          </div>
        </div>
        <div className="flex gap-2 mt-4">
          <button onClick={() => setTab('form')}
            className={tab === 'form' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            Get Recommendation
          </button>
          <button onClick={loadHistory}
            className={tab === 'history' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            <History className="w-4 h-4 inline mr-1" /> History
          </button>
        </div>
      </div>

      {tab === 'form' && (
        <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">
          {/* Form */}
          <form onSubmit={handleSubmit} className="xl:col-span-2 glass-card p-6 space-y-5">
            <h2 className="section-title">Farm Parameters</h2>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">State</label>
                <select className="select" value={form.state} onChange={setStr('state')}>
                  {STATES.map(s => <option key={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label className="label">District</label>
                <input className="input" value={form.district} onChange={setStr('district')} placeholder="e.g. Pune" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Season</label>
                <select className="select" value={form.season} onChange={setStr('season')}>
                  {SEASONS.map(s => <option key={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label className="label">Soil Type</label>
                <select className="select" value={form.soilType} onChange={setStr('soilType')}>
                  {SOILS.map(s => <option key={s}>{s}</option>)}
                </select>
              </div>
            </div>

            <div className="border-t border-gray-100 dark:border-gray-800 pt-4">
              <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Soil Nutrients (kg/ha)</p>
              <div className="grid grid-cols-3 gap-3">
                <InputField label="N" id="n" min={0} max={200} value={form.nitrogen} onChange={set('nitrogen')} />
                <InputField label="P" id="p" min={0} max={200} value={form.phosphorus} onChange={set('phosphorus')} />
                <InputField label="K" id="k" min={0} max={200} value={form.potassium} onChange={set('potassium')} />
              </div>
            </div>

            <div className="border-t border-gray-100 dark:border-gray-800 pt-4">
              <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Climate Data</p>
              <div className="grid grid-cols-2 gap-3">
                <InputField label="Temperature" id="temp" unit="°C" min={-10} max={55} step={0.1} value={form.temperature} onChange={set('temperature')} />
                <InputField label="Humidity" id="hum" unit="%" min={0} max={100} step={0.1} value={form.humidity} onChange={set('humidity')} />
                <InputField label="Rainfall" id="rain" unit="mm" min={0} max={5000} value={form.rainfall} onChange={set('rainfall')} />
                <InputField label="Soil pH" id="ph" min={0} max={14} step={0.1} value={form.phLevel} onChange={set('phLevel')} />
              </div>
            </div>

            <button type="submit" disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 py-3 text-base mt-2">
              {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Sprout className="w-5 h-5" />}
              {loading ? 'Analyzing with AI...' : 'Get AI Recommendation'}
            </button>
          </form>

          {/* Result */}
          <div className="xl:col-span-3 space-y-5">
            {loading && (
              <div className="glass-card p-10 flex flex-col items-center gap-4">
                <div className="w-16 h-16 rounded-full gradient-green flex items-center justify-center animate-pulse-green">
                  <Sprout className="w-8 h-8 text-white" />
                </div>
                <p className="text-lg font-semibold text-gray-900 dark:text-white">AI is analyzing your farm data…</p>
                <p className="text-gray-500 dark:text-gray-400 text-sm text-center">
                  Gemini is evaluating soil nutrients, climate, and seasonal patterns
                </p>
              </div>
            )}

            {result && !loading && (
              <>
                {/* Main result card */}
                <div className="glass-card overflow-hidden">
                  <div className="gradient-hero p-6 text-white">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-primary-200 text-sm font-medium mb-1">Recommended Crop</p>
                        <h2 className="text-4xl font-bold font-display">{result.recommendedCrop}</h2>
                        <p className="text-primary-200 mt-1">{result.season} Season · {result.soilType} Soil</p>
                      </div>
                      <div className="text-right">
                        <div className="text-5xl mb-1">🌾</div>
                        <div className="text-2xl font-bold font-display">{Math.round(result.confidenceScore)}%</div>
                        <p className="text-primary-200 text-sm">Confidence</p>
                      </div>
                    </div>
                    <div className="mt-4 bg-white/20 rounded-full h-2">
                      <div className="h-full rounded-full bg-white transition-all duration-1000"
                        style={{ width: `${result.confidenceScore}%` }} />
                    </div>
                  </div>

                  <div className="p-6 grid grid-cols-2 gap-4">
                    {[
                      { icon: BarChart3, label: 'Expected Yield', value: result.expectedYield, color: 'text-primary-600' },
                      { icon: DollarSign, label: 'Profit Estimate', value: result.profitEstimate, color: 'text-amber-600' },
                      { icon: Droplets, label: 'Water Requirement', value: result.waterRequirement, color: 'text-sky-600' },
                      { icon: FlaskConical, label: 'Soil pH', value: `${result.phLevel}`, color: 'text-purple-600' },
                    ].map(({ icon: Icon, label, value, color }) => (
                      <div key={label} className="flex items-start gap-3 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/50">
                        <Icon className={`w-5 h-5 mt-0.5 shrink-0 ${color}`} />
                        <div>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{label}</p>
                          <p className="text-sm font-semibold text-gray-900 dark:text-white leading-tight">{value || '—'}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Fertilizer & AI Explanation */}
                {result.fertilizerAdvice && (
                  <div className="glass-card p-6">
                    <h3 className="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
                      <FlaskConical className="w-5 h-5 text-purple-500" /> Fertilizer Advice
                    </h3>
                    <p className="text-gray-700 dark:text-gray-300 text-sm leading-relaxed">{result.fertilizerAdvice}</p>
                  </div>
                )}

                {result.geminiExplanation && (
                  <div className="glass-card p-6">
                    <h3 className="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
                      <Leaf className="w-5 h-5 text-primary-500" /> Why This Crop?
                    </h3>
                    <p className="text-gray-700 dark:text-gray-300 text-sm leading-relaxed">{result.geminiExplanation}</p>
                  </div>
                )}

                {/* Soil nutrient radar */}
                <div className="glass-card p-6">
                  <h3 className="font-semibold text-gray-900 dark:text-white mb-4">Soil Profile Radar</h3>
                  <ResponsiveContainer width="100%" height={220}>
                    <RadarChart data={radarData}>
                      <PolarGrid stroke="#e5e7eb" />
                      <PolarAngleAxis dataKey="subject" tick={{ fontSize: 12, fill: '#6b7280' }} />
                      <Radar name="Farm" dataKey="value" stroke="#16a34a" fill="#22c55e" fillOpacity={0.25} strokeWidth={2} />
                      <Tooltip formatter={(v) => `${Math.round(v)}%`} />
                    </RadarChart>
                  </ResponsiveContainer>
                </div>
              </>
            )}

            {!result && !loading && (
              <div className="glass-card p-12 flex flex-col items-center gap-4 text-center">
                <div className="w-20 h-20 rounded-3xl bg-primary-50 dark:bg-primary-950/40 flex items-center justify-center">
                  <Sprout className="w-10 h-10 text-primary-400" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white font-display">Ready to Analyze</h3>
                <p className="text-gray-500 dark:text-gray-400 max-w-xs">
                  Fill in your soil parameters and climate data on the left, then click "Get AI Recommendation".
                </p>
              </div>
            )}
          </div>
        </div>
      )}

      {tab === 'history' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-6">Recommendation History</h2>
          {histLoading ? (
            <div className="space-y-3">
              {[...Array(3)].map((_, i) => <div key={i} className="skeleton h-24 rounded-xl" />)}
            </div>
          ) : history.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <Sprout className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No recommendations yet. Get your first one!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {history.map((rec) => (
                <div key={rec.id} className="flex items-center gap-4 p-4 rounded-xl border border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                  <div className="w-12 h-12 rounded-xl gradient-green flex items-center justify-center shrink-0">
                    <Sprout className="w-6 h-6 text-white" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-gray-900 dark:text-white">{rec.recommendedCrop}</h3>
                      <span className="badge-green">{Math.round(rec.confidenceScore)}% confidence</span>
                      <span className="badge-gray">{rec.season}</span>
                    </div>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                      {rec.state}, {rec.district} · {format(new Date(rec.createdAt), 'dd MMM yyyy')}
                    </p>
                  </div>
                  <div className="text-right shrink-0">
                    <p className="text-sm font-semibold text-primary-600 dark:text-primary-400">{rec.profitEstimate?.split('-')[0]}</p>
                    <p className="text-xs text-gray-400">est. profit</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
