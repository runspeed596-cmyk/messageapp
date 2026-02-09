import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { ElmEvent } from '../api/adminApi';
import { Plus, Check, X, Trophy, Calendar, MapPin, Clock, CheckCircle, XCircle, AlertCircle, Loader2, Link as LinkIcon, Rocket, Users } from 'lucide-react';

type TabType = 'pending' | 'approved';

const Competitions = () => {
    const [activeTab, setActiveTab] = useState<TabType>('pending');
    const [pendingEvents, setPendingEvents] = useState<ElmEvent[]>([]);
    const [approvedEvents, setApprovedEvents] = useState<ElmEvent[]>([]);
    const [isAdding, setIsAdding] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [newEvent, setNewEvent] = useState<Partial<ElmEvent>>({
        title: '',
        description: '',
        date: '',
        location: '',
        imageUrl: '',
        organizer: '',
        reward: '',
        type: 'COMPETITION',
        isExternal: false,
        link: '',
        isApproved: true // Admin-created events are auto-approved
    });

    const eventTypes = [
        { value: 'COMPETITION', label: 'مسابقه', icon: Trophy },
        { value: 'STARTUP', label: 'استارتاپ', icon: Rocket },
        { value: 'CONGRESS', label: 'کنگره/همایش', icon: Users }
    ];

    useEffect(() => {
        fetchEvents();
    }, []);

    const fetchEvents = async () => {
        try {
            const [pendingRes, approvedRes] = await Promise.all([
                adminApi.getPendingEvents(),
                adminApi.getEvents()
            ]);
            if (pendingRes.data.success) {
                setPendingEvents(pendingRes.data.data);
            }
            // Approved events come from getEvents (filtered by isApproved on backend)
            if (approvedRes.data.success) {
                // Filter to show only approved ones in case backend returns all
                setApprovedEvents(approvedRes.data.data.filter(e => e.isApproved));
            }
        } catch (error) {
            console.error('Error fetching events:', error);
        }
    };

    const handleApprove = async (id: string) => {
        try {
            await adminApi.approveEvent(id);
            fetchEvents();
        } catch (error) {
            alert('خطا در تأیید رویداد');
        }
    };

    const handleReject = async (id: string) => {
        if (window.confirm('آیا از رد این رویداد اطمینان دارید؟ این عملیات قابل بازگشت نیست.')) {
            try {
                await adminApi.rejectEvent(id);
                fetchEvents();
            } catch (error) {
                alert('خطا در رد رویداد');
            }
        }
    };

    const handleSave = async () => {
        if (!newEvent.title || !newEvent.description) {
            alert('لطفاً عنوان و توضیحات را وارد کنید');
            return;
        }
        setIsLoading(true);
        try {
            await adminApi.saveEvent(newEvent as ElmEvent);
            fetchEvents();
            setIsAdding(false);
            setNewEvent({
                title: '', description: '', date: '', location: '', imageUrl: '',
                organizer: '', reward: '', type: 'COMPETITION', isExternal: false, link: '', isApproved: true
            });
        } catch (error) {
            alert('خطا در ذخیره رویداد');
        } finally {
            setIsLoading(false);
        }
    };

    const getTypeIcon = (type: string) => {
        switch (type) {
            case 'STARTUP': return <Rocket size={20} className="text-amber-400" />;
            case 'CONGRESS': return <Users size={20} className="text-indigo-400" />;
            default: return <Trophy size={20} className="text-emerald-400" />;
        }
    };

    const getTypeLabel = (type: string) => {
        return eventTypes.find(t => t.value === type)?.label || 'مسابقه';
    };

    const renderEventCard = (event: ElmEvent, isPending: boolean) => (
        <div key={event.id} className={`glass rounded-[2rem] p-6 border-white/5 hover:border-${isPending ? 'amber' : 'emerald'}-500/30 transition-all group`}>
            <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${isPending ? 'bg-amber-500/20' : 'bg-emerald-500/20'
                        }`}>
                        {getTypeIcon(event.type)}
                    </div>
                    <div>
                        <span className={`text-xs font-bold ${isPending ? 'text-amber-400' : 'text-emerald-400'}`}>
                            {getTypeLabel(event.type)}
                        </span>
                        {isPending && (
                            <span className="text-xs text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full mr-2">
                                در انتظار تأیید
                            </span>
                        )}
                    </div>
                </div>
                {isPending && (
                    <div className="flex gap-2">
                        <button onClick={() => handleApprove(event.id!)}
                            className="p-2 bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500 hover:text-white rounded-xl transition-all">
                            <CheckCircle size={18} />
                        </button>
                        <button onClick={() => handleReject(event.id!)}
                            className="p-2 bg-rose-500/20 text-rose-400 hover:bg-rose-500 hover:text-white rounded-xl transition-all">
                            <XCircle size={18} />
                        </button>
                    </div>
                )}
            </div>

            <h3 className="font-black text-lg text-white mb-2">{event.title}</h3>
            <p className="text-sm text-slate-500 mb-4 line-clamp-2">{event.description}</p>

            <div className="flex flex-wrap gap-4 text-xs text-slate-500">
                {event.date && (
                    <div className="flex items-center gap-1">
                        <Calendar size={12} />
                        <span>{event.date}</span>
                    </div>
                )}
                {event.location && (
                    <div className="flex items-center gap-1">
                        <MapPin size={12} />
                        <span>{event.location}</span>
                    </div>
                )}
                {event.reward && (
                    <div className="flex items-center gap-1 text-amber-400">
                        <Trophy size={12} />
                        <span>{event.reward}</span>
                    </div>
                )}
            </div>

            {event.link && (
                <a href={event.link} target="_blank" rel="noopener noreferrer"
                    className="mt-4 flex items-center gap-2 text-xs text-indigo-400 hover:underline">
                    <LinkIcon size={12} />
                    <span>مشاهده اطلاعات بیشتر</span>
                </a>
            )}
        </div>
    );

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-black text-white">قله علم</h1>
                    <p className="text-slate-400 mt-2">مدیریت مسابقات، استارتاپ‌ها و کنگره‌های علمی</p>
                </div>
                <button onClick={() => setIsAdding(true)}
                    className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg transition-all active:scale-95 group">
                    <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                    <span>ثبت رویداد جدید</span>
                </button>
            </div>

            {isAdding && (
                <div className="glass p-8 rounded-[2rem] border-emerald-500/20 shadow-2xl animate-in fade-in">
                    <h3 className="text-xl font-black text-white mb-6 flex items-center gap-3">
                        <Trophy className="text-emerald-400" /> ایجاد رویداد جدید (تأیید خودکار)
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2 md:col-span-2">
                            <label className="text-xs font-black text-slate-500 uppercase">عنوان رویداد</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="مثال: مسابقه برنامه‌نویسی ملی"
                                value={newEvent.title} onChange={e => setNewEvent({ ...newEvent, title: e.target.value })} />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">نوع رویداد</label>
                            <select className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none appearance-none focus:ring-2 focus:ring-emerald-500"
                                value={newEvent.type} onChange={e => setNewEvent({ ...newEvent, type: e.target.value as 'COMPETITION' | 'STARTUP' | 'CONGRESS' })}>
                                {eventTypes.map(t => <option key={t.value} value={t.value} className="bg-slate-900">{t.label}</option>)}
                            </select>
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">برگزارکننده</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="مثال: دانشگاه تهران"
                                value={newEvent.organizer} onChange={e => setNewEvent({ ...newEvent, organizer: e.target.value })} />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">تاریخ</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="مثال: ۱۴۰۴/۰۱/۱۵"
                                value={newEvent.date} onChange={e => setNewEvent({ ...newEvent, date: e.target.value })} />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">مکان</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="مثال: تهران"
                                value={newEvent.location} onChange={e => setNewEvent({ ...newEvent, location: e.target.value })} />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">جایزه</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="مثال: ۱۰ میلیون تومان"
                                value={newEvent.reward} onChange={e => setNewEvent({ ...newEvent, reward: e.target.value })} />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">لینک خارجی</label>
                            <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                placeholder="https://..."
                                value={newEvent.link} onChange={e => setNewEvent({ ...newEvent, link: e.target.value })} />
                        </div>
                        <div className="space-y-2 md:col-span-2">
                            <label className="text-xs font-black text-slate-500 uppercase">توضیحات</label>
                            <textarea className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none resize-none h-24 focus:ring-2 focus:ring-emerald-500"
                                placeholder="توضیحات رویداد..."
                                value={newEvent.description} onChange={e => setNewEvent({ ...newEvent, description: e.target.value })} />
                        </div>
                    </div>
                    <div className="flex gap-4 mt-8">
                        <button onClick={handleSave} disabled={isLoading}
                            className={`flex-1 py-4 rounded-2xl font-black flex items-center justify-center gap-3 ${isLoading ? 'bg-slate-600' : 'bg-emerald-600 hover:bg-emerald-500'} text-white`}>
                            {isLoading ? <><Loader2 className="animate-spin" /> در حال ذخیره...</> : <><Check /> ثبت رویداد</>}
                        </button>
                        <button onClick={() => setIsAdding(false)}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black">
                            <X className="inline mr-2" /> انصراف
                        </button>
                    </div>
                </div>
            )}

            <div className="flex gap-4">
                <button onClick={() => setActiveTab('pending')}
                    className={`flex-1 flex items-center justify-center gap-4 p-6 rounded-[2rem] border transition-all duration-500
                        ${activeTab === 'pending' ? 'bg-amber-500/10 border-amber-500/30 text-amber-400 scale-105 shadow-xl' : 'glass border-white/5 text-slate-500 hover:border-white/20'}`}>
                    <Clock size={24} />
                    <span className="text-lg font-black">در انتظار تأیید</span>
                    {pendingEvents.length > 0 && (
                        <span className="bg-amber-500 text-white text-xs font-black px-2 py-1 rounded-full">{pendingEvents.length}</span>
                    )}
                </button>
                <button onClick={() => setActiveTab('approved')}
                    className={`flex-1 flex items-center justify-center gap-4 p-6 rounded-[2rem] border transition-all duration-500
                        ${activeTab === 'approved' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 scale-105 shadow-xl' : 'glass border-white/5 text-slate-500 hover:border-white/20'}`}>
                    <CheckCircle size={24} />
                    <span className="text-lg font-black">تأیید شده</span>
                    <span className="bg-white/10 text-slate-400 text-xs font-black px-2 py-1 rounded-full">{approvedEvents.length}</span>
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {activeTab === 'pending' ? (
                    pendingEvents.length > 0 ? (
                        pendingEvents.map(e => renderEventCard(e, true))
                    ) : (
                        <div className="col-span-full glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                            <AlertCircle size={40} className="text-slate-700 mx-auto mb-6" />
                            <h3 className="text-xl font-black text-white">هیچ رویدادی در انتظار تأیید نیست</h3>
                            <p className="text-slate-500 mt-2">همه رویدادها بررسی شده‌اند.</p>
                        </div>
                    )
                ) : (
                    approvedEvents.length > 0 ? (
                        approvedEvents.map(e => renderEventCard(e, false))
                    ) : (
                        <div className="col-span-full glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                            <AlertCircle size={40} className="text-slate-700 mx-auto mb-6" />
                            <h3 className="text-xl font-black text-white">هیچ رویداد تأیید شده‌ای وجود ندارد</h3>
                            <p className="text-slate-500 mt-2">برای نمایش در اپلیکیشن، یک رویداد جدید ثبت کنید.</p>
                        </div>
                    )
                )}
            </div>
        </div>
    );
};

export default Competitions;
