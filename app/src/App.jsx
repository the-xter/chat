import {Routes, Route, Navigate} from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Users from './pages/Users';
import Chat from './pages/Chat';
import ProtectedRoute from './components/ProtectedRoute';
import Header from './components/Header';
import {CometDProvider} from './context/CometDContext';

export default function App() {
    return (
        <>
            <Header/>
            <Routes>
                <Route path="/login" element={<Login/>}/>
                <Route path="/signup" element={<Signup/>}/>
                <Route path="/chat" element={
                    <CometDProvider>
                        <Chat/>
                    </CometDProvider>
                }/>
                <Route
                    path="/users"
                    element={
                        <ProtectedRoute>
                            <Users/>
                        </ProtectedRoute>
                    }
                />
                <Route path="*" element={<Navigate to="/chat" replace/>}/>
            </Routes>
        </>
    );
}
