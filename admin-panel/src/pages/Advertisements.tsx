import { useState, useEffect } from 'react';
import { adminApi, getMediaUrl } from '../api/adminApi';
import type { AdRequest } from '../api/adminApi';

const STATUS_LABELS: Record<string, { label: string; color: string; bg: string }> = {
    PENDING: { label: 'در انتظار بررسی', color: 'text-amber-400', bg: 'bg-amber-500/10' },
    APPROVED: { label: 'تایید شده', color: 'text-emerald-400', bg: 'bg-emerald-500/10' },
    REJECTED: { label: 'رد شده', color: 'text-rose-400', bg: 'bg-rose-500/10' },
};

const Advertisements = () => {
    const [adRequests, setAdRequests] = useState<AdRequest[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<string | undefined>(undefined);
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    const fetchAds = async () => {
        setLoading(true);
        try {
            const res = await adminApi.getAdRequests(filter);
            setAdRequests(res.data.data.adRequests || []);
        } catch (err) {
            console.error('Failed to fetch ads:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchAds(); }, [filter]);

    const handleApprove = async (id: string) => {
        setActionLoading(id);
        try {
            await adminApi.approveAd(id);
            setAdRequests(prev => prev.map(a => a.id === id ? { ...a, status: 'APPROVED' as const } : a));
        } catch (err) {
            console.error('Approve failed:', err);
        } finally {
            setActionLoading(null);
        }
    };

    const handleReject = async (id: string) => {
        setActionLoading(id);
        try {
            await adminApi.rejectAd(id);
            setAdRequests(prev => prev.map(a => a.id === id ? { ...a, status: 'REJECTED' as const } : a));
        } catch (err) {
            console.error('Reject failed:', err);
        } finally {
            setActionLoading(null);
        }
    };

    const pendingCount = adRequests.filter(a => a.status === 'PENDING').length;

    return (
        <div className="space-y-8">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-black text-white">📢 مدیریت تبلیغات</h1>
                    <p className="text-slate-400 mt-1">
                        بررسی و تایید درخواست‌های تبلیغاتی کاربران
                    </p>
                </div>
                {pendingCount > 0 && (
                    <div className="flex items-center gap-2 px-4 py-2 bg-amber-500/10 border border-amber-500/20 rounded-xl">
                        <div className="w-2 h-2 bg-amber-400 rounded-full animate-pulse" />
                        <span className="text-amber-400 font-bold text-sm">
                            {pendingCount} درخواست جدید
                        </span>
                    </div>
                )}
            </div>

            {/* Filter Tabs */}
            <div className="flex gap-2">
                {[
                    { value: undefined, label: 'همه' },
                    { value: 'PENDING', label: 'در انتظار' },
                    { value: 'APPROVED', label: 'تایید شده' },
                    { value: 'REJECTED', label: 'رد شده' },
                ].map((tab) => (
                    <button
                        key={tab.label}
                        onClick={() => setFilter(tab.value)}
                        className={`px-5 py-2.5 rounded-xl font-bold text-sm transition-all duration-300 ${filter === tab.value
                            ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                            : 'bg-white/5 text-slate-400 hover:text-white hover:bg-white/10'
                            }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Loading */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                </div>
            ) : adRequests.length === 0 ? (
                <div className="text-center py-20">
                    <div className="text-6xl mb-4">📭</div>
                    <p className="text-slate-400 text-lg">درخواست تبلیغاتی یافت نشد</p>
                </div>
            ) : (
                /* Ad Request Cards */
                <div className="grid gap-4">
                    {adRequests.map((ad) => {
                        const status = STATUS_LABELS[ad.status] || STATUS_LABELS.PENDING;
                        const isActioning = actionLoading === ad.id;

                        return (
                            <div
                                key={ad.id}
                                className="glass rounded-2xl p-6 border border-white/5 hover:border-white/10 transition-all duration-300"
                            >
                                <div className="flex items-start gap-5">
                                    {/* Requester Avatar */}
                                    <div className="w-12 h-12 rounded-xl bg-indigo-500/10 flex items-center justify-center flex-shrink-0 overflow-hidden">
                                        {ad.requesterAvatar ? (
                                            <img
                                                src={getMediaUrl(ad.requesterAvatar)}
                                                alt={ad.requesterName}
                                                className="w-full h-full object-cover"
                                            />
                                        ) : (
                                            <span className="text-indigo-400 font-bold text-lg">
                                                {ad.requesterName.charAt(0)}
                                            </span>
                                        )}
                                    </div>

                                    {/* Content */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center gap-3 mb-2">
                                            <span className="text-white font-bold">{ad.requesterName}</span>
                                            <span className={`px-2.5 py-0.5 rounded-lg text-xs font-bold ${status.color} ${status.bg}`}>
                                                {status.label}
                                            </span>
                                            <span className="text-slate-500 text-xs mr-auto">
                                                {new Date(ad.createdAt).toLocaleDateString('fa-IR')}
                                            </span>
                                        </div>

                                        {/* Target Channel */}
                                        <div className="flex items-center gap-2 mb-3">
                                            <span className="text-slate-500 text-xs">به کانال:</span>
                                            <div className="flex items-center gap-1.5">
                                                {ad.targetChannelAvatar ? (
                                                    <img
                                                        src={getMediaUrl(ad.targetChannelAvatar)}
                                                        alt=""
                                                        className="w-5 h-5 rounded-md object-cover"
                                                    />
                                                ) : (
                                                    <div className="w-5 h-5 rounded-md bg-amber-500/20 flex items-center justify-center">
                                                        <span className="text-amber-400 text-[10px] font-bold">
                                                            {ad.targetChannelName.charAt(0)}
                                                        </span>
                                                    </div>
                                                )}
                                                <span className="text-amber-400 text-xs font-bold">{ad.targetChannelName}</span>
                                            </div>
                                        </div>

                                        {/* Message Content */}
                                        <div className="bg-white/5 rounded-xl p-4 mb-3">
                                            <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">
                                                {ad.messageContent}
                                            </p>
                                            {ad.messageMediaUrl && (
                                                <div className="mt-2">
                                                    <img
                                                        src={getMediaUrl(ad.messageMediaUrl)}
                                                        alt="Media"
                                                        className="w-40 h-28 object-cover rounded-lg"
                                                    />
                                                </div>
                                            )}
                                        </div>

                                        {/* Source Info */}
                                        <div className="flex items-center gap-4 text-xs text-slate-500">
                                            <span>نوع منبع: {ad.sourceType}</span>
                                            <span>نوع پیام: {ad.messageType}</span>
                                        </div>
                                    </div>

                                    {/* Actions */}
                                    {ad.status === 'PENDING' && (
                                        <div className="flex flex-col gap-2 flex-shrink-0">
                                            <button
                                                onClick={() => handleApprove(ad.id)}
                                                disabled={isActioning}
                                                className="px-5 py-2.5 bg-emerald-500/10 text-emerald-400 font-bold text-sm rounded-xl hover:bg-emerald-500/20 transition-all disabled:opacity-50"
                                            >
                                                {isActioning ? '...' : '✅ تایید'}
                                            </button>
                                            <button
                                                onClick={() => handleReject(ad.id)}
                                                disabled={isActioning}
                                                className="px-5 py-2.5 bg-rose-500/10 text-rose-400 font-bold text-sm rounded-xl hover:bg-rose-500/20 transition-all disabled:opacity-50"
                                            >
                                                {isActioning ? '...' : '❌ رد'}
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default Advertisements;
