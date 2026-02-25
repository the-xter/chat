import {useEffect, useState} from 'react';
import {useAuth} from '../context/AuthContext';
import api from '../api/axios';

export default function Users() {
    const {user, logout} = useAuth();
    const [users, setUsers] = useState([]);
    const [error, setError] = useState('');
    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState({username: '', email: '', password: ''});
    const [createForm, setCreateForm] = useState({username: '', email: '', password: ''});
    const [showCreate, setShowCreate] = useState(false);
    const [loading, setLoading] = useState(true);

    const fetchUsers = async () => {
        try {
            const res = await api.get('/users');
            setUsers(Array.isArray(res.data) ? res.data : []);
            setError('');
        } catch (err) {
            if (err.response?.status === 403) {
                setError('Admin access required to view all users');
            } else {
                setError(err.response?.data?.error || 'Failed to load users');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleEdit = (u) => {
        setEditingId(u.id);
        setEditForm({username: u.username, email: u.email, password: ''});
    };

    const handleCancelEdit = () => {
        setEditingId(null);
        setEditForm({username: '', email: '', password: ''});
    };

    const handleUpdate = async (id) => {
        setError('');
        const payload = {};
        if (editForm.username) payload.username = editForm.username;
        if (editForm.email) payload.email = editForm.email;
        if (editForm.password) payload.password = editForm.password;

        try {
            await api.put(`/users/${id}`, payload);
            setEditingId(null);
            fetchUsers();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to update user');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this user?')) return;
        setError('');
        try {
            await api.delete(`/users/${id}`);
            fetchUsers();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to delete user');
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.post('/auth/signup', createForm);
            setCreateForm({username: '', email: '', password: ''});
            setShowCreate(false);
            fetchUsers();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to create user');
        }
    };

    if (loading) {
        return <div className="page"><p>Loading...</p></div>;
    }

    return (
        <div className="page">
            <div className="page-header">
                <h1>Users</h1>
                <div className="header-actions">
                    <span>Logged in as <strong>{user.username}</strong></span>
                    <button onClick={logout} className="btn-secondary">Logout</button>
                </div>
            </div>

            {error && <div className="error">{error}</div>}

            <div className="toolbar">
                <button onClick={() => setShowCreate(!showCreate)}>
                    {showCreate ? 'Cancel' : 'Create User'}
                </button>
            </div>

            {showCreate && (
                <form className="inline-form" onSubmit={handleCreate}>
                    <input
                        type="text"
                        placeholder="Username"
                        value={createForm.username}
                        onChange={(e) => setCreateForm({...createForm, username: e.target.value})}
                        required
                        minLength={3}
                    />
                    <input
                        type="email"
                        placeholder="Email"
                        value={createForm.email}
                        onChange={(e) => setCreateForm({...createForm, email: e.target.value})}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={createForm.password}
                        onChange={(e) => setCreateForm({...createForm, password: e.target.value})}
                        required
                        minLength={8}
                    />
                    <button type="submit">Add</button>
                </form>
            )}

            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Roles</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map((u) => (
                    <tr key={u.id}>
                        <td>{u.id}</td>
                        <td>
                            {editingId === u.id ? (
                                <input
                                    type="text"
                                    value={editForm.username}
                                    onChange={(e) => setEditForm({...editForm, username: e.target.value})}
                                />
                            ) : (
                                u.username
                            )}
                        </td>
                        <td>
                            {editingId === u.id ? (
                                <input
                                    type="email"
                                    value={editForm.email}
                                    onChange={(e) => setEditForm({...editForm, email: e.target.value})}
                                />
                            ) : (
                                u.email
                            )}
                        </td>
                        <td>{u.roles.join(', ')}</td>
                        <td className="actions">
                            {editingId === u.id ? (
                                <>
                                    <input
                                        type="password"
                                        placeholder="New password"
                                        value={editForm.password}
                                        onChange={(e) => setEditForm({...editForm, password: e.target.value})}
                                    />
                                    <button onClick={() => handleUpdate(u.id)}>Save</button>
                                    <button onClick={handleCancelEdit} className="btn-secondary">Cancel</button>
                                </>
                            ) : (
                                <>
                                    <button onClick={() => handleEdit(u)}>Edit</button>
                                    <button onClick={() => handleDelete(u.id)} className="btn-danger">Delete</button>
                                </>
                            )}
                        </td>
                    </tr>
                ))}
                {users.length === 0 && (
                    <tr>
                        <td colSpan="5" style={{textAlign: 'center'}}>No users found</td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}
