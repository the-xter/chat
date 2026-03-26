import {createContext, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {CometD} from 'cometd';
import {useAuth} from './AuthContext';

const CometDContext = createContext(null);

export function CometDProvider({children}) {
    const {user} = useAuth();
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const [visitors, setVisitors] = useState({registered: [], guests: []});
    const [roomMembers, setRoomMembers] = useState([]);
    const [currentRoom, setCurrentRoom] = useState(null);

    const cometdRef = useRef(null);
    const roomSubRef = useRef(null);
    const cancelledRef = useRef(false);

    useEffect(() => {
        let cometd = null;
        cancelledRef.current = false;

        const timer = setTimeout(() => {
            cometd = new CometD();
            cometdRef.current = cometd;

            const cometdURL = `${window.location.protocol}//${window.location.host}/cometd`;
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
                    cometd.subscribe('/visitors', (message) => {
                        if (!cancelledRef.current) setVisitors(message.data);
                    });
                } else {
                    setError(reply.error || 'Handshake failed');
                }
            });
        }, 0);

        return () => {
            cancelledRef.current = true;
            clearTimeout(timer);
            if (cometd) {
                cometd.disconnect();
            }
            cometdRef.current = null;
            roomSubRef.current = null;
            setConnected(false);
            setVisitors({registered: [], guests: []});
            setRoomMembers([]);
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

        roomSubRef.current = cometd.subscribe('/room/' + roomId, (message) => {
            if (!cancelledRef.current) {
                setRoomMembers(message.data.members);
            }
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

        setCurrentRoom(null);
        setRoomMembers([]);
    }, [currentRoom]);

    return (
        <CometDContext.Provider value={{connected, error, visitors, roomMembers, currentRoom, joinRoom, leaveRoom}}>
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
