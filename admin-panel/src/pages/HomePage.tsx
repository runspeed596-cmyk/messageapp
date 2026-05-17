import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { HomeBanner, University } from '../api/adminApi';
import {
    Plus,
    Trash2,
    Image as ImageIcon,
    GraduationCap,
    Film,
    TicketPercent,
    Layout
} from 'lucide-react';

const HomePage = () => {
    const [activeTab, setActiveTab] = useState<'banners' | 'science' | 'entertainment' | 'discounts'>('banners');
    const [banners, setBanners] = useState<HomeBanner[]>([]);
    const [universities, setUniversities] = useState<University[]>([]);
    const [movies, setMovies] = useState<any[]>([]);
    const [discounts, setDiscounts] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isActionLoading, setIsActionLoading] = useState(false);
    const [showAddModal, setShowAddModal] = useState(false);
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    // Form states
    const [bannerForm, setBannerForm] = useState<HomeBanner>({
        title: '',
        imageUrl: '',
        linkUrl: '',
        isActive: true
    });

    const [uniForm, setUniForm] = useState<University>({
        name: '',
        ministryName: 'وزارت علوم، تحقیقات و فناوری',
        type: 'دولتی',
        establishedYear: 1300,
        studentCount: 0,
        latitude: 35.6892,
        longitude: 51.3890
    });

    const [movieForm, setMovieForm] = useState({
        title: '',
        videoUrl: '',
        thumbnailUrl: '',
        duration: '00:00',
        isActive: true
    });

    const [discountForm, setDiscountForm] = useState({
        title: '',
        brandName: '',
        percent: 0,
        code: '',
        description: ''
    });

    useEffect(() => {
        fetchData();
    }, [activeTab]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            if (activeTab === 'banners') {
                const res = await adminApi.getBanners();
                if (res.data.success) setBanners(res.data.data || []);
            } else if (activeTab === 'science') {
                const res = await adminApi.getUniversities(0, 9999);
                if (res.data.success) setUniversities(res.data.data?.content || []);
            } else if (activeTab === 'entertainment') {
                const res = await adminApi.getMovies();
                if (res.data.success) setMovies(res.data.data || []);
            } else if (activeTab === 'discounts') {
                const res = await adminApi.getDiscounts();
                if (res.data.success) setDiscounts(res.data.data || []);
            }
        } catch (err) {
            console.error("Failed to fetch data:", err);
            showNotify('error', 'خطا در دریافت اطلاعات');
        } finally {
            setIsLoading(false);
        }
    };

    const showNotify = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const handleSaveBanner = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsActionLoading(true);
        try {
            const res = await adminApi.saveBanner(bannerForm);
            if (res.data.success) {
                showNotify('success', 'بنر با موفقیت ذخیره شد');
                setShowAddModal(false);
                fetchData();
                setBannerForm({ title: '', imageUrl: '', linkUrl: '', isActive: true });
            }
        } catch (err) {
            showNotify('error', 'خطا در ذخیره بنر');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleDeleteBanner = async (id: string) => {
        if (!window.confirm('آیا از حذف این بنر اطمینان دارید؟')) return;
        try {
            const res = await adminApi.deleteBanner(id);
            if (res.data.success) {
                showNotify('success', 'بنر حذف شد');
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در حذف بنر');
        }
    };

    const handleSaveUniversity = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsActionLoading(true);
        try {
            const res = await adminApi.saveUniversity(uniForm);
            if (res.data.success) {
                showNotify('success', 'دانشگاه با موفقیت ثبت شد');
                setShowAddModal(false);
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در ثبت دانشگاه');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleDeleteUniversity = async (id: string) => {
        if (!window.confirm('آیا از حذف این دانشگاه اطمینان دارید؟')) return;
        try {
            // Need to implement deleteUniversity in adminApi if not exists
            const res = await adminApi.deleteUniversity(id);
            if (res.data.success) {
                showNotify('success', 'دانشگاه حذف شد');
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در حذف دانشگاه');
        }
    };

    const handleDeleteMovie = async (id: string) => {
        if (!window.confirm('آیا از حذف این ویدیو اطمینان دارید؟')) return;
        try {
            const res = await adminApi.deleteMovie(id);
            if (res.data.success) {
                showNotify('success', 'ویدیو حذف شد');
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در حذف ویدیو');
        }
    };

    const handleDeleteDiscount = async (id: string) => {
        if (!window.confirm('آیا از حذف این تخفیف اطمینان دارید؟')) return;
        try {
            const res = await adminApi.deleteDiscount(id);
            if (res.data.success) {
                showNotify('success', 'تخفیف حذف شد');
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در حذف تخفیف');
        }
    };

    const handleSaveMovie = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsActionLoading(true);
        try {
            const res = await adminApi.saveMovie({ ...movieForm, isActive: true } as any);
            if (res.data.success) {
                showNotify('success', 'ویدیو با موفقیت ثبت شد');
                setShowAddModal(false);
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در ثبت ویدیو');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleSaveDiscount = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsActionLoading(true);
        try {
            const res = await adminApi.saveDiscount(discountForm);
            if (res.data.success) {
                showNotify('success', 'تخفیف با موفقیت ثبت شد');
                setShowAddModal(false);
                fetchData();
            }
        } catch (err) {
            showNotify('error', 'خطا در ثبت تخفیف');
        } finally {
            setIsActionLoading(false);
        }
    };

    const tabs = [
        { id: 'banners', label: 'بنرهای تبلیغاتی', icon: <ImageIcon size={18} />, color: 'from-blue-500 to-indigo-600' },
        { id: 'science', label: 'جهان علم', icon: <GraduationCap size={18} />, color: 'from-emerald-500 to-teal-600' },
        { id: 'entertainment', label: 'سرگرمی', icon: <Film size={18} />, color: 'from-rose-500 to-pink-600' },
        { id: 'discounts', label: 'تخفیف‌های ویژه', icon: <TicketPercent size={18} />, color: 'from-amber-500 to-orange-600' },
    ];

    return (
        <div className="space-y-8 animate-in fade-in duration-700">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-white tracking-tight">مدیریت خانه</h1>
                    <p className="text-white/50 mt-1">مدیریت محتوای صفحه اصلی اپلیکیشن</p>
                </div>
                <button
                    onClick={() => setShowAddModal(true)}
                    className="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-6 py-3 rounded-2xl border border-white/10 transition-all active:scale-95 group"
                >
                    <Plus size={20} className="group-hover:rotate-90 transition-transform duration-300" />
                    <span>افزودن مورد جدید</span>
                </button>
            </div>

            {/* Notification Toast */}
            {notification && (
                <div className={`fixed top-8 left-1/2 -translate-x-1/2 z-[100] px-6 py-3 rounded-2xl shadow-2xl animate-in slide-in-from-top duration-300 border backdrop-blur-xl ${notification.type === 'success' ? 'bg-emerald-500/20 border-emerald-500/30 text-emerald-400' : 'bg-rose-500/20 border-rose-500/30 text-rose-400'
                    }`}>
                    {notification.message}
                </div>
            )}

            {/* Tabs Navigation */}
            <div className="flex flex-wrap gap-2 p-1.5 bg-black/20 backdrop-blur-xl rounded-2xl border border-white/5 w-fit">
                {tabs.map((tab) => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id as any)}
                        className={`flex items-center gap-2 px-5 py-2.5 rounded-xl transition-all duration-300 ${activeTab === tab.id
                            ? `bg-gradient-to-r ${tab.color} text-white shadow-lg shadow-black/20`
                            : 'text-white/40 hover:text-white/70 hover:bg-white/5'
                            }`}
                    >
                        {tab.icon}
                        <span className="font-medium">{tab.label}</span>
                    </button>
                ))}
            </div>

            {/* Content Area */}
            <div className="bg-white/5 backdrop-blur-2xl rounded-3xl border border-white/10 overflow-hidden min-h-[500px] flex flex-col">
                {isLoading ? (
                    <div className="flex-1 flex items-center justify-center">
                        <div className="relative w-16 h-16">
                            <div className="absolute inset-0 rounded-full border-t-2 border-r-2 border-indigo-500 animate-spin"></div>
                            <div className="absolute inset-2 rounded-full border-b-2 border-l-2 border-rose-500 animate-spin-slow"></div>
                        </div>
                    </div>
                ) : (
                    <div className="p-6">
                        {activeTab === 'banners' && (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {banners.map((banner) => (
                                    <div key={banner.id} className="group relative bg-white/5 rounded-3xl border border-white/10 overflow-hidden hover:border-white/20 transition-all duration-500">
                                        <div className="aspect-[16/9] w-full bg-gradient-to-br from-indigo-500/20 to-purple-500/20 relative">
                                            <img src={banner.imageUrl} alt={banner.title} className="w-full h-full object-cover mix-blend-overlay group-hover:scale-110 transition-transform duration-700" />
                                            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                                            <div className="absolute bottom-4 right-4 left-4">
                                                <h3 className="text-lg font-bold text-white leading-tight">{banner.title}</h3>
                                            </div>
                                        </div>
                                        <div className="p-4 flex items-center justify-between">
                                            <div className="flex items-center gap-2">
                                                <span className={`w-2.5 h-2.5 rounded-full ${banner.isActive ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
                                                <span className="text-white/60 text-sm">{banner.isActive ? 'فعال' : 'غیرفعال'}</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <button className="p-2 bg-white/5 hover:bg-white/10 rounded-xl border border-white/10 text-white/60 hover:text-white transition-colors">
                                                    <Layout size={16} />
                                                </button>
                                                <button
                                                    onClick={() => banner.id && handleDeleteBanner(banner.id)}
                                                    className="p-2 bg-rose-500/10 hover:bg-rose-500/20 rounded-xl border border-rose-500/20 text-rose-500 transition-colors"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {activeTab === 'science' && (
                            <div className="overflow-x-auto">
                                <table className="w-full text-right">
                                    <thead>
                                        <tr className="border-b border-white/5 text-white/40 text-sm">
                                            <th className="px-6 py-4 font-medium">دانشگاه</th>
                                            <th className="px-6 py-4 font-medium">نوع</th>
                                            <th className="px-6 py-4 font-medium">تعداد دانشجو</th>
                                            <th className="px-6 py-4 font-medium">موقعیت</th>
                                            <th className="px-6 py-4 font-medium">عملیات</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-white/5">
                                        {universities.map((uni) => (
                                            <tr key={uni.id} className="group hover:bg-white/5 transition-colors cursor-pointer">
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/20 flex items-center justify-center text-emerald-500 border border-emerald-500/20">
                                                            <GraduationCap size={20} />
                                                        </div>
                                                        <span className="font-semibold text-white">{uni.name}</span>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 text-white/60">{uni.type || 'نامشخص'}</td>
                                                <td className="px-6 py-4 text-white/60 font-mono">{uni.studentCount.toLocaleString()}</td>
                                                <td className="px-6 py-4">
                                                    <span className="text-xs px-2 py-1 bg-white/5 rounded-lg border border-white/10 text-white/40 font-mono">
                                                        {uni.latitude.toFixed(2)}, {uni.longitude.toFixed(2)}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <button
                                                        onClick={() => uni.id && handleDeleteUniversity(uni.id)}
                                                        className="p-2 bg-rose-500/10 hover:bg-rose-500/20 rounded-xl border border-rose-500/20 text-rose-500 transition-colors"
                                                    >
                                                        <Trash2 size={18} />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        {activeTab === 'entertainment' && (
                            <div className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                    {movies.map((movie) => (
                                        <div key={movie.id} className="group bg-white/5 rounded-3xl border border-white/10 overflow-hidden hover:border-white/20 transition-all duration-500">
                                            <div className="aspect-video relative overflow-hidden">
                                                <img src={movie.thumbnailUrl || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1000&auto=format&fit=crop'} alt={movie.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
                                                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent flex items-end p-4">
                                                    <h4 className="text-white font-bold">{movie.title}</h4>
                                                </div>
                                            </div>
                                            <div className="p-4 flex items-center justify-between">
                                                <span className="text-white/40 text-xs">{movie.duration}</span>
                                                <button
                                                    onClick={() => movie.id && handleDeleteMovie(movie.id)}
                                                    className="text-rose-500 hover:text-rose-400 transition-colors"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                                {movies.length === 0 && (
                                    <div className="flex items-center justify-center py-20 bg-black/20 rounded-3xl border border-dashed border-white/10">
                                        <div className="text-center">
                                            <div className="w-16 h-16 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-4 border border-white/10 text-white/20">
                                                <Film size={32} />
                                            </div>
                                            <h3 className="text-xl font-bold text-white mb-2">بخش سرگرمی</h3>
                                            <p className="text-white/40">در حال اتصال به ماژول ویدیو و موسیقی...</p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {activeTab === 'discounts' && (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                                {discounts.map((discount) => (
                                    <div key={discount.id} className="bg-white/5 border border-white/10 rounded-2xl p-4 hover:border-amber-500/30 group transition-all duration-300">
                                        <div className="flex items-center justify-between mb-3">
                                            <div className="w-10 h-10 bg-amber-500/20 rounded-xl flex items-center justify-center text-amber-500">
                                                <TicketPercent size={20} />
                                            </div>
                                            <span className="text-xs px-2 py-1 bg-amber-500/10 text-amber-500 rounded-lg border border-amber-500/20 font-bold">
                                                {discount.percent}%
                                            </span>
                                        </div>
                                        <h4 className="text-white font-bold mb-1 truncate">{discount.title}</h4>
                                        <p className="text-white/40 text-xs mb-3">{discount.brandName}</p>
                                        <div className="flex items-center justify-between">
                                            <span className="text-sm font-mono text-white/60 bg-black/20 px-2 py-1 rounded-lg border border-white/5">{discount.code || "---"}</span>
                                            <button
                                                onClick={() => discount.id && handleDeleteDiscount(discount.id)}
                                                className="text-white/20 hover:text-rose-500 transition-colors"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Add Modal */}
            {showAddModal && (
                <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setShowAddModal(false)} />
                    <div className="glass w-full max-w-lg rounded-[2.5rem] relative z-10 border-white/10 shadow-2xl overflow-hidden animate-in zoom-in duration-300">
                        <div className="p-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                {activeTab === 'banners' ? 'افزودن بنر جدید' :
                                    activeTab === 'science' ? 'ثبت دانشگاه جدید' :
                                        activeTab === 'entertainment' ? 'افزودن ویدیو جدید' : 'ثبت تخفیف جدید'}
                            </h2>
                            {activeTab === 'banners' ? (
                                <form onSubmit={handleSaveBanner} className="space-y-4">
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">عنوان بنر</label>
                                        <input
                                            type="text" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                            value={bannerForm.title} onChange={e => setBannerForm({ ...bannerForm, title: e.target.value })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">لینک تصویر</label>
                                        <input
                                            type="url" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                            value={bannerForm.imageUrl} onChange={e => setBannerForm({ ...bannerForm, imageUrl: e.target.value })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">لینک مقصد (اختیاری)</label>
                                        <input
                                            type="url"
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                            value={bannerForm.linkUrl || ''} onChange={e => setBannerForm({ ...bannerForm, linkUrl: e.target.value })}
                                        />
                                    </div>
                                    <div className="flex justify-end gap-3 mt-8">
                                        <button type="button" onClick={() => setShowAddModal(false)} className="px-6 py-3 rounded-xl text-white/50 hover:bg-white/5 transition-colors">انصراف</button>
                                        <button type="submit" disabled={isActionLoading} className="px-8 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-indigo-500/20 disabled:opacity-50">
                                            {isActionLoading ? 'در حال ثبت...' : 'ثبت بنر'}
                                        </button>
                                    </div>
                                </form>
                            ) : activeTab === 'science' ? (
                                <form onSubmit={handleSaveUniversity} className="space-y-4">
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">نام دانشگاه</label>
                                        <input
                                            type="text" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                            value={uniForm.name} onChange={e => setUniForm({ ...uniForm, name: e.target.value })}
                                        />
                                    </div>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <label className="text-sm text-white/50 mr-2">تعداد دانشجو</label>
                                            <input
                                                type="number" required
                                                className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                                value={uniForm.studentCount} onChange={e => setUniForm({ ...uniForm, studentCount: parseInt(e.target.value) })}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-sm text-white/50 mr-2">سال تاسیس</label>
                                            <input
                                                type="number" required
                                                className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-emerald-500"
                                                value={uniForm.establishedYear} onChange={e => setUniForm({ ...uniForm, establishedYear: parseInt(e.target.value) })}
                                            />
                                        </div>
                                    </div>
                                    <div className="flex justify-end gap-3 mt-8">
                                        <button type="button" onClick={() => setShowAddModal(false)} className="px-6 py-3 rounded-xl text-white/50 hover:bg-white/5 transition-colors">انصراف</button>
                                        <button type="submit" disabled={isActionLoading} className="px-8 py-3 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-emerald-500/20 disabled:opacity-50">
                                            {isActionLoading ? 'در حال ثبت...' : 'ثبت دانشگاه'}
                                        </button>
                                    </div>
                                </form>
                            ) : activeTab === 'entertainment' ? (
                                <form onSubmit={handleSaveMovie} className="space-y-4">
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">عنوان ویدیو</label>
                                        <input
                                            type="text" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-rose-500"
                                            value={movieForm.title} onChange={e => setMovieForm({ ...movieForm, title: e.target.value })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">لینک ویدیو</label>
                                        <input
                                            type="url" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-rose-500"
                                            value={movieForm.videoUrl} onChange={e => setMovieForm({ ...movieForm, videoUrl: e.target.value })}
                                        />
                                    </div>
                                    <div className="flex justify-end gap-3 mt-8">
                                        <button type="button" onClick={() => setShowAddModal(false)} className="px-6 py-3 rounded-xl text-white/50 hover:bg-white/5 transition-colors">انصراف</button>
                                        <button type="submit" disabled={isActionLoading} className="px-8 py-3 bg-rose-600 hover:bg-rose-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-rose-500/20 disabled:opacity-50">
                                            {isActionLoading ? 'در حال ثبت...' : 'ثبت ویدیو'}
                                        </button>
                                    </div>
                                </form>
                            ) : (
                                <form onSubmit={handleSaveDiscount} className="space-y-4">
                                    <div className="space-y-2">
                                        <label className="text-sm text-white/50 mr-2">عنوان تخفیف</label>
                                        <input
                                            type="text" required
                                            className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-amber-500"
                                            value={discountForm.title} onChange={e => setDiscountForm({ ...discountForm, title: e.target.value })}
                                        />
                                    </div>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <label className="text-sm text-white/50 mr-2">برند</label>
                                            <input
                                                type="text" required
                                                className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-amber-500"
                                                value={discountForm.brandName} onChange={e => setDiscountForm({ ...discountForm, brandName: e.target.value })}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-sm text-white/50 mr-2">درصد</label>
                                            <input
                                                type="number" required
                                                className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-amber-500"
                                                value={discountForm.percent} onChange={e => setDiscountForm({ ...discountForm, percent: parseInt(e.target.value) })}
                                            />
                                        </div>
                                    </div>
                                    <div className="flex justify-end gap-3 mt-8">
                                        <button type="button" onClick={() => setShowAddModal(false)} className="px-6 py-3 rounded-xl text-white/50 hover:bg-white/5 transition-colors">انصراف</button>
                                        <button type="submit" disabled={isActionLoading} className="px-8 py-3 bg-amber-600 hover:bg-amber-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-amber-500/20 disabled:opacity-50">
                                            {isActionLoading ? 'در حال ثبت...' : 'ثبت تخفیف'}
                                        </button>
                                    </div>
                                </form>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default HomePage;
