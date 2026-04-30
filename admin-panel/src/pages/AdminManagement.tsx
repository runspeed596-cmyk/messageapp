import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { PanelAdmin } from '../api/adminApi';
import {
    ShieldCheck, ShieldAlert, Plus, Trash2, User, Lock, Eye, EyeOff,
    Calendar, Loader2, X, Crown, UserPlus
} from 'lucide-react';

const AdminManagement = () => {
    const [admins, setAdmins] = useState<PanelAdmin[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [formUsername, setFormUsername] = useState<string>('');
    const [formPassword, setFormPassword] = useState<string>('');
    const [formDisplayName, setFormDisplayName] = useState<string>('');
    const [formIsSuperAdmin, setFormIsSuperAdmin] = useState<boolean>(false);
    const [formError, setFormError] = useState<string>('');
    const [formLoading, setFormLoading] = useState<boolean>(false);
    const [showPassword, setShowPassword] = useState<boolean>(false);

    useEffect(() => {
        fetchAdmins();
    }, []);

    const fetchAdmins = async (): Promise<void> => {
        try {
            const response = await adminApi.getPanelAdmins();
            if (response.data.success) {
                setAdmins(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching admins:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: string, username: string): Promise<void> => {
        if (window.confirm(`آیا از حذف ادمین "${username}" اطمینان دارید؟`)) {
            try {
                const response = await adminApi.deletePanelAdmin(id);
                if (response.data.success) {
                    setAdmins(admins.filter(a => a.id !== id));
                } else {
                    alert(response.data.message);
                }
            } catch (error: any) {
                alert(error.response?.data?.message || 'خطا در حذف ادمین');
            }
        }
    };

    const handleCreate = async (e: React.FormEvent): Promise<void> => {
        e.preventDefault();
        setFormError('');
        if (formUsername.trim().length < 3) {
            setFormError('نام کاربری باید حداقل ۳ کاراکتر باشد');
            return;
        }
        if (formPassword.length < 6) {
            setFormError('رمز عبور باید حداقل ۶ کاراکتر باشد');
            return;
        }
        if (formDisplayName.trim().length < 2) {
            setFormError('نام نمایشی باید حداقل ۲ کاراکتر باشد');
            return;
        }
        setFormLoading(true);
        try {
            const response = await adminApi.createPanelAdmin({
                username: formUsername.trim(),
                password: formPassword,
                displayName: formDisplayName.trim(),
                isSuperAdmin: formIsSuperAdmin
            });
            if (response.data.success) {
                setAdmins([...admins, response.data.data]);
                resetForm();
                setShowModal(false);
            } else {
                setFormError(response.data.message);
            }
        } catch (error: any) {
            setFormError(error.response?.data?.message || 'خطا در ایجاد ادمین');
        } finally {
            setFormLoading(false);
        }
    };

    const resetForm = (): void => {
        setFormUsername('');
        setFormPassword('');
        setFormDisplayName('');
        setFormIsSuperAdmin(false);
        setFormError('');
        setShowPassword(false);
    };

    if (loading) return (
        <div className="flex flex-col items-center justify-center h-[60vh] space-y-4">
            <div className="w-12 h-12 border-4 border-indigo-500/20 border-t-indigo-500 rounded-full animate-spin"></div>
            <p className="text-slate-400 font-bold animate-pulse">در حال بارگذاری لیست ادمین‌ها...</p>
        </div>
    );

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end mb-4">
                <div>
                    <h1 className="text-3xl font-black text-white">مدیریت ادمین‌ها</h1>
                    <p className="text-slate-400 mt-2">افزودن، مشاهده و حذف ادمین‌های پنل مدیریت</p>
                </div>
                <button
                    onClick={() => { resetForm(); setShowModal(true); }}
                    className="flex items-center gap-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold px-6 py-3 rounded-2xl shadow-lg shadow-indigo-500/20 transition-all active:scale-95 group"
                >
                    <UserPlus size={20} className="group-hover:scale-110 transition-transform" />
                    <span>افزودن ادمین جدید</span>
                </button>
            </div>

            {/* Admin Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {admins.map((admin) => (
                    <div
                        key={admin.id}
                        className="glass rounded-[2rem] p-6 border-white/5 shadow-xl hover:shadow-2xl transition-all duration-500 group relative overflow-hidden"
                    >
                        {/* Super Admin Glow */}
                        {admin.isSuperAdmin && (
                            <div className="absolute top-0 right-0 w-40 h-40 bg-amber-500/10 rounded-full blur-[80px] -translate-y-1/2 translate-x-1/2"></div>
                        )}

                        <div className="flex items-start justify-between relative z-10">
                            <div className="flex items-center gap-4">
                                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center ${admin.isSuperAdmin
                                        ? 'bg-amber-500/10 border border-amber-500/20 text-amber-400'
                                        : 'bg-indigo-500/10 border border-indigo-500/20 text-indigo-400'
                                    } group-hover:scale-110 transition-transform duration-500`}>
                                    {admin.isSuperAdmin ? <Crown size={24} /> : <ShieldCheck size={24} />}
                                </div>
                                <div>
                                    <div className="font-black text-white text-lg">{admin.displayName}</div>
                                    <div className="text-xs text-indigo-400 font-bold tracking-tight">@{admin.username}</div>
                                </div>
                            </div>

                            <button
                                onClick={() => handleDelete(admin.id, admin.username)}
                                className="p-3 text-slate-500 hover:text-rose-500 hover:bg-rose-500/10 rounded-xl transition-all opacity-0 group-hover:opacity-100"
                            >
                                <Trash2 size={18} />
                            </button>
                        </div>

                        <div className="mt-5 flex items-center justify-between">
                            <span className={`text-[10px] font-black px-3 py-1 rounded-full border ${admin.isSuperAdmin
                                    ? 'bg-amber-500/10 text-amber-500 border-amber-500/20'
                                    : 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20'
                                }`}>
                                {admin.isSuperAdmin ? 'سوپر ادمین' : 'ادمین'}
                            </span>
                            <div className="flex items-center gap-2 text-slate-500 text-xs font-bold">
                                <Calendar size={12} />
                                {new Date(admin.createdAt).toLocaleDateString('fa-IR')}
                            </div>
                        </div>
                    </div>
                ))}

                {admins.length === 0 && (
                    <div className="col-span-full p-20 text-center space-y-4">
                        <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-6">
                            <ShieldAlert size={40} className="text-slate-600" />
                        </div>
                        <h3 className="text-xl font-black text-white">هیچ ادمینی یافت نشد</h3>
                        <p className="text-slate-500">برای شروع، یک ادمین جدید اضافه کنید.</p>
                    </div>
                )}
            </div>

            {/* Create Admin Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[100] p-4">
                    <div className="glass w-full max-w-lg rounded-[2rem] p-8 border-white/10 shadow-2xl animate-in fade-in zoom-in duration-300 relative">
                        <button
                            onClick={() => setShowModal(false)}
                            className="absolute top-6 left-6 p-2 text-slate-500 hover:text-white hover:bg-white/10 rounded-xl transition-all"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-center gap-4 mb-8">
                            <div className="w-14 h-14 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-500/20">
                                <UserPlus size={28} className="text-white" />
                            </div>
                            <div>
                                <h2 className="text-2xl font-black text-white">افزودن ادمین جدید</h2>
                                <p className="text-slate-400 text-sm">اطلاعات ادمین جدید را وارد کنید</p>
                            </div>
                        </div>

                        <form onSubmit={handleCreate} className="space-y-5">
                            <div className="space-y-2">
                                <label className="text-sm font-medium text-slate-300 mr-1 flex items-center gap-2">
                                    <User size={16} /> نام نمایشی
                                </label>
                                <input
                                    type="text"
                                    className="w-full bg-white/5 border border-white/10 rounded-xl p-4 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600"
                                    placeholder="مثلاً: مدیر محتوا"
                                    value={formDisplayName}
                                    onChange={(e) => setFormDisplayName(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium text-slate-300 mr-1 flex items-center gap-2">
                                    <User size={16} /> نام کاربری (انگلیسی)
                                </label>
                                <input
                                    type="text"
                                    className="w-full bg-white/5 border border-white/10 rounded-xl p-4 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600"
                                    placeholder="admin2"
                                    value={formUsername}
                                    onChange={(e) => setFormUsername(e.target.value)}
                                    required
                                    style={{ direction: 'ltr' }}
                                />
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium text-slate-300 mr-1 flex items-center gap-2">
                                    <Lock size={16} /> رمز عبور
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        className="w-full bg-white/5 border border-white/10 rounded-xl p-4 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600 pl-14"
                                        placeholder="حداقل ۶ کاراکتر"
                                        value={formPassword}
                                        onChange={(e) => setFormPassword(e.target.value)}
                                        required
                                        style={{ direction: 'ltr' }}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-white transition-colors"
                                    >
                                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            {/* Super Admin Toggle */}
                            <div
                                className="flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl cursor-pointer hover:bg-white/[0.07] transition-all"
                                onClick={() => setFormIsSuperAdmin(!formIsSuperAdmin)}
                            >
                                <div className="flex items-center gap-3">
                                    <Crown size={20} className={formIsSuperAdmin ? 'text-amber-400' : 'text-slate-500'} />
                                    <div>
                                        <div className="font-bold text-white text-sm">سوپر ادمین</div>
                                        <div className="text-xs text-slate-500">دسترسی کامل به همه بخش‌ها و مدیریت ادمین‌ها</div>
                                    </div>
                                </div>
                                <div className={`w-12 h-7 rounded-full transition-all duration-300 flex items-center ${formIsSuperAdmin ? 'bg-amber-500 justify-end' : 'bg-slate-700 justify-start'
                                    }`}>
                                    <div className={`w-5 h-5 bg-white rounded-full shadow-md mx-1 transition-all duration-300`}></div>
                                </div>
                            </div>

                            {formError && (
                                <div className="bg-red-500/10 border border-red-500/20 text-red-500 p-4 rounded-xl text-sm">
                                    {formError}
                                </div>
                            )}

                            <button
                                type="submit"
                                disabled={formLoading}
                                className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all active:scale-95 flex items-center justify-center gap-3 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {formLoading ? (
                                    <Loader2 className="animate-spin" />
                                ) : (
                                    <>
                                        <Plus size={20} />
                                        <span>ایجاد ادمین</span>
                                    </>
                                )}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminManagement;
