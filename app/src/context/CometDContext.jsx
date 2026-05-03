import {createContext, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {CometD} from 'cometd';
import {useAuth} from './AuthContext';

const CometDContext = createContext(null);

export function visitorKey(user) {
    if (!user) return null;
    return user.id ?? `name:${user.name}`;
}

export function CometDProvider({children}) {
    const {user} = useAuth();
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const [visitors, setVisitors] = useState([]);
    const [currentRoom, setCurrentRoom] = useState(null);

    const cometdRef = useRef(null);
    const roomSubRef = useRef(null);
    const roomVisitorSubRef = useRef(null);
    const cancelledRef = useRef(false);
    const currentRoomRef = useRef(null);
    currentRoomRef.current = currentRoom;

    useEffect(() => {
        const cometdURL = `${window.location.protocol}//${window.location.host}/cometd`;

        const handlePageHide = () => {
            const cometd = cometdRef.current;
            if (!cometd) return;
            const clientId = cometd.getClientId?.();
            if (!clientId) return;

            const messages = [];
            let id = 1;
            if (currentRoomRef.current) {
                messages.push({
                    channel: '/service/room',
                    data: {action: 'leave', roomId: currentRoomRef.current},
                    clientId,
                    id: String(id++),
                });
            }
            messages.push({channel: '/meta/disconnect', clientId, id: String(id++)});
            const blob = new Blob([JSON.stringify(messages)], {type: 'application/json'});
            navigator.sendBeacon(cometdURL, blob);

            if (currentRoomRef.current) {
                cometd.publish('/service/room', {action: 'leave', roomId: currentRoomRef.current});
            }
            cometd.disconnect();
        };
        window.addEventListener('pagehide', handlePageHide);

        let cometd = null;
        cancelledRef.current = false;

        const timer = setTimeout(() => {
            cometd = new CometD();
            cometdRef.current = cometd;

            cometd.configure({url: cometdURL});

            cometd.addListener('/meta/connect', (message) => {
                if (cancelledRef.current || cometd.isDisconnected()) {
                    return;
                }
                setConnected(message.successful === true);
            });

            const ext = {};
            if (user?.token) {
                ext.auth = {token: user.token};
            }

            cometd.handshake(ext, (reply) => {
                if (cancelledRef.current) return;
                if (reply.successful) {
                    setError(null);
                } else {
                    setError(reply.error || 'Handshake failed');
                }
            });
        }, 0);

        return () => {
            cancelledRef.current = true;
            clearTimeout(timer);
            window.removeEventListener('pagehide', handlePageHide);
            if (cometd) {
                const roomToLeave = currentRoomRef.current;
                cometd.batch(() => {
                    if (roomToLeave) {
                        cometd.publish('/service/room', {action: 'leave', roomId: roomToLeave});
                    }
                    cometd.disconnect();
                });
            }
            cometdRef.current = null;
            roomSubRef.current = null;
            roomVisitorSubRef.current = null;
            setConnected(false);
            setVisitors([]);
            setCurrentRoom(null);
        };
    }, [user?.token]);

    const joinRoom = useCallback((roomId) => {
        const cometd = cometdRef.current;
        if (!cometd || cancelledRef.current) return;

        if (roomSubRef.current) {
            cometd.unsubscribe(roomSubRef.current);
            roomSubRef.current = null;
        }
        if (roomVisitorSubRef.current) {
            cometd.unsubscribe(roomVisitorSubRef.current);
            roomVisitorSubRef.current = null;
        }

        roomSubRef.current = cometd.subscribe('/room/' + roomId, (message) => {
            if (!cancelledRef.current) {
                setVisitors(message.data.visitors);
            }
        });

        roomVisitorSubRef.current = cometd.subscribe('/room/' + roomId + '/visitor', (message) => {
            if (cancelledRef.current) return;
            const {user, action} = message.data;
            if (!user) return;
            const key = visitorKey(user);
            setVisitors(prev => {
                if (action === 'joined') {
                    if (prev.some(v => visitorKey(v) === key)) return prev;
                    return [...prev, user];
                }
                if (action === 'left') {
                    return prev.filter(v => visitorKey(v) !== key);
                }
                return prev;
            });
        });

        cometd.publish('/service/room', {action: 'join', roomId});
        setCurrentRoom(roomId);
    }, []);

    const leaveRoom = useCallback(() => {
        const cometd = cometdRef.current;
        if (!cometd || !currentRoom || cancelledRef.current) return;

        cometd.publish('/service/room', {action: 'leave', roomId: currentRoom});

        if (roomSubRef.current) {
            cometd.unsubscribe(roomSubRef.current);
            roomSubRef.current = null;
        }
        if (roomVisitorSubRef.current) {
            cometd.unsubscribe(roomVisitorSubRef.current);
            roomVisitorSubRef.current = null;
        }

        setCurrentRoom(null);
        setVisitors([]);
    }, [currentRoom]);

    return (
        <CometDContext.Provider value={{connected, error, visitors, currentRoom, joinRoom, leaveRoom}}>
            {children}
        </CometDContext.Provider>
    );
}

export function useCometD() {
    const context = useContext(CometDContext);
    if (!context) {
        throw new Error('useCometD must be used within a CometDProvider');
    }
    return context;
}
