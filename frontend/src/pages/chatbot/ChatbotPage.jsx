import React, { useState, useEffect, useRef, useCallback } from 'react'
import {
  MessageSquare, Send, Loader2, Plus, Trash2, Globe,
  Bot, User, ImageIcon, X, ChevronDown
} from 'lucide-react'
import { chatService } from '../../services/services'
import { useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { format } from 'date-fns'
import { useDropzone } from 'react-dropzone'

const LANGUAGES = [
  { code: 'en', label: '🇬🇧 English' },
  { code: 'hi', label: '🇮🇳 Hindi' },
  { code: 'mr', label: '🍊 Marathi' },
]

const SUGGESTED_QUERIES = [
  'What crops should I plant in October in Maharashtra?',
  'My tomato leaves are turning yellow. What should I do?',
  'Which fertilizer is best for wheat crop?',
  'How to control whitefly in cotton?',
  'What is the best time to irrigate rice?',
  'PM-KISAN scheme eligibility criteria',
]

const MessageBubble = ({ msg }) => {
  const isUser = msg.role === 'USER'
  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : 'flex-row'} animate-slide-up`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
        isUser ? 'gradient-green' : 'bg-primary-100 dark:bg-primary-900/50'
      }`}>
        {isUser
          ? <User className="w-4 h-4 text-white" />
          : <Bot className="w-4 h-4 text-primary-600 dark:text-primary-400" />}
      </div>
      <div className={isUser ? 'chat-bubble-user' : 'chat-bubble-ai'}>
        {msg.imageUrl && (
          <img src={msg.imageUrl} alt="attachment" className="w-40 h-32 object-cover rounded-xl mb-2" />
        )}
        <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.message}</p>
        <p className={`text-xs mt-1.5 ${isUser ? 'text-primary-200' : 'text-gray-400'}`}>
          {msg.createdAt ? format(new Date(msg.createdAt), 'HH:mm') : ''}
        </p>
      </div>
    </div>
  )
}

