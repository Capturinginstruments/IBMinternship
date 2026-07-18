import React, { useState, useEffect } from 'react'
import {
  TrendingUp, TrendingDown, Search, RefreshCw, Loader2,
  BarChart2, Info, MapPin, Filter
} from 'lucide-react'
import { marketService } from '../../services/services'
import toast from 'react-hot-toast'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine, BarChart, Bar, Legend
} from 'recharts'

const STATES = ['Maharashtra','Punjab','Haryana','Uttar Pradesh','Rajasthan','Madhya Pradesh',
  'Gujarat','Karnataka','Andhra Pradesh','Telangana','Tamil Nadu','Kerala','West Bengal','Bihar']
const COMMODITIES = ['Wheat','Rice','Tomato','Onion','Potato','Cotton','Sugarcane','Soybean',
  'Maize','Groundnut','Mustard','Gram','Arhar','Jowar','Bajra','Tur']

export default function MarketPage() {
  const [prices, setPrices] = useState([])
  const [trend, setTrend] = useState([])
  const [loading, setLoading] = useState(false)
  const [trendLoading, setTrendLoading] = useState(false)
  const [advice, setAdvice] = useState('')
  const [adviceLoading, setAdviceLoading] = useState(false)
  const [commodity, setCommodity] = useState('Wheat')
  const [state, setState] = useState('Maharashtra')
  const [district, setDistrict] = useState('')
  const [tab, setTab] = useState('prices') // 'prices' | 'trends'

  const fetchPrices = async () => {
    setLoading(true)
    try {
      const res = await marketService.getPrices(commodity, state, district || undefined)
      setPrices(res.data.data)
    } catch { toast.error('Failed to fetch market prices') }
    finally { setLoading(false) }
  }

  const fetchTrend = async () => {
    setTrendLoading(true)
    try {
      const res = await marketService.getTrend(commodity, state, 30)
      setTrend(res.data.data.map(p => ({
        date: p.tradeDate,
        modal: Number(p.modalPrice),
        max: Number(p.maxPrice),
        min: Number(p.minPrice),
      })))
    } catch { toast.error('Failed to load price trend') }
    finally { setTrendLoading(false) }
  }

  const fetchAdvice = async () => {
    setAdviceLoading(true)
    setAdvice('')
    try {
      const res = await marketService.getSellAdvice(commodity, state)
      setAdvice(res.data.data.advice)
    } catch { toast.error('Could not generate advice') }
    finally { setAdviceLoading(false) }
  }

  useEffect(() => { fetchPrices() }, [])

  const avgModal = prices.length > 0
    ? Math.round(prices.reduce((a, p) => a + Number(p.modalPrice), 0) / prices.length)
    : 0
  const maxModal = prices.length > 0 ? Math.max(...prices.map(p => Number(p.modalPrice))) : 0
  const minModal = prices.length > 0 ? Math.min(...prices.map(p => Number(p.modalPrice))) : 0

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-amber-100 dark:bg-amber-900/40 flex items-center justify-center">
            <TrendingUp className="w-6 h-6 text-amber-600" />
          </div>
          <div>
            <h1 className="page-title">Market Prices</h1>
            <p className="page-subtitle">Live mandi prices from data.gov.in + AI sell timing advice</p>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="glass-card p-5 mb-6">
        <div className="flex flex-wrap items-end gap-4">
          <div className="flex-1 min-w-40">
            <label className="label">Commodity</label>
            <select className="select" value={commodity} onChange={e => setCommodity(e.target.value)}>
              {COMMODITIES.map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
          <div className="flex-1 min-w-40">
            <label className="label">State</label>
            <select className="select" value={state} onChange={e => setState(e.target.value)}>
              {STATES.map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="flex-1 min-w-40">
            <label className="label">District (Optional)</label>
            <input className="input" placeholder="All districts" value={district}
              onChange={e => setDistrict(e.target.value)} />
          </div>
          <button onClick={() => { fetchPrices(); fetchTrend() }}
            className="btn-primary flex items-center gap-2">
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            Search
          </button>
        </div>
      </div>

      {/* Summary stats */}
      {prices.length > 0 && (
        <div className="grid grid-cols-3 gap-4 mb-6">
          {[
            { label: 'Avg Modal Price', value: `₹${avgModal.toLocaleString()}`, sub: 'per quintal', color: 'bg-amber-500' },
            { label: 'Highest Price', value: `₹${maxModal.toLocaleString()}`, sub: 'max recorded', color: 'bg-primary-600' },
            { label: 'Lowest Price', value: `₹${minModal.toLocaleString()}`, sub: 'min recorded', color: 'bg-red-500' },
          ].map(({ label, value, sub, color }) => (
            <div key={label} className="glass-card p-5 flex items-center gap-4">
              <div className={`w-12 h-12 rounded-2xl ${color} flex items-center justify-center shrink-0`}>
                <TrendingUp className="w-6 h-6 text-white" />
              </div>
              <div>
                <p className="text-sm text-gray-500 dark:text-gray-400">{label}</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white font-display">{value}</p>
                <p className="text-xs text-gray-400">{sub}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-2 mb-5">
        <button onClick={() => setTab('prices')} className={tab === 'prices' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
          Current Prices
        </button>
        <button onClick={() => { setTab('trends'); fetchTrend() }} className={tab === 'trends' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
          Price Trend
        </button>
        <button onClick={() => { setTab('advice'); fetchAdvice() }} className={tab === 'advice' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
          AI Sell Advice
        </button>
      </div>

      {tab === 'prices' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-5">{commodity} Prices — {state}</h2>
          {loading ? (
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}
            </div>
          ) : prices.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <TrendingUp className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No prices found. Try different filters.</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-100 dark:border-gray-800">
                      {['Market', 'District', 'Min Price', 'Max Price', 'Modal Price', 'Date'].map(h => (
                        <th key={h} className="text-left py-3 px-4 text-gray-500 dark:text-gray-400 font-medium">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {prices.map((p, i) => (
                      <tr key={i} className="border-b border-gray-50 dark:border-gray-800/50 hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors">
                        <td className="py-3 px-4 font-medium text-gray-900 dark:text-white">{p.marketName}</td>
                        <td className="py-3 px-4 text-gray-500 dark:text-gray-400">{p.district}</td>
                        <td className="py-3 px-4 text-gray-700 dark:text-gray-300">₹{Number(p.minPrice).toLocaleString()}</td>
                        <td className="py-3 px-4 text-gray-700 dark:text-gray-300">₹{Number(p.maxPrice).toLocaleString()}</td>
                        <td className="py-3 px-4">
                          <span className="font-bold text-amber-600 dark:text-amber-400">
                            ₹{Number(p.modalPrice).toLocaleString()}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-gray-400 text-xs">{p.tradeDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}

      {tab === 'trends' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-5">{commodity} 30-Day Price Trend — {state}</h2>
          {trendLoading ? (
            <div className="skeleton h-72 rounded-xl" />
          ) : trend.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <BarChart2 className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No trend data available</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={trend}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#9ca3af' }} />
                <YAxis tick={{ fontSize: 11, fill: '#9ca3af' }} tickFormatter={v => `₹${v}`} />
                <Tooltip formatter={(v) => `₹${v.toLocaleString()}`} />
                <Legend />
                <Line type="monotone" dataKey="modal" name="Modal" stroke="#d97706" strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="max" name="Max" stroke="#22c55e" strokeWidth={1.5} dot={false} strokeDasharray="5 5" />
                <Line type="monotone" dataKey="min" name="Min" stroke="#ef4444" strokeWidth={1.5} dot={false} strokeDasharray="5 5" />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      )}

      {tab === 'advice' && (
        <div className="glass-card p-6">
          <h2 className="section-title mb-5 flex items-center gap-2">
            <Info className="w-5 h-5 text-primary-500" /> AI Sell Timing Advice
          </h2>
          {adviceLoading ? (
            <div className="flex flex-col items-center gap-4 py-10">
              <Loader2 className="w-10 h-10 animate-spin text-amber-500" />
              <p className="text-gray-500 dark:text-gray-400">Gemini AI is analyzing market trends…</p>
            </div>
          ) : advice ? (
            <div className="alert-info text-sm leading-relaxed whitespace-pre-line">{advice}</div>
          ) : (
            <div className="text-center py-10 text-gray-500 dark:text-gray-400">
              <Info className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>Select commodity and state then click "AI Sell Advice" tab</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
