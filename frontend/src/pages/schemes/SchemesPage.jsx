import React, { useState, useEffect } from 'react'
import {
  BookOpen, Search, Bookmark, BookmarkCheck, ExternalLink,
  Filter, ChevronRight, Loader2, Calendar, MapPin, X
} from 'lucide-react'
import { schemeService } from '../../services/services'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

const CATEGORIES = ['ALL','SUBSIDY','LOAN','INSURANCE','TRAINING','EQUIPMENT','SEED','FERTILIZER']

const SchemeCard = ({ scheme, onBookmark }) => (
  <div className="glass-card-hover p-5">
    <div className="flex items-start justify-between gap-3 mb-3">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap mb-1.5">
          <span className="badge-yellow text-xs">{scheme.category?.replace(/_/g, ' ')}</span>
          {scheme.isActive && <span className="badge-green text-xs">Active</span>}
          {scheme.deadline && new Date(scheme.deadline) < new Date()
            ? <span className="badge-red text-xs">Deadline Passed</span>
            : scheme.deadline
            ? <span className="badge-blue text-xs">Deadline: {format(new Date(scheme.deadline), 'dd MMM yyyy')}</span>
            : null}
        </div>
        <h3 className="font-semibold text-gray-900 dark:text-white leading-snug">{scheme.title}</h3>
      </div>
      <button onClick={() => onBookmark(scheme.id)}
        className={`shrink-0 p-2 rounded-xl transition-all duration-200 ${
          scheme.isBookmarked
            ? 'text-amber-500 bg-amber-50 dark:bg-amber-900/30 hover:bg-amber-100'
            : 'text-gray-400 hover:text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-900/20'
        }`}>
        {scheme.isBookmarked ? <BookmarkCheck className="w-5 h-5" /> : <Bookmark className="w-5 h-5" />}
      </button>
    </div>

    <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed line-clamp-3 mb-4">
      {scheme.description}
    </p>

    {scheme.benefits && (
      <div className="bg-primary-50 dark:bg-primary-950/30 rounded-xl p-3 mb-4">
        <p className="text-xs font-semibold text-primary-700 dark:text-primary-300 mb-1">Key Benefits</p>
        <p className="text-xs text-primary-600 dark:text-primary-400 line-clamp-2">{scheme.benefits}</p>
      </div>
    )}

    <div className="flex items-center justify-between">
      <div className="flex items-center gap-3 text-xs text-gray-400">
        {scheme.applicableStates && (
          <span className="flex items-center gap-1">
            <MapPin className="w-3 h-3" /> {scheme.applicableStates.split(',')[0]}…
          </span>
        )}
      </div>
      {scheme.officialUrl && (
        <a href={scheme.officialUrl} target="_blank" rel="noopener noreferrer"
          className="text-xs text-primary-600 dark:text-primary-400 flex items-center gap-1 hover:underline font-medium">
          Official Site <ExternalLink className="w-3 h-3" />
        </a>
      )}
    </div>
  </div>
)

