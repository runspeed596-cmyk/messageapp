import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { University } from '../api/adminApi';
import { Plus, Check, X, GraduationCap, MapPin, Globe, BookOpen, Users as UsersIcon, Search, Trash2, Building2, FileText, Trophy, Loader2 } from 'lucide-react';

const Universities = () => {
    const [unis, setUnis] = useState<University[]>([]);
    const [isAdding, setIsAdding] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    // Location data
    const [countries] = useState<string[]>(['ایران']);
    const [provinces, setProvinces] = useState<string[]>([]);
    const [cities, setCities] = useState<string[]>([]);

    const [newUni, setNewUni] = useState<University>({
        name: '',
        country: 'ایران',
        province: '',
        city: '',
        ministryName: 'وزارت علوم، تحقیقات و فناوری',
        type: 'دولتی',
        establishedYear: 1300,
        studentCount: 0,
        faculties: '',
        departments: '',
        iranRank: 0,
        worldRank: 0,
        articleCount: 0,
        journalCount: 0,
        facilities: '',
        latitude: 35.6892,
        longitude: 51.3890,
        imageUrl: '',
        websiteUrl: ''
    });

    useEffect(() => {
        fetchUnis();
        fetchProvinces('ایران');
    }, []);

    const fetchUnis = async () => {
        try {
            const response = await adminApi.getUniversities();
            if (response.data.success) {
                setUnis(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching universities:', error);
        }
    };

    const fetchProvinces = async (country: string) => {
        try {
            const response = await adminApi.getProvinces(country);
            if (response.data.success) {
                setProvinces(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching provinces:', error);
        }
    };

    const fetchCities = async (province: string) => {
        try {
            const response = await adminApi.getCities(province);
            if (response.data.success) {
                setCities(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching cities:', error);
        }
    };

    const handleProvinceChange = (province: string) => {
        setNewUni({ ...newUni, province, city: '' });
        fetchCities(province);
    };

    const handleSave = async () => {
        if (!newUni.name || !newUni.province || !newUni.city) {
            alert('لطفاً نام دانشگاه، استان و شهر را وارد کنید');
            return;
        }

        setIsLoading(true);
        try {
            const uniToSave: University = {
                ...newUni,
                iranRank: Number(newUni.iranRank) || 0,
                worldRank: Number(newUni.worldRank) || 0,
                studentCount: Number(newUni.studentCount) || 0,
                articleCount: Number(newUni.articleCount) || 0,
                journalCount: Number(newUni.journalCount) || 0,
                establishedYear: Number(newUni.establishedYear) || 1300,
            };
            await adminApi.saveUniversity(uniToSave);
            fetchUnis();
            setIsAdding(false);
            resetForm();
        } catch (error) {
            alert('خطا در ذخیره دانشگاه');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('آیا از حذف این دانشگاه اطمینان دارید؟')) {
            try {
                await adminApi.deleteUniversity(id);
                setUnis(unis.filter(u => u.id !== id));
            } catch (error) {
                alert('خطا در حذف دانشگاه');
            }
        }
    };

    const resetForm = () => {
        setNewUni({
            name: '',
            country: 'ایران',
            province: '',
            city: '',
            ministryName: 'وزارت علوم، تحقیقات و فناوری',
            type: 'دولتی',
            establishedYear: 1300,
            studentCount: 0,
            faculties: '',
            departments: '',
            iranRank: 0,
            worldRank: 0,
            articleCount: 0,
            journalCount: 0,
            facilities: '',
            latitude: 35.6892,
            longitude: 51.3890,
            imageUrl: '',
            websiteUrl: ''
        });
        setCities([]);
    };

    const ministryOptions = [
        'وزارت علوم، تحقیقات و فناوری',
        'وزارت بهداشت، درمان و آموزش پزشکی',
        'دانشگاه آزاد اسلامی',
        'سایر'
    ];

    const typeOptions = ['دولتی', 'آزاد اسلامی', 'غیرانتفاعی', 'علوم پزشکی', 'پیام نور', 'فنی و حرفه‌ای', 'جامع علمی کاربردی'];

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-black text-white">جهان علم</h1>
                    <p className="text-slate-400 mt-2">مدیریت دانشگاه‌ها و مراکز آموزش عالی در نقشه کره زمین</p>
                </div>
                <button
                    onClick={() => setIsAdding(true)}
                    className="flex items-center gap-3 bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg shadow-indigo-500/20 transition-all active:scale-95 group"
                >
                    <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                    <span>ثبت دانشگاه جدید</span>
                </button>
            </div>

            {isAdding && (
                <div className="glass p-8 rounded-[2rem] border-indigo-500/20 shadow-2xl animate-in fade-in slide-in-from-top-4 duration-500">
                    <div className="flex items-center gap-4 mb-8">
                        <div className="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center text-indigo-400">
                            <GraduationCap size={24} />
                        </div>
                        <h2 className="text-xl font-black text-white">ثبت مرکز علمی جدید</h2>
                    </div>

                    {/* Location Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <MapPin size={16} /> موقعیت جغرافیایی
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">کشور</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none appearance-none"
                                    value={newUni.country}
                                    onChange={e => setNewUni({ ...newUni, country: e.target.value })}
                                >
                                    {countries.map(c => <option key={c} className="bg-slate-900">{c}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">استان</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none appearance-none"
                                    value={newUni.province}
                                    onChange={e => handleProvinceChange(e.target.value)}
                                >
                                    <option value="" className="bg-slate-900">انتخاب استان...</option>
                                    {provinces.map(p => <option key={p} className="bg-slate-900">{p}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">شهر</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none appearance-none"
                                    value={newUni.city}
                                    onChange={e => setNewUni({ ...newUni, city: e.target.value })}
                                    disabled={!newUni.province}
                                >
                                    <option value="" className="bg-slate-900">انتخاب شهر...</option>
                                    {cities.map(c => <option key={c} className="bg-slate-900">{c}</option>)}
                                </select>
                            </div>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">عرض جغرافیایی (Latitude)</label>
                                <input
                                    type="number" step="0.0001"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.latitude}
                                    onChange={e => setNewUni({ ...newUni, latitude: parseFloat(e.target.value) })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">طول جغرافیایی (Longitude)</label>
                                <input
                                    type="number" step="0.0001"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.longitude}
                                    onChange={e => setNewUni({ ...newUni, longitude: parseFloat(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Basic Info Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <Building2 size={16} /> اطلاعات پایه
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div className="space-y-2 lg:col-span-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">نام کامل دانشگاه</label>
                                <input
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    placeholder="مثال: دانشگاه تهران"
                                    value={newUni.name}
                                    onChange={e => setNewUni({ ...newUni, name: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">سال تأسیس</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.establishedYear}
                                    onChange={e => setNewUni({ ...newUni, establishedYear: parseInt(e.target.value) })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">وزارت مربوطه</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none appearance-none"
                                    value={newUni.ministryName}
                                    onChange={e => setNewUni({ ...newUni, ministryName: e.target.value })}
                                >
                                    {ministryOptions.map(m => <option key={m} className="bg-slate-900">{m}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">نوع دانشگاه</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none appearance-none"
                                    value={newUni.type}
                                    onChange={e => setNewUni({ ...newUni, type: e.target.value })}
                                >
                                    {typeOptions.map(t => <option key={t} className="bg-slate-900">{t}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد دانشجو</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.studentCount}
                                    onChange={e => setNewUni({ ...newUni, studentCount: parseInt(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Academic Info Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <BookOpen size={16} /> اطلاعات آکادمیک
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">دانشکده‌ها (با کاما جدا کنید)</label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none resize-none h-24"
                                    placeholder="مثال: فنی و مهندسی، علوم پایه، پزشکی، ادبیات"
                                    value={newUni.faculties}
                                    onChange={e => setNewUni({ ...newUni, faculties: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">رشته‌ها (با کاما جدا کنید)</label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none resize-none h-24"
                                    placeholder="مثال: مهندسی کامپیوتر، پزشکی، حقوق"
                                    value={newUni.departments}
                                    onChange={e => setNewUni({ ...newUni, departments: e.target.value })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Rankings Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <Trophy size={16} /> رتبه‌بندی و آمار
                        </h3>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">رتبه در ایران</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.iranRank || ''}
                                    onChange={e => setNewUni({ ...newUni, iranRank: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">رتبه در جهان</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.worldRank || ''}
                                    onChange={e => setNewUni({ ...newUni, worldRank: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد مقالات</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.articleCount}
                                    onChange={e => setNewUni({ ...newUni, articleCount: parseInt(e.target.value) })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد مجلات</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.journalCount}
                                    onChange={e => setNewUni({ ...newUni, journalCount: parseInt(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Facilities Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <FileText size={16} /> امکانات
                        </h3>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">امکانات دانشگاه</label>
                            <textarea
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none resize-none h-24"
                                placeholder="مثال: کتابخانه مرکزی، خوابگاه، سالن ورزشی، آزمایشگاه‌های تخصصی"
                                value={newUni.facilities}
                                onChange={e => setNewUni({ ...newUni, facilities: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="flex gap-4 mt-10">
                        <button
                            onClick={handleSave}
                            disabled={isLoading}
                            className={`flex-1 text-white py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all
                                ${isLoading ? 'bg-slate-600 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-500'}`}
                        >
                            {isLoading ? (
                                <><Loader2 size={20} className="animate-spin" /> در حال ذخیره...</>
                            ) : (
                                <><Check size={20} /> ثبت نهایی اطلاعات</>
                            )}
                        </button>
                        <button
                            onClick={() => { setIsAdding(false); resetForm(); }}
                            disabled={isLoading}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all"
                        >
                            <X size={20} /> انصراف و بستن فرم
                        </button>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 gap-4">
                {unis.map((uni) => (
                    <div key={uni.id} className="glass p-6 rounded-[1.5rem] border-white/5 hover:border-indigo-500/30 transition-all group">
                        <div className="flex justify-between items-start">
                            <div className="flex items-center gap-6">
                                <div className="w-16 h-16 bg-white/5 text-indigo-400 rounded-2xl flex items-center justify-center border border-white/10 group-hover:scale-110 transition-transform duration-500">
                                    <GraduationCap size={32} />
                                </div>
                                <div>
                                    <h3 className="font-black text-xl text-white">{uni.name}</h3>
                                    <div className="text-xs text-slate-500 flex flex-wrap gap-4 mt-2 font-bold">
                                        <span className="flex items-center gap-2 text-indigo-400">
                                            <MapPin size={12} /> {uni.city}, {uni.province}
                                        </span>
                                        <span className="flex items-center gap-2">
                                            <UsersIcon size={12} /> {uni.studentCount?.toLocaleString('fa-IR')} دانشجو
                                        </span>
                                        <span className="flex items-center gap-2 text-purple-400">
                                            <BookOpen size={12} /> {uni.type}
                                        </span>
                                        {uni.iranRank && (
                                            <span className="flex items-center gap-2 text-emerald-400">
                                                <Trophy size={12} /> رتبه {uni.iranRank} در ایران
                                            </span>
                                        )}
                                    </div>
                                    {uni.faculties && (
                                        <div className="text-xs text-slate-600 mt-2">
                                            دانشکده‌ها: {uni.faculties}
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                                <button className="p-3 bg-white/5 text-slate-400 hover:text-white hover:bg-white/10 rounded-xl transition-all">
                                    <Globe size={20} />
                                </button>
                                <button
                                    onClick={() => handleDelete(uni.id!)}
                                    className="p-3 bg-white/5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-xl transition-all"
                                >
                                    <Trash2 size={20} />
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
                {unis.length === 0 && (
                    <div className="glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                        <Search size={40} className="text-slate-700 mx-auto mb-6" />
                        <h3 className="text-xl font-black text-white">هیچ دانشگاهی ثبت نشده است</h3>
                        <p className="text-slate-500 mt-2">برای نمایش در کره زمین، حداقل یک دانشگاه اضافه کنید.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Universities;
