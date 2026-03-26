import {useCometD} from '../context/CometDContext';
import {useAuth} from '../context/AuthContext';
import './Chat.css';

const ROOM_ID = 'general';

export default function Chat() {
    const {user} = useAuth();
    const {connected, error, visitors, roomMembers, currentRoom, joinRoom, leaveRoom} = useCometD();

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
                        <h3>Members ({roomMembers.length})</h3>
                        <ul className="members-list">
                            {roomMembers.map((name, i) => (
                                <li key={i} className="member">{name}</li>
                            ))}
                            {roomMembers.length === 0 && (
                                <li className="member empty">No members yet</li>
                            )}
                        </ul>
                    </div>
                </div>
            )}

            <div className="visitors-container">
                <div className="visitors-panel">
                    <h3>Registered Users ({visitors.registered.length})</h3>
                    <ul className="visitors-list">
                        {visitors.registered.map((name, i) => (
                            <li key={i} className="visitor registered">{name}</li>
                        ))}
                        {visitors.registered.length === 0 && (
                            <li className="visitor empty">No registered users online</li>
                        )}
                    </ul>
                </div>
                <div className="visitors-panel">
                    <h3>Guests ({visitors.guests.length})</h3>
                    <ul className="visitors-list">
                        {visitors.guests.map((name, i) => (
                            <li key={i} className="visitor guest">{name}</li>
                        ))}
                        {visitors.guests.length === 0 && (
                            <li className="visitor empty">No guests online</li>
                        )}
                    </ul>
                </div>
            </div>
        </div>
    );
}
