import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { adminApi } from '../api/adminApi';
import { ShieldCheck, Lock, User, ArrowLeft, Loader2 } from 'lucide-react';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const response = await adminApi.login({ username, password });
            if (response.data.success) {
                login(response.data.data.token);
            } else {
                setError(response.data.message);
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'خطا در برقراری ارتباط با سرور');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center relative overflow-hidden rtl font-[Vazirmatn]">
            {/* Animated Background Orbs */}
            <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] bg-indigo-600/20 rounded-full blur-[120px] animate-pulse"></div>
            <div className="absolute bottom-[-10%] left-[-10%] w-[500px] h-[500px] bg-purple-600/10 rounded-full blur-[120px] animate-pulse delay-700"></div>

            <div className="glass w-full max-w-md p-8 rounded-[2rem] relative z-10 border-white/10 shadow-2xl animate-in fade-in zoom-in duration-500">
                <div className="flex flex-col items-center mb-10">
                    <div className="w-20 h-20 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-500/30 mb-6 group-hover:scale-110 transition-transform duration-500">
                        <ShieldCheck size={40} className="text-white" />
                    </div>
                    <h1 className="text-3xl font-black text-white mb-2">خوش آمدید</h1>
                    <p className="text-slate-400 text-sm">پنل مدیریت عالی‌رتبه (Super Admin)</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 mr-1 flex items-center gap-2">
                            <User size={16} /> نام کاربری
                        </label>
                        <input
                            type="text"
                            className="w-full bg-white/5 border border-white/10 rounded-xl p-4 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600"
                            placeholder="admin"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 mr-1 flex items-center gap-2">
                            <Lock size={16} /> رمز عبور
                        </label>
                        <input
                            type="password"
                            className="w-full bg-white/5 border border-white/10 rounded-xl p-4 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-500 p-4 rounded-xl text-sm animate-shake">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all active:scale-95 flex items-center justify-center gap-3 disabled:opacity-50 disabled:cursor-not-allowed group"
                    >
                        {loading ? (
                            <Loader2 className="animate-spin" />
                        ) : (
                            <>
                                <span>ورود به سیستم</span>
                                <ArrowLeft size={20} className="group-hover:-translate-x-1 transition-transform" />
                            </>
                        )}
                    </button>
                </form>

                <div className="mt-10 pt-6 border-t border-white/5 text-center">
                    <p className="text-xs text-slate-500">
                        تمامی فعالیت‌ها در این پنل به همراه IP آدرس لاگ‌برداری می‌شود.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
