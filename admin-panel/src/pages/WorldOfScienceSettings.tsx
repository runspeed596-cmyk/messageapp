import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import type { FieldOfStudy, EducationLevel, Faculty } from '../api/adminApi';
import {
    BookOpen, GraduationCap, Building2, Plus, Trash2, Check, X, Loader2,
    Pencil, ArrowRight, AlertTriangle
} from 'lucide-react';

type Tab = 'fields' | 'levels' | 'faculties';

const WorldOfScienceSettings = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<Tab>('fields');
    const [fieldsOfStudy, setFieldsOfStudy] = useState<FieldOfStudy[]>([]);
    const [educationLevels, setEducationLevels] = useState<EducationLevel[]>([]);
    const [faculties, setFaculties] = useState<Faculty[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>('');

    // Field form
    const [showFieldForm, setShowFieldForm] = useState<boolean>(false);
    const [fieldForm, setFieldForm] = useState<FieldOfStudy>({ name: '', educationLevel: '', displayOrder: 0 });
    const [editingFieldId, setEditingFieldId] = useState<string | null>(null);

    // Level form
    const [showLevelForm, setShowLevelForm] = useState<boolean>(false);
    const [levelForm, setLevelForm] = useState<EducationLevel>({ name: '', displayOrder: 0 });
    const [editingLevelId, setEditingLevelId] = useState<string | null>(null);

    // Faculty form
    const [showFacultyForm, setShowFacultyForm] = useState<boolean>(false);
    const [facultyForm, setFacultyForm] = useState<Faculty>({ name: '', displayOrder: 0 });
    const [editingFacultyId, setEditingFacultyId] = useState<string | null>(null);

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(''), 4000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const loadData = async (): Promise<void> => {
        setLoading(true);
        try {
            const [fieldsRes, levelsRes, facultiesRes] = await Promise.all([
                adminApi.getFieldsOfStudy(),
                adminApi.getEducationLevels(),
                adminApi.getFaculties()
            ]);
            setFieldsOfStudy(fieldsRes.data.data || []);
            setEducationLevels(levelsRes.data.data || []);
            setFaculties(facultiesRes.data.data || []);
        } catch (err) {
            console.error('Error loading data:', err);
        }
        setLoading(false);
    };

    // ─── Field of Study handlers ───
    const handleSaveField = async (): Promise<void> => {
        if (!fieldForm.name.trim()) return;
        if (!fieldForm.educationLevel) {
            setError('تعیین مقطع تحصیلی الزامی است');
            return;
        }
        try {
            const res = await adminApi.saveFieldOfStudy(fieldForm);
            if (!res.data.success) {
                setError(res.data.message || 'خطا در ذخیره رشته');
                return;
            }
            setShowFieldForm(false);
            setFieldForm({ name: '', educationLevel: '', displayOrder: 0 });
            setEditingFieldId(null);
            loadData();
        } catch (err: any) {
            const msg: string = err?.response?.data?.message || 'خطا در ذخیره رشته';
            setError(msg);
        }
    };

    const handleEditField = (field: FieldOfStudy): void => {
        setFieldForm({ id: field.id, name: field.name, educationLevel: field.educationLevel, displayOrder: field.displayOrder });
        setEditingFieldId(field.id!);
        setShowFieldForm(true);
    };

    const handleDeleteField = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این رشته مطمئن هستید؟')) return;
        try {
            await adminApi.deleteFieldOfStudy(id);
            loadData();
        } catch (err) {
            console.error('Error deleting field:', err);
        }
    };

    // ─── Education Level handlers ───
    const handleSaveLevel = async (): Promise<void> => {
        if (!levelForm.name.trim()) return;
        try {
            await adminApi.saveEducationLevel(levelForm);
            setShowLevelForm(false);
            setLevelForm({ name: '', displayOrder: 0 });
            setEditingLevelId(null);
            loadData();
        } catch (err) {
            console.error('Error saving level:', err);
        }
    };

    const handleEditLevel = (level: EducationLevel): void => {
        setLevelForm({ id: level.id, name: level.name, displayOrder: level.displayOrder });
        setEditingLevelId(level.id!);
        setShowLevelForm(true);
    };

    const handleDeleteLevel = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این مقطع مطمئن هستید؟')) return;
        try {
            await adminApi.deleteEducationLevel(id);
            loadData();
        } catch (err) {
            console.error('Error deleting level:', err);
        }
    };

    // ─── Faculty handlers ───
    const handleSaveFaculty = async (): Promise<void> => {
        if (!facultyForm.name.trim()) return;
        try {
            const res = await adminApi.saveFaculty(facultyForm);
            if (!res.data.success) {
                setError(res.data.message || 'خطا در ذخیره دانشکده');
                return;
            }
            setShowFacultyForm(false);
            setFacultyForm({ name: '', displayOrder: 0 });
            setEditingFacultyId(null);
            loadData();
        } catch (err: any) {
            const msg: string = err?.response?.data?.message || 'خطا در ذخیره دانشکده';
            setError(msg);
        }
    };

    const handleEditFaculty = (faculty: Faculty): void => {
        setFacultyForm({ id: faculty.id, name: faculty.name, displayOrder: faculty.displayOrder });
        setEditingFacultyId(faculty.id!);
        setShowFacultyForm(true);
    };

    const handleDeleteFaculty = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این دانشکده مطمئن هستید؟')) return;
        try {
            await adminApi.deleteFaculty(id);
            loadData();
        } catch (err) {
            console.error('Error deleting faculty:', err);
        }
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-black text-white">تنظیمات جهان علم</h1>
                    <p className="text-slate-400 mt-2">مدیریت رشته‌های تحصیلی، مقاطع و دانشکده‌ها</p>
                </div>
                <button
                    onClick={() => navigate('/universities')}
                    className="flex items-center gap-2 glass text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 border border-emerald-500/20 px-5 py-2.5 rounded-xl font-bold text-sm transition-all"
                >
                    بخش اصلی دانشگاه‌ها
                    <ArrowRight size={16} className="rotate-180" />
                </button>
            </div>

            {/* Error Toast */}
            {error && (
                <div className="fixed top-6 left-1/2 -translate-x-1/2 z-50 bg-rose-600/90 backdrop-blur-xl text-white px-6 py-3 rounded-2xl shadow-2xl shadow-rose-500/30 flex items-center gap-3 animate-in fade-in slide-in-from-top-4  duration-300 border border-rose-400/30">
                    <AlertTriangle size={18} />
                    <span className="font-bold text-sm">{error}</span>
                </div>
            )}

            {/* Tabs */}
            <div className="flex gap-3 flex-wrap">
                <button
                    onClick={() => setActiveTab('fields')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'fields'
                        ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <BookOpen size={18} />
                    رشته‌های تحصیلی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{fieldsOfStudy.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('faculties')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'faculties'
                        ? 'bg-amber-600 text-white shadow-lg shadow-amber-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Building2 size={18} />
                    دانشکده‌ها
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{faculties.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('levels')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'levels'
                        ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <GraduationCap size={18} />
                    مقاطع تحصیلی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{educationLevels.length}</span>
                </button>
            </div>

            {loading && (
                <div className="flex justify-center py-20">
                    <Loader2 size={32} className="text-indigo-400 animate-spin" />
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Fields of Study Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'fields' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <BookOpen size={20} className="text-indigo-400" />
                            رشته‌های تحصیلی ({fieldsOfStudy.length})
                        </h2>
                        <button
                            onClick={() => { setShowFieldForm(true); setEditingFieldId(null); setFieldForm({ name: '', educationLevel: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن رشته
                        </button>
                    </div>

                    {showFieldForm && (
                        <div className="glass p-6 rounded-2xl border-emerald-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-emerald-400 mb-4">{editingFieldId ? '✏️ ویرایش رشته' : 'ثبت رشته جدید'}</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام رشته *</label>
                                    <input
                                        type="text"
                                        value={fieldForm.name}
                                        onChange={(e) => setFieldForm({ ...fieldForm, name: e.target.value })}
                                        placeholder="مثال: مهندسی کامپیوتر"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="w-48 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">مقطع تحصیلی *</label>
                                    <select
                                        value={fieldForm.educationLevel}
                                        onChange={(e) => setFieldForm({ ...fieldForm, educationLevel: e.target.value })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    >
                                        <option value="" className="bg-slate-900">انتخاب مقطع</option>
                                        {educationLevels
                                            .sort((a, b) => a.displayOrder - b.displayOrder)
                                            .map(level => (
                                                <option key={level.id} value={level.name} className="bg-slate-900">{level.name}</option>
                                            ))}
                                    </select>
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={fieldForm.displayOrder}
                                        onChange={(e) => setFieldForm({ ...fieldForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleSaveField}
                                        className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowFieldForm(false); setFieldForm({ name: '', educationLevel: '', displayOrder: 0 }); setEditingFieldId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام رشته</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">مقطع</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {fieldsOfStudy
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((field, index) => (
                                        <tr key={field.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{field.name}</td>
                                            <td className="p-4 text-center">
                                                <span className="bg-indigo-500/10 text-indigo-400 px-3 py-1 rounded-lg text-xs font-bold">
                                                    {field.educationLevel || '—'}
                                                </span>
                                            </td>
                                            <td className="p-4 text-center text-sm text-slate-400">{field.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditField(field)}
                                                        className="p-2 bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteField(field.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {fieldsOfStudy.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز رشته‌ای ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Faculties Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'faculties' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Building2 size={20} className="text-amber-400" />
                            دانشکده‌ها ({faculties.length})
                        </h2>
                        <button
                            onClick={() => { setShowFacultyForm(true); setEditingFacultyId(null); setFacultyForm({ name: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن دانشکده
                        </button>
                    </div>

                    {showFacultyForm && (
                        <div className="glass p-6 rounded-2xl border-amber-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-amber-400 mb-4">{editingFacultyId ? '✏️ ویرایش دانشکده' : 'ثبت دانشکده جدید'}</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام دانشکده *</label>
                                    <input
                                        type="text"
                                        value={facultyForm.name}
                                        onChange={(e) => setFacultyForm({ ...facultyForm, name: e.target.value })}
                                        placeholder="مثال: فنی و مهندسی"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-amber-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={facultyForm.displayOrder}
                                        onChange={(e) => setFacultyForm({ ...facultyForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-amber-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleSaveFaculty}
                                        className="flex items-center gap-2 bg-amber-600 hover:bg-amber-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowFacultyForm(false); setFacultyForm({ name: '', displayOrder: 0 }); setEditingFacultyId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام دانشکده</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب نمایش</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {faculties
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((faculty, index) => (
                                        <tr key={faculty.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{faculty.name}</td>
                                            <td className="p-4 text-center text-sm text-slate-400">{faculty.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditFaculty(faculty)}
                                                        className="p-2 bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteFaculty(faculty.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {faculties.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز دانشکده‌ای ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Education Levels Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'levels' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <GraduationCap size={20} className="text-purple-400" />
                            مقاطع تحصیلی ({educationLevels.length})
                        </h2>
                        <button
                            onClick={() => { setShowLevelForm(true); setEditingLevelId(null); setLevelForm({ name: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن مقطع
                        </button>
                    </div>

                    {showLevelForm && (
                        <div className="glass p-6 rounded-2xl border-purple-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-purple-400 mb-4">{editingLevelId ? '✏️ ویرایش مقطع' : 'ثبت مقطع جدید'}</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام مقطع *</label>
                                    <input
                                        type="text"
                                        value={levelForm.name}
                                        onChange={(e) => setLevelForm({ ...levelForm, name: e.target.value })}
                                        placeholder="مثال: کارشناسی"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-purple-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={levelForm.displayOrder}
                                        onChange={(e) => setLevelForm({ ...levelForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-purple-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleSaveLevel}
                                        className="flex items-center gap-2 bg-purple-600 hover:bg-purple-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowLevelForm(false); setLevelForm({ name: '', displayOrder: 0 }); setEditingLevelId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام مقطع</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب نمایش</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {educationLevels
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((level, index) => (
                                        <tr key={level.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{level.name}</td>
                                            <td className="p-4 text-center text-sm text-slate-400">{level.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditLevel(level)}
                                                        className="p-2 bg-purple-500/10 text-purple-400 hover:bg-purple-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteLevel(level.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {educationLevels.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز مقطعی ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WorldOfScienceSettings;
