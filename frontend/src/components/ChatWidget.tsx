import { useEffect, useRef, useState } from 'react'
import { sendChatMessage } from '../api'
import type { ChatMessage } from '../types'
import CarCard from './CarCard'
import './ChatWidget.css'

const GREETING: ChatMessage = {
  id: 'greeting',
  role: 'assistant',
  text: "Hi, I'm your car assistant. Tell me what you're looking for — for example \"7-seater diesel SUV under 15 lakhs\".",
}

function ChatWidget() {
  const [open, setOpen] = useState(true)
  const [messages, setMessages] = useState<ChatMessage[]>([GREETING])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const listRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight
    }
  }, [messages, sending])

  async function handleSend() {
    const text = input.trim()
    if (!text || sending) return

    const userMessage: ChatMessage = { id: crypto.randomUUID(), role: 'user', text }
    setMessages((prev) => [...prev, userMessage])
    setInput('')
    setSending(true)

    try {
      const res = await sendChatMessage(text)
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          text: res.reply,
          suggestions: res.suggestions,
        },
      ])
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          text: "Sorry, I couldn't reach the recommendation service. Please try again.",
        },
      ])
    } finally {
      setSending(false)
    }
  }

  if (!open) {
    return (
      <button className="chat-fab" onClick={() => setOpen(true)} aria-label="Open chat assistant">
        Ask the assistant
      </button>
    )
  }

  return (
    <div className="chat-widget">
      <header className="chat-widget__header">
        <span>Car Assistant</span>
        <button
          className="chat-widget__close"
          onClick={() => setOpen(false)}
          aria-label="Close chat"
        >
          ×
        </button>
      </header>

      <div className="chat-widget__messages" ref={listRef}>
        {messages.map((message) => (
          <div key={message.id} className={`chat-message chat-message--${message.role}`}>
            <p>{message.text}</p>
            {message.suggestions && message.suggestions.length > 0 && (
              <div className="chat-message__suggestions">
                {message.suggestions.map((car, idx) => (
                  <CarCard key={`${car.make}-${car.model}-${idx}`} car={car} compact />
                ))}
              </div>
            )}
          </div>
        ))}
        {sending && (
          <div className="chat-message chat-message--assistant chat-message--typing">
            <span></span>
            <span></span>
            <span></span>
          </div>
        )}
      </div>

      <form
        className="chat-widget__input"
        onSubmit={(e) => {
          e.preventDefault()
          handleSend()
        }}
      >
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Describe the car you want…"
          aria-label="Message"
        />
        <button type="submit" disabled={sending || !input.trim()}>
          Send
        </button>
      </form>
    </div>
  )
}

export default ChatWidget