export default function ChatbotPage() {
  const [searchParams] = useSearchParams()
  const [sessions, setSessions] = useState([])
  const [sessionId, setSessionId] = useState(searchParams.get('session') || '')
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [language, setLanguage] = useState('en')
  const [loading, setLoading] = useState(false)
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const bottomRef = useRef(null)
  const inputRef = useRef(null)

  const scrollToBottom = () => bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  useEffect(() => { scrollToBottom() }, [messages])

  useEffect(() => {
    loadSessions()
    if (sessionId) loadHistory(sessionId)
  }, [])

  const loadSessions = async () => {
    try {
      const res = await chatService.getSessions()
      setSessions(res.data.data || [])
    } catch {}
  }

  const loadHistory = async (sid) => {
    setLoading(true)
    try {
      const res = await chatService.getSessionHistory(sid)
      setMessages(res.data.data || [])
    } catch {}
    finally { setLoading(false) }
  }

  const startNewChat = () => {
    setSessionId('')
    setMessages([])
    setInput('')
    inputRef.current?.focus()
  }

  const selectSession = (sid) => {
    setSessionId(sid)
    loadHistory(sid)
  }

  const deleteSession = async (sid, e) => {
    e.stopPropagation()
    try {
      await chatService.deleteSession(sid)
      setSessions(s => s.filter(x => x !== sid))
      if (sid === sessionId) startNewChat()
      toast.success('Chat deleted')
    } catch { toast.error('Failed to delete chat') }
  }

  const onDrop = useCallback((accepted) => {
    if (!accepted.length) return
    const file = accepted[0]
    setImageFile(file)
    setImagePreview(URL.createObjectURL(file))
  }, [])

  const { getRootProps, getInputProps } = useDropzone({
    onDrop, accept: { 'image/*': [] }, maxSize: 5 * 1024 * 1024, multiple: false, noClick: true
  })

  const sendMessage = async () => {
    const text = input.trim()
    if (!text && !imageFile) return
    setInput('')

    let imageBase64 = null
    let imageMimeType = null
    if (imageFile) {
      const buf = await imageFile.arrayBuffer()
      imageBase64 = btoa(String.fromCharCode(...new Uint8Array(buf)))
      imageMimeType = imageFile.type
      setImageFile(null)
      setImagePreview(null)
    }

    const userMsg = {
      id: Date.now(), role: 'USER', message: text, createdAt: new Date().toISOString(),
      imageUrl: imagePreview
    }
    setMessages(m => [...m, userMsg])
    setLoading(true)

    try {
      const res = await chatService.sendMessage({
        sessionId: sessionId || undefined,
        message: text, imageBase64, imageMimeType, language
      })
      const reply = res.data.data
      if (!sessionId && reply.sessionId) {
        setSessionId(reply.sessionId)
        setSessions(s => [reply.sessionId, ...s.filter(x => x !== reply.sessionId)])
      }
      setMessages(m => [...m, reply])
    } catch { toast.error('Failed to send message') }
    finally { setLoading(false) }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return (
    <div className="flex h-[calc(100vh-4rem)] overflow-hidden">
      {/* Session sidebar */}
      <div className={`bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 flex flex-col transition-all duration-300 ${
        sidebarOpen ? 'w-64' : 'w-0 overflow-hidden'
      }`}>
        <div className="p-4 border-b border-gray-100 dark:border-gray-800">
          <button onClick={startNewChat} className="btn-primary w-full flex items-center gap-2 justify-center text-sm py-2.5">
            <Plus className="w-4 h-4" /> New Chat
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-3 space-y-1 no-scrollbar">
          {sessions.length === 0 ? (
            <p className="text-xs text-gray-400 text-center mt-6">No chats yet</p>
          ) : sessions.map(sid => (
            <button key={sid} onClick={() => selectSession(sid)}
              className={`w-full flex items-center gap-2 px-3 py-2.5 rounded-xl text-left transition-all duration-200 group ${
                sid === sessionId
                  ? 'bg-primary-50 dark:bg-primary-950/40 text-primary-700 dark:text-primary-300'
                  : 'hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300'
              }`}>
              <MessageSquare className="w-4 h-4 shrink-0" />
              <span className="text-xs flex-1 truncate">{sid.slice(0, 8)}…</span>
              <button onClick={(e) => deleteSession(sid, e)}
                className="opacity-0 group-hover:opacity-100 p-1 hover:text-red-500 transition-all">
                <Trash2 className="w-3 h-3" />
              </button>
            </button>
          ))}
        </div>
      </div>

      {/* Chat area */}
      <div className="flex-1 flex flex-col min-w-0" {...getRootProps()}>
        <input {...getInputProps()} />

        {/* Chat topbar */}
        <div className="h-14 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 flex items-center px-4 gap-3 shrink-0">
          <button onClick={() => setSidebarOpen(o => !o)} className="btn-icon">
            <ChevronDown className={`w-5 h-5 transform transition-transform ${sidebarOpen ? '' : '-rotate-90'}`} />
          </button>
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900/50 flex items-center justify-center">
              <Bot className="w-5 h-5 text-primary-600 dark:text-primary-400" />
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-900 dark:text-white leading-tight">KisanAI</p>
              <p className="text-xs text-primary-500 leading-tight">Your AI Farming Assistant</p>
            </div>
          </div>
          <div className="ml-auto">
            <select className="select text-sm py-1.5 px-3 w-36"
              value={language} onChange={e => setLanguage(e.target.value)}>
              {LANGUAGES.map(l => <option key={l.code} value={l.code}>{l.label}</option>)}
            </select>
          </div>
        </div>

        {/* Messages */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {messages.length === 0 && (
            <div className="flex flex-col items-center justify-center h-full text-center py-12">
              <div className="w-20 h-20 rounded-3xl gradient-green flex items-center justify-center mb-4 animate-float">
                <Bot className="w-10 h-10 text-white" />
              </div>
              <h2 className="text-xl font-bold text-gray-900 dark:text-white font-display mb-2">
                Hello! I'm KisanAI 🌾
              </h2>
              <p className="text-gray-500 dark:text-gray-400 mb-8 max-w-sm">
                Your AI farming assistant. Ask me anything about crops, diseases, weather, fertilizers, or government schemes!
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-w-lg w-full">
                {SUGGESTED_QUERIES.map((q) => (
                  <button key={q} onClick={() => { setInput(q); inputRef.current?.focus() }}
                    className="text-left px-4 py-2.5 text-sm text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl hover:border-primary-400 hover:bg-primary-50 dark:hover:bg-primary-950/20 transition-all duration-200">
                    {q}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messages.map((msg) => <MessageBubble key={msg.id} msg={msg} />)}

          {loading && (
            <div className="flex gap-3 animate-slide-up">
              <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900/50 flex items-center justify-center">
                <Bot className="w-4 h-4 text-primary-600" />
              </div>
              <div className="chat-bubble-ai">
                <div className="flex gap-1 items-center h-5">
                  <div className="w-2 h-2 bg-primary-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                  <div className="w-2 h-2 bg-primary-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                  <div className="w-2 h-2 bg-primary-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                </div>
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        {/* Image preview */}
        {imagePreview && (
          <div className="px-4 py-2 bg-white dark:bg-gray-900 border-t border-gray-100 dark:border-gray-800">
            <div className="relative inline-block">
              <img src={imagePreview} alt="attachment" className="h-20 rounded-xl object-cover" />
              <button onClick={() => { setImageFile(null); setImagePreview(null) }}
                className="absolute -top-2 -right-2 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center">
                <X className="w-3 h-3" />
              </button>
            </div>
          </div>
        )}

        {/* Input area */}
        <div className="p-4 bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-end gap-3 bg-gray-50 dark:bg-gray-800 rounded-2xl p-3">
            <label className="btn-icon cursor-pointer shrink-0 self-end mb-0.5">
              <ImageIcon className="w-5 h-5 text-gray-400" />
              <input type="file" accept="image/*" className="hidden"
                onChange={e => {
                  const f = e.target.files?.[0]
                  if (f) { setImageFile(f); setImagePreview(URL.createObjectURL(f)) }
                }} />
            </label>
            <textarea
              ref={inputRef}
              rows={1}
              className="flex-1 bg-transparent outline-none text-sm text-gray-900 dark:text-gray-100 resize-none max-h-32 placeholder-gray-400 dark:placeholder-gray-500"
              placeholder="Ask KisanAI about crops, diseases, weather, schemes…"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              style={{ lineHeight: '1.5' }}
            />
            <button onClick={sendMessage} disabled={loading || (!input.trim() && !imageFile)}
              className="w-10 h-10 rounded-xl gradient-green flex items-center justify-center disabled:opacity-40 hover:shadow-green transition-all duration-200 shrink-0 self-end">
              {loading ? <Loader2 className="w-4 h-4 text-white animate-spin" /> : <Send className="w-4 h-4 text-white" />}
            </button>
          </div>
          <p className="text-xs text-gray-400 text-center mt-2">
            Press Enter to send · Shift+Enter for new line · Drop images to analyze
          </p>
        </div>
      </div>
    </div>
  )
}
