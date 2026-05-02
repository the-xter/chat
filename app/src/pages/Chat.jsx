import {useEffect} from 'react';
import {useCometD, visitorKey} from '../context/CometDContext';
import './Chat.css';

const ROOM_ID = 'general';

export default function Chat() {
    const {connected, error, visitors, currentRoom, joinRoom} = useCometD();

    useEffect(() => {
        if (connected && !currentRoom) {
            joinRoom(ROOM_ID);
        }
    }, [connected, currentRoom, joinRoom]);

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
                <div className="messages-area"></div>
                <form className="message-bar" onSubmit={(e) => e.preventDefault()}>
                    <input type="text" placeholder="Type a message..."/>
                    <button type="submit">Send</button>
                </form>
            </section>
        </div>
    );
}