export default function SchemesPage() {
  const [schemes, setSchemes] = useState([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('ALL')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [tab, setTab] = useState('all') // 'all' | 'bookmarks'
  const [bookmarks, setBookmarks] = useState([])

  const loadSchemes = async (pg = 0, cat = category, q = query) => {
    setLoading(true)
    try {
      let res
      if (q.trim()) {
        res = await schemeService.search(q, pg)
      } else if (cat && cat !== 'ALL') {
        res = await schemeService.filter({ category: cat, page: pg, size: 12 })
      } else {
        res = await schemeService.getAll(pg, 12)
      }
      setSchemes(res.data.data.content || [])
      setTotalPages(res.data.data.totalPages || 0)
      setPage(pg)
    } catch { toast.error('Failed to load schemes') }
    finally { setLoading(false) }
  }

  const loadBookmarks = async () => {
    setLoading(true)
    try {
      const res = await schemeService.getBookmarks()
      setBookmarks(res.data.data.content || [])
    } catch { toast.error('Failed to load bookmarks') }
    finally { setLoading(false) }
  }

  useEffect(() => { loadSchemes() }, [])

  const handleBookmark = async (id) => {
    try {
      const res = await schemeService.toggleBookmark(id)
      const isBookmarked = res.data.data.isBookmarked
      toast.success(isBookmarked ? 'Scheme bookmarked!' : 'Bookmark removed')
      setSchemes(s => s.map(sc => sc.id === id ? { ...sc, isBookmarked } : sc))
      setBookmarks(b => b.map(sc => sc.id === id ? { ...sc, isBookmarked } : sc))
    } catch { toast.error('Failed to update bookmark') }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    loadSchemes(0, category, query)
  }

  const handleCategoryChange = (cat) => {
    setCategory(cat)
    loadSchemes(0, cat, query)
  }

  return (
    <div className="page-container animate-fade-in">
      <div className="page-header">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-amber-100 dark:bg-amber-900/40 flex items-center justify-center">
            <BookOpen className="w-6 h-6 text-amber-600" />
          </div>
          <div>
            <h1 className="page-title">Government Schemes</h1>
            <p className="page-subtitle">Explore central and state agricultural schemes and subsidies</p>
          </div>
        </div>
      </div>

      {/* Search and tabs */}
      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <form onSubmit={handleSearch} className="flex gap-2 flex-1">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input className="input pl-10" placeholder="Search schemes (e.g. PM-KISAN, crop insurance…)"
              value={query} onChange={e => setQuery(e.target.value)} />
          </div>
          <button type="submit" className="btn-primary shrink-0">Search</button>
        </form>
        <div className="flex gap-2 shrink-0">
          <button onClick={() => { setTab('all'); loadSchemes() }}
            className={tab === 'all' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            All Schemes
          </button>
          <button onClick={() => { setTab('bookmarks'); loadBookmarks() }}
            className={tab === 'bookmarks' ? 'btn-primary text-sm px-4 py-2' : 'btn-secondary text-sm px-4 py-2'}>
            <BookmarkCheck className="w-4 h-4 inline mr-1" /> Saved
          </button>
        </div>
      </div>

      {/* Category filter */}
      {tab === 'all' && (
        <div className="flex gap-2 flex-wrap mb-6">
          {CATEGORIES.map(cat => (
            <button key={cat}
              onClick={() => handleCategoryChange(cat)}
              className={`px-3 py-1.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                category === cat
                  ? 'bg-primary-600 text-white'
                  : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-400 border border-gray-200 dark:border-gray-700 hover:border-primary-400'
              }`}>
              {cat === 'ALL' ? 'All' : cat.replace(/_/g, ' ')}
            </button>
          ))}
        </div>
      )}

      {/* Schemes grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {[...Array(6)].map((_, i) => <div key={i} className="skeleton h-64 rounded-2xl" />)}
        </div>
      ) : (
        <>
          {tab === 'all' && schemes.length === 0 && (
            <div className="text-center py-16 text-gray-500 dark:text-gray-400">
              <BookOpen className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No schemes found matching your search</p>
            </div>
          )}
          {tab === 'bookmarks' && bookmarks.length === 0 && (
            <div className="text-center py-16 text-gray-500 dark:text-gray-400">
              <Bookmark className="w-12 h-12 mx-auto mb-3 text-gray-300" />
              <p>No bookmarked schemes yet. Browse and bookmark schemes to save them here.</p>
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {(tab === 'all' ? schemes : bookmarks).map(s => (
              <SchemeCard key={s.id} scheme={s} onBookmark={handleBookmark} />
            ))}
          </div>

          {/* Pagination */}
          {tab === 'all' && totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 mt-8">
              <button onClick={() => loadSchemes(page - 1)} disabled={page === 0}
                className="btn-secondary text-sm px-4 py-2 disabled:opacity-40">← Prev</button>
              <span className="text-sm text-gray-500 dark:text-gray-400">
                Page {page + 1} of {totalPages}
              </span>
              <button onClick={() => loadSchemes(page + 1)} disabled={page >= totalPages - 1}
                className="btn-secondary text-sm px-4 py-2 disabled:opacity-40">Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
