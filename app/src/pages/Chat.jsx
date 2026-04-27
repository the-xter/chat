import {useCometD, visitorKey} from '../context/CometDContext';
import {useAuth} from '../context/AuthContext';
import './Chat.css';

const ROOM_ID = 'general';

export default function Chat() {
    const {user} = useAuth();
    const {connected, error, visitors, currentRoom, joinRoom, leaveRoom} = useCometD();

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
        <div className="page">
            <h1>Chat</h1>
            <div className="connection-status">
                {connected
                    ? 'Connected' + (user ? ` as ${user.username}` : ' as Guest')
                    : 'Connecting...'}
            </div>

            {connected && !currentRoom && (
                <button className="join-room-btn" onClick={() => joinRoom(ROOM_ID)}>
                    Join Room
                </button>
            )}

            {currentRoom && (
                <div className="room-panel">
                    <div className="room-header">
                        <h2>Room: {currentRoom}</h2>
                        <button className="leave-room-btn" onClick={leaveRoom}>
                            Leave
                        </button>
                    </div>
                    <div className="room-members">
                        <h3>Visitors ({visitors.length})</h3>
                        <ul className="members-list">
                            {visitors.map((visitor) => (
                                <li key={visitorKey(visitor)} className="member">{visitor.name}</li>
                            ))}
                            {visitors.length === 0 && (
                                <li className="member empty">No visitors yet</li>
                            )}
                        </ul>
                    </div>
                </div>
            )}

        </div>
    );
}
