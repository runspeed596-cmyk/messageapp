import React, { useEffect, useState } from 'react';
import { adminApi, type Feedback } from '../api/adminApi';
import { Eye, MessageSquare, Star, X } from 'lucide-react';

const FeedbackManagement: React.FC = () => {
    const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(1);
    
    // Modal state for editing status/note
    const [selectedFeedback, setSelectedFeedback] = useState<Feedback | null>(null);
    const [newStatus, setNewStatus] = useState<string>('');
    const [adminNote, setAdminNote] = useState<string>('');
    const [updating, setUpdating] = useState<boolean>(false);

    useEffect(() => {
        loadFeedbacks();
    }, [page]);

    const loadFeedbacks = async () => {
        setLoading(true);
        try {
            const res = await adminApi.getFeedbacks(page, 20);
            if (res.data.success) {
                setFeedbacks(res.data.data.content);
                setTotalPages(res.data.data.totalPages);
            }
        } catch (error) {
            console.error("Failed to load feedbacks", error);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdate = async () => {
        if (!selectedFeedback) return;
        setUpdating(true);
        try {
            const res = await adminApi.updateFeedbackStatus(selectedFeedback.id, newStatus, adminNote);
            if (res.data.success) {
                setSelectedFeedback(null);
                loadFeedbacks();
            }
        } catch (error) {
            console.error("Failed to update feedback", error);
        } finally {
            setUpdating(false);
        }
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'OPEN': return <span className="bg-emerald-500/10 text-emerald-500 text-[10px] font-black px-3 py-1 rounded-full border border-emerald-500/20">جدید</span>;
            case 'IN_PROGRESS': return <span className="bg-amber-500/10 text-amber-500 text-[10px] font-black px-3 py-1 rounded-full border border-amber-500/20">در حال بررسی</span>;
            case 'RESOLVED': return <span className="bg-indigo-500/10 text-indigo-400 text-[10px] font-black px-3 py-1 rounded-full border border-indigo-500/20">رفع شده</span>;
            case 'CLOSED': return <span className="bg-slate-500/10 text-slate-400 text-[10px] font-black px-3 py-1 rounded-full border border-slate-500/20">بسته شده</span>;
            default: return <span className="bg-slate-500/10 text-slate-400 text-[10px] font-black px-3 py-1 rounded-full border border-slate-500/20">{status}</span>;
        }
    };

    const renderStars = (rating: number) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <Star key={i} size={16} className={i <= rating ? 'text-amber-400 fill-amber-400' : 'text-slate-600'} />
            );
        }
        return <div className="flex gap-1" dir="ltr">{stars}</div>;
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end mb-4">
                <div>
                    <h1 className="text-3xl font-black text-white">مدیریت انتقادات و پیشنهادات</h1>
                    <p className="text-slate-400 mt-2">بررسی و مدیریت بازخوردهای کاربران اپلیکیشن</p>
                </div>
            </div>

            <div className="glass rounded-[2rem] overflow-hidden border-white/5 shadow-2xl">
                <div className="overflow-x-auto">
                    <table className="w-full text-right border-collapse min-w-[800px]">
                        <thead>
                            <tr className="border-b border-white/5">
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">کاربر</th>
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">عنوان</th>
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">امتیاز</th>
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">وضعیت</th>
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest">تاریخ</th>
                                <th className="p-6 text-xs font-black text-slate-400 uppercase tracking-widest text-left">عملیات</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-white/5">
                            {loading ? (
                                <tr>
                                    <td colSpan={6} className="p-20 text-center">
                                        <div className="w-12 h-12 border-4 border-indigo-500/20 border-t-indigo-500 rounded-full animate-spin mx-auto mb-4"></div>
                                        <p className="text-slate-400 font-bold animate-pulse">در حال بارگذاری...</p>
                                    </td>
                                </tr>
                            ) : feedbacks.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="p-20 text-center space-y-4">
                                        <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-6">
                                            <MessageSquare size={40} className="text-slate-600" />
                                        </div>
                                        <h3 className="text-xl font-black text-white">هیچ رکوردی یافت نشد</h3>
                                        <p className="text-slate-500">در حال حاضر بازخوردی برای نمایش وجود ندارد.</p>
                                    </td>
                                </tr>
                            ) : (
                                feedbacks.map((fb) => (
                                    <tr key={fb.id} className="group hover:bg-white/[0.02] transition-colors">
                                        <td className="p-6">
                                            <div className="font-black text-white text-sm">{fb.userDisplayName || 'ناشناس'}</div>
                                        </td>
                                        <td className="p-6 text-sm text-slate-300 max-w-xs truncate" title={fb.title}>{fb.title}</td>
                                        <td className="p-6 whitespace-nowrap">{renderStars(fb.rating)}</td>
                                        <td className="p-6 whitespace-nowrap">{getStatusBadge(fb.status)}</td>
                                        <td className="p-6 whitespace-nowrap text-sm text-slate-400 font-bold" dir="ltr">{new Date(fb.createdAt).toLocaleDateString('fa-IR')}</td>
                                        <td className="p-6">
                                            <div className="flex justify-end items-center gap-2">
                                                <button
                                                    onClick={() => {
                                                        setSelectedFeedback(fb);
                                                        setNewStatus(fb.status);
                                                        setAdminNote(fb.adminNote || '');
                                                    }}
                                                    className="p-3 text-slate-500 hover:text-indigo-400 hover:bg-indigo-400/10 rounded-xl transition-all flex items-center gap-2"
                                                    title="مشاهده و بررسی"
                                                >
                                                    <Eye size={18} />
                                                    <span className="text-xs font-bold hidden md:block">بررسی</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
                
                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="p-4 border-t border-white/5 flex items-center justify-between">
                        <button
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={page === 0}
                            className="px-4 py-2 bg-white/5 text-white rounded-xl hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed transition-all text-sm font-bold"
                        >
                            قبلی
                        </button>
                        <span className="text-sm text-slate-400 font-bold mt-1">
                            صفحه {page + 1} از {totalPages}
                        </span>
                        <button
                            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                            disabled={page >= totalPages - 1}
                            className="px-4 py-2 bg-white/5 text-white rounded-xl hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed transition-all text-sm font-bold"
                        >
                            بعدی
                        </button>
                    </div>
                )}
            </div>

            {/* Edit Modal */}
            {selectedFeedback && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
                    <div className="bg-slate-900 border border-white/10 rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl animate-in zoom-in-95 duration-200 rtl">
                        <div className="flex justify-between items-center p-6 border-b border-white/5">
                            <h3 className="text-xl font-black text-white flex items-center gap-2">
                                <MessageSquare size={20} className="text-indigo-400" />
                                بررسی انتقاد / پیشنهاد
                            </h3>
                            <button 
                                onClick={() => setSelectedFeedback(null)}
                                className="text-slate-400 hover:text-white hover:bg-white/5 p-2 rounded-xl transition-all"
                            >
                                <X size={20} />
                            </button>
                        </div>
                        
                        <div className="p-6 space-y-6">
                            <div className="bg-white/5 p-4 rounded-2xl border border-white/5">
                                <div className="flex justify-between items-start mb-4">
                                    <div>
                                        <p className="text-xs text-slate-400 mb-1">نام کاربر</p>
                                        <p className="font-bold text-white text-sm">{selectedFeedback.userDisplayName}</p>
                                    </div>
                                    <div>{renderStars(selectedFeedback.rating)}</div>
                                </div>
                                <div className="mb-2">
                                    <p className="text-xs text-slate-400 mb-1">عنوان</p>
                                    <p className="font-bold text-white text-sm">{selectedFeedback.title}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-slate-400 mb-1">متن پیام</p>
                                    <p className="text-sm text-slate-300 leading-relaxed whitespace-pre-wrap">{selectedFeedback.description}</p>
                                </div>
                            </div>
                            
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-300 ml-2">تغییر وضعیت</label>
                                <select
                                    value={newStatus}
                                    onChange={(e) => setNewStatus(e.target.value)}
                                    className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm font-bold"
                                >
                                    <option className="bg-slate-800" value="OPEN">جدید (OPEN)</option>
                                    <option className="bg-slate-800" value="IN_PROGRESS">در حال بررسی (IN_PROGRESS)</option>
                                    <option className="bg-slate-800" value="RESOLVED">رفع شده (RESOLVED)</option>
                                    <option className="bg-slate-800" value="CLOSED">بسته شده (CLOSED)</option>
                                </select>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-300 ml-2">یادداشت مدیر (اختیاری)</label>
                                <textarea
                                    value={adminNote}
                                    onChange={(e) => setAdminNote(e.target.value)}
                                    rows={3}
                                    className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600 text-sm"
                                    placeholder="یادداشت‌ها و پیگیری‌های مربوط به این تیکت را اینجا بنویسید..."
                                />
                            </div>
                        </div>

                        <div className="p-6 border-t border-white/5 flex gap-3 rtl:flex-row-reverse">
                            <button
                                type="button"
                                onClick={() => setSelectedFeedback(null)}
                                className="flex-1 py-3 bg-white/5 hover:bg-white/10 text-white font-bold rounded-xl transition-all"
                            >
                                انصراف
                            </button>
                            <button
                                type="button"
                                onClick={handleUpdate}
                                disabled={updating}
                                className="flex-1 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl shadow-lg shadow-indigo-500/20 transition-all active:scale-95 disabled:opacity-50"
                            >
                                {updating ? 'در حال ثبت...' : 'ثبت تغییرات'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FeedbackManagement;
