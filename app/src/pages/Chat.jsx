import {useEffect, useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useCometD, visitorKey} from '../context/CometDContext';
import {useAuth} from '../context/AuthContext';
import './Chat.css';

const ROOM_ID = 'general';

const STYLE_LETTERS = [
    {letter: 'B', command: 'bold', className: 'style-bold'},
    {letter: 'I', command: 'italic', className: 'style-italic'},
    {letter: 'U', command: 'underline', className: 'style-underline'},
];

function formatTime(isoString) {
    try {
        const d = new Date(isoString);
        if (Number.isNaN(d.getTime())) return '';
        return d.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    } catch {
        return '';
    }
}

export default function Chat() {
    const {connected, error, visitors, messages, currentRoom, joinRoom, sendChatMessage} = useCometD();
    const {user} = useAuth();
    const navigate = useNavigate();
    const editorRef = useRef(null);
    const savedRangeRef = useRef(null);
    const styleControlRef = useRef(null);
    const messagesAreaRef = useRef(null);
    const [stylePopoverOpen, setStylePopoverOpen] = useState(false);
    const [selectedStyle, setSelectedStyle] = useState('A');

    useEffect(() => {
        if (connected && !currentRoom) {
            joinRoom(ROOM_ID);
        }
    }, [connected, currentRoom, joinRoom]);

    useEffect(() => {
        const el = messagesAreaRef.current;
        if (el) el.scrollTop = el.scrollHeight;
    }, [messages]);

    useEffect(() => {
        if (!stylePopoverOpen) return;
        const handler = (e) => {
            if (styleControlRef.current && !styleControlRef.current.contains(e.target)) {
                setStylePopoverOpen(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, [stylePopoverOpen]);

    const saveSelection = () => {
        const sel = window.getSelection();
        if (!sel || sel.rangeCount === 0) return;
        const range = sel.getRangeAt(0);
        if (editorRef.current && editorRef.current.contains(range.commonAncestorContainer)) {
            savedRangeRef.current = range.cloneRange();
        }
    };

    const restoreSelection = () => {
        const range = savedRangeRef.current;
        if (!range || !editorRef.current) return false;
        editorRef.current.focus();
        const sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
        return true;
    };

    const applyStyle = (command, value = null) => {
        if (!editorRef.current) return;
        if (!restoreSelection()) {
            editorRef.current.focus();
        }
        document.execCommand(command, false, value);
        saveSelection();
    };

    const handleStyleLetter = (e, {letter, command}) => {
        e.preventDefault();
        applyStyle(command);
        setSelectedStyle(prev => (prev === letter ? 'A' : letter));
        setStylePopoverOpen(false);
    };

    const handleSend = () => {
        if (!user) {
            navigate('/signup');
            return;
        }
        if (!editorRef.current) return;
        const text = editorRef.current.innerText.replace(/\s+/g, ' ').trim();
        if (!text) return;
        const sent = sendChatMessage(text);
        if (sent) {
            editorRef.current.innerHTML = '';
            savedRangeRef.current = null;
        }
    };

    const handleEditorKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSend();
        }
    };

    const handleEditorPaste = (e) => {
        e.preventDefault();
        const text = e.clipboardData.getData('text');
        const singleLine = text.replace(/[\r\n]+/g, ' ');
        if (singleLine) {
            document.execCommand('insertText', false, singleLine);
        }
    };

    const triggerClassName = STYLE_LETTERS.find(s => s.letter === selectedStyle)?.className ?? '';

    if (error) {
        return (
            <div className="page">
                <div className="error">
                    <h2>Connection Error</h2>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="chat-layout">
            <aside className="visitors-pane">
                <ul className="members-list">
                    {visitors.map((visitor) => (
                        <li key={visitorKey(visitor)} className="member">{visitor.name}</li>
                    ))}
                    {visitors.length === 0 && (
                        <li className="member empty">No visitors yet</li>
                    )}
                </ul>
            </aside>
            <section className="messages-pane">
                <div className="messages-area" ref={messagesAreaRef}>
                    {messages.map((msg, idx) => {
                        const key = msg.messageId ?? `tmp:${idx}`;
                        const isOwn = user && msg.sender && msg.sender.id != null && msg.sender.id === user.id;
                        const time = msg.createdAt ? formatTime(msg.createdAt) : '';
                        return (
                            <div key={key} className={`message${isOwn ? ' message-own' : ''}`}>
                                <div className="message-meta">
                                    <span className="message-sender">{msg.sender?.name ?? 'unknown'}</span>
                                    {time && <span className="message-time">{time}</span>}
                                </div>
                                <div className="message-text">{msg.text}</div>
                            </div>
                        );
                    })}
                </div>
                <form className="message-bar" onSubmit={(e) => { e.preventDefault(); handleSend(); }}>
                    <div className="format-toolbar">
                        <input
                            type="color"
                            className="format-color"
                            title="Text color"
                            onMouseDown={saveSelection}
                            onChange={(e) => applyStyle('foreColor', e.target.value)}
                        />
                        <div
                            ref={styleControlRef}
                            className={`font-style-control${stylePopoverOpen ? ' open' : ''}`}
                        >
                            <span
                                className={`font-style-trigger ${triggerClassName}`}
                                onMouseDown={(e) => {
                                    e.preventDefault();
                                    setStylePopoverOpen(o => !o);
                                }}
                            >{selectedStyle}</span>
                            <div className="font-style-popover">
                                {STYLE_LETTERS.map(s => (
                                    <span
                                        key={s.letter}
                                        className={`style-letter ${s.className}`}
                                        onMouseDown={(e) => handleStyleLetter(e, s)}
                                    >{s.letter}</span>
                                ))}
                            </div>
                        </div>
                    </div>
                    <div className="message-input-row">
                        <div
                            ref={editorRef}
                            className="message-input"
                            contentEditable
                            suppressContentEditableWarning
                            onMouseUp={saveSelection}
                            onKeyUp={saveSelection}
                            onKeyDown={handleEditorKeyDown}
                            onPaste={handleEditorPaste}
                            onBlur={saveSelection}
                        />
                        <button type="submit">Send</button>
                    </div>
                </form>
            </section>
        </div>
    );
}
