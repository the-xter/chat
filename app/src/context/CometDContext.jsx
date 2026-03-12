import {createContext, useContext, useEffect, useRef, useState, useCallback} from 'react';
import {CometD} from 'cometd';
import {useAuth} from './AuthContext';

const CometDContext = createContext(null);

export function CometDProvider({children}) {
    const {user} = useAuth();
    const cometdRef = useRef(null);
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const [visitors, setVisitors] = useState({registered: [], guests: []});

    const connect = useCallback(() => {
        if (cometdRef.current) return;
        setError(null);

        const cometd = new CometD();
        cometdRef.current = cometd;

        const cometdURL = `${window.location.protocol}//${window.location.host}/cometd`;
        cometd.configure({url: cometdURL});

        cometd.addListener('/meta/connect', (message) => {
            if (cometd.isDisconnected()) {
                setConnected(false);
                return;
            }
            setConnected(message.successful === true);
        });

        const ext = {};
        if (user?.token) {
            ext.auth = {token: user.token};
        }

        cometd.handshake(ext, (reply) => {
            if (reply.successful) {
                setError(null);
                cometd.subscribe('/visitors', (message) => {
                    setVisitors(message.data);
                });
            } else {
                setError(reply.error || 'Handshake failed');
                cometdRef.current = null;
            }
        });
    }, [user]);

    const disconnect = useCallback(() => {
        if (cometdRef.current) {
            cometdRef.current.disconnect();
            cometdRef.current = null;
            setConnected(false);
            setError(null);
            setVisitors({registered: [], guests: []});
        }
    }, []);

    useEffect(() => {
        return () => {
            if (cometdRef.current) {
                cometdRef.current.disconnect();
                cometdRef.current = null;
            }
        };
    }, []);

    return (
        <CometDContext.Provider value={{connect, disconnect, connected, error, visitors}}>
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
