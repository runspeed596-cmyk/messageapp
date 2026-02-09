import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { User } from '../api/adminApi';
import { Trash2, User as UserIcon, Calendar, Phone, Search, MoreVertical, Shield } from 'lucide-react';

const Users = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await adminApi.getUsers();
            if (response.data.success) {
                setUsers(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('آیا از حذف این کاربر اطمینان دارید؟ این عمل غیرقابل بازگشت است.')) {
            try {
                await adminApi.deleteUser(id);
                setUsers(users.filter(u => u.id !== id));
            } catch (error) {
                alert('خطا در حذف کاربر');
            }
        }
    };

    if (loading) return (
        <div className="flex flex-col items-center justify-center h-[60vh] space-y-4">
            <div className="w-12 h-12 border-4 border-indigo-500/20 border-t-indigo-500 rounded-full animate-spin"></div>
            <p className="text-slate-400 font-bold animate-pulse">در حال فراخوانی لیست کاربران...</p>
        </div>
    );

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end mb-4">
                <div>
                    <h1 className="text-3xl font-black text-white">مدیریت کاربران</h1>
                    <p className="text-slate-400 mt-2">مشاهده، جستجو و کنترل دسترسی کاربران پلتفرم</p>
                </div>
                <div className="flex gap-4">
                    <div className="relative group">
                        <Search className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-indigo-400 transition-colors" size={18} />
                        <input
                            type="text"
                            placeholder="جستجوی کاربر..."
                            className="glass pr-12 pl-4 py-3 rounded-2xl text-sm focus:ring-2 focus:ring-indigo-500 outline-none w-64 transition-all"
                        />
                    </div>
                </div>
            </div>

            <div className="glass rounded-[2rem] overflow-hidden border-white/5 shadow-2xl">
                <table className="w-full text-right border-collapse">
                    <thead>
                        <tr className="border-b border-white/5">
                            <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">پروفایل کاربر</th>
                            <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">اطلاعات تماس</th>
                            <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">تاریخ عضویت</th>
                            <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest text-center">وضعیت</th>
                            <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest text-left">عملیات</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                        {users.map((user) => (
                            <tr key={user.id} className="group hover:bg-white/[0.02] transition-colors">
                                <td className="p-6">
                                    <div className="flex items-center gap-4">
                                        <div className="relative">
                                            <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 overflow-hidden group-hover:scale-110 transition-transform duration-500">
                                                {user.avatarUrl ? (
                                                    <img src={user.avatarUrl} alt="" className="w-full h-full object-cover" />
                                                ) : (
                                                    <UserIcon size={24} />
                                                )}
                                            </div>
                                            <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-emerald-500 border-4 border-slate-900 rounded-full"></div>
                                        </div>
                                        <div>
                                            <div className="font-black text-white text-lg">{user.displayName}</div>
                                            <div className="text-xs text-indigo-400 font-bold tracking-tight">@{user.username}</div>
                                        </div>
                                    </div>
                                </td>
                                <td className="p-6">
                                    <div className="flex flex-col gap-1">
                                        <div className="flex items-center gap-2 text-slate-300 font-bold text-sm">
                                            <Phone size={14} className="text-slate-500" />
                                            <span style={{ direction: 'ltr' }}>{user.phoneNumber}</span>
                                        </div>
                                        <div className="text-[10px] text-slate-500">تایید شده</div>
                                    </div>
                                </td>
                                <td className="p-6">
                                    <div className="flex items-center gap-2 text-slate-300 text-sm font-bold">
                                        <Calendar size={14} className="text-slate-500" />
                                        {new Date(user.createdAt).toLocaleDateString('fa-IR')}
                                    </div>
                                </td>
                                <td className="p-6 text-center">
                                    <span className="bg-emerald-500/10 text-emerald-500 text-[10px] font-black px-3 py-1 rounded-full border border-emerald-500/20">فعال</span>
                                </td>
                                <td className="p-6">
                                    <div className="flex justify-end items-center gap-2">
                                        <button className="p-3 text-slate-500 hover:text-indigo-400 hover:bg-indigo-400/10 rounded-xl transition-all"><Shield size={18} /></button>
                                        <button
                                            onClick={() => handleDelete(user.id)}
                                            className="p-3 text-slate-500 hover:text-rose-500 hover:bg-rose-500/10 rounded-xl transition-all"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                        <button className="p-3 text-slate-500 hover:text-white rounded-xl"><MoreVertical size={18} /></button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {users.length === 0 && (
                    <div className="p-20 text-center space-y-4">
                        <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-6">
                            <UserIcon size={40} className="text-slate-600" />
                        </div>
                        <h3 className="text-xl font-black text-white">هیچ کاربری یافت نشد</h3>
                        <p className="text-slate-500">در حال حاضر کاربری برای نمایش وجود ندارد یا دیتابیس خالی است.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Users;
