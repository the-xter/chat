import {Routes, Route, Navigate} from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Users from './pages/Users';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<Login/>}/>
            <Route path="/signup" element={<Signup/>}/>
            <Route
                path="/users"
                element={
                    <ProtectedRoute>
                        <Users/>
                    </ProtectedRoute>
                }
            />
            <Route path="*" element={<Navigate to="/login" replace/>}/>
        </Routes>
    );
}
