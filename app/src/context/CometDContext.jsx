import {createContext, useContext, useEffect, useState} from 'react';
import {CometD} from 'cometd';
import {useAuth} from './AuthContext';

const CometDContext = createContext(null);

export function CometDProvider({children}) {
    const {user} = useAuth();
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const [visitors, setVisitors] = useState({registered: [], guests: []});

    useEffect(() => {
        let cometd = null;
        let cancelled = false;

        const timer = setTimeout(() => {
            cometd = new CometD();

            const cometdURL = `${window.location.protocol}//${window.location.host}/cometd`;
            cometd.configure({url: cometdURL});

            cometd.addListener('/meta/connect', (message) => {
                if (cancelled || cometd.isDisconnected()) {
                    return;
                }
                setConnected(message.successful === true);
            });

            const ext = {};
            if (user?.token) {
                ext.auth = {token: user.token};
            }

            cometd.handshake(ext, (reply) => {
                if (cancelled) return;
                if (reply.successful) {
                    setError(null);
                    cometd.subscribe('/visitors', (message) => {
                        if (!cancelled) setVisitors(message.data);
                    });
                } else {
                    setError(reply.error || 'Handshake failed');
                }
            });
        }, 0);

        return () => {
            cancelled = true;
            clearTimeout(timer);
            if (cometd) {
                cometd.disconnect();
            }
            setConnected(false);
            setVisitors({registered: [], guests: []});
        };
    }, [user?.token]);

    return (
        <CometDContext.Provider value={{connected, error, visitors}}>
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
