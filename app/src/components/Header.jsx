import {useState, useRef, useEffect} from 'react';
import {Link} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import './Header.css';

export default function Header() {
    const {user, logout} = useAuth();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                setMenuOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const initial = user?.username?.charAt(0).toUpperCase() || '?';

    return (
        <header className="site-header">
            <div className="site-header-inner">
                <Link to="/" className="site-logo">Chat</Link>

                <div className="site-header-right">
                    {user ? (
                        <div className="avatar-menu" ref={menuRef}>
                            <button
                                className="avatar"
                                onClick={() => setMenuOpen(!menuOpen)}
                                aria-label="User menu"
                            >
                                {initial}
                            </button>
                            {menuOpen && (
                                <div className="avatar-dropdown">
                                    <div className="avatar-dropdown-name">{user.username}</div>
                                    <button
                                        className="avatar-dropdown-logout"
                                        onClick={() => {
                                            setMenuOpen(false);
                                            logout();
                                        }}
                                    >
                                        Log out
                                    </button>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className="header-auth">
                            <Link to="/signup" className="header-signup-link">Sign up</Link>
                            <Link to="/login"><button>Log in</button></Link>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
}
