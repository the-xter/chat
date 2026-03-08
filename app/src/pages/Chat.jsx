import {useEffect} from 'react';
import {useCometD} from '../context/CometDContext';
import {useAuth} from '../context/AuthContext';
import './Chat.css';

export default function Chat() {
    const {user} = useAuth();
    const {connect, disconnect, connected, error, visitors} = useCometD();

    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

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
